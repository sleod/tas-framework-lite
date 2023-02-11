package ch.qa.testautomation.framework.common.enumerations;

import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;

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
        throw new ApollonBaseException(ApollonErrorKeys.ENUM_NOT_SUPPORTED);
    }

    public String value() {
        return format;
    }
}
