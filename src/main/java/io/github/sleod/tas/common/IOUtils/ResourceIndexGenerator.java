package io.github.sleod.tas.common.IOUtils;

import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceIndexGenerator {

    /**
     * generate resource index and write to a property file in given folder
     *
     * @param filePath index property file path which contains the scan result
     * @return Map of file name and relative path list
     * @throws Exception IO Exception
     */
    public static Map<String, List<String>> generateResourceIndex(String filePath) throws Exception {
        return generateFileIndex("src/main/resources", filePath);
    }

    public static void main(String[] args) throws Exception {
        generateFileIndex(args[0], args[1]);
    }

    /**
     * generate file index in a given folder
     *
     * @param rootFolder     root folder for file scanning
     * @param outputFilePath index property file path which contains the scan result
     * @return Map of file name and relative path list
     * @throws Exception IO Exception
     */
    public static Map<String, List<String>> generateFileIndex(String rootFolder, String outputFilePath) throws Exception {

        Path root = Paths.get(rootFolder).toAbsolutePath().normalize();
        Path output = Paths.get(outputFilePath).toAbsolutePath().normalize();

        if (!Files.isDirectory(root)) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Not a directory: " + root);
        }

        Map<String, List<String>> index;

        try (Stream<Path> paths = Files.walk(root)) {
            index = paths.filter(Files::isRegularFile)
                    .filter(path -> !path.toAbsolutePath().normalize().equals(output))
                    .collect(Collectors.groupingBy(path -> path.getFileName().toString(),
                            Collectors.mapping(path -> root.relativize(path)
                                    .toString().replace(File.separatorChar, '/'), Collectors.toList())
                    ));
        }
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectMapperSingleton.mapper().writeValue(output.toFile(), index);
        return index;
    }

}