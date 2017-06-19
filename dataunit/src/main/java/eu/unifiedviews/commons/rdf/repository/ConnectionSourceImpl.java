package eu.unifiedviews.commons.rdf.repository;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

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
     * To hold type of the RDF repository - local, graphdb, etc.
     */
    private ManagableRepository.Type repositoryType;


    /**
     * Used repository.
     *
     * @param repository
     * @param retryOnFailure
     */
    public ConnectionSourceImpl(Repository repository, boolean retryOnFailure, ManagableRepository.Type type) {
        this.repository = repository;
        this.retryOnFailure = retryOnFailure;
        this.repositoryType = type;
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

    @Override
    public ManagableRepository.Type getRepositoryType() {
        return repositoryType;
    }

}
