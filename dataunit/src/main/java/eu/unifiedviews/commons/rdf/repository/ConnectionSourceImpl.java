package eu.unifiedviews.commons.rdf.repository;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;

/**
 * Provides access to connections into working rdf repository.
 *
 * @author Škoda Petr
 */
class ConnectionSourceImpl implements ConnectionSource {

    /**
     * Underlying repository.
     */
    private final Repository repository;

    /**
     * If true then connection is considered to be unreliable and in case of failure the operation should be
     * tried again.
     */
    private final boolean retryOnFailure;

    /**
     * Used repository.
     *
     * @param repository
     * @param retryOnFailure
     */
    public ConnectionSourceImpl(Repository repository, boolean retryOnFailure) {
        this.repository = repository;
        this.retryOnFailure = retryOnFailure;
    }

    @Override
    public RepositoryConnection getConnection() throws RepositoryException {
        return repository.getConnection();
    }

    @Override
    public boolean isRetryOnFailure() {
        return retryOnFailure;
    }

    @Override
    public ValueFactory getValueFactory() {
        return repository.getValueFactory();
    }

}
