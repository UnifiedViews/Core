package eu.unifiedviews.commons.dataunit.core;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Provides access to connections into working rdf repository.
 * 
 * @author Škoda Petr
 */
public interface ConnectionSource {

    /**
     * @return Connection into working repository.
     * @throws RepositoryException
     */
    RepositoryConnection getConnection() throws RepositoryException;

    /**
     * This is set to false for LocalRDF, RemoteRDF, InMemoryRDF and set to true for Virtuoso. Specified e.g. in
     * {@link eu.unifiedviews.commons.rdf.repository.LocalRDF}
     * 
     * @return True if operation should retry on RDF failure.
     */
    boolean isRetryOnFailure();

    /**
     * @return Value factory for working repository.
     */
    ValueFactory getValueFactory();

}
