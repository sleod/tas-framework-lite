package ch.qa.testautomation.tas.common.logging;

import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.TestStepMonitor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logger class of System using log4j2
 */
public class SystemLogger {

    /**
     * Level	Value   Description
     * OFF	    0	    No logging
     * FATAL	100	    The application is unusable. Action needs to be taken immediately.
     * ERROR	200	    An error occurred in the application.
     * WARN	    300	    Something unexpected—though not necessarily an error—happened and needs to be watched.
     * INFO	    400	    A normal, expected, relevant event happened.
     * DEBUG	500	    Used for debugging purposes
     * TRACE	600	    Used for debugging purposes—includes the most detailed information
     */

    //name for default system logger. Main key for operating appender and loggers.
    private static final Logger LOGGER = LogManager.getLogger("SystemLogger");
    public static final Level STEP_INFO = Level.forName("STEP_INFO", 350);

    public synchronized static void logStepInfo(String text) {
        String info = getSimpleCustomInfo("STEP_INFO", text);
        stepInfo(text);
        TestStepMonitor.getCurrentStep().getTestStepResult().logInfo(info);
    }

    /**
     * log throwable with fatal error that need to terminate system immediately
     *
     * @param ex throwable
     */
    public static void fatal(Throwable ex) {
        LOGGER.fatal(ex.getMessage(), ex);
        //exit system with exception in init phase
        System.exit(1);
    }

    /**
     * log throwable with fatal error that need to terminate system immediately
     *
     * @param ex throwable
     */
    public static void error(Throwable ex) {
        LOGGER.error(ex.getMessage(), ex);
    }

    public static void error(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= Level.ERROR.intLevel()) {
            LOGGER.error(msg);
        }
    }

    public static void warn(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= Level.WARN.intLevel()) {
            LOGGER.warn(msg);
        }
    }

    public static void info(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= Level.INFO.intLevel()) {
            LOGGER.info(msg);
        }
    }

    public static void stepInfo(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= STEP_INFO.intLevel()) {
            LOGGER.log(STEP_INFO, msg);
        }
    }

    public static void debug(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= Level.DEBUG.intLevel()) {
            LOGGER.debug(msg);
        }
    }

    public static void trace(String msg) {
        if (Level.getLevel(PropertyResolver.getTASLogLevel()).intLevel() >= Level.TRACE.intLevel()) {
            LOGGER.trace(msg);
        }
    }

    /**
     * format simple custom information
     *
     * @param type log type
     * @param info content
     * @return formatted string
     */
    public static synchronized String getSimpleCustomInfo(String type, String info) {
        return DateTimeUtils.getISOTimestamp() + ": [" + Thread.currentThread().getName() + "] [" + type + "]: " + info;
    }
}