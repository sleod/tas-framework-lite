package ch.qa.testautomation.tas.common.processhandling;

/**
 * A factory class for creating {@link ProcessBuilder} instances.
 * This class implements the {@link IProcessBuilderFactory} interface.
 */
public class DefaultProcessBuilderFactory implements IProcessBuilderFactory {

    /**
     * Creates a new {@link ProcessBuilder} instance using the specified command.
     *
     * @param command an array of strings representing the command to be executed and its arguments
     * @return a new {@link ProcessBuilder} instance configured with the specified command
     */    @Override
    public ProcessBuilder createProcessBuilder(String[] command) {
        return new ProcessBuilder(command);
    }
}