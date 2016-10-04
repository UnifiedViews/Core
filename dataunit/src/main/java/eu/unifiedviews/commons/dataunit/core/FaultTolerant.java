package eu.unifiedviews.commons.dataunit.core;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 *
 * @author Škoda Petr
 */
public interface FaultTolerant {

    /**
     * Interface for user function to execute.
     */
    interface Code {
        
        /**
         * Code to execute.
         *
         * @param connection
         * @throws org.openrdf.repository.RepositoryException
         * @throws eu.unifiedviews.dataunit.DataUnitException
         */
        void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException;        
        
    }

    /**
     * Execute given code with some level of fault tolerance.
     *
     * @param codeToExecute
     * @throws org.openrdf.repository.RepositoryException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    void execute(Code codeToExecute) throws RepositoryException, DataUnitException;

}
