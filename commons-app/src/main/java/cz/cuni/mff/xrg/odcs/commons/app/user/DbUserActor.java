package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

public interface DbUserActor extends DbAccess<UserActor> {

    UserActor getUserActorByExternalId(String externalId);

}
