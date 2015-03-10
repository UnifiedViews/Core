package cz.cuni.mff.xrg.odcs.commons.app.user;

import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

/**
 * Interface providing access to {@link User} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbUser extends DbAccess<User> {

    /**
     * @return list of all users persisted in database
     */
    public List<User> getAll();

    /**
     * Find User by his unique username.
     * 
     * @param username
     * @return user
     */
    public User getByUsername(String username);
    
    /**
     * Find User by his unique external identifier.
     * 
     * @param extid
     * @return user
     */
    public User getByExtId(String extid);
    
}
