package ch.qa.testautomation.tas.common.enumerations;

import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;

/**
 * image format general, for screenshot is PNG recommended
 */
public enum ImageFormat {
    JPEG("jpg"), PNG("png");

    private final String format;

    ImageFormat(String format) {
        this.format = format;
    }

    public static ImageFormat getFormat(String format) {
        switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> {
                return JPEG;
            }
            case "png" -> {
                return PNG;
            }
        }
        throw new ExceptionBase(ExceptionErrorKeys.ENUM_NOT_SUPPORTED, format);
    }

    public String value() {
        return format;
    }
}
