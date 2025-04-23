package ch.qa.testautomation.tas.common.processhandling;


import ch.qa.testautomation.tas.common.logging.SystemLogger;

import java.util.Optional;

/**
 * @author Intellon
 * @date 16.05.2024
 *
 * CrossPlatformProcessScanner is the main class that serves as the entry point of the program.
 * This class determines the user's operating system and selects the appropriate ProcessHandler implementation.
 * It is responsible for initializing and executing the specific ProcessHandler.
 */
public class CrossPlatformProcessScanner {

    /**
     * Runs the process scanner. Detects the operating system and executes the appropriate process handler.
     */
    public void run() {
        String osName = System.getProperty("os.name").toLowerCase();
        SystemLogger.info("Detected OS: " + osName);
        getProcessHandler(osName)
                .ifPresentOrElse(
                        IProcessHandler::execute,
                        () -> SystemLogger.warn("Unsupported operating system: " + osName)
                );
    }

    /**
     * Returns an appropriate {@link IProcessHandler} based on the detected operating system name.
     *
     * @param osName the name of the operating system
     * @return an {@link Optional} containing an instance of {@link IProcessHandler} suitable for the operating system, or an empty {@link Optional} if unsupported
     */
    Optional<IProcessHandler> getProcessHandler(String osName) {
        return getWindowsHandler(osName)
                .or(() -> getUnixHandler(osName));
    }

    /**
     * Determines if the operating system is Windows and returns an {@link Optional} containing a {@link WindowsProcessHandler}.
     *
     * @param osName the name of the operating system
     * @return an {@link Optional} containing a {@link WindowsProcessHandler} if the OS is Windows, or an empty {@link Optional} otherwise
     */
    private Optional<IProcessHandler> getWindowsHandler(String osName) {
        return osName.contains("windows") ? Optional.of(new WindowsProcessHandler()) : Optional.empty();
    }

    /**
     * Determines if the operating system is a Unix-based system (Mac, Linux, AIX) and returns an {@link Optional} containing a {@link UnixProcessHandler}.
     *
     * @param osName the name of the operating system
     * @return an {@link Optional} containing a {@link UnixProcessHandler} if the OS is Unix-based, or an empty {@link Optional} otherwise
     */
    private Optional<IProcessHandler> getUnixHandler(String osName) {
        return (osName.contains("mac") || osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) ?
                Optional.of(new UnixProcessHandler()) : Optional.empty();
    }
}
