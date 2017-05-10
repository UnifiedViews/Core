package cz.cuni.mff.xrg.odcs.backend;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import cz.cuni.mff.xrg.odcs.backend.auxiliaries.AppLock;
import cz.cuni.mff.xrg.odcs.backend.auxiliaries.DatabaseInitializer;
import cz.cuni.mff.xrg.odcs.backend.communication.EmbeddedHttpServer;
import cz.cuni.mff.xrg.odcs.backend.logback.MdcExecutionLevelFilter;
import cz.cuni.mff.xrg.odcs.backend.logback.MdcFilter;
import cz.cuni.mff.xrg.odcs.backend.logback.SqlAppender;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Backend entry point.
 * 
 * @author Petyr
 */
@Component
public class AppEntry {

    private final static String SPRING_CONFIG_FILE = "backend-context.xml";
    private static final Logger LOG = LoggerFactory.getLogger(AppEntry.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ModuleFacade moduleFacade;

    @Autowired
    private SqlAppender sqlAppender;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private EmbeddedHttpServer httpProbeServer;

    private RollingFileAppender createAppender(LoggerContext loggerContext,
            String logDirectory, String logFile, int logHistory, String logMaxSize) {
        final RollingFileAppender rfAppender = new RollingFileAppender();
        rfAppender.setContext(loggerContext);
        rfAppender.setFile(logDirectory + logFile + ".log");
        {
            SizeAndTimeBasedRollingPolicy rollingPolicy = new SizeAndTimeBasedRollingPolicy();
            rollingPolicy.setContext(loggerContext);
            // rolling policies need to know their parent
            // it's one of the rare cases, where a sub-component knows about its parent
            rollingPolicy.setParent(rfAppender);
            rollingPolicy.setFileNamePattern(logDirectory + logFile + ".%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(logHistory);
            rollingPolicy.setTotalSizeCap(FileSize.valueOf(logMaxSize));
            rollingPolicy.setMaxFileSize(FileSize.valueOf("10MB"));

            rfAppender.setRollingPolicy(rollingPolicy);

            rollingPolicy.start();

        }
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d [%thread] %-5level exec:%X{execution} dpu:%X{dpuInstance} %logger{50} - %msg%n");
        rfAppender.setEncoder(encoder);
        encoder.start();

        return rfAppender;
    }

    private void initLogbackAppender() {
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
            //not logging exception, default value is used in this case
        }

        try {
            logHistory = appConfig.getInteger(ConfigProperty.BACKEND_LOG_KEEP);
        } catch (Exception e) {
            //not logging exception, default value is used in this case
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


        //check if there is any preferred max size for logs
        String logMaxSize = "1GB";
        // we try to load values from configuration
        try {
            logMaxSize = appConfig.getString(ConfigProperty.BACKEND_LOG_MAX_SIZE);
            // user set path, ensure that it end's on file separator
        } catch (MissingConfigPropertyException e) {
            //not logging exception, default value is used in this case
        }

        // now prepare the logger 

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        RollingFileAppender allLog = createAppender(loggerContext, logDirectory,
                "backend", logHistory, logMaxSize);
        allLog.start();

        RollingFileAppender errorLog = createAppender(loggerContext, logDirectory,
                "backend_err", logHistory, logMaxSize);
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

    private void run() throws Exception {
        // the log back is not initialised here ..
        // we add file appender
        initLogbackAppender();

        // the sql appender cooperate with spring, so we need spring first
        initLogbackSqlAppender();

        // Initialize DPUs by preloading all thier JAR bundles
        // TODO use lazyloading instead of preload?
        moduleFacade.preLoadAllDPUs();

        // try to get application-lock
        // we construct lock key based on port
        final StringBuilder lockKey = new StringBuilder();
        lockKey.append("INTLIB_");
        lockKey.append(appConfig.getInteger(ConfigProperty.BACKEND_PORT));
        if (!AppLock.setLock(lockKey.toString())) {
            // another application is already running
            LOG.warn("Another instance of UnifiedViews is already running on this machine.");
            return;
        }

        databaseInitializer.initialize();
        httpProbeServer.startServer();

        // infinite loop
        while (true) {
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILE);
        context.registerShutdownHook();

        AppEntry appEntry = context.getBean(AppEntry.class);
        appEntry.run();

        LOG.info("Closing application ...");
        context.close();
    }

}
