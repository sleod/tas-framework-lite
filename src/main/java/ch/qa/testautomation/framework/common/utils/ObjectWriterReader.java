package ch.qa.testautomation.framework.common.utils;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import java.io.*;

/**
 * Read and Write Serializable Object
 */
public class ObjectWriterReader {

    /**
     * write serializable object to local
     *
     * @param object   object
     * @param fileName file name
     */
    public static void WriteObject(Serializable object, String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // Write objects to file
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            SystemLogger.trace("File not found: " + fileName);
        } catch (IOException e) {
            SystemLogger.trace("IO Exception of Stream!");
            SystemLogger.error(e);
        }
    }

    /**
     * read local file to deserialize object
     *
     * @param object   object with given type
     * @param fileName file name
     * @return deserialized object
     */
    public static Object readObject(Object object, String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            // Read objects
            object = objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            SystemLogger.trace("File not found: " + fileName);
        } catch (IOException e) {
            SystemLogger.trace("IO Exception of Stream!");
            SystemLogger.error(e);
        } catch (ClassNotFoundException e) {
            SystemLogger.trace("Read Object failed! Object class can not be found!");
            SystemLogger.error(e);
        }
        return object;
    }

}
