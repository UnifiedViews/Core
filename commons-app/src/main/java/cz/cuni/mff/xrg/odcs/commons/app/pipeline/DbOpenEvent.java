package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Interface for access to {@link OpenEvent}s.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
public interface DbOpenEvent extends DbAccess<OpenEvent> {

    /**
     * Finds open pipeline event by user and pipeline.
     * 
     * @param pipeline
     * @param user
     * @return open pipeline event
     */
    public OpenEvent getOpenEvent(Pipeline pipeline, User user);

    /**
     * Fetches all events when given pipeline was open in pipeline canvas.
     * 
     * @param pipeline
     * @return list of {@link OpenEvent}s for given pipeline.
     */
    public List<OpenEvent> getOpenEvents(Pipeline pipeline);

    /**
     * Fetches all events when given pipeline was open in pipeline canvas.
     * 
     * @param pipeline
     * @param from
     *            select only events with later timestamp
     * @return list of {@link OpenEvent}s for given pipeline since given date.
     */
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, Date from);

    /**
     * Fetches all events when given pipeline was open in pipeline canvas.
     * 
     * @param pipeline
     * @param user
     *            who's events will not be included in the returned list,
     *            or null
     * @return list of {@link OpenEvent}s for given pipeline opened by given
     *         user.
     */
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, User user);

    /**
     * Fetches all events when given pipeline was open in pipeline canvas.
     * 
     * @param pipeline
     * @param from
     *            select only events with later timestamp
     * @param user
     *            who's events will not be included in the returned list,
     *            or null
     * @return list of events
     */
    public List<OpenEvent> getOpenEvents(Pipeline pipeline, Date from, User user);

}
