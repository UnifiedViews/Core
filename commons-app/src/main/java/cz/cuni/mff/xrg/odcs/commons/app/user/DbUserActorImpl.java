package cz.cuni.mff.xrg.odcs.commons.app.user;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

public class DbUserActorImpl extends DbAccessBase<UserActor> implements DbUserActor {

    public DbUserActorImpl() {
        super(UserActor.class);
    }

    @Override
    public UserActor getUserActorByExternalId(String externalId) {
        final String stringQuery = "SELECT e FROM UserActor e WHERE e.externalId = :oexternalId";
        TypedQuery<UserActor> query = createTypedQuery(stringQuery);
        query.setParameter("oexternalId", externalId);
        return execute(query);
    }

}
