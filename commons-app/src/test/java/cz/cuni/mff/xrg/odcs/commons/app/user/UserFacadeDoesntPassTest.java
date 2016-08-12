package cz.cuni.mff.xrg.odcs.commons.app.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;

/**
 * Test suite for facade persisting {@link User}s.
 * 
 * @author Jan Vojt
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class UserFacadeDoesntPassTest {

    @Autowired
    private UserFacade facade;

    /**
     * Test of getAllUsers method, of class UserFacade.
     */
    @Test
    @Transactional
    public void testGetAllUsers() {
        List<User> users = facade.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());

        User user = users.get(0);
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
    }

    /**
     * Test of getUser method, of class UserFacade.
     */
    @Test
    @Transactional
    public void testGetUser() {
        User user = facade.getUser(1L);
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
    }

    /**
     * Test fetching user by his username.
     */
    @Test
    public void testGetUserByUsername() {
        User user = facade.getUserByUsername("jdoe");
        assertNotNull(user);
        assertEquals(1L, (long) user.getId());

        User user2 = facade.getUserByUsername("notexistinguser");
        assertNull(user2);
    }

    /**
     * Test of save method, of class UserFacade.
     */
    @Test
    @Transactional
    public void testSave() {
        User user = facade.createUser("abcd", "abcd", new EmailAddress("jay@example.com"));
        facade.save(user);
    }

    /**
     * Test of save method, of class UserFacade.
     */
    @Test
    @Transactional
    public void testSave2() {
        User user = facade.createUser("abcd", "abcd", new EmailAddress("jay+mailbox@example.com"));
        facade.save(user);
    }

    /**
     * Test of delete method, of class UserFacade.
     */
    @Test
    @Transactional
    public void testDelete() {
        facade.delete(facade.getUser(1L));
        facade.delete(facade.getUser(2L));
        List<User> users = facade.getAllUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetNotification() {
        User user = facade.getUser(1L);
        assertNotNull(user);
        assertNotNull(user.getNotification());
    }
}
