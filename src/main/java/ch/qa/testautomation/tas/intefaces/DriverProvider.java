package ch.qa.testautomation.tas.intefaces;

public interface DriverProvider {

    <T> T getDriver();

    void close();

    void initialize();
}
