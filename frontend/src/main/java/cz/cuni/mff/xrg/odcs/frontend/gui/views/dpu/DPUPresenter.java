package cz.cuni.mff.xrg.odcs.frontend.gui.views.dpu;

import java.io.File;

import com.vaadin.data.util.IndexedContainer;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUTemplateWrap;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;

/**
 * Interface for presenter that takes care of presenting information about DPUs.
 * 
 * @author Bogo
 */
public interface DPUPresenter extends Presenter {

    /**
     * Save given DPU.
     * 
     * @param dpuWrap
     *            DPU to save.
     * @return true when succesful, false otherwise
     */
    public boolean saveDPUEventHandler(DPUTemplateWrap dpuWrap);

    /**
     * Select given DPU.
     * 
     * @param dpu
     *            DPU to select.
     * @param oldValue
     *            the value which is currently selected (to prevent out navigation if we are unable to select new value, e.g. configuration invalid).
     */
    public void selectDPUEventHandler(DPUTemplateRecord dpu, Object oldValue);

    /**
     * Open dialog for creating new DPU.
     */
    public void openDPUCreateEventHandler();

    /**
     * Import DPUTemplate.
     */
    public void importDPUTemplateEventHandler();

    /**
     * Export all DPUs.
     */
    public void exportAllEventHandler();

    /**
     * Check whether current user has permission on selected DPU.
     * 
     * @param permission
     *            Permission name.
     * @return If the user has given permission.
     */
    public boolean hasPermission(String permission);

    /**
     * Copy selected DPU.
     */
    public void copyDPUEventHandler();

    /**
     * Delete selected DPU.
     */
    public void deleteDPUEventHandler();

    /**
     * Replace selected DPU's jar.
     * 
     * @param file
     *            New jar file of the DPU.
     */
    public void dpuUploadedEventHandler(File file);

    /**
     * Show detail of given pipeline.
     * 
     * @param id
     *            Id of pipeline
     */
    public void pipelineDetailEventHandler(Long id);

    /**
     * Delete given pipeline.
     * 
     * @param id
     *            Id of pipeline
     */
    public void pipelineDeleteEventHandler(Long id);

    /**
     * Show status of given pipeline.
     * 
     * @param id
     *            Id of pipeline
     */
    public void pipelineStatusEventHandler(Long id);

    /**
     * Get data(pipelines) for selected DPU.
     * 
     * @param dpu
     * @return data(pipelines) for selected DPU
     */
    public IndexedContainer getTableData(DPUTemplateRecord dpu);

    /**
     * Decides whether to show pipeline delete button in the listing
     * of pipelines using given DPU template.
     * 
     * @param pipelineId
     *            ID of pipeline to be deleted
     * @return true if user has permission to delete given pipeline,
     *         false otherwise
     */
    public boolean showPipelineDeleteButton(long pipelineId);

    /**
     * Decides whether to show pipeline detail button in the listing
     * of pipelines using given DPU template.
     * 
     * @param pipelineId
     *            ID of the pipeline to display detail of
     * @return true if user has permission to view given pipeline,
     *         false otherwise
     */
    public boolean showPipelineDetailButton(long pipelineId);

    /**
     * Decides whether to show pipeline status button in the listing
     * of pipelines using given DPU template.
     * 
     * @param pipelineId
     *            ID of the pipeline to display detail of
     * @return true if user has permission to view executions of given pipeline,
     *         false otherwise
     */
    public boolean showPipelineStatusButton(long pipelineId);

    /**
     * View interface for DPU.
     */
    public interface DPUView {

        /**
         * Generate view, that interact with given presenter.
         * 
         * @param presenter
         * @return generated view
         */
        public Object enter(final DPUPresenter presenter);

        /**
         * Refresh view.
         */
        public void refresh();

        /**
         * Instruct tree component to select provided DPU (old value usually, when cancelling out navigation)
         * 
         * @param oldValue
         *            the DPU instance/template to be selected by tree component
         */
        void treeSetValue(Object oldValue);

        /**
         * Select DPU.
         * 
         * @param dpu
         *            DPU to select.
         */
        public void selectNewDPU(DPUTemplateRecord dpu);

        /**
         * Check if the view has changes.
         * 
         * @return true If the view has changes.
         * @throws DPUWrapException
         *             when configuration entered at the moment is invalid and the comparison for changes cannot be done
         */
        public boolean isChanged() throws DPUWrapException;

        /**
         * Save selected DPU.
         * 
         * @return true when succesful, false otherwise
         */
        public boolean saveDPUTemplate();

        /**
         * Removes pipeline form *DPU instances* table (doesn't delete from db)
         * 
         * @param id
         */
        void removePipelineFromTable(long id);

    }

    /**
     * @return
     */
    public boolean isLayoutInitialized();
}
