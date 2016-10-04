package cz.cuni.mff.xrg.odcs.frontend.auxiliaries;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Embedded;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Class with helper methods used in frontend.
 * 
 * @author Bogo
 */
public class DecorationHelper {

    /**
     * Gets corresponding icon for given {@link PipelineExecutionStatus}.
     * 
     * @param status
     *            Status to get icon for.
     * @return Icon for given status.
     */
    public static ThemeResource getIconForExecutionStatus(PipelineExecutionStatus status) {
        ThemeResource img = null;
        switch (status) {
            case FINISHED_SUCCESS:
                img = new ThemeResource("icons/ok.png");
                break;
            case FINISHED_WARNING:
                img = new ThemeResource("icons/warning.png");
                break;
            case FAILED:
                img = new ThemeResource("icons/error.png");
                break;
            case RUNNING:
                img = new ThemeResource("icons/running.png");
                break;
            case QUEUED:
                img = new ThemeResource("icons/queued.png");
                break;
            case CANCELLED:
                img = new ThemeResource("icons/cancelled.png");
                break;
            case CANCELLING:
                img = new ThemeResource("icons/cancelling.png");
                break;
            default:
                //no icon
                break;
        }
        return img;
    }

    /**
     * Finds the final cause for given {@link Throwable}.
     * 
     * @param t
     *            Throwable which cause should be found.
     * @return Final cause of Throwable.
     */
    public static Throwable findFinalCause(Throwable t) {
        for (; t != null; t = t.getCause()) {
            if (t.getCause() == null) // We're at final cause
            {
                return t;
            }
        }
        return t;
    }

    /**
     * Formats duration in miliseconds to hh:mm:ss string. Returns empty string
     * for duration lesser than zero.
     * 
     * @param duration
     * @return Formatted duration.
     */
    public static String formatDuration(long duration) {
        if (duration < 0) {
            return "";
        }
        //to seconds
        duration /= 1000;
        short seconds = (short) (duration % 60);
        duration -= seconds;
        //to minutes
        duration /= 60;
        short minutes = (short) (duration % 60);
        duration -= minutes;
        short hours = (short) (duration / 60);

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Gets duration of given {@link PipelineExecution}.
     * 
     * @param exec
     *            Pipeline execution.
     * @return Duration of {@link PipelineExecution} or -1 if execution wasn't
     *         finished.
     */
    public static String getDuration(PipelineExecution exec) {
        long duration = -1;
        if (exec != null) {
            duration = exec.getDuration();
        }
        return formatDuration(duration);
    }

    /**
     * Get icon for scheduled column.
     * 
     * @param isScheduled
     *            True for icon for scheduled execution.
     * @return Icon for scheduled column.
     */
    public static Embedded getIconForScheduled(boolean isScheduled) {
        ThemeResource img = new ThemeResource(isScheduled ? "icons/scheduled.png" : "icons/not_scheduled.png");
        String description = isScheduled ? Messages.getString("DecorationHelper.shceduled") : Messages.getString("DecorationHelper.manual");
        Embedded emb = new Embedded(description, img);
        emb.setDescription(description);
        return emb;
    }
}
