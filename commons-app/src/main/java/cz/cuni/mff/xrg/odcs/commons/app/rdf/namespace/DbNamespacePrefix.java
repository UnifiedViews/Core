package cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

/**
 * Interface providing access to {@link NamespacePrefix} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbNamespacePrefix extends DbAccess<NamespacePrefix> {

    /**
     * Fetch all RDF namespace prefixes defined in application.
     * 
     * @return all namespace prefixes
     */
    public List<NamespacePrefix> getAllPrefixes();

    /**
     * Find prefix with given name in persistent storage.
     * 
     * @param name
     *            abbreviated name
     * @return namespace prefix for given name
     */
    public NamespacePrefix getByName(String name);

}
