package cz.cuni.mff.xrg.odcs.commons.app.auth;

import static cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash.createHash;
import static cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash.validatePassword;
import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

/**
 * Tests the basic functionality of the PasswordHash class.
 * 
 * @author Jan Vojt
 */
public class PasswordHashTest {

    @Test
    public void testCreateHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            createHash("p\r\nassw0Rd!");
        }
        System.out.println(String.valueOf(System.currentTimeMillis() - time));
        assertTrue(System.currentTimeMillis() - time > 2000L);
        String hash = createHash("test");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        System.out.println("passphrase 'test' hash is: " + hash);
    }

    @Test
    public void testHashesForEquality() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "s0m3P4ss//rd54";
        String hash = createHash(password);
        String secondHash = createHash(password);

        assertNotSame(hash, secondHash);
    }

    @Test
    public void testWrongPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "s0m3P4ss//rd54";
        String wrongPassword = "wrongpassword";
        String hash = createHash(password);

        assertFalse(validatePassword(wrongPassword, hash));
    }

    @Test
    public void testPasswordValidation() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "s0m3P4ss//rd54";
        String hash = createHash(password);
        assertTrue(validatePassword(password, hash));
    }

}
