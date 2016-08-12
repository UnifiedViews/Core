package cz.cuni.mff.xrg.odcs.commons.app.auth;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.user.OwnedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.Permission;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Evaluates whether currently authorized user has a given permission on a given
 * object.
 * 
 * @author Jan Vojt
 */
public class DefaultPermissionEvaluator implements AuthAwarePermissionEvaluator {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultPermissionEvaluator.class);

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    /**
     * Authorization logic for permissions on entities.
     * 
     * @param auth
     *            authentication context
     * @param target
     *            entity to try permission on
     * @param perm
     *            permission requested on target
     * @return true if authenticated user has a given permission on target,
     *         false otherwise
     */
    @Override
    public boolean hasPermission(Authentication auth, Object target, Object perm) {

        if (target instanceof User)
            return true;

        // check for missing authentication context
        if (auth == null) {
            return false;
        }

        Permission foundPermission = null;

        String adminPermission = this.appConfig.getString(ConfigProperty.ADMIN_PERMISSION);

        for (GrantedAuthority ga : auth.getAuthorities()) {

            if (ga.getAuthority().equals(adminPermission)) {
                return true;
            }
            if (ga.getAuthority().equals(perm.toString())) {
                foundPermission = (Permission) ga;
            }
        }

        return hasPermissionUser(auth, target, (String) perm, foundPermission);

    }

    private boolean hasPermissionUser(Authentication auth, Object target, String requestedPerm, Permission foundPermission) {

        if (target instanceof User)
            return true;

        // entity owner is almighty
        if (target instanceof OwnedEntity) {
            OwnedEntity oTarget = (OwnedEntity) target;
            User owner = oTarget.getOwner();
            if (owner != null && auth.getName().equals(owner.getUsername())) {
                return true;
            }
        }

        // check if our entity is shared
        if (target instanceof SharedEntity) {
            SharedEntity sTarget = (SharedEntity) target;
            if (foundPermission != null) {
                if (foundPermission.isSharedEntityInstanceWriteRequired() && !ShareType.PUBLIC_RW.equals(sTarget.getShareType())) {
                    return false;
                } else {
                    //only owner can delete
                    if (ShareType.PUBLIC.contains(sTarget.getShareType()) && !EntityPermissions.PIPELINE_DELETE.equals(requestedPerm)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        // Pipeline execution actions are always viewable if
        // pipeline itself is viewable
        if (target instanceof PipelineExecution) {
            Pipeline pipe = ((PipelineExecution) target).getPipeline();
            boolean viewPipe = hasPermission(pipe, requestedPerm);
            if (viewPipe) {
                // user has permission to view pipeline
                // for this execution -> allow to see execution as well
                return true;
            }
        }

        // in other cases be restrictive
        LOG.debug("Method hasPermission refused access for object <{}> and permission <{}>.",
                target, requestedPerm);
        return false;
    }

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
    @Override
    public boolean hasPermission(Object target, Object perm) {
        return hasPermission(SecurityContextHolder.getContext().getAuthentication(), target, perm);
    }

    /**
     * Unsupported.
     * 
     * @param auth
     * @param targetId
     * @param targetType
     * @param permission
     * @return always throws exception
     */
    @Override
    public boolean hasPermission(Authentication auth,
            Serializable targetId, String targetType, Object permission) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
