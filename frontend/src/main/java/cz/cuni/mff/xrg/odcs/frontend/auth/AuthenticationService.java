package cz.cuni.mff.xrg.odcs.frontend.auth;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.vaadin.server.VaadinService;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.frontend.RequestHolder;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Handles login and logout actions in frontend application.
 * 
 * @author Jan Vojt
 */
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Time interval given in seconds after which remember-me cookie should
     * expire.
     * TODO move to configuration
     */
    public static final int COOKIE_TTL = 60 * 24 * 3600;

    /**
     * Attribute key for storing {@link Authentication} in HTTP session.
     */
    public static final String SESSION_KEY = "authentication";

    /**
     * Session key where remember-me token is stored.
     */
    public static final String REMEMBER_ME_KEY = "remember-me";

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authManager;

    @Autowired
    private AuthenticationContext authCtx;

    @Autowired
    private LogoutHandler logoutHandler;

    @Autowired
    private UserFacade userFacade;

    /**
     * Creates security context and saves authentication details into session.
     * 
     * @param login
     * @param password
     * @param rememberMe
     * @param httpRequest
     * @throws AuthenticationException
     */
    public void login(String login, String password, boolean rememberMe, HttpServletRequest httpRequest)
            throws AuthenticationException {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, password);

        token.setDetails(new WebAuthenticationDetails(httpRequest));

        authenticate(authManager.authenticate(token), httpRequest);

        if (rememberMe) {
            saveRememberMeToken(login);
        }
    }

    /**
     * Try to locate remember-me cookie and authenticate user.
     * 
     * @param request
     *            HTTP request
     * @return true if there is a cookie with a valid token
     */
    public boolean tryRememberMeLogin(HttpServletRequest request) {
        RememberMeToken token = getRememberMeToken();

        if (token == null) {
            // there is no cookie saved
            return false;
        }

        try {
            validateToken(token);
        } catch (AuthenticationException ex) {
            clearRememberMeCookie();
            return false;
        }

        // cookie is valid and we can authenticate user
        User user = userFacade.getUserByUsername(token.getUsername());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authenticate(authToken, request);
        return true;
    }

    /**
     * Validates remember-me token. This method does not modify the cookie in
     * any way. You should probably delete the cookie yourself after this method
     * throws {@link AuthenticationException} of any kind.
     * 
     * @param token
     *            remember-me token
     * @throws AuthenticationException
     */
    private void validateToken(RememberMeToken token)
            throws AuthenticationException {

        boolean invalid = token == null
                || token.getCreated() == null
                || token.getHash() == null
                || token.getUsername() == null;

        if (invalid) {
            throw new InsufficientAuthenticationException(Messages.getString("AuthenticationService.token.invalid"));
        }

        assert token != null;

        User user = userFacade.getUserByUsername(token.getUsername());

        if (user == null) {
            throw new UsernameNotFoundException(Messages.getString("AuthenticationService.user.not.found", token.getUsername()));
        }

        String toHash = generateStringToHash(user, token.getCreated());
        try {
            if (!PasswordHash.validatePassword(toHash, token.getHash())) {
                LOG.error("Invalid authentication token hash. This is most likely an attack!");
                throw new InsufficientAuthenticationException(Messages.getString("AuthenticationService.token.hash.invalid"));
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("Could not validate hash for given remember-me token.", ex);
            throw new InsufficientAuthenticationException(Messages.getString("AuthenticationService.validation.error"));
        }

        Date now = new Date();
        long diff = now.getTime() - token.getCreated().getTime();
        if (diff > COOKIE_TTL * 1000) {
            throw new CredentialsExpiredException(Messages.getString("AuthenticationService.token.expired"));
        }
    }

    /**
     * Generates a hash signing the remember-me cookie with given timestamp.
     * 
     * @param user
     * @param now
     *            timestamp so we can validate token later
     * @return hash signing token
     */
    private String generateStringToHash(User user, Date now) {

        StringBuilder toHash = new StringBuilder(user.getUsername());
        toHash.append(RememberMeToken.SEP);
        toHash.append(now.getTime());
        toHash.append(RememberMeToken.SEP);
        toHash.append(user.getPassword());

        return toHash.toString();
    }

    /**
     * Clears security context and removes authentication from session.
     * 
     * @param httpRequest
     */
    public void logout(HttpServletRequest httpRequest) {

        Authentication authentication = authCtx.getAuthentication();

        logoutHandler.logout(httpRequest, null, authentication);

        // clear session
        RequestHolder.getRequest().getSession().removeAttribute(SESSION_KEY);

        clearRememberMeCookie();
    }

    /**
     * Saves remember-me token into a cookie.
     * 
     * @param username
     */
    private void saveRememberMeToken(String username) {
        User user = userFacade.getUserByUsername(username);
        Date now = new Date();
        String toHash = generateStringToHash(user, now);
        String hash;
        try {
            hash = PasswordHash.createHash(toHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException("Could not hash remember-me token.", ex);
        }
        RememberMeToken token = new RememberMeToken(username, now, hash);

        Cookie cookie;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(token);
            // control characters are not allowed in HTTP headers -> base64 encode
            cookie = new Cookie(REMEMBER_ME_KEY, new String(Base64.encode(baos.toByteArray())));
            cookie.setMaxAge(COOKIE_TTL);
            // if app is root, getContextPath returns empty string -> append slash
            cookie.setPath(VaadinService.getCurrentRequest().getContextPath() + "/");
        } catch (IOException ex) {
            LOG.error("Could not serialize remember-me token.", ex);
            return;
        }

        VaadinService.getCurrentResponse().addCookie(cookie);
    }

    /**
     * Clears remember-me data.
     */
    private void clearRememberMeCookie() {
        // setting cookie's maxAge to 0 will delete it immediately
        Cookie cookie = new Cookie(REMEMBER_ME_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath(VaadinService.getCurrentRequest().getContextPath() + "/");
        VaadinService.getCurrentResponse().addCookie(cookie);
    }

    /**
     * Tries to locate the cookie with remember-me token.
     * 
     * @return remember-me token or null
     */
    private RememberMeToken getRememberMeToken() {
        RememberMeToken token = null;
        for (Cookie cookie : VaadinService.getCurrentRequest().getCookies()) {
            if (REMEMBER_ME_KEY.equals(cookie.getName())) {

                byte[] decoded = Base64.decode(cookie.getValue().getBytes());
                try (InputStream file = new ByteArrayInputStream(decoded);
                        InputStream buffer = new BufferedInputStream(file);
                        ObjectInput input = new ObjectInputStream(buffer);) {

                    token = (RememberMeToken) input.readObject();

                } catch (ClassNotFoundException | IOException ex) {
                    LOG.error("Could not deserialize remember-me token.", ex);
                }
            }
        }
        return token;
    }

    /**
     * Sets up given authentication.
     * 
     * @param authentication
     * @param request
     */
    private void authenticate(Authentication authentication, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_KEY, authentication);
        authCtx.setAuthentication(authentication);
    }

}
