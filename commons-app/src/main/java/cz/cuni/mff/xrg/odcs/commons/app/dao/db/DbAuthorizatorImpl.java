package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.auth.SharedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.user.OwnedEntity;
import eu.unifiedviews.commons.dao.view.ExecutionView;
import eu.unifiedviews.commons.dao.view.PipelineView;

/**
 * Implementation of authorization logic for {@link CriteriaBuilder}.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
class DbAuthorizatorImpl implements DbAuthorizator {

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    @Override
    public Predicate getAuthorizationPredicate(CriteriaBuilder cb, Path<?> root, Class<?> entityClass) {

        if (this.authCtx == null) {
            // no athorization
            return null;
        }

        String adminPermission = appConfig.getString(ConfigProperty.ADMIN_PERMISSION);

        for (GrantedAuthority ga : authCtx.getUser().getAuthorities()) {
            if (adminPermission.equals(ga.getAuthority()))
                return null;
        }

        return getAuthorizationPredicateUser(cb, root, entityClass);
    }

    public Predicate getAuthorizationPredicateUser(CriteriaBuilder cb, Path<?> root, Class<?> entityClass) {

        Predicate predicate = null;

        if (PipelineView.class.isAssignableFrom(entityClass)) {
            predicate = or(cb, predicate, cb.equal(root.get("usrName"), authCtx.getUser().getUsername()));

            Predicate subPredicate = null;
            subPredicate = cb.notEqual(root.get("usrName"), authCtx.getUser().getUsername());
            subPredicate = and(cb, subPredicate, cb.notEqual(root.get("shareType"), ShareType.PRIVATE));

            predicate = or(cb, predicate, subPredicate);
            return predicate;
        }

        if (ExecutionView.class.isAssignableFrom(entityClass)) {
            predicate = or(cb, predicate, cb.equal(root.get("ownerName"), authCtx.getUser().getUsername()));
            return predicate;
        }

        if (OwnedEntity.class.isAssignableFrom(entityClass)) {
            predicate = or(cb, predicate, cb.equal(root.get("owner"), authCtx.getUser()));
        }

        if (SharedEntity.class.isAssignableFrom(entityClass)) {
            Predicate subPredicate = null;
            subPredicate = cb.notEqual(root.get("owner"), authCtx.getUser());
            subPredicate = and(cb, subPredicate, cb.notEqual(root.get("shareType"), ShareType.PRIVATE));

            predicate = or(cb, predicate, subPredicate);
        }

        // PipelineExecution is also viewable whenever its Pipeline is viewable
        if (PipelineExecution.class.isAssignableFrom(entityClass)) {
            predicate = or(cb, predicate, getAuthorizationPredicate(cb, root.get("pipeline"), Pipeline.class));
        }

        return predicate;
    }

    private Predicate or(CriteriaBuilder cb, Predicate left, Predicate right) {
        if (left == null) {
            return right;
        } else {
            return cb.or(left, right);
        }
    }

    private Predicate and(CriteriaBuilder cb, Predicate left, Predicate right) {
        if (left == null) {
            return right;
        } else {
            return cb.and(left, right);
        }
    }

    /**
     * Gets property path.
     * 
     * @param root
     *            the root where path starts form
     * @param propertyId
     *            the property ID
     * @return the path to property
     */
    private Path<Object> getPropertyPath(final Root<?> root, final Object propertyId) {
        final String[] propertyIdParts = ((String) propertyId).split("\\.");

        Path<Object> path = null;
        for (final String part : propertyIdParts) {
            if (path == null) {
                path = root.get(part);
            } else {
                path = path.get(part);
            }
        }
        return path;
    }

}
