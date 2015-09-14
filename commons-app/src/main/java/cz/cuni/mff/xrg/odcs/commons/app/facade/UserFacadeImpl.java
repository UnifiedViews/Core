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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.user.DbRoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.DbUser;
import cz.cuni.mff.xrg.odcs.commons.app.user.DbUserActor;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.NotificationRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserNotificationRecord;

/**
 * Facade for managing persisted User entities.
 * 
 * @author Jan Vojt
 */
@Transactional(readOnly = true)
class UserFacadeImpl implements UserFacade {

    private static final Logger LOG = LoggerFactory
            .getLogger(UserFacadeImpl.class);

    @Autowired
    private DbUser userDao;

    @Autowired
    private DbRoleEntity roleDao;

    @Autowired
    private DbUserActor actorDao;

    /**
     * Factory for a new User.
     * 
     * @param username
     * @param plainPassword
     * @param email
     * @return new user instance
     */
    @Override
    public User createUser(String username, String plainPassword,
            EmailAddress email) {

        User user = new User();
        user.setUsername(username);
        user.setExternalIdentifier(username);
        user.setPassword(plainPassword);
        user.setEmail(email);

        // set default notification setting
        UserNotificationRecord notify = new UserNotificationRecord();
        user.setNotification(notify);

        notify.addEmail(email);
        notify.setTypeError(NotificationRecordType.INSTANT);
        notify.setTypeSuccess(NotificationRecordType.DAILY);
        notify.setTypeStarted(NotificationRecordType.NO_REPORT);

        return user;
    }

    @Override
    public User createOrUpdateUser(String userName, String fullUserName, String actorId, String actorName, List<String> roles) {
        User user = this.getUserByExtId(userName);
        // create user if does not exist
        if (user == null) {
            user = this.createUser(userName, "*****", new EmailAddress(userName + "@nomail.com"));
            if (fullUserName != null) {
                user.setFullName(fullUserName);
            } else {
                user.setFullName(userName);
            }
            user.setExternalIdentifier(userName);
            user.setTableRows(20);
            this.saveNoAuth(user);
        }

        // update user information
        user.getRoles().clear();
        for (String rolename : roles) {
            if (rolename != null) {
                RoleEntity role = this.getRoleByName(rolename);
                if (role != null) {
                    user.addRole(role);
                }
            }
        }

        if (actorId != null) {
            UserActor actor = this.getUserActorByExternalId(actorId);
            if (actor == null) {
                actor = new UserActor();
                actor.setName(actorName);
                actor.setExternalId(actorId);
                this.save(actor);
            }
            user.setUserActor(actor);
        }

        this.save(user);
        return user;
    }

    /**
     * @return list of all users persisted in database
     */
    @PostFilter("hasPermission(filterObject, 'user.management')")
    @Override
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    /**
     * @param id
     *            primary key
     * @return user with given id or <code>null<code>
     */
    @PostAuthorize("hasPermission(returnObject, 'user.management')")
    @Override
    public User getUser(long id) {
        return userDao.getInstance(id);
    }

    /**
     * Find User by his unique username. This method is not secured, so that yet
     * unauthenticated users can login.
     * 
     * @param username
     * @return user
     */
    @Override
    public User getUserByUsername(String username) {
        User user = userDao.getByUsername(username);
        if (user == null) {
            LOG.info("User with username {} was not found.", username);
        }

        return user;
    }

    /**
     * Find User by his unique username. This method is not secured, so that yet
     * unauthenticated users can login.
     * 
     * @param username
     * @return user
     */
    @Override
    public User getUserByExtId(String extid) {
        User user = userDao.getByExtId(extid);
        if (user == null) {
            LOG.info("User with username {} was not found.", extid);
        }

        return user;
    }

    /**
     * Saves any modifications made to the User into the database.
     * 
     * @param user
     */
    @Transactional
    @PreAuthorize("hasPermission(#user, 'user.management')")
    @Override
    public void save(User user) {
        userDao.save(user);
    }

    /**
     * Saves any modifications made to the User into the database.No Authorization of call, used for creating of user during authorization
     * 
     * @param user
     */
    @Transactional
    @Override
    public void saveNoAuth(User user) {
        userDao.save(user);
    }

    /**
     * Deletes user from database.
     * 
     * @param user
     */
    @Transactional
    @PreAuthorize("hasPermission(#user, 'user.management')")
    @Override
    public void delete(User user) {
        userDao.delete(user);
    }

    /**
     * @return list of all roles persisted in database
     */
    @Override
    public List<RoleEntity> getAllRoles() {
        return roleDao.getAllRoles();
    }

    /**
     * @param name
     *            name
     * @return RoleEntity or null
     */
    @Override
    public RoleEntity getRoleByName(String name) {
        return roleDao.getRoleByName(name);
    }

    @Transactional
    @Override
    public void save(RoleEntity role) {
        roleDao.save(role);
    }

    /**
     * Deletes pipeline from database.
     * 
     * @param user
     */
    @Transactional
    @Override
    public void delete(RoleEntity role) {
        roleDao.delete(role);
    }

    @Override
    public UserActor getUserActorByExternalId(String externalId) {
        return this.actorDao.getUserActorByExternalId(externalId);
    }

    @Override
    public void save(UserActor userActor) {
        this.actorDao.save(userActor);
    }

}
