package ch.sleod.testautomation.framework.common.enumerations;

/**
 * image format general, for screenshot is PNG recommended
 */
public enum ImageFormat {
    JPEG("jpg"), PNG("PNG");

    private String format;

    ImageFormat(String format) {
        this.format = format;
    }

    public static ImageFormat getFormat(String format) {
        switch (format) {
            case "jpg":
                return JPEG;
            case "PNG":
                return PNG;
        }
        throw new RuntimeException("\""+format+"\" is not supported! Defined are (jpg and PNG).");
    }

    public String value() {
        return format;
    }
}
