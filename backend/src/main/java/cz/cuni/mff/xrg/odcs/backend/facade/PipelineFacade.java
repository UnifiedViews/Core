package cz.cuni.mff.xrg.odcs.backend.facade;

import cz.cuni.mff.xrg.odcs.commons.app.communication.EmailSender;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade for managing pipelines, which tolerates database crashes. This facade
 * is specially altered for servicing backend, where we do not want to trash
 * all progress of unfinished pipeline runs just because of a short database
 * outage.
 *
 * @author Jan Vojt
 */
public class PipelineFacade extends cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineFacade {
	
	private static final Logger LOG = LoggerFactory.getLogger(PipelineFacade.class);
	
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
	 * Number of email notifications about failures already sent.
	 */
	private static int emailsSent = 0;
	
	@Autowired(required = false)
	private EmailSender emailSender;
	
	@Autowired(required = false)
	private AppConfig config;
	
	/**
	 * Configures behavior in case of database outages.
	 */
	@PostConstruct
	public void reconfigure() {
		if (config == null) {
			LOG.info("No configoration for database reconnects given, using defaults.");
			return;
		}
		try {
			retries = config.getInteger(ConfigProperty.VIRTUOSO_RETRIES);
		} catch (MissingConfigPropertyException ex) {
			LOG.info("Missing config property {}, using default value {}.", ex.getProperty(), retries);
		}
		try {
			wait = config.getInteger(ConfigProperty.VIRTUOSO_WAIT);
		} catch (MissingConfigPropertyException ex) {
			LOG.info("Missing config property {}, using default value {}.", ex.getProperty(), wait);
		}
	}

	@Override
	public Pipeline getPipeline(long id) {
		int attempts = 0;
		while (true) try {
			attempts++;
			return super.getPipeline(id);
		} catch (RuntimeException ex) {
			// presume DB error
			handleRetries(attempts, ex);
		}
	}

	@Override
	@Transactional
	public void save(Pipeline pipeline) {
		int attempts = 0;
		while (true) try {
			attempts++;
			super.save(pipeline);
			return;
		} catch (IllegalArgumentException ex) {
			// given pipeline is a removed entity
			throw ex;
		} catch (RuntimeException ex) {
			// presume DB error
			handleRetries(attempts, ex);
		}
	}

	@Override
	public PipelineExecution getExecution(long id) {
		int attempts = 0;
		while (true) try {
			attempts++;
			return super.getExecution(id);
		} catch (RuntimeException ex) {
			handleRetries(attempts, ex);
		}
	}

	@Override
	public List<PipelineExecution> getExecutions(Pipeline pipeline) {
		int attempts = 0;
		while (true) try {
			attempts++;
			return super.getExecutions(pipeline);
		} catch (RuntimeException ex) {
			handleRetries(attempts, ex);
		}
	}

	@Override
	@Transactional
	public void save(PipelineExecution exec) {
		int attempts = 0;
		while (true) try {
			attempts++;
			super.save(exec);
			return;
		} catch (IllegalArgumentException ex) {
			// given execution is a removed entity
			throw ex;
		} catch (RuntimeException ex) {
			handleRetries(attempts, ex);
		}
	}

	@Override
	@Transactional
	public void delete(PipelineExecution exec) {
		int attempts = 0;
		while (true) try {
			attempts++;
			super.delete(exec);
			return;
		} catch (IllegalArgumentException ex) {
			// given execution is not persisted
			throw ex;
		} catch (PersistenceException ex) {
			handleRetries(attempts, ex);
		}
	}
	
	@Override
	public List<PipelineExecution> getAllExecutions() {
		int attempts = 0;
		while (true) try {
			attempts++;
			return super.getAllExecutions();
		} catch (IllegalArgumentException ex) {
			// invalid SQL query was called
			throw ex;
		} catch (PersistenceException ex) {
			handleRetries(attempts, ex);
		}
	}

	@Override
	public Date getLastExecTime(Pipeline pipeline, PipelineExecutionStatus status) {
		int attempts = 0;
		while (true) try {
			attempts++;
			return super.getLastExecTime(pipeline, status);
		} catch (IllegalArgumentException ex) {
			// invalid SQL query was called
			throw ex;
		} catch (RuntimeException ex) {
			handleRetries(attempts, ex);
		}
	}
	
	// TODO refactor following methods into ErrorHandler
	
	/**
	 * Logic for deciding whether to continue db reconnect attempts. If we
	 * decide to stop trying, exception is thrown.
	 * 
	 * @param attempts number of attempts so far
	 * @param ex exception to be thrown in case we give up
	 */
	private void handleRetries(int attempts, RuntimeException ex) {
		
		LOG.warn("Database is down after {} attempts.", attempts);
		if (attempts == 1) {
			// send notification after first error only
			notify(ex);
		}
		
		boolean loop = true;
		if (retries >= 0) {
			loop = attempts <= retries;
		}
		if (loop && wait > 0) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted while sleeping.", e);
			}
		} else if (!loop) {
			LOG.error("Giving up on database after {} retries.", attempts);
			throw ex;
		}
	}
	
	/**
	 * Sends email notification to administrator about DB outage.
	 * 
	 * @param ex 
	 */
	private void notify(RuntimeException ex) {
		if (emailSender != null) {
			synchronized (PipelineFacade.class) {
				if (emailsSent <= 0) {
					String subject = "Intlib - RDBMS error";
					String recipient = config.getString(ConfigProperty.EMAIL_ADMIN);
					emailSender.send(subject, ex.toString(), recipient);
					emailsSent++;
				}
			}
		}
	}
	
}
