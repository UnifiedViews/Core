package cz.cuni.mff.xrg.odcs.commons.app.auth;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Keeps authentication context for current user session.
 * 
 * @author Jan Vojt
 */
public class AuthenticationContext implements ApplicationListener<AuthenticationSuccessEvent> {

    /**
     * Retrieves username of currently logged in user from session.
     * 
     * @return username if user is authenticated, empty string otherwise
     */
    public String getUsername() {
        Authentication auth = getAuthentication();
        return auth == null ? "" : auth.getName();
    }

    /**
     * Retrieves currently authenticated user.
     * 
     * @return logged-in user
     */
    public User getUser() {
        Authentication auth = getAuthentication();
        return auth == null ? null : (User) auth.getPrincipal();
    }

    /**
     * Decides whether any user is currently successfully authenticated.
     * Anonymous users are not considered authenticated.
     * 
     * @return authentication status
     */
    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return false;
        }

        if (auth instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return auth.isAuthenticated();
    }

    /**
     * Clears all authentication data in user session. Use when logging out user.
     */
    public void clear() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Successful authentication handler needs to setup {@link Authentication} in security context.
     * 
     * @param event
     *            successful authentication event
     */
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        SecurityContextHolder.getContext().setAuthentication(event.getAuthentication());
    }

    /**
     * Helper method for getting authentication from session context.
     * 
     * @return authentication
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Allows setting the authentication from external modules. This is needed
     * for instance when loading authentication from session in web frontend.
     * 
     * @param authentication
     */
    public void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
