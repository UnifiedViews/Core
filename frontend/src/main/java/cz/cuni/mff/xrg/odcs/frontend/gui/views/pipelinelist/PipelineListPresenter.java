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
package cz.cuni.mff.xrg.odcs.frontend.gui.views.pipelinelist;

import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;
import eu.unifiedviews.commons.dao.view.PipelineView;

/**
 * Interface for presenter that take care about presenting information about
 * pipelines.
 * 
 * @author Bogo
 */
public interface PipelineListPresenter extends Presenter {

    /**
     * Refresh data from data sources.
     */
    public void refreshEventHandler();

    /**
     * Copy pipeline with given id.
     * 
     * @param id
     *            Pipeline id.
     */
    public void copyEventHandler(long id);

    /**
     * Delete pipeline with given id.
     * 
     * @param id
     *            Pipeline id.
     */
    public void deleteEventHandler(long id);

    /**
     * Tells whether user has permission to delete pipeline with given id, so
     * we can decide whether to hide delete button.
     * 
     * @param pipelineId
     *            id of pipeline to be deleted
     * @return true if user has permission to delete, false otherwise
     */
    public boolean canDeletePipeline(long pipelineId);

    /**
     * Tells whether user has permission to edit pipeline with given id, so
     * we can decide whether to hide edit button.
     * 
     * @param pipelineId
     *            id of pipeline to be edited
     * @return true if user has permission to edit, false otherwise
     */
    public boolean canEditPipeline(long pipelineId);

    /**
     * Tells whether user has permission to run pipeline with given id, so
     * we can decide whether to hide edit button.
     * 
     * @param pipelineId
     *            id of pipeline to be run
     * @return true if user has permission to run, false otherwise
     */
    public boolean canRunPipeline(long pipelineId);

    /**
     * Tells whether user has permission to debug pipeline with given id, so
     * we can decide whether to hide debug button.
     * 
     * @param pipelineId
     *            id of pipeline to be debug
     * @return true if user has permission to debug, false otherwise
     */
    public boolean canDebugPipeline(long pipelineId);

    /**
     * Tells whether user has permission to schedule pipeline with given id, so
     * we can decide whether to hide schedule button.
     * 
     * @param pipelineId
     *            id of pipeline to be scheduled
     * @return true if user has permission to schedule, false otherwise
     */
    public boolean canSchedulePipeline(long pipelineId);

    /**
     * Tells whether user has permission to copy pipeline with given id, so
     * we can decide whether to hide copy button.
     * 
     * @param pipelineId
     *            id of pipeline to be copied
     * @return true if user has permission to copy, false otherwise
     */
    public boolean canCopyPipeline(long pipelineId);

    /**
     * Schedule pipeline with given id.
     * 
     * @param id
     *            Pipeline id.
     */
    public void scheduleEventHandler(long id);

    /**
     * Run pipeline with given id.
     * 
     * @param id
     *            Pipeline id.
     * @param inDebugMode
     *            Run in debug mode.
     */
    public void runEventHandler(long id, boolean inDebugMode);

    /**
     * Navigate to other view.
     * 
     * @param where
     *            View class.
     * @param param
     *            Parameter for new view or null.
     */
    public void navigateToEventHandler(Class where, Object param);

    /**
     * Select given page.
     * 
     * @param newPageNumber
     *            Page to select.
     */
    public void pageChangedHandler(Integer newPageNumber);

    /**
     * Informs about filter.
     * 
     * @param name
     *            Name of the filter.
     * @param filterValue
     *            Value of the filter.
     */
    public void filterParameterEventHander(String name, Object filterValue);

    /**
     * Initiate import pipeline process.
     */
    public void importPipeline();

    /**
     * View interface for pipeline list.
     */
    public interface PipelineListView {

        /**
         * Generate view, that interact with given presenter.
         * 
         * @param presenter
         * @return view
         */
        public Object enter(final PipelineListPresenter presenter);

        /**
         * Set data for view.
         * 
         * @param dataObject
         */
        public void setDisplay(PipelineListData dataObject);

        /**
         * Select given page.
         * 
         * @param pageNumber
         *            Page to select.
         */
        public void setPage(int pageNumber);

        /**
         * Set filter.
         * 
         * @param key
         *            Name of the filter.
         * @param value
         *            Value of the filter.
         */
        public void setFilter(String key, Object value);

        /**
         * Refresh paging controls of the table.
         */
        public void refreshTableControls();
    }

    /**
     * Data object for handling informations between view and presenter.
     */
    public final class PipelineListData {

        private final ReadOnlyContainer<PipelineView> container;

        /**
         * Gets the container.
         * 
         * @return Container.
         */
        public ReadOnlyContainer<PipelineView> getContainer() {
            return container;
        }

        /**
         * Constructor
         * 
         * @param container
         *            Container to hold.
         */
        public PipelineListData(ReadOnlyContainer<PipelineView> container) {
            this.container = container;
        }
    }

    /**
     * @return
     */
    public boolean isLayoutInitialized();
}
