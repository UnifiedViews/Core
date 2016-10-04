package cz.cuni.mff.xrg.odcs.backend.db;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.communication.EmailSender;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;

/**
 * Aspect for automatically reconnection in case of database problems.
 * The aspect is designed for servicing backend, where we do not want to trash
 * all progress of unfinished pipeline runs just because of a short database
 * outage.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
@Aspect
@Order(1)
class SQLDatabaseReconnectAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SQLDatabaseReconnectAspect.class);

    /**
     * Configuration how many subsequent failures must occur before sending
     * email notification.
     */
    private static final int NOTIFY_AFTER_RETRIES = 2;

    /**
     * Application configuration.
     */
    @Autowired(required = false)
    private AppConfig appConfig;

    /**
     * Service providing notification email sending functionality.
     */
    @Autowired(required = false)
    private EmailSender emailSender;

    /**
     * Number of retries before giving up on reconnecting with db. Zero means
     * immediate failure, -1 means never give up and keep trying.
     */
    private int retries = -1;

    /**
     * Number of milliseconds to wait before next retrial.
     */
    private int wait = 2000;

    /**
     * True if email for last outage has been send.
     */
    private static boolean emailSent = false;

    @PostConstruct
    public void configure() {
        if (appConfig == null) {
            LOG.info("No configuration for database reconnects given, using defaults.");
            return;
        }
        try {
            retries = appConfig.getInteger(ConfigProperty.DATABASE_RETRIES);
        } catch (MissingConfigPropertyException ex) {
            LOG.info("Missing config property {}, using default value '{}'.",
                    ex.getProperty(), retries);
        }
        try {
            wait = appConfig.getInteger(ConfigProperty.DATABASE_WAIT);
        } catch (MissingConfigPropertyException ex) {
            LOG.info("Missing config property {}, using default value '{}'.",
                    ex.getProperty(), wait);
        }
    }

    /**
     * Aspect that wrap methods on every facade.
     * 
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("execution(public * cz.cuni.mff.xrg.odcs.commons.app.facade.Facade+.*(..))")
    public Object failureTolerant(ProceedingJoinPoint pjp) throws Throwable {
        int attempts = 0;
        while (true) {
            try {
                // count attempts so we can check number of retries later
                attempts++;
                Object result = pjp.proceed();

                // reset email status on success
                emailSent = false;
                return result;

            } catch (RuntimeException ex) { // TODO more specific exception?
                LOG.warn("failureTolerant has caught exception", ex);
                LOG.warn("Database is down after {} attempts.", attempts);

                // check whether we should notify admin
                if (attempts == NOTIFY_AFTER_RETRIES) {
                    notify(ex);
                }

                // check whether retry attempts were exhausted
                if (retries != -1 && attempts > retries) {
                    // we have waited for too long
                    LOG.error("Giving up on database after {} retries.", attempts);
                    throw ex;
                }
            }

            // wait for some time before the next try
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                LOG.error("Thread interrupted while sleeping.", e);
            }
        }
    }

    /**
     * Sends email notification to administrator about DB outage.
     * 
     * @param ex
     */
    private synchronized void notify(Exception ex) {
        if (emailSender != null && appConfig != null && !emailSent) {
            final String subject = Messages.getString("SQLDatabaseReconnectAspect.database.error");
            String body = Messages.getString("SQLDatabaseReconnectAspect.database.exception") + ex.toString();
            String recipient = appConfig.getString(ConfigProperty.EMAIL_ADMIN);
            emailSender.send(subject, body, recipient);
            // 
            emailSent = true;
        }
    }

}
