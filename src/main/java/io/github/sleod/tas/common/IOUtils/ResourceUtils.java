package io.github.sleod.tas.common.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.sleod.tas.common.logging.SystemLogger;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ResourceUtils {
    private static final String INDEX_FILE = "resource-index.json";
    private static final Map<String, List<String>> FILE_INDEX = loadIndex();

    private ResourceUtils() {
    }

    /**
     * Returns an InputStream for a resource by file name or file path.
     *
     * @throws FileNotFoundException    if the name is absent from the index
     * @throws IllegalArgumentException if several resources share that name
     */
    public static String getResourceFileContent(String fileName) throws IOException {
        return FileOperation.readFileStreamToLinedString(getResourceFileAsStream(getFilePathWithName(fileName)));
    }

    /**
     * read resource file content to string with break
     *
     * @param filePath is path of a resource file in classpath, must be with secured existence in index, otherwise will throw exception
     * @return String content of file
     */
    public static String readResourceFileToString(String filePath) throws IOException {
        return FileOperation.readFileStreamToLinedString(getResourceFileAsStream(filePath));
    }

    /**
     * read resource file to lined string into a List
     *
     * @param filePath is path of a resource file in classpath
     * @return List of String of the file content
     */
    public static List<String> readResourceFileToLinedString(String filePath) throws IOException {
        String content = readResourceFileToString(filePath);
        return Arrays.asList(content.split(System.lineSeparator()));
    }

    /**
     * Checks if a resource file exists in the index.
     *
     * @param filename the name of the resource file
     * @return true if the resource file exists, false otherwise
     */
    public static boolean isResourceFileExists(String filename) {
        return FILE_INDEX.containsKey(filename);
    }

    public static boolean resourceFolderExists(String folderPath) {
        String path = folderPath.replace('\\', '/');
        if (!path.endsWith("/")) {
            path += "/";
        }
        String finalPath = path;
        return FILE_INDEX.values().stream().flatMap(List::stream)
                .anyMatch(filePath -> filePath.startsWith(finalPath));
    }

    /**
     * Finds resource paths matching the given include and exclude patterns.
     *
     * @param includePatterns List of glob patterns to include
     * @param excludePatterns List of glob patterns to exclude
     * @return List of matching resource paths
     */
    public static List<String> findResourcePaths(List<String> includePatterns, List<String> excludePatterns) {
        if (includePatterns == null || includePatterns.isEmpty()) {
            throw new IllegalArgumentException("At least one include pattern is required");
        }
        List<Pattern> includes = includePatterns.stream().map(ResourceUtils::toGlobPattern).toList();
        List<Pattern> excludes = excludePatterns == null ? List.of() : excludePatterns.stream().map(ResourceUtils::toGlobPattern).toList();

        return FILE_INDEX.values().stream().flatMap(List::stream)
                .filter(path -> matchesAny(path, includes))
                .filter(path -> !matchesAny(path, excludes)).sorted().toList();
    }

    public static List<String> findResourcePaths(String inFolder, String fileName) {
        if (inFolder == null || inFolder.isBlank()) {
            throw new IllegalArgumentException("Folder path cannot be null or empty");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (isResourceFileExists(fileName)) {
            return FILE_INDEX.get(fileName).stream().filter(path -> path.contains("/" + inFolder + "/")).toList();
        }
        throw new ExceptionBase(ExceptionErrorKeys.RESOURCE_FILE_NOT_FOUND, fileName);
    }

    /**
     * Lists file paths of a given file name pattern within a specified folder.
     *
     * @param inFolder        the folder to search within
     * @param fileNamePattern the pattern of the file name to search for
     * @return List of matching file paths
     */
    public static List<String> listFilePathsOfName(String inFolder, String fileNamePattern, boolean caseSensitive) {
        if (inFolder == null || inFolder.isBlank()) {
            throw new IllegalArgumentException("Folder path cannot be null or empty");
        }
        if (fileNamePattern == null || fileNamePattern.isBlank()) {
            throw new IllegalArgumentException("File name pattern cannot be null or empty");
        }
        Pattern namePattern = Pattern.compile(fileNamePattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        inFolder = inFolder.replace('\\', '/').replaceFirst("^/+", "").replaceFirst("/+$", "");
        Pattern folderPattern = Pattern.compile("(?:^|/)" + inFolder + "(?:/|$)");
        return FILE_INDEX.entrySet().stream()
                .filter(entry -> {
                    String fileName = entry.getKey();
                    return namePattern.matcher(fileName).matches();})
                .flatMap(entry -> entry.getValue().stream())
                .filter(path -> folderPattern.matcher(path).find())
                .toList();
    }

    public static String getFilePathWithName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        List<String> paths = FILE_INDEX.get(FileOperation.getFileName(fileName));
        if (paths == null || paths.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.RESOURCE_FILE_NOT_FOUND, fileName);
        }
        //filter by test environment if multiple resources found only.
        //file should be unique across test environments which its path contains environment name, but if not, we will try to filter by test environment.
        if (paths.size() > 1) {
            SystemLogger.debug("Multiple resources found for file name: " + fileName + ". Check test environment for duplicates.");
            String testEnvironment = PropertyResolver.getTestEnvironment();
            List<String> filtered = paths.stream().filter(path -> path.contains("/" + testEnvironment + "/")).toList();
            if (filtered.size() > 1) {
                throw new IllegalArgumentException("Multiple resources found for file name: " + fileName +
                                                   ". But still could not determine a unique resource for the current test environment: " + testEnvironment + "."
                                                   + System.lineSeparator() + " Found paths: " + paths);
            } else if (filtered.isEmpty()) {
                throw new IllegalArgumentException("Multiple resources found for file name: " + fileName +
                                                   ". But none of them match the current test environment: " + testEnvironment + "."
                                                   + System.lineSeparator() + " Found paths: " + paths);
            } else {
                paths = filtered;
                SystemLogger.debug("Found resource for file name: " + fileName);
            }
        }
        return paths.getFirst();
    }

    private static InputStream getResourceFileAsStream(String filepath) throws IOException {
        InputStream input = ResourceUtils.class.getClassLoader().getResourceAsStream(getFilePathWithName(filepath));
        if (input == null) {
            throw new FileNotFoundException("Indexed resource does not exist on classpath: " + filepath);
        }
        return input;
    }

    private static boolean matchesAny(String path, List<Pattern> patterns) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    private static Pattern toGlobPattern(String glob) {
        String normalized = glob.replace('\\', '/').replaceFirst("^/+", "");

        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);

            if (current == '*') {
                boolean isDoubleStar = i + 1 < normalized.length() && normalized.charAt(i + 1) == '*';

                if (isDoubleStar) {
                    i++;
                    // **/ means zero or more directory segments
                    if (i + 1 < normalized.length() && normalized.charAt(i + 1) == '/') {
                        i++;
                        regex.append("(?:.*/)?");
                    } else {
                        regex.append(".*");
                    }
                } else {
                    regex.append("[^/]*");
                }
            } else if (current == '?') {
                regex.append("[^/]");
            } else if (".+()^$|{}[]".indexOf(current) >= 0) {
                regex.append('\\').append(current);
            } else {
                regex.append(current);
            }
        }
        return Pattern.compile(regex.append('$').toString());
    }

    /**
     * Loads the resource index from the classpath or generates it if not in package mode.
     *
     * @return A map of file names to their corresponding resource paths.
     */
    private static Map<String, List<String>> loadIndex() {
        if (PropertyResolver.isPackageMode()) {
            try (InputStream input = ResourceUtils.class.getClassLoader().getResourceAsStream(INDEX_FILE)) {
                if (input == null) {
                    throw new IllegalStateException("Resource index not found on classpath: " + INDEX_FILE);
                }
                return ObjectMapperSingleton.mapper().readValue(input, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, e, "Could not load resource index: " + INDEX_FILE);
            }
        } else {
            try {
                return ResourceIndexGenerator.generateResourceIndex(INDEX_FILE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}