package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import java.util.Date;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessRead;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Logs are using special row as workaround for LIMIT, that can cause
 * full result scan. This require support from whole respective DAO layer.
 *
 * @author Petyr
 */
public interface DbLogRead extends DbAccessRead<Log> {

    /**
     * Delete all logs that are older then given date.
     *
     * @param date
     *            Date threshold.
     */
    void prune(Date date);

    /**
     * Retrieve the highest relative log index for given execution. If there are
     * no logs for given execution then return null.
     *
     * @param executionId
     *            Execution id.
     * @return Can be null.
     */
    Long getLastRelativeIndex(Long executionId);

    /**
     * Delete all logs that are older then given date.
     *
     * @param date
     *            Date threshold.
     */
    void delete(PipelineExecution execution);

}
