package ch.qa.testautomation.tas.common.utils;

import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;

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
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_WRITING, ex, fileName);
        }
    }

    /**
     * read local file to deserialize object
     *
     * @param fileName file name
     * @return deserialized object
     */
    public static <T> T readObject(Class<T> targetClass, String fileName) {
        Object object;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            // Read objects
            object = objectInputStream.readObject();
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, fileName);
        } catch (ClassNotFoundException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Read Object failed! Object class can not be found!" + targetClass);
        }
        return (T) object;
    }

}
