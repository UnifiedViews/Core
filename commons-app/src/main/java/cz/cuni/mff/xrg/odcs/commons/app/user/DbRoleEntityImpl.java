package cz.cuni.mff.xrg.odcs.commons.app.user;

import java.util.List;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

public class DbRoleEntityImpl extends DbAccessBase<RoleEntity> implements
		DbRoleEntity {

	public DbRoleEntityImpl() {
		super(RoleEntity.class);
	}

	@Override
	public List<RoleEntity> getAllRoles() {
        final String stringQuery = "SELECT r FROM RoleEntity r";
        return executeList(stringQuery);
  	}

	@Override
	public RoleEntity getRoleByName(String name) {
        final String stringQuery = "SELECT e FROM RoleEntity e WHERE e.name = :rname";
        TypedQuery<RoleEntity> query = createTypedQuery(stringQuery);
        query.setParameter("rname", name);
        return execute(query);
	}
}