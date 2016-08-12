package cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

/**
 * Interface providing access to {@link NamespacePrefix} data objects.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
class DbNamespacePrefixImpl extends DbAccessBase<NamespacePrefix>
        implements DbNamespacePrefix {

    public DbNamespacePrefixImpl() {
        super(NamespacePrefix.class);
    }

    @Override
    public List<NamespacePrefix> getAllPrefixes() {
        final String stringQuery = "SELECT e FROM NamespacePrefix e";
        return executeList(stringQuery);
    }

    @Override
    public NamespacePrefix getByName(String name) {
        final String stringQuery = "SELECT e FROM NamespacePrefix e WHERE e.name = :name";
        TypedQuery<NamespacePrefix> query = createTypedQuery(stringQuery);
        query.setParameter("name", name);
        return execute(query);
    }

}
