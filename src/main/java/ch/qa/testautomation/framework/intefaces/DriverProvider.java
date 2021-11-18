package ch.qa.testautomation.framework.intefaces;

public interface DriverProvider {

    <T> T getDriver();

    void close();

    void initialize();
}