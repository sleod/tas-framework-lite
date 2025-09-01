package ch.qa.testautomation.tas.exception;

/**
 * Exception thrown when test data is empty or not found for a given test case and step.
 */
public class TestDataEmptyException extends  ExceptionBase{
    public TestDataEmptyException(String testcaseName, String stepName) {
        super(ExceptionErrorKeys.TEST_DATA_NOT_MATCH, testcaseName, stepName);
    }
}
