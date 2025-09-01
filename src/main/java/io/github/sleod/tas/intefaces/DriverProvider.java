package io.github.sleod.tas.intefaces;

/**
  Interface for providing WebDriver instances.
 */
public interface DriverProvider {

    <T> T getDriver();

    void close();

    void initialize();
}
