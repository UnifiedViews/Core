package cz.cuni.mff.xrg.odcs.commons.app.execution;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter.Compare;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.DbLogRead;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;

/**
 * Test suite for {@link DbLogReader}.
 * 
 * @author Škoda Petr <skodapetr@gmail.com>
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DbLogReadDoesntPassTest {

    @Autowired
    private DbLogRead reader;

    @Test
    public void getLastRelativeId() {
        Long lastId = reader.getLastRelativeIndex(1l);
        Assert.assertNotNull(lastId);
        assertEquals(12l, (long) lastId);
    }

    @Test
    public void getSizeOfExecution() {
        DbQueryBuilder<Log> builder = reader.createQueryBuilder();

        builder.addFilter(Compare.equal("execution", 1l));

        assertEquals(12l, reader.executeSize(builder.getCountQuery()));
    }

    @Test
    public void getSizeOfExecutionDpu() {
        DbQueryBuilder<Log> builder = reader.createQueryBuilder();

        builder.addFilter(Compare.equal("execution", 1l));
        builder.addFilter(Compare.equal("dpu", 1l));

        assertEquals(7l, reader.executeSize(builder.getCountQuery()));
    }

    @Test
    public void queryLogsFromExecution() {
        DbQueryBuilder<Log> builder = reader.createQueryBuilder();
        builder.addFilter(Compare.equal("execution", 1l));

        List<Log> logs = reader.executeList(builder.getQuery().limit(10, 2));

        assertEquals(2, logs.size());
        assertEquals(11l, (long) logs.get(0).getRelativeId());
        assertEquals(12l, (long) logs.get(1).getRelativeId());
    }

    @Test
    public void queryLogsFromExecutionAndDPU() {
        DbQueryBuilder<Log> builder = reader.createQueryBuilder();
        builder.addFilter(Compare.equal("execution", 1l));
        builder.addFilter(Compare.equal("dpu", 1l));

        List<Log> logs = reader.executeList(builder.getQuery().limit(2, 4));

        assertEquals(4, logs.size());
        // also checks some ids
        assertEquals(6l, (long) logs.get(0).getRelativeId());
        assertEquals(7l, (long) logs.get(1).getRelativeId());
        assertEquals(9l, (long) logs.get(2).getRelativeId());
        assertEquals(10l, (long) logs.get(3).getRelativeId());
    }

}
