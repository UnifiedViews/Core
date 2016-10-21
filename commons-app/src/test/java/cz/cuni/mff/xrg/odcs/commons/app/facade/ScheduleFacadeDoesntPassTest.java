package cz.cuni.mff.xrg.odcs.commons.app.facade;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleType;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;

/**
 * Test suite for schedule facade interface. Each test is run in own
 * transaction, which is rolled back in the end.
 * 
 * @author michal.klempa@eea.sk
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ScheduleFacadeDoesntPassTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    /**
     * Test of createSchedule method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testCreateSchedule() {
        System.out.println("createSchedule");
        Schedule schedule = scheduleFacade.createSchedule();
        assertNotNull(schedule);
        assertNotNull(schedule.getAfterPipelines());
    }

    /**
     * Test of getSchedulesFor method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testGetSchedulesFor() {
        System.out.println("getAllSchedules");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        List<Schedule> schedules = scheduleFacade.getSchedulesFor(pipeline);
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty());

        Schedule schedule = scheduleFacade.createSchedule();
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);

        List<Schedule> schedules1 = scheduleFacade.getSchedulesFor(pipeline);
        assertNotNull(schedules1);
        assertTrue(schedules1.size() == 1);
        assertEquals(schedule, schedules1.get(0));
    }

    /**
     * Test of getAllTimeBased method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testGetAllTimeBased() {
        System.out.println("getAllTimeBased");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        List<Schedule> schedules = scheduleFacade.getAllTimeBasedNotQueuedRunning();
        assertNotNull(schedules);

        Schedule schedule = scheduleFacade.createSchedule();
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);

        List<Schedule> schedules1 = scheduleFacade.getAllTimeBasedNotQueuedRunning();
        assertNotNull(schedules1);
        assertTrue(schedules.size() + 1 == schedules1.size());

        assertTrue(schedules1.contains(schedule));
    }

    /**
     * Test of getSchedule method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testGetSchedule() {
        System.out.println("getSchedule");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);
        assertNotNull(schedule.getId());
        Long id = schedule.getId();

        Schedule schedule1 = scheduleFacade.getSchedule(id);
        assertNotNull(schedule1);
        assertEquals(schedule, schedule1);
    }

    /**
     * Test of save method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testSave() {
        System.out.println("save");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);
        assertNotNull(schedule.getId());
    }

    /**
     * Test of delete method, of class ScheduleFacade.
     */
    @Test
    @Transactional
    public void testDelete() {
        System.out.println("delete");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);
        assertNotNull(schedule.getId());
        Long id = schedule.getId();
        em.flush();

        scheduleFacade.delete(schedule);
        Schedule schedule1 = scheduleFacade.getSchedule(id);
        assertNull(schedule1);
    }

    /**
     * Test of deleteNotification method, of class ScheduleFacade.
     */
//    @Test
//    TODO enable this test
    @Transactional
    public void testDeleteNotification() {
        System.out.println("deleteNotification");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);

        ScheduleNotificationRecord scheduleNotificationRecord = new ScheduleNotificationRecord();
        EmailAddress emailAddress = new EmailAddress("user@example.com");
        Set<EmailAddress> emailAddresses = new HashSet<>();
        emailAddresses.add(emailAddress);

        scheduleNotificationRecord.setSchedule(schedule);
        scheduleNotificationRecord.setEmails(emailAddresses);
        schedule.setNotification(scheduleNotificationRecord);
        scheduleFacade.save(schedule);

        scheduleFacade.deleteNotification(schedule.getNotification());
        em.flush();

        Schedule schedule1 = scheduleFacade.getSchedule(schedule.getId());
        assertNotNull(schedule1);
        assertEquals(schedule, schedule1);

        ScheduleNotificationRecord scheduleNotificationRecord1 = schedule1.getNotification();
        assertNull(scheduleNotificationRecord1);
    }

    /**
     * Test of execute method, of class ScheduleFacade.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.save(schedule);
        assertNotNull(schedule.getId());

        scheduleFacade.execute(schedule);
        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.get(0).getSchedule().isEnabled());
        assertEquals(pipeline, executions.get(0).getPipeline());
        assertEquals(schedule, executions.get(0).getSchedule());
    }

    /**
     * Test of execute method, of class ScheduleFacade.
     */
    @Test
    public void testExecute2() {
        System.out.println("execute");
        Pipeline pipeline = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline);

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setJustOnce(true);
        schedule.setEnabled(true);
        schedule.setPipeline(pipeline);
        scheduleFacade.save(schedule);
        assertNotNull(schedule.getId());

        scheduleFacade.execute(schedule);
        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertFalse(executions.get(0).getSchedule().isEnabled());
        assertEquals(pipeline, executions.get(0).getPipeline());
        assertEquals(schedule, executions.get(0).getSchedule());
    }

    /**
     * Test of executeFollowers method, of class ScheduleFacade.
     */
    @Test
    public void testExecuteFollowers_0args() {
        System.out.println("executeFollowers_0args");

        Pipeline pipeline = pipelineFacade.createPipeline();
        assertNull(pipeline.getId());
        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.execute(schedule);
        assertNotNull(schedule.getId());

        Pipeline pipeline2 = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline2);

        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.size() == 1);
        PipelineExecution pipelineExecution = executions.get(0);
        pipelineExecution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        pipelineExecution.setEnd(new Date());
        pipelineFacade.save(pipelineExecution);

        Schedule schedule2 = scheduleFacade.createSchedule();
        schedule2.addAfterPipeline(pipeline);
        schedule2.setType(ScheduleType.AFTER_PIPELINE);
        schedule2.setPipeline(pipeline2);
        schedule2.setEnabled(true);
        scheduleFacade.save(schedule2);

        scheduleFacade.executeFollowers();
        List<PipelineExecution> executions2 = pipelineFacade.getExecutions(pipeline2);
        assertNotNull(executions2);
        assertFalse(executions2.isEmpty());
        assertTrue(executions2.get(0).getSchedule().isEnabled());
        assertEquals(pipeline2, executions2.get(0).getPipeline());
        assertEquals(schedule2, executions2.get(0).getSchedule());
    }

    /**
     * Test of executeFollowers method, of class ScheduleFacade.
     */
    @Test
    public void testExecuteFollowers_Pipeline() {
        System.out.println("executeFollowers_Pipeline");

        Pipeline pipeline = pipelineFacade.createPipeline();
        assertNull(pipeline.getId());
        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.execute(schedule);
        assertNotNull(schedule.getId());

        Pipeline pipeline2 = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline2);

        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.size() == 1);
        PipelineExecution pipelineExecution = executions.get(0);
        pipelineExecution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        pipelineExecution.setEnd(new Date());
        pipelineFacade.save(pipelineExecution);

        Schedule schedule2 = scheduleFacade.createSchedule();
        schedule2.addAfterPipeline(pipeline);
        schedule2.setType(ScheduleType.AFTER_PIPELINE);
        schedule2.setPipeline(pipeline2);
        schedule2.setEnabled(true);
        scheduleFacade.save(schedule2);

        scheduleFacade.executeFollowers(pipeline);
        List<PipelineExecution> executions2 = pipelineFacade.getExecutions(pipeline2);
        assertNotNull(executions2);
        assertFalse(executions2.isEmpty());
        assertTrue(executions2.get(0).getSchedule().isEnabled());
        assertEquals(pipeline2, executions2.get(0).getPipeline());
        assertEquals(schedule2, executions2.get(0).getSchedule());
    }

    /**
     * Test of executeFollowers method, of class ScheduleFacade.
     */
    @Test
    public void testExecuteFollowers_Pipeline2() {
        System.out.println("executeFollowers_Pipeline2");

        Pipeline pipeline = pipelineFacade.createPipeline();
        assertNull(pipeline.getId());
        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.execute(schedule);
        assertNotNull(schedule.getId());

        Pipeline pipeline2 = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline2);

        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.size() == 1);
        PipelineExecution pipelineExecution = executions.get(0);
        pipelineExecution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        //pipelineExecution.setEnd(new Date());
        pipelineFacade.save(pipelineExecution);

        Schedule schedule2 = scheduleFacade.createSchedule();
        schedule2.addAfterPipeline(pipeline);
        schedule2.setType(ScheduleType.AFTER_PIPELINE);
        schedule2.setPipeline(pipeline2);
        schedule2.setEnabled(true);
        scheduleFacade.save(schedule2);

        scheduleFacade.executeFollowers(pipeline);
        List<PipelineExecution> executions2 = pipelineFacade.getExecutions(pipeline2);
        assertNotNull(executions2);
        assertTrue(executions2.isEmpty());
    }

    /**
     * Test of executeFollowers method, of class ScheduleFacade.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testExecuteFollowers_Pipeline3() throws InterruptedException {
        System.out.println("executeFollowers_Pipeline");

        Pipeline pipeline = pipelineFacade.createPipeline();
        assertNull(pipeline.getId());
        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.execute(schedule);
        assertNotNull(schedule.getId());

        Pipeline pipeline2 = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline2);

        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.size() == 1);
        PipelineExecution pipelineExecution = executions.get(0);
        pipelineExecution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        pipelineExecution.setEnd(new Date());
        pipelineFacade.save(pipelineExecution);

        Thread.sleep(1000L);

        Schedule schedule2 = scheduleFacade.createSchedule();
        schedule2.addAfterPipeline(pipeline);
        schedule2.setType(ScheduleType.AFTER_PIPELINE);
        schedule2.setPipeline(pipeline2);
        schedule2.setEnabled(true);
        schedule2.setLastExecution(new Date());
        scheduleFacade.save(schedule2);

        scheduleFacade.executeFollowers(pipeline);
        List<PipelineExecution> executions2 = pipelineFacade.getExecutions(pipeline2);
        assertNotNull(executions2);
        assertTrue(executions2.isEmpty());
    }

    /**
     * Test of executeFollowers method, of class ScheduleFacade.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testExecuteFollowers_Pipeline4() throws InterruptedException {
        System.out.println("executeFollowers_Pipeline");

        Pipeline pipeline = pipelineFacade.createPipeline();
        assertNull(pipeline.getId());
        pipelineFacade.save(pipeline);
        assertNotNull(pipeline.getId());

        Schedule schedule = scheduleFacade.createSchedule();
        assertNull(schedule.getId());
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setPipeline(pipeline);
        schedule.setEnabled(true);
        scheduleFacade.execute(schedule);
        assertNotNull(schedule.getId());

        Pipeline pipeline2 = pipelineFacade.createPipeline();
        pipelineFacade.save(pipeline2);

        Date dd = new Date();
        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        assertTrue(executions.size() == 1);
        PipelineExecution pipelineExecution = executions.get(0);
        pipelineExecution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        pipelineExecution.setEnd(dd);
        pipelineFacade.save(pipelineExecution);

        Schedule schedule2 = scheduleFacade.createSchedule();
        schedule2.addAfterPipeline(pipeline);
        schedule2.setType(ScheduleType.AFTER_PIPELINE);
        schedule2.setPipeline(pipeline2);
        schedule2.setEnabled(true);
        schedule2.setLastExecution(dd);
        scheduleFacade.save(schedule2);

        scheduleFacade.executeFollowers(pipeline);
        List<PipelineExecution> executions2 = pipelineFacade.getExecutions(pipeline2);
        assertNotNull(executions2);
        assertFalse(executions2.isEmpty());
        assertTrue(executions2.get(0).getSchedule().isEnabled());
        assertEquals(pipeline2, executions2.get(0).getPipeline());
        assertEquals(schedule2, executions2.get(0).getSchedule());
    }

    /**
     * Test of getAllSchedules method, of class ScheduleFacade.
     */
    @Test
    public void testGetAllSchedules() {
        List<Schedule> result = scheduleFacade.getAllSchedules();
        assertEquals(2, result.size());
    }

    /**
     * Test of fetching email notification settings for schedule.
     */
    @Test
    public void testGetNotification() {
        Schedule sch = scheduleFacade.getSchedule(1L);

        assertNotNull(sch);
        assertNotNull(sch.getNotification());
        assertNotNull(sch.getNotification().getEmails());
        assertEquals(1, sch.getNotification().getEmails().size());

        EmailAddress email = sch.getNotification().getEmails().iterator().next();
        assertEquals("scheduler@example.com", email.getEmail());
    }

    /**
     * Test deleting schedule notification.
     */
    @Test
    @Transactional
    public void testDeleteScheduleNotification() {

        Schedule sch = scheduleFacade.getSchedule(1L);
        assertNotNull(sch);
        assertNotNull(sch.getNotification());

        scheduleFacade.deleteNotification(sch.getNotification());
        em.flush();
        em.clear();

        Schedule ret = scheduleFacade.getSchedule(1L);
        assertNotNull(ret);
        assertNull(ret.getNotification());
    }

    @Test
    @Transactional
    public void testChangeLastExecution() {
        Date now = new Date();
        Schedule sch = scheduleFacade.getSchedule(1L);
        sch.setLastExecution(now);
        scheduleFacade.save(sch);

        em.flush();
        em.clear();

        Schedule ret = scheduleFacade.getSchedule(1L);
        assertEquals(now, ret.getLastExecution());
    }

}
