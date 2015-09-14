/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;

/**
 * Facade for managing persisted User entities.
 * 
 * @author Jan Vojt
 */
public interface UserFacade extends Facade {

    /**
     * Factory for a new User.
     * 
     * @param username
     * @param password
     * @param email
     * @return new user instance
     */
    User createUser(String username, String password, EmailAddress email);

    /**
     * Creates or updates user info, depending if user with 'username' already exists.
     *
     * @param userName
     * @param fullUserName
     * @param actorId
     * @param actorName
     * @param roles
     * @return new or updated user instance
     */
    User createOrUpdateUser(String userName, String fullUserName, String actorId, String actorName, List<String> roles);

    /**
     * @return list of all users persisted in database
     */
    List<User> getAllUsers();

    /**
     * @param id
     *            primary key
     * @return user with given id or <code>null<code>
     */
    User getUser(long id);

    /**
     * Find User by his unique username. This method is not secured, so that yet
     * unauthenticated users can login.
     * 
     * @param username
     * @return user
     */
    User getUserByUsername(String username);

    /**
     * Find User by his externalId. This method is not secured, so that yet
     * unauthenticated users can login.
     * 
     * @param extid
     * @return user
     */
    User getUserByExtId(String extid);

    /**
     * Saves any modifications made to the User into the database.
     * 
     * @param user
     */
    void save(User user);

    /**
     * Saves any modifications made to the User into the database without authorization. Useful during login process.
     * 
     * @param user
     */
    void saveNoAuth(User user);

    /**
     * Deletes user from database.
     * 
     * @param user
     */
    void delete(User user);

    /**
     * @return list of all roles persisted in database
     */
    List<RoleEntity> getAllRoles();

    /**
     * Returns RoleEntity object by its name
     * 
     * @param name
     *            role name
     * @return RoleEntity or null
     */
    RoleEntity getRoleByName(String name);

    /**
     * Saves any modification made to the Role into the database
     * 
     * @param role
     */
    void save(RoleEntity role);

    /**
     * Deletes role from database.
     * 
     * @param user
     */
    void delete(RoleEntity role);

    /**
     * Get user actor by external id
     * 
     * @param externalId
     * @return
     */
    UserActor getUserActorByExternalId(String externalId);

    /**
     * Save user actor into database
     * 
     * @param userActor
     */
    void save(UserActor userActor);
}
