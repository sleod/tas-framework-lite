package ch.qa.testautomation.tas.rest.base;

/**
 * Enum for query options in REST API calls.
 */
public enum QUERY_OPTION {
    FAILED_ONLY("failed_only"), SUCCESS_ONLY("success_only"), EXCEPT_SUCCESS("except_success"), ALL("all");

    private final String option;

    QUERY_OPTION(String option) {
        this.option = option;
    }

    public String option() {
        return option;
    }
}
