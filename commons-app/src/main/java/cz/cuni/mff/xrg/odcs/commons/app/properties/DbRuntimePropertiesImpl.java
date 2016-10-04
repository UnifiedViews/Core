package cz.cuni.mff.xrg.odcs.commons.app.properties;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

/**
 * Implementation for accessing {@link RuntimeProperty} data objects.
 * 
 * @author mvi
 *
 */
@Transactional(propagation = Propagation.MANDATORY)
public class DbRuntimePropertiesImpl extends DbAccessBase<RuntimeProperty> implements DbRuntimeProperties {

    public DbRuntimePropertiesImpl() {
        super(RuntimeProperty.class);
    }

    @Override
    public List<RuntimeProperty> getAll() {
        final String queryString = "SELECT e FROM RuntimeProperty e";
        return executeList(queryString);
    }

    @Override
    public RuntimeProperty getByName(String name) {
        final String stringQuery = "SELECT e FROM RuntimeProperty e"
                + " WHERE e.name = :name";

        TypedQuery<RuntimeProperty> query = createTypedQuery(stringQuery);
        query.setParameter("name", name);

        return execute(query);
    }

}
