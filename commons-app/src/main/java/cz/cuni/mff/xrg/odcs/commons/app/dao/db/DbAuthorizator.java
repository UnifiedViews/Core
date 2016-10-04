package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 * Adds filters to container based on authorization logic.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
public interface DbAuthorizator {

    /**
     * Return authorization {@link Predicate}.
     * 
     * @param cb
     * @param root
     * @param entityClass
     * @return authorization predicate for DB query
     */
    Predicate getAuthorizationPredicate(CriteriaBuilder cb, Path<?> root, Class<?> entityClass);

}
