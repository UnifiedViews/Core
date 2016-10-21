package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import com.vaadin.ui.CustomComponent;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionInfo;

/**
 * @author Bogo
 */
public abstract class QueryView extends CustomComponent {

    private DataUnitInfo dataUnitInfo;

    private ExecutionInfo executionInfo;

    private DPUInstanceRecord selectedDpu;

    /**
     * Browse data unit.
     */
    public abstract void browseDataUnit();

    /**
     * Set querying enabled.
     * 
     * @param enabled
     */
    public abstract void setQueryingEnabled(boolean enabled);

    /**
     * Reset view.
     */
    public abstract void reset();

    /**
     * Set execution info.
     * 
     * @param executionInfo
     */
    public void setExecutionInfo(ExecutionInfo executionInfo) {
        this.executionInfo = executionInfo;
    }

    /**
     * Set data unit info.
     * 
     * @param duInfo
     */
    public void setDataUnitInfo(DataUnitInfo duInfo) {
        dataUnitInfo = duInfo;
    }

    /**
     * Set selected DPU.
     * 
     * @param dpu
     */
    public void setSelectedDpu(DPUInstanceRecord dpu) {
        selectedDpu = dpu;
    }

    /**
     * Get execution info.
     * 
     * @return execution info
     */
    protected ExecutionInfo getExecutionInfo() {
        return executionInfo;
    }

    /**
     * Get data unit info.
     * 
     * @return data unit info
     */
    protected DataUnitInfo getDataUnitInfo() {
        return dataUnitInfo;
    }

    /**
     * Get selected DPU.
     * 
     * @return selected DPU
     */
    protected DPUInstanceRecord getSelectedDpu() {
        return selectedDpu;
    }
}
