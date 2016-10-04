package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbPipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;

/**
 * Test suite for {@link DbSheduleImpl}
 * 
 * @author Petyr
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class DbSheduleDoesntPassTest {

    @Autowired
    private DbSchedule scheduleDao;

    @Autowired
    private DbPipeline pipelineDao;

    @Test
    @Transactional
    public void testGetFollowers() {

        Pipeline pipe = pipelineDao.getInstance(1L);
        List<Schedule> followers = scheduleDao.getFollowers(pipe, true);

        assertNotNull(followers);
        assertEquals(1, followers.size());
        assertEquals(2L, (long) followers.get(0).getId());
    }

    /**
     * Test of getAllSchedules method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testGetLastExecForRunAfter() {
        Schedule schedule = scheduleDao.getInstance(2);

        Assert.assertNotNull(schedule);

        // just try to excute this
        List<Date> times = scheduleDao.getLastExecForRunAfter(schedule);

        Assert.assertNotNull(times);
        Assert.assertEquals(1, times.size());

        Assert.assertEquals(null, times.get(0));
    }

}
