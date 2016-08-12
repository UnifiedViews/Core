package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * Test suite for{@link ScheduleNextRun}
 * 
 * @author Petyr
 */
public class ScheduleNextRunTest {

    /**
     * If pipeline is disabled it should always return false.
     */
    @Test
    public void disabled() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(false);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(false);
        // prepare times -> run one hour in future
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(new Date());
        calendarFirst.add(Calendar.HOUR, -1);
        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        assertNull(nextRunScheduled);
    }

    /**
     * If in {@link ScheduleType#AFTER_PIPELINE} state then null
     * should be returned.
     */
    @Test
    public void runAfter() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.AFTER_PIPELINE);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);
        assertNull(nextRunScheduled);
    }

    /**
     * As first run it should return the value of first.
     */
    @Test
    public void firstInFutureNonStrict() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(false);
        // prepare times -> run one hour in future
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(new Date());
        calendarFirst.add(Calendar.HOUR, -1);

        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        assertEquals(calendarFirst.getTime(), nextRunScheduled);
    }

    /**
     * First execution is in past, strict mode, just once,
     * then the pipeline should not be executed.
     */
    @Test
    public void firstInFutureStrictJustOnce() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(10);
        schedule.setJustOnce(true);
        // prepare times -> run one hour in future
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(new Date());
        calendarFirst.add(Calendar.HOUR, -1);

        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        assertNull(nextRunScheduled);
    }

    /**
     * First execution is in past, strict mode, no just once. We are over
     * the tolerance. In this case we should run in the next period.
     */
    @Test
    public void firstInFutureStrict() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(10);
        schedule.setJustOnce(false);
        // prepare times -> run one hour in future
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(new Date());
        calendarFirst.add(Calendar.HOUR, -1);

        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        // calculate nextTime
        Calendar calendarNext = Calendar.getInstance();
        calendarNext.setTime(calendarFirst.getTime());
        calendarNext.add(Calendar.HOUR, 2);

        assertEquals(calendarNext.getTime(), nextRunScheduled);
    }

    /**
     * There were already some execution. But the next execution is in past.
     * For non strict mode it should be in past.
     */
    @Test
    public void nextInPast() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(false);
        schedule.setJustOnce(false);
        // prepare times -> run one hour in future
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -5);

        // set schedule
        schedule.setFirstExecution(calendar.getTime()); // -5
        calendar.add(Calendar.HOUR, 2);
        schedule.setLastExecution(calendar.getTime()); // - 3

        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        // calculate nextTime
        calendar.add(Calendar.HOUR, 2); // -1

        assertEquals(calendar.getTime(), nextRunScheduled);
    }

    /**
     * There were already some execution. But the next execution is in past.
     * For strict mode the execution should be moved into the feature.
     */
    @Test
    public void nextInPastStrict() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(10);
        schedule.setJustOnce(false);
        // prepare times -> run one hour in future
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -5);

        // set schedule
        schedule.setFirstExecution(calendar.getTime()); // -5
        calendar.add(Calendar.HOUR, 2);
        schedule.setLastExecution(calendar.getTime()); // - 3

        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        // calculate nextTime
        calendar.add(Calendar.HOUR, 2); // -1
        calendar.add(Calendar.HOUR, 2); // +1

        assertEquals(calendar.getTime(), nextRunScheduled);
    }

    /**
     * There were already some execution. But the next execution is in past,
     * but in tolerance. So it should return the value in past.
     */
    @Test
    public void nextInPastStrictInTolerance() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(70);
        schedule.setJustOnce(false);
        // prepare times -> run one hour in future
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -5);

        // set schedule
        schedule.setFirstExecution(calendar.getTime()); // -5
        calendar.add(Calendar.HOUR, 2);
        schedule.setLastExecution(calendar.getTime()); // - 3

        schedule.setPeriod(2);
        schedule.setPeriodUnit(PeriodUnit.HOUR);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        // calculate nextTime
        calendar.add(Calendar.HOUR, 2); // -1
        // tolerance is 70 minute .. so 1+ hour

        assertEquals(calendar.getTime(), nextRunScheduled);
    }

    /**
     * There were no execution, we miss the first execution (and tolerance)
     * now it's the time for next run.
     */
    @Test
    public void firstInPastStrict() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(5);
        schedule.setJustOnce(false);
        // prepare times -> the first execution should be two hour ago
        //	and some minutes .. -> as nextRunSchedule would be now .. 
        //	so it will not pass the before() test
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.setTime(new Date());
        calendarFirst.add(Calendar.HOUR, -2);
        calendarFirst.add(Calendar.MINUTE, -2);

        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(1);
        schedule.setPeriodUnit(PeriodUnit.HOUR);
        // as the period is one hour .. we should run now .. 

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);
        Date now = new Date();
        assertTrue(nextRunScheduled.before(now));
    }

    /**
     * Last execution was in year X .. the next should be in X + 1. So test
     * of over year scheduling.
     */
    public void overYearTest() {
        Schedule schedule = new Schedule();
        schedule.setEnabled(true);
        schedule.setType(ScheduleType.PERIODICALLY);
        schedule.setStrictlyTimed(true);
        schedule.setStrictToleranceMinutes(10);
        schedule.setJustOnce(false);
        // prepare times -> run one hour in future
        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.set(2000, 12, 30, 7, 0);

        // set schedule
        schedule.setFirstExecution(calendarFirst.getTime());
        schedule.setPeriod(3);
        schedule.setPeriodUnit(PeriodUnit.DAY);

        Date nextRunScheduled = ScheduleNextRun.calculateNextRun(schedule);

        // calculate nextTime
        Calendar calendarNext = Calendar.getInstance();
        calendarNext.set(2001, 1, 2, 7, 0);

        assertEquals(calendarNext.getTime(), nextRunScheduled);
    }
}
