package ch.qa.testautomation.framework.exception;

public enum ApollonErrorKeys {

    //Core Component
    TEST_DATA_REFERENCE_FORMAT_UNSUPPORTED("TEST_DATA_REFERENCE_FORMAT_UNSUPPORTED"),
    TEST_DATA_REFERENCE_NO_MATCH("TEST_DATA_REFERENCE_NO_MATCH"),
    TEST_DATA_FOR_TEST_OBJECT_NOT_DEFINED("TEST_DATA_FOR_TEST_OBJECT_NOT_DEFINED"),
    TEST_DATA_NOT_MATCH("TEST_DATA_NOT_MATCH"),
    TEST_DATA_GLOBAL_FILE_SHOULD_BE_UNIQUE("TEST_DATA_GLOBAL_FILE_SHOULD_BE_UNIQUE"),
    ADDITIONAL_TEST_DATA_FILE_CAN_ONLY_BE_JSON("ADDITIONAL_TEST_DATA_FILE_CAN_ONLY_BE_JSON"),
    TEST_CASE_NOT_FOUND("TEST_CASE_NOT_FOUND"),
    TEST_CASE_ID_IS_REQUIRED("TEST_CASE_ID_IS_REQUIRED"),
    TEST_CASE_INITIALIZATION_FAILED("TEST_CASE_INITIALIZATION_FAILED"),
    TEST_FAILURE_UNKNOWN("TEST_FAILURE_UNKNOWN"),
    OBJECT_NOT_FOUND("OBJECT_NOT_FOUND"),
    TEST_STEP_REQUIRED_PARAMETER_NOT_FOUND("TEST_STEP_REQUIRED_PARAMETER_NOT_FOUND"),
    TEST_DATA_PARAMETER_NOT_FOUND("TEST_DATA_PARAMETER_NOT_FOUND"),
    FAIL_ON_GET_TESTCASE_ID_FROM_TFS("FAIL_ON_GET_TESTCASE_ID_FROM_TFS"),
    FAIL_ON_GET_TESTCASE_ID_FROM_JIRA("FAIL_ON_GET_TESTCASE_ID_FROM_JIRA"),
    PAGE_IS_NOT_DISPLAYED_WITH_URL("PAGE_IS_NOT_DISPLAYED_WITH_URL"),
    IMPLEMENTATION_NOT_FOUND("IMPLEMENTATION_NOT_FOUND"),
    METHOD_NOT_FOUND("METHOD_NOT_FOUND"),
    METHOD_SHOULD_BE_PUBLIC("METHOD_SHOULD_BE_PUBLIC"),
    NO_WEB_DRIVER_INITIALIZED("NO_WEB_DRIVER_INITIALIZED"),
    INITIALIZATION_FAILED("INITIALIZATION_FAILED"),
    CONNECTION_TO_DB_IS_NOT_SET_WELL("CONNECTION_TO_DB_IS_NOT_SET_WELL"),
    METHOD_WITH_ANNOTATION_SHOULD_ONLY_BE_DEFINED_ONCE("METHOD_WITH_ANNOTATION_SHOULD_ONLY_BE_DEFINED_ONCE"),
    COLUMN_AND_CSV_HEADER_ARE_NOT_IDENTICAL("COLUMN_AND_CSV_HEADER_ARE_NOT_IDENTICAL"),
    DEFINED_MULTIPLE_TESTCASES("DEFINED_MULTIPLE_TESTCASES"),
    SERIES_NUMBER_FORMAT_WRONG("SERIES_NUMBER_FORMAT_WRONG"),
    FAILED_TO_RELOAD_COOKIES("FAILED_TO_RELOAD_COOKIES"),
    KEY_NOT_FOUND_IN_TEST_DATA_SOURCE("KEY_NOT_FOUND_IN_TEST_DATA_SOURCE"),
    DEFINED_MULTIPLE_FILES("DEFINED_MULTIPLE_FILES"),

    //Controller
    DOWNLOAD_WEB_DRIVER_NOT_DEVELOPED_YET("DOWNLOAD_WEB_DRIVER_NOT_DEVELOPED_YET"),
    PROPERTIES_HAS_CONFLICT("PROPERTIES_HAS_CONFLICT"),

    //Logging
    IOEXCEPTION_BY_SCREEN_CAPTURE("IOEXCEPTION_BY_SCREEN_CAPTURE"),
    EXCEPTION_BY_INVOKE_ANNOTATION("EXCEPTION_BY_INVOKE_ANNOTATION"),


    //Utils
    INTERRUPTED_WHILE_WAITING("INTERRUPTED_WHILE_WAITING"),

    //Mobile
    CAPABILITY_IS_NOT_SET("CAPABILITY_IS_NOT_SET"),
    ANDROID_AND_IOS_DRIVER_BOTH_ARE_NULL("ANDROID_AND_IOS_DRIVER_BOTH_ARE_NULL"),

    //REST
    GIVEN_TEST_NOT_FOUND_IN_TESTPLAN("GIVEN_TEST_NOT_FOUND_IN_TESTPLAN"),
    TEST_POINT_NOT_FOUND("TEST_POINT_NOT_FOUND"),
    ERROR_WHILE_GETTING_TESTCASE_ID("ERROR_WHILE_GETTING_TESTCASE_ID"),
    JIRA_ISSUE_SUMMARY_EXISTS("JIRA_ISSUE_SUMMARY_EXISTS"),
    TEST_RUN_OR_TEST_RUN_RESULT_IS_NULL("TEST_RUN_OR_TEST_RUN_RESULT_IS_NULL"),

    FAIL_TO_DOWNLOAD_FILE("FAIL_TO_DOWNLOAD_FILE"),

    //Common Enumerations
    FORMAT_NOT_SUPPORTED("FORMAT_NOT_SUPPORTED"),
    BROWSER_NOT_SUPPORTED("BROWSER_NOT_SUPPORTED"),

    //IO
    NULL_EXCEPTION("NULL_EXCEPTION"),
    NULL_EMPTY_EXCEPTION("NULL_EXCEPTION_EMPTY"),
    IOEXCEPTION_GENERAL("IOEXCEPTION_GENERAL"),
    IOEXCEPTION_BY_READING("IOEXCEPTION_BY_READING"),
    IOEXCEPTION_BY_WRITING("IOEXCEPTION_BY_WRITING"),
    ERROR_LIST_FILE_IN_FOLDER("ERROR_LIST_FILE_IN_FOLDER"),

    //Remote Web Driver
    GRDNUUID_REALDEVICEUUID_NOT_SET("GRDNUUID_REALDEVICEUUID_NOT_SET"),

    //Custom Message
    CUSTOM_MESSAGE("CUSTOM_MESSAGE"),

    //Mobile
    MOBILE_DRIVER_TYPE_UNDEFINED("MOBILE_DRIVER_TYPE_UNDEFINED"),
    //json
    EXCEPTION_BY_DESERIALIZATION("EXCEPTION_BY_DESERIALIZATION"),

    //Config file
    CONFIG_ERROR("CONFIG_ERROR"),
    CONFIG_FILE_NOT_FOUND("CONFIG_FILE_NOT_FOUND"),
    CONFIG_EMPTY_OR_NOT_FOUND("CONFIG_EMPTY_OR_NOT_FOUND"),

    //parsing
    EXCEPTION_BY_PARSING("EXCEPTION_BY_PARSING");

    private final String messageKey;

    ApollonErrorKeys(String message) {
        this.messageKey = message;
    }

    public String getErrorKey() {

        return messageKey;
    }
}