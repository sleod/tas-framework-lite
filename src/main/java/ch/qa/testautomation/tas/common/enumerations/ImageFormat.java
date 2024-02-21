package ch.qa.testautomation.tas.common.enumerations;

import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;

/**
 * image format general, for screenshot is PNG recommended
 */
public enum ImageFormat {
    JPEG("jpg"), PNG("PNG");

    private final String format;

    ImageFormat(String format) {
        this.format = format;
    }

    public static ImageFormat getFormat(String format) {
        switch (format) {
            case "jpg" -> {
                return JPEG;
            }
            case "PNG" -> {
                return PNG;
            }
        }
        throw new ExceptionBase(ExceptionErrorKeys.ENUM_NOT_SUPPORTED,format);
    }

    public String value() {
        return format;
    }
}
