package cz.cuni.mff.xrg.odcs.commons.app.user;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

public class DbOrganizationImpl extends DbAccessBase<Organization> implements DbOrganization {

    public DbOrganizationImpl() {
        super(Organization.class);
    }
    
    @Override
    public Organization getOrganizationByName(String name) {
        final String stringQuery = "SELECT e FROM Organization e WHERE e.name = :oname";
        TypedQuery<Organization> query = createTypedQuery(stringQuery);
        query.setParameter("oname", name);
        return execute(query);
    }
}
