package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.user.DbOrganization;
import cz.cuni.mff.xrg.odcs.commons.app.user.DbRoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.DbUser;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.NotificationRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.user.Organization;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
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
    private DbOrganization organizationDao;

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
        user.setPassword(plainPassword);
        user.setEmail(email);

        // set default notification setting
        UserNotificationRecord notify = new UserNotificationRecord();
        user.setNotification(notify);

        notify.addEmail(email);
        notify.setTypeError(NotificationRecordType.INSTANT);
        notify.setTypeSuccess(NotificationRecordType.DAILY);

        return user;
    }

    /**
     * @return list of all users persisted in database
     */
    @PostFilter("hasPermission(filterObject, 'user.read')")
    @Override
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    /**
     * @param id
     *            primary key
     * @return user with given id or <code>null<code>
     */
    @PostAuthorize("hasPermission(returnObject, 'user.read')")
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
    @PreAuthorize("hasPermission(#user, 'user.create')")
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
    @PreAuthorize("hasPermission(#user, 'user.delete')")
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
    public Organization getOrganizationByName(String name) {
        return organizationDao.getOrganizationByName(name);
    }

    @Transactional
    @Override
    public void save(Organization o) {
        organizationDao.save(o);
    }
}