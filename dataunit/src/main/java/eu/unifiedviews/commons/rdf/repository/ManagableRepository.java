package eu.unifiedviews.commons.rdf.repository;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;

/**
 *
 * Handler to RDF Working repository
 *
 * @author Škoda Petr
 */
public interface ManagableRepository {

    /**
     * Repository type.
     */
    public static enum Type {
        LOCAL_RDF,
        INMEMORY_RDF,
        REMOTE_RDF,
        VIRTUOSO,
        GRAPHDB
    }

    /**
     * Returns the type of the repository (see @link #Type)
     * @return Type of the repository
     */
    public Type getRepositoryType();

    /**
     *
     * @return Connection source for this repository.
     */
    public ConnectionSource getConnectionSource();

    /**
     * Called on repository release. Can not be called after {@link #delete()}
     *
     * @throws eu.unifiedviews.commons.rdf.repository.RDFException
     */
    public void release() throws RDFException;

    /**
     * Delete repository. Can not be called after {@link #release()}
     * Should call release() inside.
     * 
     * @throws eu.unifiedviews.commons.rdf.repository.RDFException
     */
    public void delete() throws RDFException;

}
