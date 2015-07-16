package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.classic.Level;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter.Compare;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.DbLogRead;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Facade for fetching persisted entities. Manipulating logs is not implemented,
 * as these should be created immutable by backend only.
 * 
 * @author Jan Vojt
 */
@Transactional(readOnly = true)
class LogFacadeImpl implements LogFacade {

    @Autowired
    private DbLogRead logDao;

    /**
     * Return true if there exist logs with given level for given dpu instance
     * of given pipeline execution.
     * 
     * @param exec
     * @param level
     * @return
     */
    @Override
    public boolean existLogsGreaterOrEqual(PipelineExecution exec, Level level) {
        DbQueryBuilder<Log> builder = logDao.createQueryBuilder();
        // add filters
        builder.addFilter(Compare.equal("execution", exec.getId()));
        builder.addFilter(Compare.greaterEqual("logLevel", level.toInt()));
        // execute
        return logDao.executeSize(builder.getCountQuery()) > 0;
    }

    /**
     * Return list of all usable log's levels without aggregations. Ordered
     * descending by priority.
     * 
     * @return
     */
    @Override
    public ArrayList<Level> getAllLevels() {
        ArrayList result = new ArrayList(5);

        result.add(Level.ERROR);
        result.add(Level.WARN);
        result.add(Level.INFO);
        result.add(Level.DEBUG);
        result.add(Level.TRACE);

        return result;
    }
}
