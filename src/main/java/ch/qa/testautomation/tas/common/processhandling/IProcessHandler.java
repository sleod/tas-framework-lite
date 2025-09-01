package ch.qa.testautomation.tas.common.processhandling;

/**
 * The ProcessHandler interface defines a general method for executing processes.
 * This allows for flexible implementations of different process handling methods
 * for different operating systems. Each specific implementation must implement
 * this interface and define the execute method accordingly.
 */
public interface IProcessHandler {

    /**
     * Executes a process. The specific implementation should define
     * the steps and logic required to execute the process.
     */
    void execute();
}
