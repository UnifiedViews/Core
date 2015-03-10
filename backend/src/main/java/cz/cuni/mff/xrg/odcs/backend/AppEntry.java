package cz.cuni.mff.xrg.odcs.backend;

import java.io.File;
import java.util.Locale;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import eu.unifiedviews.commons.i18n.DataunitLocaleHolder;
import org.h2.store.Data;
import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import cz.cuni.mff.xrg.odcs.backend.auxiliaries.AppLock;
import cz.cuni.mff.xrg.odcs.backend.logback.MdcExecutionLevelFilter;
import cz.cuni.mff.xrg.odcs.backend.logback.MdcFilter;
import cz.cuni.mff.xrg.odcs.backend.logback.SqlAppender;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;

/**
 * Backend entry point.
 * 
 * @author Petyr
 */
public class AppEntry {

    /**
     * Path to the spring configuration file.
     */
    private final static String SPRING_CONFIG_FILE = "backend-context.xml";

    /**
     * Logger class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AppEntry.class);

    /**
     * Spring context.
     */
    private AbstractApplicationContext context = null;

    /**
     * Initialise spring and load configuration.
     */
    private void initSpring() {
        // load spring
        context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);
        context.registerShutdownHook();
    }

    private RollingFileAppender createAppender(LoggerContext loggerContext,
            String logDirectory, String logFile, int logHistory) {
        final RollingFileAppender rfAppender = new RollingFileAppender();
        rfAppender.setContext(loggerContext);
        rfAppender.setFile(logDirectory + logFile + ".log");
        {
            TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
            rollingPolicy.setContext(loggerContext);
            // rolling policies need to know their parent
            // it's one of the rare cases, where a sub-component knows about its parent
            rollingPolicy.setParent(rfAppender);
            rollingPolicy.setFileNamePattern(logDirectory + logFile + ".%d{yyyy-MM-dd}.%i.log");
            //rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedTriggeringPolicy);
            rollingPolicy.setMaxHistory(logHistory);

            rfAppender.setRollingPolicy(rollingPolicy);

            SizeAndTimeBasedFNATP triggeringPolicy;
            {
                // triger for name changing	
                triggeringPolicy = new SizeAndTimeBasedFNATP();
                triggeringPolicy.setMaxFileSize("10MB");
                triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
                rfAppender.setTriggeringPolicy(triggeringPolicy);
            }

            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
            rollingPolicy.start();

            {
                // we need TimeBasedRollingPolicy to have the 
                // FileNamePattern pattern initialized which is done in rollingPolicy.start();
                triggeringPolicy.start();
            }
        }
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d [%thread] %-5level exec:%X{execution} dpu:%X{dpuInstance} %logger{50} - %msg%n");
        rfAppender.setEncoder(encoder);
        encoder.start();

        return rfAppender;
    }

    private void initLogbackAppender(AppConfig appConfig) {
        // default values
        String logDirectory = "";
        int logHistory = 14;
        // we try to load values from configuration
        try {
            logDirectory = appConfig.getString(ConfigProperty.BACKEND_LOG_DIR);
            // user set path, ensure that it end's on file separator
            if (logDirectory.endsWith(File.separator) || logDirectory.isEmpty()) {
                // ok it ends or it's empty
            } else {
                // no .. just add
                logDirectory = logDirectory + File.separator;
            }
        } catch (Exception e) {
        }

        try {
            logHistory = appConfig.getInteger(ConfigProperty.BACKEND_LOG_KEEP);
        } catch (Exception e) {
        }

        // check existance of directory
        if (logDirectory.isEmpty() || FileUtils.exists(logDirectory)) {
            // ok directory exist or is default
        } else {
            // can not find log directory .. 
            try {
                FileUtils.createDirectory(logDirectory);
            } catch (Exception e) {
                System.err.println("Failed to create log directory '" + logDirectory + "'");
                System.exit(1);
            }
        }

        // now prepare the logger 

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        RollingFileAppender allLog = createAppender(loggerContext, logDirectory,
                "backend", logHistory);
        allLog.start();

        RollingFileAppender errorLog = createAppender(loggerContext, logDirectory,
                "backend_err", logHistory);
        {
            // add filter
            ThresholdFilter levelFilter = new ThresholdFilter();
            levelFilter.setLevel(Level.ERROR.toString());
            levelFilter.start();
            errorLog.addFilter(levelFilter);
        }
        errorLog.start();

        // we have the appender, now we need to attach it
        // under root logger

        ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(allLog);
        logbackLogger.addAppender(errorLog);
    }

    private void initLogbackSqlAppender() {

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        SqlAppender sqlAppender = context.getBean(SqlAppender.class);
        sqlAppender.setContext(loggerContext);

        MdcExecutionLevelFilter mdcLevelFilter = new MdcExecutionLevelFilter();
        mdcLevelFilter.setContext(loggerContext);
        sqlAppender.addFilter(mdcLevelFilter);

        MdcFilter mdcFilter = new MdcFilter();
        mdcFilter.setRequiredKey(Log.MDC_EXECUTION_KEY_NAME);
        mdcFilter.setContext(loggerContext);
        sqlAppender.addFilter(mdcFilter);

        // start add under the root loger
        sqlAppender.start();
        ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(sqlAppender);
    }

    private void initLocale() {
        // retrieve app config
        AppConfig appConfig = context.getBean(AppConfig.class);
        Locale locale = Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE));
        // set retrieved locale to LocaleHolders
        LocaleHolder.setLocale(locale);
        DataunitLocaleHolder.setLocale(locale);
        LOG.info("Using locale: " + LocaleHolder.getLocale());
    }

    /**
     * Main execution method.
     * 
     * @param args
     */
    private void run(String[] args) {
        // initialise
        initSpring();

        // initialize locale settings from DB, so we need spring first
        initLocale();

        // the log back is not initialised here .. 
        // we add file appender
        initLogbackAppender(context.getBean(AppConfig.class));

        // the sql appender cooperate with spring, so we need spring first
        initLogbackSqlAppender();

        // Initialize DPUs by preloading all thier JAR bundles
        // TODO use lazyloading instead of preload?
        ModuleFacade modules = context.getBean(ModuleFacade.class);
        modules.preLoadAllDPUs();

        // try to get application-lock 
        // we construct lock key based on port		
        final StringBuilder lockKey = new StringBuilder();
        lockKey.append("INTLIB_");
        lockKey.append(context.getBean(AppConfig.class).getInteger(ConfigProperty.BACKEND_PORT));
        if (!AppLock.setLock(lockKey.toString())) {
            // another application is already running
            LOG.info("Another instance of ODCleanStore is probably running.");
            context.close();
            return;
        }

        // print some information ..
        LOG.info("Running ...");

        // infinite loop
        while (true) {
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void main(String[] args) {
        AppEntry app = new AppEntry();
        app.run(args);

        LOG.info("Closing application ...");
    }

}
