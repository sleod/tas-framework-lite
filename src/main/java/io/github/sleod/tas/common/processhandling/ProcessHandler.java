package io.github.sleod.tas.common.processhandling;

import io.github.sleod.tas.common.logging.SystemLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The ProcessHandler class provides methods for scanning, analyzing, and terminating processes
 * based on command-line arguments. This class is utilized by specific ProcessHandler implementations
 * to initiate the process, analyze the output, and support both Windows and Unix-based systems
 * by adapting the command line syntax accordingly.
 */
public class ProcessHandler {
    private final IProcessBuilderFactory processBuilderFactory;

    /**
     * Constructs a {@link ProcessHandler} with the default {@link IProcessBuilderFactory} implementation.
     */
    public ProcessHandler() {
        this(new DefaultProcessBuilderFactory());
    }

    /**
     * Constructs a {@link ProcessHandler} with the specified {@link IProcessBuilderFactory}.
     *
     * @param processBuilderFactory the factory used to create {@link ProcessBuilder} instances
     */
    public ProcessHandler(IProcessBuilderFactory processBuilderFactory) {
        this.processBuilderFactory = processBuilderFactory;
    }

    /**
     * Executes a process scan using the specified command and terminates specific processes based on criteria.
     *
     * @param command   the command to execute for scanning processes
     * @param isWindows a flag indicating if the operating system is Windows
     */
    public void executeProcessScan(String command, boolean isWindows) {
        try {
            ProcessBuilder builder = processBuilderFactory.createProcessBuilder(command.split(" "));
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("chrome") && (line.contains("--headless") || line.contains("--disable-gpu") || line.contains("--remote-debugging-port") || line.contains("--enable-automation") || line.contains("--no-sandbox"))) {
                    String processId = extractProcessId(line, isWindows);
                    if (processId != null) {
                        SystemLogger.info("Terminating process: " + processId);
                        terminateProcess(processId, isWindows);
                    }
                }
            }
        } catch (IOException e) {
            SystemLogger.error("Error scanning processes: " + e.getMessage());
        }
    }

    /**
     * Extracts the process ID from the provided line of process information.
     *
     * @param line      the line of process information
     * @param isWindows a flag indicating if the operating system is Windows
     * @return the extracted process ID, or null if it could not be determined
     */
    private String extractProcessId(String line, boolean isWindows) {
        String[] parts = line.trim().split("\\s+");
        return isWindows ? parts[parts.length - 1] : parts[1]; // Process ID is last on Windows, second on Unix
    }

    /**
     * Terminates the process with the specified process ID.
     *
     * @param processId the ID of the process to terminate
     * @param isWindows a flag indicating if the operating system is Windows
     */
    private void terminateProcess(String processId, boolean isWindows) {
        try {
            String[] cmd = isWindows ? new String[]{"cmd.exe", "/c", "taskkill", "/F", "/PID", processId}
                    : new String[]{"kill", "-9", processId};
            new ProcessBuilder(cmd).start();
            SystemLogger.info("Successfully terminated process: " + processId);
        } catch (IOException e) {
            SystemLogger.error("Failed to terminate process " + processId + ": " + e.getMessage());
        }
    }
}
