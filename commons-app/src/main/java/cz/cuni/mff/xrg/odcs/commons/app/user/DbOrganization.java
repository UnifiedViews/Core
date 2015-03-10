package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

public interface DbOrganization extends DbAccess<Organization> {

    Organization getOrganizationByName(String name);
}
