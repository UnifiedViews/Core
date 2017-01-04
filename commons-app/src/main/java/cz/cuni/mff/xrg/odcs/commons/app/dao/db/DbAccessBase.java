package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Partial implementation of {@link DbAccess} interface.
 * 
 * @author Petyr
 * @param <T>
 */
public abstract class DbAccessBase<T extends DataObject>
        extends DbAccessReadBase<T>
        implements DbAccess<T> {

    public DbAccessBase(Class<T> entityClass) {
        super(entityClass);
    }

    @Transactional
    @Override
    public void save(T object) {
        if (object.getId() == null) {
            em.persist(object);
        } else {
            em.merge(object);
        }
    }

    @Transactional
    @Override
    public void delete(T object) {
        // we might be trying to remove detached entity
        if (!em.contains(object) && object.getId() != null) {
            object = getInstance(object.getId());
        }
        em.remove(object);
    }

}
