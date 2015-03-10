package cz.cuni.mff.xrg.odcs.commons.app.user;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

public interface DbRoleEntity extends DbAccess<RoleEntity> {

	public List<RoleEntity> getAllRoles();
	
	public RoleEntity getRoleByName(String name);
	
}
