package ch.sleod.testautomation.framework.common.logging;

import ch.sleod.testautomation.framework.common.utils.TimeUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.component.TestStepResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Logger class of System using log4j2
 */
public class SystemLogger {

    //name for default system logger. Main key for operating appender and loggers.
    private static final String LOGGER_NAME = PropertyResolver.getDefaultLoggerName();
    private static final Logger LOGGER = LogManager.getLogger(LOGGER_NAME);
    private static final String DEFAULT_PATTERN = "%d{ISO8601} [%t] %-5level: %msg%n%throwable";
    private static final Map<String, TestStepResult> stepResults = new LinkedHashMap<>();

    public static void setCurrTestStepResult(String key, TestStepResult stepResult) {
        stepResults.put(key, stepResult);
    }

    public static void logStepInfo(String key, String text) {
        String info = getSimpleCustomInfo("INFO", "Step Info: " + text);
        info(text);
        stepResults.get(key).logInfo(info);
    }

    /**
     * log message with level name
     *
     * @param level level name
     * @param msg   message
     */
    public static void log(String level, String msg) {
        LOGGER.log(Level.getLevel(level), msg);
    }

    /**
     * log throwable
     *
     * @param ex throwable
     */
    public static void error(Throwable ex) {
        LOGGER.error(ex.getMessage(), ex);
    }

    /**
     * log string with pattern to format
     *
     * @param level   level name
     * @param pattern pattern for format
     * @param args    used for format via pattern
     */
    public static void log(String level, String pattern, String... args) {
        String format = pattern.replace("{}", "%s");
        Object[] strings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                strings[i] = "null";
            } else {
                strings[i] = args[i];
            }
        }
        log(level, String.format(format, strings));
    }

    public static void info(String info) {
        LOGGER.info(info);
    }

    public static void debug(String info) {
        LOGGER.debug(info);
    }

    public static void trace(String info) {
        LOGGER.trace(info);
    }

    public static void warn(String info) {
        LOGGER.warn(info);
    }

    /**
     * create appender to file writer for single report
     *
     * @param writer      file writer
     * @param appenderRef name of writer
     */
    public static Appender addAppender(final Writer writer, final String appenderRef, String appenderLevel) {
        final LoggerContext context = LoggerContext.getContext(false);
        final Configuration config = context.getConfiguration();
        final PatternLayout layout = PatternLayout.createDefaultLayout(config);
        final Appender appender = WriterAppender.createAppender(layout, null, writer, appenderRef, false, true);
        appender.start();
        config.addAppender(appender);
        updateLoggers(appender, config, appenderLevel);
        context.updateLoggers(config);
        return appender;
    }

    /**
     * add Appender for logging to separate file to LOGGER
     *
     * @param pattern  default Pattern in case pattern null: "%d{ISO8601} [%t] %-5level: %msg%n%throwable"
     * @param fileName log file name
     */
    public static void addFileAppender(String pattern, String fileName, String appenderRef, String appenderLevel) {
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();
        Appender appender = buildFileAppender(config, buildLayout(config, checkPattern(pattern)), fileName, appenderRef);
        appender.start();
        config.addAppender(appender);
        updateLogger(appender, config, LOGGER_NAME, appenderLevel);
        context.updateLoggers(config);
    }

    /**
     * add Appender for rolling file
     *
     * @param logPattern  default Pattern in case log Pattern null: "%d{ISO8601} [%t] %-5level: %msg%n%throwable"
     * @param fileName    rolling file name
     * @param filePattern file pattern for rolling
     */
    public static void addRollingFileAppender(String logPattern, String fileName, String filePattern, String appanderRef, String appenderLevel) {
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern(checkPattern(checkPattern(logPattern)))
                .build();

        RollingFileAppender appender = RollingFileAppender.newBuilder()
                .setConfiguration(config)
                .withName(appanderRef)
                .withLayout(layout)
                .withFileName(fileName)
                .withFilePattern(filePattern)
                .withPolicy(SizeBasedTriggeringPolicy.createPolicy("10MB"))
                .build();

        appender.start();
        config.addAppender(appender);
        updateLogger(appender, config, LOGGER_NAME, appenderLevel);
        context.updateLoggers(config);
    }

    /**
     * add file LOGGER
     *
     * @param appenderLevel level
     * @param fileName      rolling file name
     * @param pattern       file pattern for rolling
     */
    public static void addFileLogger(String appenderLevel, String pattern, String fileName, String appenderRef) {
        Level level = Level.getLevel(appenderLevel);
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();
        AppenderRef ref = AppenderRef.createAppenderRef("File", level, null);
        AppenderRef[] refs = new AppenderRef[]{ref};
        Layout layout = buildLayout(config, checkPattern(pattern));
        Appender appender = buildFileAppender(config, layout, fileName, appenderRef);
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, level, "SystemLogger", "true", refs, null, config, null);
        loggerConfig.addAppender(appender, level, null);
        config.addLogger(LOGGER_NAME, loggerConfig);
        context.updateLoggers(config);
    }

    /**
     * build File Appender
     *
     * @param config      current config of LOGGER
     * @param layout      appender layout
     * @param fileName    log file name
     * @param appenderRef appender name
     * @return return appender
     */
    @SuppressWarnings("unchecked")
    public static Appender buildFileAppender(final Configuration config, Layout layout, String fileName, String appenderRef) {
        return FileAppender.newBuilder()
                .setConfiguration(config)
                .withName(appenderRef)
                .withLayout(layout)
                .withFileName(fileName)
                .build();
    }

    /**
     * build layout with log pattern
     *
     * @param config  current config of LOGGER
     * @param pattern default Pattern in case log Pattern null: "%d{ISO8601} [%t] %-5level: %msg%n%throwable"
     * @return layout
     */
    public static Layout buildLayout(final Configuration config, String pattern) {
        return PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern(checkPattern(pattern))
                .build();
    }

    /**
     * remove appender with name
     *
     * @param ref name of appender
     */
    public static void removeAppender(String ref) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        config.getLoggerConfig(LOGGER_NAME).removeAppender(ref);
        ctx.updateLoggers();
    }

    /**
     * format simple custom information
     *
     * @param type log type
     * @param info content
     * @return formatted string
     */
    public static synchronized String getSimpleCustomInfo(String type, String info) {
        return TimeUtils.getISOTimestamp() + ": [" + Thread.currentThread().getName() + "] [" + type + "]: " + info;
    }

    private static String checkPattern(String pattern) {
        String logPattern = DEFAULT_PATTERN;
        if (pattern != null && !pattern.isEmpty() && !pattern.equalsIgnoreCase("default")) {
            logPattern = pattern;
        }
        return logPattern;
    }

    /**
     * update all configured loggers
     *
     * @param appender new appender to add
     * @param config   current config
     */
    private static void updateLoggers(final Appender appender, final Configuration config, String lv) {
        final Level level = Level.getLevel(lv);
        final Filter filter = null;
        for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(appender, level, filter);
        }
        config.getRootLogger().addAppender(appender, level, filter);
    }

    /**
     * update to named LOGGER
     *
     * @param appender   new appender to add
     * @param config     current config
     * @param loggerName target LOGGER
     */
    private static void updateLogger(final Appender appender, final Configuration config, String loggerName, String lv) {
        final Level level = Level.getLevel(lv);
        final Filter filter = null;
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        loggerConfig.addAppender(appender, level, filter);
    }

}
