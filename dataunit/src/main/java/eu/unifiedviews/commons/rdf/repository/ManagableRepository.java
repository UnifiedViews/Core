package eu.unifiedviews.commons.rdf.repository;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;

/**
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
        VIRTUOSO
    }

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
     * 
     * @throws eu.unifiedviews.commons.rdf.repository.RDFException
     */
    public void delete() throws RDFException;

}
