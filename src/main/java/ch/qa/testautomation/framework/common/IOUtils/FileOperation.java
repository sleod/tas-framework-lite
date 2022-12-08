package ch.qa.testautomation.framework.common.IOUtils;

import ch.qa.testautomation.framework.common.utils.StringTextUtils;
import ch.qa.testautomation.framework.core.assertion.Matchers;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static java.util.Arrays.asList;

public class FileOperation {

    private static final List<String> extensions = asList("txt", "csv", "log", "out", "html", "json", "xml", "pdf", "png", "jpg", "jpeg", "mp4");
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-5][0-9a-fA-F]{3}-[089ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";

    public static boolean isAllowedFileExtension(String fileName) {
        return extensions.contains(getFileNameExtension(fileName));
    }

    public static String getAllowedFileExtension() {
        return String.join(",", extensions);
    }

    public static String getMediaTypeOfFile(String fileName) {
        if (isAllowedFileExtension(fileName)) {
            String ext = getFileNameExtension(fileName);
            String mediaType;
            switch (ext) {
                case "png" -> mediaType = "image/png";
                case "jpg", "jpeg" -> mediaType = "image/jpeg";
                case "pdf" -> mediaType = "application/pdf";
                case "xml" -> mediaType = "application/xml";
                case "json" -> mediaType = "application/json";
                case "mp4" -> mediaType = "video/mp4";
                default -> mediaType = "text/plain";
            }
            return mediaType;
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "File is not allowed in Framework: " + fileName);
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
     * @return String of file content in list in utf-8
     */
    public static List<String> readFileToStringList(String filePath) {
        List<String> list = Collections.emptyList();
        if (isFileExists(filePath)) {
            try {
                list = Files.readAllLines(getFile(filePath), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, filePath);
            }
        } else {
            warn("File to read does not exist! -> " + filePath);
        }
        return list;
    }

    /**
     * @param inputStream is input stream of file
     * @return String of file content with break
     */
    public static String readFileToLinedString(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            try {
                while (reader.ready()) {
                    sb.append(reader.readLine());
                    sb.append(System.lineSeparator());
                }
            } catch (IOException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, "InputStream");
            }
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.NULL_EXCEPTION, "InputStream");
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
        File file = new File(filePath);
        return file.getName();
    }

    /**
     * @param root   root file
     * @param suffix without '.' e.g. 'txt', 'story'
     * @return list of files
     */
    public static List<File> listAllDeepFiles(File root, String suffix) {
        if (root.exists() && root.isDirectory()) {
            FilenameFilter filter = (file, name) -> name.toLowerCase().endsWith(".".concat(suffix.toLowerCase()));
            return asList(Objects.requireNonNull(root.listFiles(filter)));
        } else {
            warn(root.getAbsolutePath() + " may not a directory or an empty one!");
            return Collections.emptyList();
        }
    }

    /**
     * @param root    root file
     * @param pattern regex patten for matcher like "TEST-(.*?)([.]xml)"
     * @return list of files
     */
    public static List<File> listFilesWithRegex(File root, String pattern) {
        if (root.exists() && root.isDirectory()) {
            FilenameFilter filter = (file, name) -> {
                Matcher<String> matcher = Matchers.matchPattern(pattern);
                return matcher.matches(name.toLowerCase());
            };
            return asList(Objects.requireNonNull(root.listFiles(filter)));
        } else {
            warn(root.getAbsolutePath() + " may not a directory or an empty one!");
            return Collections.emptyList();
        }
    }

    /**
     * rename file with given name
     *
     * @param old     file to rename
     * @param newName new file name
     * @return file after rename
     */
    public static File renameFile(File old, String newName) {
        File newFile = new File(newName);
        old.renameTo(newFile);
        return newFile;
    }

    /**
     * find file that contains the given string in a map of files
     *
     * @param value        wanted string
     * @param mapOfStories map of files
     * @return first file contains the wanted string
     */
    public static File getFirstMatchedFileWithText(String value, LinkedHashMap<String, File> mapOfStories) {
        File file = null;
        for (String key : mapOfStories.keySet()) {
            File temp = mapOfStories.get(key);
            String content = readFileToLinedString(temp.getPath());
            if (content.contains(value)) {
                file = temp;
                break;
            }
        }
        return file;
    }

    /**
     * delete given file
     *
     * @param file file to be deleted
     * @throws IOException io exception
     */
    public static void deleteFile(File file) throws IOException {
        boolean dq = Files.deleteIfExists(file.toPath());
        trace("try to deleteQCEntityInQC File: " + file + " : " + dq);
    }

    /**
     * retrieve file from resource folder
     *
     * @param srcFile    to be retrieved
     * @param targetFile save to target
     * @return file
     */
    public static File retrieveFileFromResources(String srcFile, File targetFile) {
        File absoluteTargetFile = targetFile.getAbsoluteFile(); // this was needed to ensure both copy and other file operations use same interpretation of the path
        if (!absoluteTargetFile.exists()) {
            absoluteTargetFile.getParentFile().mkdirs();
        }
        try {
            Files.deleteIfExists(targetFile.toPath());
            if (srcFile.startsWith("/")) {
                srcFile = srcFile.substring(1);
            }
            if (retrieveFileFromResourcesAsStream(srcFile) != null) {
                Files.copy(retrieveFileFromResourcesAsStream(srcFile), absoluteTargetFile.toPath());
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.OBJECT_NOT_FOUND, srcFile);
            }
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, srcFile);
        }
        return absoluteTargetFile;
    }

    /**
     * retrieve file as stream from resource folder
     *
     * @param relativePath relative path to file
     * @return input stream
     */
    public static InputStream retrieveFileFromResourcesAsStream(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.NULL_EMPTY_EXCEPTION, "relativePath");
        } else {
            return FileOperation.class.getClassLoader().getResourceAsStream(relativePath);
        }
    }

    /**
     * get last modified file in directory with suffix
     *
     * @param dirPath path of directory
     * @return file
     */
    public static File getLatestFileFromDir(String dirPath, String suffix) {
        if (dirPath != null) {
            File dir = new File(dirPath);
            List<File> allFiles = listAllDeepFiles(dir, suffix);
            if (allFiles == null || allFiles.size() == 0) {
                return null;
            }
            File lastModifiedFile = allFiles.get(0);
            for (File file : allFiles) {
                if (lastModifiedFile.lastModified() < file.lastModified()) {
                    lastModifiedFile = file;
                }
            }
            return lastModifiedFile;
        } else {
            return null;
        }
    }

    /**
     * write byte array of content to file in utf-8
     *
     * @param bytes  to be written
     * @param target to local file
     */
    public static void writeBytesToFile(byte[] bytes, File target) {
        writeStringToFile(new String(bytes), target);
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
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_WRITING, ex, target);
        }
    }

    /**
     * write String to file with charset UTF-8
     *
     * @param text to be written
     * @param path to local file path
     */
    public static void writeStringToFile(String text, String path) {
        if (StringTextUtils.isValid(path)) {
            writeStringToFile(text, new File(path));
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.NULL_EXCEPTION, "path");
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
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_WRITING, ex, file.getPath());
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
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "removing file");
            }
        });
    }

    /**
     * delete folder
     *
     * @param pathDir Folder to delete
     */
    public static void deleteFolder(String pathDir) {

        File file = new File(pathDir);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "removing folder " + pathDir);
        }
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
     * get file path from resource folder
     *
     * @param relativePath relative path to file
     * @return file path
     */
    public static String getFilePathFromResource(String relativePath) {
        return FileLocator.findResource(relativePath).toString();
    }

    /**
     * encode whole file to base64 string
     *
     * @param file file
     * @return encoded string
     */
    public static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "encoding file " + file.getName());
        }
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
     * check if string matches to UUID pattern
     *
     * @param uuid uuid
     * @return if string match to uuid regex
     */
    public static boolean isUUID(String uuid) {
        return uuid.matches("^" + UUID_REGEX + "$");
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
            warn("IOException while moving file " + srcFile + " to " + tarFile + "!\n" + ex.getMessage());
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
            Files.copy(srcFile, tarFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "moving file: " + srcFile + " to file: " + tarFile, ex);
        }
    }

    /**
     * check if filePath in current classpath or in resource folder exists
     *
     * @param filePath relative or full path of file
     * @return true be found
     */
    public static boolean isFileExists(String filePath) {
        return new File(filePath).exists() || FileLocator.isResourceFileExists(filePath);
    }

    /**
     * get the file with filePath, which be secured using {@link #isFileExists(String filePath)}
     *
     * @param filePath filePath
     * @return the file
     */
    public static Path getFile(String filePath) {
        if (!new File(filePath).exists()) {
            return FileLocator.findResource(filePath);
        } else {
            return new File(filePath).toPath();
        }
    }

}
