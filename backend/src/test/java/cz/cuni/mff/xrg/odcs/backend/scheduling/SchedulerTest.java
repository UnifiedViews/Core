/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.backend.scheduling;

import cz.cuni.mff.xrg.odcs.backend.execution.EngineMock;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ExecutionFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = { "classpath:backend-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class SchedulerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerTest.class);

    public static final Integer RUNNIG_PPL_LIMIT = 2;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private ExecutionFacade executionFacade;

    private class EngineMockWithLimit extends EngineMock {
        @Override
        protected Integer getLimitOfScheduledPipelines() {
            return RUNNIG_PPL_LIMIT;
        }
    }

    @Test
    @Transactional
    public void test() {
        Pipeline ppl = pipelineFacade.createPipeline();
        ppl.getGraph().addDpuInstance(new DPUInstanceRecord());
        pipelineFacade.save(ppl);
        Schedule schedule = scheduleFacade.createSchedule();
        schedule.setType(ScheduleType.PERIODICALLY);
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.MINUTE, -1);
        schedule.setFirstExecution(cal.getTime());
        schedule.setEnabled(true);
        schedule.setPipeline(ppl);
        schedule.setPriority((long) 1);
        scheduleFacade.save(schedule);
        scheduler.timeBasedCheck();
        EngineMock engine = new EngineMockWithLimit();
        engine.setPipelineFacade(pipelineFacade);
        engine.setExecutionFacade(this.executionFacade);
        engine.doCheck();
    }

    @Test
    @Transactional
    public void test2() {
        Pipeline ppl = pipelineFacade.createPipeline();
        ppl.getGraph().addDpuInstance(new DPUInstanceRecord());
        pipelineFacade.save(ppl);

        Schedule schedule3 = createSchedule(4, ppl);
        Schedule schedule4 = createSchedule(5, ppl);

        scheduler.timeBasedCheck();

        EngineMock engine = new EngineMockWithLimit();
        engine.setPipelineFacade(pipelineFacade);
        engine.setExecutionFacade(this.executionFacade);

        engine.doCheck();
        assertEquals(2, engine.historyOfExecution.size());

        final Set<Long> history = new HashSet<>();
        history.add(schedule4.getId());
        history.add(schedule3.getId());

        for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
            assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
        }

    }

    public Schedule createSchedule(int priority, Pipeline ppl) {
        Calendar cal = Calendar.getInstance();
        Schedule schedule = scheduleFacade.createSchedule();
        schedule.setType(ScheduleType.PERIODICALLY);
        cal.add(Calendar.MINUTE, -1);
        schedule.setFirstExecution(cal.getTime());
        schedule.setEnabled(true);
        schedule.setPipeline(ppl);
        schedule.setPriority((long) priority);
        scheduleFacade.save(schedule);
        return schedule;

    }

    @Test
    @Transactional
    public void test3() {
        Pipeline ppl = pipelineFacade.createPipeline();
        ppl.getGraph().addDpuInstance(new DPUInstanceRecord());
        pipelineFacade.save(ppl);
        Schedule schedule = createSchedule(0, ppl);
        Schedule schedule2 = createSchedule(0, ppl);
        Schedule schedule3 = createSchedule(0, ppl);
        Schedule schedule4 = createSchedule(3, ppl);
        scheduler.timeBasedCheck();

        EngineMock engine = new EngineMockWithLimit();
        engine.setPipelineFacade(pipelineFacade);
        engine.setExecutionFacade(this.executionFacade);
        engine.doCheck();

        assertEquals(3, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule.getId());
            history.add(schedule2.getId());
            history.add(schedule3.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }

        engine.numberOfRunningJobs--;
        engine.doCheck();

        assertEquals(3, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule.getId());
            history.add(schedule2.getId());
            history.add(schedule3.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }

        engine.numberOfRunningJobs--;
        engine.numberOfRunningJobs--;
        engine.doCheck();

        assertEquals(4, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule.getId());
            history.add(schedule2.getId());
            history.add(schedule3.getId());
            history.add(schedule4.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }
    }

    @Test
    @Transactional
    public void test4() {
        Pipeline ppl = pipelineFacade.createPipeline();
        ppl.getGraph().addDpuInstance(new DPUInstanceRecord());
        pipelineFacade.save(ppl);

        Schedule schedule = createSchedule(2, ppl);
        Schedule schedule2 = createSchedule(3, ppl);
        Schedule schedule3 = createSchedule(4, ppl);
        Schedule schedule4 = createSchedule(5, ppl);

        scheduler.timeBasedCheck();

        EngineMock engine = new EngineMockWithLimit();
        engine.setPipelineFacade(pipelineFacade);
        engine.setExecutionFacade(this.executionFacade);

        engine.doCheck();
        assertEquals(2, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule3.getId());
            history.add(schedule4.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }

        engine.numberOfRunningJobs--;
        engine.doCheck();

        assertEquals(3, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule2.getId());
            history.add(schedule3.getId());
            history.add(schedule4.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }

        engine.numberOfRunningJobs--;
        engine.doCheck();

        assertEquals(4, engine.historyOfExecution.size());
        {
            final Set<Long> history = new HashSet<>();
            history.add(schedule.getId());
            history.add(schedule2.getId());
            history.add(schedule3.getId());
            history.add(schedule4.getId());

            for (int i = 0; i < engine.historyOfExecution.size(); ++i) {
                assertTrue(history.contains(engine.historyOfExecution.get(i).getSchedule().getId()));
            }
        }
    }
}
