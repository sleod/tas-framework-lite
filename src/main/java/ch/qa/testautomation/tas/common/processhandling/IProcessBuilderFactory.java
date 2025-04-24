package ch.qa.testautomation.tas.common.processhandling;

/**
 * @author Intellon
 * @date 17.05.2024
 *
 * An interface for creating {@link ProcessBuilder} instances.
 * Implementing classes are responsible for providing the logic to
 * create and configure {@link ProcessBuilder} objects.
 */
public interface IProcessBuilderFactory {

    /**
     * Creates a new {@link ProcessBuilder} instance using the specified command.
     *
     * @param command an array of strings representing the command to be executed and its arguments
     * @return a new {@link ProcessBuilder} instance configured with the specified command
     */
    ProcessBuilder createProcessBuilder(String[] command);
}