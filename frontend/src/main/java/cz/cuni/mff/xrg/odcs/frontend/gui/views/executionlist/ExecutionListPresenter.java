package cz.cuni.mff.xrg.odcs.frontend.gui.views.executionlist;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DebuggingView;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;
import eu.unifiedviews.commons.dao.view.ExecutionView;

/**
 * Interface for presenter that take care about presenting information about
 * executions.
 * 
 * @author Petyr
 */
public interface ExecutionListPresenter extends Presenter {

    /**
     * Refresh data from data sources.
     */
    public void refreshEventHandler();

    /**
     * Stop given execution.
     * 
     * @param executionId
     */
    public void stopEventHandler(long executionId);

    /**
     * Show debug data for given execution.
     * 
     * @param executionId
     */
    public void showDebugEventHandler(long executionId);

    /**
     * Re-run given execution.
     * 
     * @param executionId
     */
    public void runEventHandler(long executionId);

    /**
     * Re-run given execution in debug mode.
     * 
     * @param executionId
     */
    public void debugEventHandler(long executionId);

    /**
     * Stop refreshing.
     */
    public void stopRefreshEventHandler();

    /**
     * Tells whether user has permission to stop pipeline execution, so we know
     * whether to render stop button.
     * 
     * @param executionId
     *            id of pipeline execution
     * @return true if user has permission to stop given pipeline execution,
     *         false otherwise
     */
    public boolean canStopExecution(long executionId);

    /**
     * Tells whether user has permission to read log.
     * 
     * @param executionId
     *            id of pipeline execution
     * @return true if user has permission to read log of given pipeline execution,
     *         false otherwise
     */
    public boolean canReadLog(long executionId);

    /**
     * Tells whether user has permission to debug pipeline execution, so we know
     * whether to render debug button.
     * 
     * @param executionId
     *            id of pipeline execution
     * @return true if user has permission to debug given pipeline execution,
     *         false otherwise
     */
    public boolean canDebugData(long executionId);

    /**
     * Tells whether user has permission to run pipeline execution, so we know
     * whether to render run button.
     * 
     * @param executionId
     *            id of pipeline execution
     * @return true if user has permission to run given pipeline execution,
     *         false otherwise
     */
    public boolean canRunPipeline(long executionId);

    /**
     * Tells whether user has permission to debug pipeline execution, so we know
     * whether to render debug button.
     * 
     * @param executionId
     *            id of pipeline execution
     * @return true if user has permission to debug given pipeline execution,
     *         false otherwise
     */
    public boolean canDebugPipeline(long executionId);

    
    /**
     * Start refreshing of given {@link DebuggingView}.
     * 
     * @param debugView
     *            Detail to refresh.
     * @param execution
     *            Execution of the detail.
     */
    public void startDebugRefreshEventHandler(DebuggingView debugView, PipelineExecution execution);

    /**
     * Changes table page.
     * 
     * @param newPageNumber
     *            Page to select.
     */
    public void pageChangedHandler(Integer newPageNumber);

    /**
     * Filters data by given parameter.
     * 
     * @param name
     *            Name of the filter.
     * @param filterValue
     *            Value of the filter.
     */
    public void filterParameterEventHander(String name, Object filterValue);

    /**
     * Navigates to given view.
     * 
     * @param where
     *            Class of the view.
     * @param param
     *            Params for the new view.
     */
    public void navigateToEventHandler(Class where, Object param);

    /**
     * View that can be used with the presenter.
     */
    public interface ExecutionListView {

        /**
         * Generate view, that interact with given presenter.
         * 
         * @param presenter
         * @return view
         */
        public Object enter(final ExecutionListPresenter presenter);

        /**
         * Set data for view.
         * 
         * @param dataObject
         */
        public void setDisplay(ExecutionListData dataObject);

        /**
         * Show detail for given execution.
         * 
         * @param execution
         * @param detailDataObject
         */
        public void showExecutionDetail(PipelineExecution execution, ExecutionDetailData detailDataObject);

        /**
         * Refreshes the table.
         * 
         * @param modified
         *            Whether the data are modified.
         */
        public void refresh(boolean modified);

        /**
         * Selects execution with given id.
         * 
         * @param execId
         *            Id of execution to select.
         */
        public void setSelectedRow(Long execId);

        /**
         * Sets value of given filter.
         * 
         * @param name
         *            Name of filter.
         * @param value
         *            Value of filter.
         */
        public void setFilter(String name, Object value);
        
        /**
         * Resets selected filters.
         */
        void resetFilters();

        /**
         * Navigates table to given page.
         * 
         * @param pageNumber
         *            Page to select.
         */
        public void setPage(int pageNumber);

        /**
         * Finds on which page is the selected execution
         * 
         * @param execId
         * @return
         */
		public int getExecPage(Long execId);

		/**
		 * Checks if there is execution with selected executionId in the table
		 * 
		 * @param executionId
		 * @return
		 */
		public boolean hasExecution(long executionId);
    }

    /**
     * Data object for handling informations between view and presenter.
     */
    public final class ExecutionListData {

        private final ReadOnlyContainer<ExecutionView> container;

        /**
         * Gets container with {@link ExecutionView}s.
         * 
         * @return Container.
         */
        public ReadOnlyContainer<ExecutionView> getContainer() {
            return container;
        }

        /**
         * Constructor.
         * 
         * @param container
         *            Container to hold.
         */
        public ExecutionListData(ReadOnlyContainer<ExecutionView> container) {
            this.container = container;
        }
    }

    /**
     * Data for execution detail.
     * 
     * @deprecated
     */
    @Deprecated
    public class ExecutionDetailData {

        private final ReadOnlyContainer<MessageRecord> messageContainer;

        /**
         * Gets {@link MessageRecord} container.
         * 
         * @return container
         */
        public ReadOnlyContainer<MessageRecord> getMessageContainer() {
            return messageContainer;
        }

        /**
         * Constructor.
         * 
         * @param messageContainer
         *            Container to hold.
         */
        public ExecutionDetailData(ReadOnlyContainer<MessageRecord> messageContainer) {
            this.messageContainer = messageContainer;
        }

    }

    /**
     * 
     * @return
     */
	public boolean isLayoutInitialized();
}
