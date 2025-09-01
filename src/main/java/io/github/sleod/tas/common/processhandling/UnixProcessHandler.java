package io.github.sleod.tas.common.processhandling;

import io.github.sleod.tas.common.logging.SystemLogger;

/**
 * UnixProcessHandler implements the ProcessHandler interface for Unix-based operating systems
 * such as Linux and macOS. This class uses the `ps` command executed via a shell
 * to find and analyze active Chrome processes. Similar to the WindowsProcessHandler,
 * this implementation focuses on detecting and terminating automated processes.
 */
public class UnixProcessHandler implements IProcessHandler {
    private final ProcessHandler scanner;

    /**
     * Constructs a {@link UnixProcessHandler} with a default {@link ProcessHandler} for scanning processes.
     */
    public UnixProcessHandler() {
        this.scanner = new ProcessHandler();
    }

    /**
     * Executes the process handler. This method initiates a process scan specific to Unix systems,
     * looking for processes related to Chrome and terminating them based on specific criteria.
     */
    @Override
    public void execute() {
        SystemLogger.info("Executing Unix Process Handler");
        scanner.executeProcessScan("sh -c ps aux | grep chrome", false);
    }
}