package ch.qa.testautomation.framework.common.IOUtils;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static java.lang.Integer.MAX_VALUE;

public class FileLocator {

    /**
     * Finds paths from a source path, allowing for includes/excludes. Paths
     * found are prefixed with specified path by {@link
     * #prefix(String, List<String>)} and normalised by {@link
     * #normalise(List<String>)}.
     *
     * @param searchIn   the source path to search in
     * @param includes   the List of include patterns, or <code>null</code> if
     *                   none
     * @param excludes   the List of exclude patterns, or <code>null</code> if
     *                   none
     * @param prefixWith the root path prefixed to all paths found, or
     *                   <code>null</code> if none
     * @return A List of paths found
     */
    public static List<String> findPaths(Path searchIn, List<String> includes, List<String> excludes, String prefixWith) {
        return normalise(prefix(prefixWith, sort(scanDirectory(searchIn, includes, excludes))));
    }

    /**
     * find all regular file paths within dir with max deep
     * (p, bfa) -> bfa.isRegularFile()&& p.getFileName().toString().matches(".*\\.jpg")
     * && bfa.lastModifiedTime().toMillis() > System.currentMillis() - 86400000
     *
     * @param sDir    start dir
     * @param maxDeep max deep
     * @return list of paths
     */
    public static List<Path> listRegularFilesRecursive(String sDir, int maxDeep) {
        List<Path> paths;
        try {
            paths = Files.find(Paths.get(sDir), maxDeep, (p, bfa) -> bfa.isRegularFile()).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, sDir);
        }
        return paths;
    }

    /**
     * find all regular file paths within dir with max deep
     * (p, bfa) -> bfa.isRegularFile()&& p.getFileName().toString().matches(".*\\.jpg")
     *
     * @param sDir    start dir
     * @param maxDeep max deep
     * @return list of paths
     */
    public static List<Path> listRegularFilesRecursiveMatchedToName(String sDir, int maxDeep, String name) {
        List<Path> paths;
        try {
            paths = Files.find(Paths.get(sDir), maxDeep, (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(".*" + name + ".*"))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, sDir + "/" + name);
        }
        if (paths.isEmpty()) {
            trace("File with name '" + name + "' was not found in folder " + sDir);
        }
        return paths;
    }

    /**
     * find all regular file paths within dir with max deep
     * (p, bfa) -> bfa.isRegularFile()&& p.getFileName().toString().matches(".*\\.jpg")
     *
     * @param sDir    start dir
     * @param maxDeep max deep
     * @return list of paths
     */
    public static Path findExactFile(String sDir, int maxDeep, String name) {
        Optional<Path> paths;
        try {
            paths = Files.find(Paths.get(sDir), maxDeep, (p, bfa) -> bfa.isRegularFile() && p.getFileName().toString().matches(name)).findFirst();
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, sDir + "/" + name);
        }
        if (paths.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.OBJECT_NOT_FOUND, sDir + "/" + name);
        }
        return paths.get();
    }


    /**
     * walk through folder and get files only
     *
     * @param path folder path
     * @return list of path of files only
     * @throws IOException ex
     */
    public static List<Path> walkThrough(Path path) throws IOException {
        return walkThrough(path, MAX_VALUE);
    }

    /**
     * walk through folder and get files only
     *
     * @param path folder path
     * @return list of path of files only
     * @throws IOException ex
     */
    public static List<Path> walkThrough(Path path, int maxDepth) throws IOException {
        List<Path> paths = new LinkedList<>();
        Stream<Path> walk = Files.walk(path, maxDepth);
        for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
            Path filePath = it.next();
            if (!Files.isDirectory(filePath)) {
                paths.add(filePath);
                SystemLogger.trace("Found File: " + filePath);
            }
        }
        return paths;
    }

    /**
     * find local resource with relative path
     *
     * @param relativePath path of file don't need "/" at beginning
     * @return path of target
     */
    public static Path findResource(String relativePath) {
        if (!isResourceFileExists(relativePath)) {
            throw new ApollonBaseException(ApollonErrorKeys.OBJECT_NOT_FOUND, relativePath);
        } else {
            String location = Objects.requireNonNull(FileLocator.class.getClassLoader().getResource(relativePath)).getPath();
            return new File(location).toPath();
        }
    }

    /**
     * verify if resource file with relative path exists
     *
     * @param relativePath relative file path
     * @return true be found
     */
    public static boolean isResourceFileExists(String relativePath) {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        URL url = FileLocator.class.getClassLoader().getResource(relativePath);
        return Objects.nonNull(url) && !url.toString().contains("jar!");
    }


    /**
     * find resource with relative path also in case the files in a jar file
     *
     * @param relativePath path of file don't need "/" at beginning
     * @return path of target
     */
    public static Path findLocalResource(String relativePath) {
        Path path = findResource(relativePath);
        if (!path.toString().contains("jar!")) {
            trace("Found Local Path: " + relativePath);
            return path;
        } else {
            warn("Local Path: " + relativePath + " can not be found!");
            return null;
        }
    }

    /**
     * find resource with relative path also in case the files in a jar file
     *
     * @param relativePath path of file don't need "/" at beginning
     * @return path of target
     */
    public static InputStream loadResource(String relativePath) {
        String location;
        //clean up first '/'
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return FileOperation.retrieveFileFromResourcesAsStream(relativePath);
    }

    public static String getProjectBaseDir() {
        return Paths.get("").toAbsolutePath().toString();
    }

    protected static List<String> prefix(final String prefixWith, List<String> paths) {
        if (StringUtils.isBlank(prefixWith)) {
            return paths;
        }
        List<String> transformed = new ArrayList<>(paths);
        CollectionUtils.transform(transformed, path -> prefixWith + path);
        return transformed;
    }

    protected static List<String> normalise(List<String> paths) {
        List<String> transformed = new ArrayList<>(paths);
        CollectionUtils.transform(transformed, path -> path.replace('\\', '/'));
        return transformed;
    }

    protected static List<String> sort(List<String> input) {
        List<String> sorted = new ArrayList<>(input);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    /**
     * scan directory with path and patterns
     *
     * @param basedir  dir path of directory
     * @param includes includes pattern for including file with suffix
     * @param excludes excludes pattern for excluding file with suffix
     * @return list of file paths
     */
    private static List<String> scanDirectory(Path basedir, List<String> includes, List<String> excludes) {
        List<String> result = new ArrayList<>();
        if (basedir.toFile().exists()) {
            List<File> files = DirectoryScanner.scan(basedir.toFile(), includes, excludes);
            files.forEach(file -> result.add(file.getPath()));
        }
        return result;
    }
}
