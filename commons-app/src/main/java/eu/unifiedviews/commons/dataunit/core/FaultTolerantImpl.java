/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.commons.dataunit.core;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 * @author Škoda Petr
 */
public class FaultTolerantImpl implements FaultTolerant {

    private static final Logger LOG = LoggerFactory.getLogger(FaultTolerantImpl.class);

    private final ConnectionSource connectionSource;

    /**
     * Wait time between attempts.
     */
    private final int waitTime;

    private final int numberOfAttemps;

    public FaultTolerantImpl(ConnectionSource connectionSource, int waitTime, int numberOfAttemps) {
        LOG.info("FaultTolerantImpl waitTime={} numberOfAttemps={}", waitTime, numberOfAttemps);
        this.connectionSource = connectionSource;
        this.waitTime = waitTime;
        this.numberOfAttemps = numberOfAttemps;
    }

    @Override
    public void execute(FaultTolerant.Code codeToExecute) throws RepositoryException, DataUnitException {
        if (innerExecute(connectionSource, codeToExecute)) {
            // Success.
        } else {
            for (int i = 2; numberOfAttemps == -1 || i < numberOfAttemps; ++i) {
                // Wait for some time.
                try {
                    LOG.debug("Sleeping for {}ms before {} attempt from {}", waitTime, i, numberOfAttemps);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    LOG.info("Interrupted!");
                    // Re-interrupt.
                    Thread.currentThread().interrupt();
                }
                // Try again.
                if (innerExecute(connectionSource, codeToExecute)) {
                    return;
                }
            }
            // If we are here we fail to execute user code.
            throw new DataUnitException(Messages.getString("FaultTolerantImpl.user.code.execution.fail"));
        }
    }

    /**
     * @param connectionSource
     * @param codeToExecute
     * @return False if operation fail.
     * @throws RepositoryException
     * @throws DataUnitException
     */
    private static boolean innerExecute(ConnectionSource connectionSource, Code codeToExecute) throws RepositoryException, DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = connectionSource.getConnection();
            connection.begin();
            // Execute user given code.
            codeToExecute.execute(connection);
            connection.commit();
            // Success.
            return true;
        } catch (RepositoryException ex) {
            if (connectionSource.isRetryOnFailure()) {
                LOG.error("Operation failed for RepositoryException, we may try again.", ex);
                return false;
            } else {
                // Re-throw, instant failure.
                throw ex;
            }
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (RepositoryException ex) {
                LOG.warn("Error when closing connection.", ex);
            }
        }
    }

}
