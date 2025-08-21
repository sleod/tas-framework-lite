package ch.qa.testautomation.tas.exception;

public class TestDataEmptyException extends  ExceptionBase{
    public TestDataEmptyException(String testcaseName, String stepName) {
        super(ExceptionErrorKeys.TEST_DATA_NOT_MATCH, testcaseName, stepName);
    }
}
