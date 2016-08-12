package cz.cuni.mff.xrg.odcs.commons.app.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Permission evaluator helper bean provides methods for checking user permissions on entities and user authorities
 * Should be used instead of {@link AuthAwarePermissionEvaluator} as it also provides method for checking user authorities
 */
public class PermissionUtils {

    @Autowired
    private AuthenticationContext authCtx;

    @Autowired
    private AuthAwarePermissionEvaluator permissions;

    @Autowired
    private AppConfig appConfig;

    /**
     * This method is used to check given permission for specific entity
     * 
     * @param target
     *            Entity to check permission for
     * @param perm
     *            Permission to check for entity
     * @return True if user has permission for given entity, False otherwise
     */
    public boolean hasPermission(Object target, Object perm) {
        return this.permissions.hasPermission(target, perm);
    }

    /**
     * This method is used to check if user has specific authority (permission) in general,
     * with no connection to any specific entity.
     * 
     * @param authority
     *            User authority to check
     * @return True if user has given permission, False otherwise
     */
    public boolean hasUserAuthority(String authority) {
        if (authority == null)
            return false;

        String adminPermission = this.appConfig.getString(ConfigProperty.ADMIN_PERMISSION);

        for (GrantedAuthority ga : getUser().getAuthorities()) {
            if (adminPermission.equals(ga.getAuthority()) || authority.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private User getUser() {
        return this.authCtx.getUser();
    }

}
