package cz.cuni.mff.xrg.odcs.commons.app.user;

import org.springframework.security.core.GrantedAuthority;

/**
 * Set of roles in the system.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum Role implements GrantedAuthority {

    ROLE_USER("User"),
    ROLE_ADMIN("Administrator");

    /**
     * Human-readable string representation of role.
     */
    private final String role;

    private Role(String role) {
        this.role = role;
    }

    /**
     * Returns string value of authority.
     * 
     * @return String value of authority.
     */
    @Override
    public String getAuthority() {
        return name();
    }

    /**
     * Returns string value of role.
     * 
     * @return string value of role.
     */
    @Override
    public String toString() {
        return role;
    }
}
