package ch.qa.testautomation.tas.common.enumerations;

/**
 * file format for test data
 */
public enum FileFormat {
    JSON("json"), EXCEL("xls"), EXCELX("xlsx"), CSV("csv"), SQL("sql");
    private final String format;

    FileFormat(String format) {
        this.format = format;
    }

    public String value() {
        return format;
    }
}