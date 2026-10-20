package io.github.sleod.tas.common.IOUtils;

import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.logging.SystemLogger.info;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;
import static java.util.Arrays.asList;

/**
 * Utility class for file operations.
 */
public class FileOperation {

    /**
     * Commonly referenced by public methods for file extension validation.
     */
    private static final List<String> extensions = asList("txt", "csv", "log", "out", "html", "json", "xml", "pdf", "png", "jpg", "jpeg", "mp4", "zip");
    /**
     * Commonly referenced by public methods for UUID validation.
     */
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-5][0-9a-fA-F]{3}-[089ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";

    /**
     * Checks if the given file has an allowed file extension.
     *
     * @param fileName the name of the file to check
     * @return true if the file has an allowed extension, false otherwise
     */
    public static boolean isAllowedFileExtension(String fileName) {
        return extensions.contains(getFileNameExtension(fileName));
    }

    /**
     * Returns a comma-separated list of allowed file extensions.
     *
     * @return a comma-separated list of allowed file extensions
     */
    public static String getAllowedFileExtension() {
        return String.join(",", extensions);
    }

    public static String getMediaTypeOfFile(String fileName) {
        if (isAllowedFileExtension(fileName)) {
            String ext = getFileNameExtension(fileName);
            String mediaType;
            switch (ext) {
                case "png", "jpg", "jpeg", "gif", "svg+xml", "tiff" -> mediaType = "image/" + ext;
                case "json", "xml", "pdf", "zip", "yaml" -> mediaType = "application/" + ext;
                case "mp4", "ogg", "webm" -> mediaType = "video/" + ext;
                case "html", "csv" -> mediaType = "text/" + ext;
                default -> mediaType = "text/plain";
            }
            return mediaType;
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "File is not allowed in Framework: " + fileName);
        }
    }

    /**
     * @param filePath is absolute path from file to read
     * @return String of file content with break
     */
    public static String readFileToLinedString(String filePath) {
        StringBuilder sb = new StringBuilder();
        List<String> lines = readFileToStringList(filePath);
        for (String line : lines) {
            sb.append(line);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * @param filePath is absolute path from file to read
     * @return String list of file content in list in utf-8
     */
    public static List<String> readFileToStringList(String filePath) {
        List<String> list;
        if (isResourceFileExists(filePath)) {
            try {
                list = Files.readAllLines(getFile(filePath), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_READING, ex, filePath);
            }
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_READING, "File to read does not exist! -> " + filePath);
        }
        return list;
    }

    /**
     * @param inputStream is input stream of file
     * @return String of file content with break
     */
    public static String readFileStreamToLinedString(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            try {
                while (reader.ready()) {
                    sb.append(reader.readLine());
                    sb.append(System.lineSeparator());
                }
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_READING, ex, "InputStream");
            }
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION, "InputStream");
        }
        return sb.toString();
    }

    /**
     * get file name via file path
     *
     * @param filePath path to file
     * @return The name of the file or directory denoted by this abstract
     * pathname, or the empty string if this pathname's name sequence
     * is empty
     */
    public static String getFileName(String filePath) {
        return new File(filePath).getName();
    }

    /**
     * delete given file
     *
     * @param file file to be deleted
     */
    public static void deleteFile(File file) {
        try {
            boolean isOK = Files.deleteIfExists(file.toPath());
            debug("try to delete File: " + file + " : " + isOK);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "Failed on deleting file: " + file.getPath());
        }
    }

    public static void deleteFolder(File folder) {
        if (folder != null && folder.exists()) {
            info("Deleting folder: " + folder.getAbsolutePath());
            try (Stream<Path> paths = Files.walk(folder.toPath())) {
                paths.sorted(Comparator.reverseOrder()).forEach(file -> FileOperation.deleteFile(file.toFile()));
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "Failed on deleting folder: " + folder.getPath());
            }
        } else info("Delete folder: " + folder + " does not exist!");
    }


    /**
     * retrieve file as stream from resource folder
     *
     * @param relativePath relative path to file
     * @return input stream
     */
    public static InputStream retrieveFileFromResourcesAsStream(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION_EMPTY, "relativePath");
        } else {
            return FileOperation.class.getClassLoader().getResourceAsStream(relativePath);
        }
    }

    /**
     * stream mediaString base64 encoded content to file
     *
     * @param mediaString to be written
     * @param target      to local file
     */
    public static void streamMediaStringToFile(String mediaString, File target) {
        try (FileOutputStream stream = new FileOutputStream(target)) {
            stream.write(Base64.getDecoder().decode(mediaString));
        } catch (Exception ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, target);
        }
    }

    /**
     * write String to file with charset UTF-8
     *
     * @param text   to be written
     * @param target to local file
     */
    public static void writeStringToFile(String text, File target) {
        try {
            if (!target.getParentFile().exists()) {
                Assertions.assertTrue(target.getParentFile().mkdirs(), "Failed on make Folder for File: " + target.getAbsolutePath());
            }
            Files.writeString(target.toPath(), text, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, target);
        }
    }

    /**
     * write String to file with charset UTF-8
     *
     * @param text to be written
     * @param path to local file path
     */
    public static void writeStringToFile(String text, String path) {
        if (isValid(path)) {
            writeStringToFile(text, new File(path));
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION, "path");
        }
    }


    /**
     * read file content to byte array
     *
     * @param file to be read
     * @return byte array
     */
    public static byte[] readFileToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, file.getPath());
        }
    }


    /**
     * delete files via path
     *
     * @param resultFiles list of file paths
     */
    public static void deleteFiles(List<String> resultFiles) {
        resultFiles.forEach(path -> {
            try {
                Files.deleteIfExists(new File(path).toPath());
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "removing file");
            }
        });
    }

    /**
     * list all files in resource folder
     *
     * @param folder folder name
     * @return file array
     */
    public static File[] getResourceFolderFiles(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = Objects.requireNonNull(url).getPath();
        return new File(path).listFiles();
    }

    /**
     * encode whole file to base64 string
     *
     * @param file file
     * @return encoded string
     */
    public static String encodeFileToBase64(File file) {
        return Base64.getEncoder().encodeToString(readFileToByteArray(file));
    }

    /**
     * get file extension after last "." in lowercase
     *
     * @param fileName file name
     * @return extension Returns the string after character '.'
     */
    public static String getFileNameExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } else {
            return "";
        }
    }

    /**
     * check String UUID start with
     *
     * @param valueToCheck value to be checked
     * @return true when im String eine UUID gefunden wurde
     */
    public static boolean startWithUUID(String valueToCheck) {
        return valueToCheck.matches("^" + UUID_REGEX + ".*?$");
    }

    /**
     * Move File to dir
     *
     * @param srcFile source file
     * @param tarFile target file
     */
    public static void moveFileTo(Path srcFile, Path tarFile) {
        try {
            Files.move(srcFile, tarFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            debug("IOException while moving file " + srcFile + " to " + tarFile + "!\n" + ex.getMessage());
        }
    }

    /**
     * Copy File to dir
     *
     * @param srcFile source file
     * @param tarFile target file
     */
    public static void copyFileTo(Path srcFile, Path tarFile) {
        try {
            if (!tarFile.toFile().getParentFile().exists()) {
                if (tarFile.toFile().mkdirs()) {
                    debug("Make dirs successful!");
                } else {
                    debug("Make dirs unsuccessful!");
                }
            }
            debug("Copy file: " + srcFile + " -> " + tarFile);
            Files.copy(srcFile, tarFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_GENERAL, ex, "Copy file: " + srcFile + " to file: " + tarFile);
        }
    }

    /**
     * check if filePath in current classpath or in resource folder exists
     *
     * @param filePath relative or full path of file
     * @return true be found
     */
    public static boolean isResourceFileExists(String filePath) {
        return isValid(filePath) && ResourceUtils.isResourceFileExists(FileOperation.getFileName(filePath));
    }

    /**
     * get the file with filePath, which be secured using {@link #isResourceFileExists(String filePath)}
     *
     * @param filePath filePath
     * @return the file
     */
    public static Path getFile(String filePath) {
        if (isResourceFileExists(filePath)) {
            return Path.of(ResourceUtils.getFilePathWithName(FileOperation.getFileName(filePath)));
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "File does not exist in resource folder or classpath: " + filePath);
        }
    }

    /**
     * Creates the specified directory and any necessary parent directories.
     *
     * @param folder the directory to create
     */
    public static void makeDirs(File folder) {
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                debug("Make dirs successful!");
            } else {
                debug("Make dirs unsuccessful!");
            }
        }
    }
}
