package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Implementation for accessing {@link OpenEvent} data objects.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
class DbOpenEventImpl extends DbAccessBase<OpenEvent> implements DbOpenEvent {

    public DbOpenEventImpl() {
        super(OpenEvent.class);
    }

    @Override
    public OpenEvent getOpenEvent(Pipeline pipeline, User user) {
        final String stringQuery = "SELECT e FROM OpenEvent e"
                + " WHERE e.owner = :user"
                + " AND e.pipeline = :ppl";
        TypedQuery<OpenEvent> query = createTypedQuery(stringQuery);
        query.setParameter("user", user);
        query.setParameter("ppl", pipeline);
        return execute(query);
    }

    @Override
    public List<OpenEvent> getOpenEvents(Pipeline pipeline) {
        final String stringQuery = "SELECT e FROM OpenEvent e"
                + " LEFT JOIN FETCH e.owner u" // eagerly load users
                + " WHERE e.pipeline = :pipe";
        TypedQuery<OpenEvent> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        return executeList(query);
    }

    @Override
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, Date from) {
        final String stringQuery = "SELECT e FROM OpenEvent e"
                + " LEFT JOIN FETCH e.owner u" // eagerly load users
                + " WHERE e.pipeline = :pipe"
                + " AND e.timestamp >= :tsp";
        TypedQuery<OpenEvent> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        query.setParameter("tsp", from);
        return executeList(query);
    }

    @Override
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, User user) {
        final String stringQuery = "SELECT e FROM OpenEvent e"
                + " LEFT JOIN FETCH e.owner u" // eagerly load users
                + " WHERE e.pipeline = :pipe"
                + " AND e.owner <> :usr";
        TypedQuery<OpenEvent> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        query.setParameter("usr", user);
        return executeList(query);
    }

    @Override
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, Date from, User user) {
        final String stringQuery = "SELECT e FROM OpenEvent e"
                + " LEFT JOIN FETCH e.owner u" // eagerly load users
                + " WHERE e.pipeline = :pipe"
                + " AND e.timestamp >= :tsp"
                + " AND e.owner <> :usr";
        TypedQuery<OpenEvent> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        query.setParameter("tsp", from);
        query.setParameter("usr", user);
        return executeList(query);
    }

}
