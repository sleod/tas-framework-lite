package io.github.sleod.tas.common.enumerations;

/**
 * enum of test status
 */
public enum TestStatus {

    PASS(1, "Passed"), FAIL(-1, "Failed"), COMPLETE(2, "Complete"), NO_RUN(0, "No Run"),
    SKIPPED(5, "Skipped"), BROKEN(4, "Broken"), NOT_COMPLETE(3, "Not Complete");
    private final int intValue;
    private final String text;

    public String text() {
        return text;
    }

    public int intValue() {
        return intValue;
    }

    TestStatus(int intValue, String text) {
        this.intValue = intValue;
        this.text = text;
    }

}
