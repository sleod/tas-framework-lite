package ch.qa.testautomation.tas.rest.jira;

public enum JiraRunStatus {
    FAIL(-1, "FAIL"), PASS(1, "PASS"), TODO(0, "TODO"), ABORTED(4, "ABORTED"),
    NA(99, "N/A"), EXECUTING(3, "EXECUTING");
    private final int intValue;
    private final String text;

    public String text() {
        return text;
    }

    public int intValue() {
        return intValue;
    }

    public static String getText(int index) {
        switch (index) {
            case -1:
                return FAIL.text();
            case 1:
                return PASS.text();
            case 0:
                return TODO.text();
            case 3:
                return EXECUTING.text();
            case 4:
                return ABORTED.text();
        }
        return "N/A";
    }

    JiraRunStatus(int intValue, String text) {
        this.intValue = intValue;
        this.text = text;
    }
}
