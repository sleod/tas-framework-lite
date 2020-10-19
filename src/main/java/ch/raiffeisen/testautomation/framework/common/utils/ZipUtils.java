package ch.raiffeisen.testautomation.framework.common.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;

public class ZipUtils {

    /**
     * unzip file to current folder
     *
     * @param style zip file
     */
    public static void unzipFile(File style) {
        unzip(style, style.getPath());
    }

    /**
     * zip file into current folder
     *
     * @param zipFile zip file
     */
    public static void unzipFileHere(File zipFile) {
        File zipFilePath = zipFile.getAbsoluteFile();
        unzip(zipFilePath, zipFilePath.getParentFile().getPath());
    }

    /**
     * zip files to target path
     *
     * @param files       to be zipped
     * @param zipFilePath path to new zipped file
     * @return zip file
     */
    public static File zipFiles(List<File> files, String zipFilePath) {
        File zipFile = new File(zipFilePath);
        try {
            zipFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (File file : files) {
                zos.putNextEntry(new ZipEntry(file.getName()));
                byte[] bytes = Files.readAllBytes(file.toPath());
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
            zos.close();
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", zipFilePath);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
        return zipFile;
    }

    /**
     * zip single file to target path
     *
     * @param file        file to zip
     * @param zipFilePath path to new zip file
     * @return file
     */
    public static File zipFile(File file, String zipFilePath) {
        File zipFile = new File(zipFilePath);
        try {
            zipFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry(file.getName()));
            byte[] bytes = Files.readAllBytes(file.toPath());
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();
        } catch (IOException ex) {
            error(ex);
        }
        return zipFile;
    }

    /**
     * unzip file to target folder
     *
     * @param zipFilePath path of zip file
     * @param targetDir   new unzipped file location
     */
    private static void unzip(File zipFilePath, String targetDir) {
        try {
            // Open the zip file
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();
                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
                System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
                        name, size, compressedSize);
                // Do we need to create a directory ?
                File newFile = new File(targetDir + File.separator + name);
                if (name.endsWith("/")) {
                    newFile.mkdirs();
                    continue;
                }

                File parent = newFile.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                // Extract the file
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
