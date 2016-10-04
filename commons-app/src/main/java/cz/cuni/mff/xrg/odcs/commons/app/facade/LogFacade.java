package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Facade for fetching persisted entities. Manipulating logs is not implemented,
 * as these should be created immutable by backend only.
 *
 * @author Jan Vojt
 */
public interface LogFacade extends Facade {

    /**
     * Return true if there exist logs with given level for given DPU instance
     * of given pipeline execution.
     *
     * @param exec
     * @param level
     * @return true if logs exist, false otherwise
     */
    boolean existLogsGreaterOrEqual(PipelineExecution exec, Level level);

    /**
     * Return list of all usable log's levels without aggregations. Ordered
     * descending by priority.
     *
     * @return list of all log levels
     */
    ArrayList<Level> getAllLevels();

    /**
     * Creates an input stream for logging.
     *
     * @param filters
     *            Filters to apply.
     * @return Input stream with the logs in text form.
     */
    InputStream getLogsAsStream(List<Object> filters);

    /**
     * Deletes logs of PipelineExecution.
     *
     * @param execution
     *            PipelineExecution which logs will be removed.
     * @return
     */
    void deleteLogs(PipelineExecution execution);
}
