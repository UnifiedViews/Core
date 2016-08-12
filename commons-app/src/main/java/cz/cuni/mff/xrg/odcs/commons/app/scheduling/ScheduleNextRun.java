package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculate time of next time for given schedule.
 * 
 * @author Petyr
 */
class ScheduleNextRun {

    private static final Logger LOG = LoggerFactory
            .getLogger(ScheduleNextRun.class);

    private ScheduleNextRun() {
    }

    /**
     * Return T = start + (period * x) where x is minimal as T > reference.
     * 
     * @param start
     *            Start time.
     * @param reference
     *            Time we have to pass.
     * @param period
     *            Period to add.
     * @param periodUnit
     *            Period unit.
     * @return
     */
    private static Date calculateNext(Date start, Date reference, int period,
            PeriodUnit periodUnit) {
        return calculateNext(start, reference, period, periodUnit, 0);
    }

    /**
     * Return T = start + (period * x) where x is minimal for which holds T >
     * reference - strictTollerance.
     * 
     * @param start
     *            Start time.
     * @param reference
     *            Time we have to pass.
     * @param period
     *            Period to add.
     * @param periodUnit
     *            Period unit.
     * @param strictTollerance
     * @return
     */
    private static Date calculateNext(Date start, Date reference, int period,
            PeriodUnit periodUnit, int strictTollerance) {
        Calendar calendarResult = Calendar.getInstance();
        Calendar calendarReference = Calendar.getInstance();
        // set
        calendarResult.setTime(start);
        calendarReference.setTime(reference);
        // subtract time for strict timing
        calendarReference.add(Calendar.MINUTE, -strictTollerance);

        // iterate
        while (!calendarResult.after(calendarReference)) {
            // increase
            switch (periodUnit) {
                case YEAR:
                    calendarResult.add(Calendar.YEAR, period);
                    break;
                case WEEK:
                    calendarResult.add(Calendar.WEEK_OF_YEAR, period);
                    break;
                case DAY:
                    calendarResult.add(Calendar.DAY_OF_YEAR, period);
                    break;
                case HOUR:
                    calendarResult.add(Calendar.HOUR, period);
                    break;
                case MINUTE:
                    calendarResult.add(Calendar.MINUTE, period);
                    break;
                case MONTH:
                    calendarResult.add(Calendar.MONTH, period);
                    break;
            }
        }

        return calendarResult.getTime();
    }

    /**
     * Check if it is possible to run schedule in given time if the schedule is
     * in strict mode. The strict mode means that the pipeline can not be run
     * much later then it's scheduled time.
     * 
     * @param runTime
     * @param tolerance
     * @return True if schedule can be run in given time.
     */
    private static boolean checkStrict(Date nextRun, Integer tolerance) {
        // the nextRun must be in future ..
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(new Date());

        if (tolerance == null) {
            // no tolerance
        } else {
            calendarNow.add(Calendar.MINUTE, -tolerance);
        }

        if (nextRun.after(calendarNow.getTime())) {
            // calendarNow < nextRun - run in future .. we can do this
            return true;
        } else {
            // we have to run in future ..
            return false;
        }
    }

    /**
     * Calculate time of next run for given {@link Schedule}. If schedule is not
     * time dependent or is disabled then return null.
     * 
     * @param schedule
     * @return Estimate of time for next execution or null.
     */
    public static Date calculateNextRun(Schedule schedule) {
        if (!schedule.isEnabled()) {
            return null;
        }

        if (schedule.getType() == ScheduleType.AFTER_PIPELINE) {
            return null;
        }

        // if there were no previous run or we are justOnce
        // then return time of first execution
        if (schedule.getLastExecution() == null || schedule.isJustOnce()) {
            Date nextRun = schedule.getFirstExecution();
            final Integer tolerance = schedule.getStrictToleranceMinutes();

            // check for strict
            if (schedule.isStrictlyTimed() && !checkStrict(nextRun, tolerance)) {
                if (schedule.isJustOnce()) {
                    // we miss that ..
                    return null;
                } else {
                    // schedule to the future
                    nextRun = calculateNext(schedule.getFirstExecution(),
                            new Date(), schedule.getPeriod(),
                            schedule.getPeriodUnit(),
                            schedule.getStrictToleranceMinutes());
                }
            }

            return nextRun;
        }
        // do we know period
        if (schedule.getPeriod() == null) {
            LOG.warn("Period unit for {} is null and it should not be.",
                    schedule.getId());
            return null;
        }
        // get time of next run
        Date nextRun = calculateNext(schedule.getFirstExecution(),
                schedule.getLastExecution(), schedule.getPeriod(),
                schedule.getPeriodUnit());
        // check if we are in strict mode
        final Integer tolerance = schedule.getStrictToleranceMinutes();
        if (schedule.isStrictlyTimed() && !checkStrict(nextRun, tolerance)) {
            // schedule in to the future, we miss execution time
            nextRun = calculateNext(schedule.getFirstExecution(), new Date(),
                    schedule.getPeriod(), schedule.getPeriodUnit(),
                    tolerance);
        }
        return nextRun;
    }

}
