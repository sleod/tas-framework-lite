package io.github.sleod.tas.common.utils;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cross-platform Node.js locator.
 * <p>
 * Usage:
 * <p>
 *     NodeLocator.NodeInfo info = NodeLocator.findNode();
 *     if (info == null) {
 *         System.out.println("Node.js not found");
 *     } else {
 *         System.out.println("Node binary : " + info.getExecutable());
 *         System.out.println("Node version: " + info.getVersion());
 *     }
 */
public final class NodeLocator {

    private NodeLocator() {
        // no instance
    }

    /* ------------ Public API ------------ */

    public static NodeInfo findNode() {
        // 1. Try platform-specific hints and env vars
        Path path = tryPlatformAwareLookup();

        // 2. Try PATH search (which/where + realpath/canonicalize)
        if (path == null) {
            path = tryPathLookup();
        }

        if (path == null) {
            return null;
        }

        // 3. Canonicalize (resolve symlinks etc.)
        Path canonical = canonicalize(path);

        // 4. Get version
        String version = readNodeVersion(canonical);

        return new NodeInfo(canonical, version);
    }

    /* ------------ Result model ------------ */

    @Getter
    public static final class NodeInfo {
        private final Path executable;
        private final String version;

        private NodeInfo(Path executable, String version) {
            this.executable = executable;
            this.version = version;
        }

        @Override
        public String toString() {
            return "NodeInfo{executable=" + executable + ", version=" + version + "}";
        }
    }

    /* ------------ OS dispatch ------------ */

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static boolean isUnixLike() {
        // covers macOS, Linux, WSL, etc.
        return !isWindows();
    }

    /* ------------ Phase 1: platform-aware lookup ------------ */

    private static Path tryPlatformAwareLookup() {
        // First, honor NODE_HOME if set
        String nodeHome = System.getenv("NODE_HOME");
        if (nodeHome != null && !nodeHome.isBlank()) {
            Path candidate = isWindows()
                    ? Paths.get(nodeHome, "node.exe")
                    : Paths.get(nodeHome, "bin", "node");
            if (isExecutable(candidate)) {
                return candidate;
            }
        }

        if (isWindows()) {
            // Windows registry (Node official installer)
            Path regPath = findNodeFromWindowsRegistry();
            if (isExecutable(regPath)) {
                return regPath;
            }
        } else {
            // nvm (~/.nvm/versions/node/*/bin/node)
            Path nvmPath = findNodeFromNvm();
            if (isExecutable(nvmPath)) {
                return nvmPath;
            }

            // Common Homebrew locations
            Path brewIntel = Paths.get("/usr/local/bin/node");
            if (isExecutable(brewIntel)) {
                return brewIntel;
            }
            Path brewArm = Paths.get("/opt/homebrew/bin/node");
            if (isExecutable(brewArm)) {
                return brewArm;
            }
        }

        return null;
    }

    /* ------------ Phase 2: PATH lookup ------------ */

    private static Path tryPathLookup() {
        if (isWindows()) {
            // 1. where node
            Path fromWhere = runAndParseSinglePath("where", "node");
            if (isExecutable(fromWhere)) {
                return fromWhere;
            }
            // nothing else special to do for Windows here
            return null;
        } else {
            // macOS / Linux
            // Preferred: realpath "$(which node)"
            Path fromRealpath = tryWhichThenRealpath();
            if (isExecutable(fromRealpath)) {
                return fromRealpath;
            }

            // Fallback: which node (without realpath)
            Path fromWhich = runAndParseSinglePath("which", "node");
            if (isExecutable(fromWhich)) {
                return fromWhich;
            }

            // Fallback: POSIX-y 'command -v node'
            Path fromCommandV = runAndParseSinglePath("sh", "-c", "command -v node");
            if (isExecutable(fromCommandV)) {
                return fromCommandV;
            }

            return null;
        }
    }

    /* ------------ Helpers: isExecutable, canonicalize ------------ */

    private static boolean isExecutable(Path p) {
        return p != null && Files.isRegularFile(p) && Files.isReadable(p);
    }

    private static Path canonicalize(Path p) {
        if (p == null) return null;
        // Try OS-level realpath first on Unix to resolve symlinks from brew/nvm
        if (isUnixLike()) {
            Path viaRealpath = runAndParseSinglePath("realpath", p.toString());
            if (viaRealpath != null && Files.isRegularFile(viaRealpath)) {
                p = viaRealpath;
            }
        }
        // As a final step, use Java's canonical resolution
        try {
            return p.toRealPath(); // resolves symlinks if possible
        } catch (IOException e) {
            // can't canonicalize, just return original
            return p.toAbsolutePath();
        }
    }

    /* ------------ macOS/Linux: nvm lookup ------------ */

    private static Path findNodeFromNvm() {
        // ~/.nvm/versions/node/<ver>/bin/node
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) return null;

        Path versionsRoot = Paths.get(home, ".nvm", "versions", "node");
        if (!Files.isDirectory(versionsRoot)) return null;

        Path bestNode = null;
        String bestVersionDirName = null;

        try (var stream = Files.list(versionsRoot)) {
            for (Path verDir : (Iterable<Path>) stream::iterator) {
                if (!Files.isDirectory(verDir)) continue;
                String dirName = verDir.getFileName().toString();
                Path maybeNode = verDir.resolve("bin").resolve("node");
                if (Files.isRegularFile(maybeNode)) {
                    // pick lexicographically "largest" version directory as heuristic "latest"
                    if (bestVersionDirName == null ||
                            dirName.compareTo(bestVersionDirName) > 0) {
                        bestVersionDirName = dirName;
                        bestNode = maybeNode;
                    }
                }
            }
        } catch (IOException ignored) {
        }

        return bestNode;
    }

    /* ------------ Windows: registry lookup ------------ */

    private static Path findNodeFromWindowsRegistry() {
        // reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Node.js" /v InstallPath
        try {
            Process proc = new ProcessBuilder(
                    "reg", "query",
                    "HKEY_LOCAL_MACHINE\\SOFTWARE\\Node.js",
                    "/v", "InstallPath"
            ).redirectErrorStream(true).start();

            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("InstallPath")) {
                        // Expected like:
                        // InstallPath    REG_SZ    C:\Program Files\nodejs\
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 0) {
                            String last = parts[parts.length - 1];
                            Path exe = Paths.get(last, "node.exe");
                            if (Files.isRegularFile(exe)) {
                                return exe;
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /* ------------ which + realpath combo (Unix-like) ------------ */

    private static Path tryWhichThenRealpath() {
        // 1. which node
        Path which = runAndParseSinglePath("which", "node");
        if (!isExecutable(which)) {
            return null;
        }

        // 2. realpath <whichResult>
        Path resolved = runAndParseSinglePath("realpath", which.toString());
        if (isExecutable(resolved)) {
            return resolved;
        }

        // fallback to which result
        return which;
    }

    /* ------------ generic process runner to get first line path ------------ */

    private static Path runAndParseSinglePath(String... cmd) {
        try {
            Process proc = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = br.readLine();
                if (line != null && !line.isBlank()) {
                    String cleaned = line.trim().replace("\"", "");
                    Path p = Paths.get(cleaned);
                    if (Files.exists(p)) {
                        return p;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /* ------------ read node -v ------------ */

    private static String readNodeVersion(Path nodePath) {
        if (!isExecutable(nodePath)) return null;
        try {
            Process proc = new ProcessBuilder(nodePath.toString(), "-v")
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = br.readLine();
                if (line != null && !line.isBlank()) {
                    return line.trim(); // ex: "v22.9.0"
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

}
