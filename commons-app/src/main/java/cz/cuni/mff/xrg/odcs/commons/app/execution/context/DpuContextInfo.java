package cz.cuni.mff.xrg.odcs.commons.app.execution.context;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;

/**
 * Class used to work with context for single DPU, or global context if no DPU
 * is set.
 * 
 * @author Petyr
 */
public class DpuContextInfo {

    /**
     * Binded context.
     */
    private final ExecutionContextInfo executionContext;

    /**
     * Binded instance.
     */
    private final DPUInstanceRecord dpuInstance;

    /**
     * Binded dpu context.
     */
    private final ProcessingUnitInfo dpuInfo;

    public DpuContextInfo(ExecutionContextInfo executionContext, DPUInstanceRecord dpuInstance) {
        this.executionContext = executionContext;
        this.dpuInstance = dpuInstance;
        this.dpuInfo = executionContext.getContexts().get(dpuInstance);
    }

    /**
     * Add info record for new input {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param name
     *            Input name.
     * @param type
     *            DataUnit type.
     * @return Index of new DataUnitInfo.
     */
    public Integer createInput(String name, ManagableDataUnit.Type type) {
        return addDataUnit(name, type, true);
    }

    /**
     * Add info record for new output {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param name
     *            Output name.
     * @param type
     *            DataUnit type.
     * @return Index of new DataUnitInfo.
     */
    public Integer createOutput(String name, ManagableDataUnit.Type type) {
        return addDataUnit(name, type, false);
    }

    /**
     * Add info record for {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param name
     *            Name of DataUnit.
     * @param type
     *            Type of DataUnit.
     * @param isInput
     *            True in case of input DataUnit.
     * @return Index of new DataUnitInfo.
     */
    private Integer addDataUnit(String name, ManagableDataUnit.Type type, boolean isInput) {
        // add information
        Integer index = 0;
        if (dpuInfo.dataUnits.isEmpty()) {
        } else {
            index = dpuInfo.dataUnits.get(dpuInfo.dataUnits.size() - 1).getIndex() + 1;
        }
        DataUnitInfo dataUnitInfo = new DataUnitInfo(index, name, type, isInput);
        dpuInfo.dataUnits.add(dataUnitInfo);
        return index;
    }

    /**
     * @return list of stored {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}s.
     */
    public List<DataUnitInfo> getDataUnits() {
        return dpuInfo.dataUnits;
    }

    /**
     * Return id for {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit} of given
     * index. Does not control existence of {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param index
     *            DataUnit's index.
     * @return id for {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit} of
     *         given index
     */
    public String createId(Integer index) {
        return "exec_" + executionContext.getExecution().getId().toString() + "_dpu_" + dpuInstance.getId().toString() + "_du_" + index.toString();
    }

}
