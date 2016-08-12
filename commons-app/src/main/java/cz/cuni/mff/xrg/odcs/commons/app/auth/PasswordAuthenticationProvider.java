package cz.cuni.mff.xrg.odcs.commons.app.auth;

import static cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash.validatePassword;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Provider for custom authentication logic. Uses custom password hashing
 * provided by {@link PasswordHash}.
 * 
 * @author Jan Vojt
 */
public class PasswordAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordAuthenticationProvider.class);

    private final UserFacade userFacade;

    @Autowired
    private AppConfig appConfig;

    /**
     * Constructor sets up dependencies.
     * 
     * @param userFacade
     */
    public PasswordAuthenticationProvider(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * Authentication logic.
     * 
     * @param userDetails
     *            user to validate password against
     * @param authentication
     *            token with password to validate
     * @throws AuthenticationException
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        String password = (String) authentication.getCredentials();

        try {
            if (!validatePassword(password, userDetails.getPassword())) {
                throw new BadCredentialsException("Invalid username and/or password.");
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException("Could not generate password hash while authenticating user.", ex);
        }
    }

    /**
     * Fetches user by his username to be tried for authentication.
     * 
     * @param username
     * @param authentication
     * @return user
     * @throws AuthenticationException
     */
    @Override
    protected UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        User user = this.userFacade.getUserByUsername(authentication.getName());
        if (user == null) {
            throw new BadCredentialsException(username);
        }

        return user;
    }

}
