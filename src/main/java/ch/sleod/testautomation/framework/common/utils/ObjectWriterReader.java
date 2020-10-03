package ch.sleod.testautomation.framework.common.utils;

import java.io.*;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.trace;

public class ObjectWriterReader {

    /**
     * write serializable object to local
     *
     * @param object   object
     * @param fileName file name
     */
    public static void WriteObject(Serializable object, String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // Write objects to file
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            trace("File not found: " + fileName);
        } catch (IOException e) {
            trace("IO Exception of Stream!");
            error(e);
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
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            // Read objects
            object = objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            trace("File not found: " + fileName);
        } catch (IOException e) {
            trace("IO Exception of Stream!");
            error(e);
        } catch (ClassNotFoundException e) {
            trace("Read Object failed! Object class can not be found!");
            error(e);
        }
        return object;
    }

}
