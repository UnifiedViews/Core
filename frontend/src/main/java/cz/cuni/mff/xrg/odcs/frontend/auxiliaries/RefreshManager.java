package cz.cuni.mff.xrg.odcs.frontend.auxiliaries;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;

import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DebuggingView;

/**
 * Manager for refresh events in frontend.
 * 
 * @author Bogo
 */
public class RefreshManager {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RefreshManager.class);

    /**
     * Interval between refreshes.
     */
    public static final int REFRESH_INTERVAL = 5000;

    /**
     * Minimal interval between refreshes. If the time from last finished
     * refresh is smaller than this, next refresh is skipped.
     */
    public static final int MIN_REFRESH_INTERVAL = 4000;

    private Refresher refresher;

    private HashMap<String, RefreshListener> listeners;

    /**
     * Name for backend status {@link RefreshListener}.
     */
    public static final String BACKEND_STATUS = "backend_status";

    /**
     * Name for execution monitor {@link RefreshListener}.
     */
    public static final String EXECUTION_MONITOR = "execution_monitor";

    /**
     * Name for debugging view {@link RefreshListener}.
     */
    public static final String DEBUGGINGVIEW = "debugging_view";

    /**
     * Name for pipeline list {@link RefreshListener}.
     */
    public static final String PIPELINE_LIST = "pipeline_list";

    /**
     * Name for scheduler {@link RefreshListener}.
     */
    public static final String SCHEDULER = "scheduler";

    /**
     * Name for pipeline edit {@link RefreshListener}.
     */
    public static final String PIPELINE_EDIT = "pipeline_edit";

    /**
     * Constructor.
     * 
     * @param refresher
     *            Refresher to manage.
     */
    public RefreshManager(Refresher refresher) {
        this.refresher = refresher;
        this.listeners = new HashMap<>(3);

    }

    /**
     * Add listener.
     * 
     * @param name
     *            Name of the listener.
     * @param listener
     *            Listener to add.
     */
    public void addListener(String name, RefreshListener listener) {
        if (listeners.containsKey(name)) {
            RefreshListener oldListener = listeners.remove(name);
            refresher.removeListener(oldListener);
        }
        refresher.addListener(listener);
        listeners.put(name, listener);
    }

    /**
     * Removes {@link RefreshListener} with given name.
     * 
     * @param name
     *            Name of the listener.
     */
    public void removeListener(String name) {
        RefreshListener removedListener = listeners.remove(name);
        if (removedListener != null) {
            refresher.removeListener(removedListener);
        }
    }

    /**
     * Create {@link RefreshListener} for debugging view.
     * 
     * @param debug
     *            Debugging view to refresh.
     * @param exec
     *            Pipeline execution shown in debugging view.
     * @param pipelineFacade
     *            PipelineFacade.
     * @return Refresh listener.
     */
    public static RefreshListener getDebugRefresher(final DebuggingView debug, final PipelineExecution exec, final PipelineFacade pipelineFacade) {
        return new Refresher.RefreshListener() {
            boolean isWorking = true;

            PipelineExecution execution = exec;

            boolean lastExecutionStatus = false;

            boolean isLogsSet = false;

            private long lastRefreshFinished = 0;

            @Override
            public void refresh(Refresher source) {
                if (!isWorking) {
                    return;
                }
                if (new Date().getTime() - lastRefreshFinished > MIN_REFRESH_INTERVAL) {
                    execution = pipelineFacade.getExecution(execution.getId());
                    boolean isRunFinished = !(execution.getStatus() == PipelineExecutionStatus.QUEUED || execution.getStatus() == PipelineExecutionStatus.RUNNING || execution.getStatus() == PipelineExecutionStatus.CANCELLING);

                    if (debug.isRefreshingAutomatically()) {
                        lastExecutionStatus = true;
                        // do all the refresh job .. 
                        debug.refresh();
                    } else {
                        lastExecutionStatus = false;
                    }
                    isRunFinished &= lastExecutionStatus;
                    if (isRunFinished) {
                        isWorking = false;
                        LOG.debug("Execution finished.");
                        LOG.debug("Refresh stopped.");
                    }
                    LOG.debug("DebuggingView refreshed.");
                    lastRefreshFinished = new Date().getTime();
                }
            }
        };
    }
    
    /**
     * Manualy triggering refresh event on all added listeners
     */
    public void triggerRefresh() {
    	if (listeners == null || listeners.isEmpty()) {
			return;
		}
    	
    	for (RefreshListener listener : listeners.values()) {
			listener.refresh(refresher);
		}
    }
}
