package cz.cuni.mff.xrg.odcs.commons.app.auth;

import org.springframework.security.access.PermissionEvaluator;

/**
 * Permission evaluator aware of current authentication context.
 * 
 * @see #hasPermission(java.lang.Object, java.lang.Object)
 * @author Jan Vojt
 */
public interface AuthAwarePermissionEvaluator extends PermissionEvaluator {

    /**
     * Resolves permissions on given target object for currently authenticated
     * user.
     * 
     * @param target
     *            entity to try permission on
     * @param perm
     *            permission requested on target
     * @return true if authenticated user has a given permission on target,
     *         false otherwise
     */
    public boolean hasPermission(Object target, Object perm);

}
