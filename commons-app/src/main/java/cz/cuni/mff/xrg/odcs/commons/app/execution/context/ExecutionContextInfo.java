package cz.cuni.mff.xrg.odcs.commons.app.execution.context;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Hold and manage context for pipeline execution.
 * Complete read write interface for execution context. Enable writing data into
 * context and asking for directories. Provide methods for creating file names
 * for DataUnits.
 * The directory structure used by context is following;
 * ./working/DPU_ID/DATAUNIT_INDEX/ - DataUnit working directory
 * ./working/DPU_ID/tmp/ - DPU working directory
 * ./storage/DPU_ID/DATAUNIT_INDEX/ - storage for DataUnit results ./result/ -
 * place for DPU's files that should be accessible to the user
 * 
 * @author Petyr
 */
@Entity
@Table(name = "exec_context_pipeline")
public class ExecutionContextInfo implements DataObject {

    /**
     * Name of working sub directory.
     */
    @Deprecated
    private static final String WORKING_DIR = "working";

    /**
     * Name of DPU tmp directory.
     */
    @Deprecated
    private static final String WORKING_TMP_DIR = "tmp";

    /**
     * Name of storage directory in which the DataUnits are save into.
     */
    @Deprecated
    private static final String STORAGE_DIR = "storage";

    /**
     * Directory for results.
     */
    @Deprecated
    private static final String RESULT_DIR = "result";

    /**
     * Prefix for DPU folder.
     */
    @Deprecated
    private static final String DPU_ID_PREFIX = "dpu_";

    /**
     * Unique id of pipeline execution.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_exec_context_pipeline")
    @SequenceGenerator(name = "seq_exec_context_pipeline", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Dummy column, should be removed in the future. Without this dummy attribute,
     * the list of attributes in this table is empty which causes issues:
     * "Exception Description: The list of fields to insert into the table [DatabaseTable(exec_context_pipeline)] is empty.
     * You must define at least one mapping for this table."
     * In the future, this table should be removed completely.
     */
    @SuppressWarnings("unused")
    @Column(name = "dummy")
    private Boolean dummy = false;

    /**
     * Id of respective execution. Used to create relative path to the context
     * directory.
     */
    @OneToOne(mappedBy = "context", fetch = FetchType.LAZY)
    private PipelineExecution execution;

    /**
     * Contexts for DPU's. Indexed by {@link DPUInstanceRecord}.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @MapKeyJoinColumn(name = "dpu_instance_id", referencedColumnName = "id")
    @JoinColumn(name = "exec_context_pipeline_id")
    private Map<DPUInstanceRecord, ProcessingUnitInfo> contexts;

    @PreRemove
    public void preRemove() {
        execution.setContext(null);
    }

    /**
     * Empty constructor for JPA.
     */
    public ExecutionContextInfo() {
        contexts = new HashMap<>();
    }

    /**
     * Create info for given execution.
     * 
     * @param execution
     */
    public ExecutionContextInfo(PipelineExecution execution) {
        this.contexts = new HashMap<>();
        this.execution = execution;
    }

    /**
     * Return context for given DPUInstanceRecord. Create new context if need.
     * 
     * @param id
     *            DPUInstanceRecord's id.
     * @return DataProcessingUnitInfo
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    private ProcessingUnitInfo getContext(DPUInstanceRecord dpuInstance) {
        // check existence
        if (!contexts.containsKey(dpuInstance)) {
            // unknown context -> add
            ProcessingUnitInfo pui = new ProcessingUnitInfo();
            contexts.put(dpuInstance, pui);
        }
        // return data
        return contexts.get(dpuInstance);
    }

    /**
     * Add info record for new input {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param dpuInstance
     *            The {@link DPUInstanceRecord} which will work with the
     *            DataUnit.
     * @param name
     *            Name of data unit.
     * @param type
     *            {@link DataUnit.Type Type} of data unit.
     * @return Index of new DataUnitInfo.
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public Integer createInput(DPUInstanceRecord dpuInstance,
            String name,
            ManagableDataUnit.Type type) {
        return getContext(dpuInstance).addDataUnit(name, type, true);
    }

    /**
     * Add info record for new output {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}.
     * 
     * @param dpuInstance
     *            The {@link DPUInstanceRecord} which will work with the
     *            DataUnit.
     * @param name
     *            Name of data unit.
     * @param type
     *            {@link DataUnit.Type Type} of data unit.
     * @return Index of new DataUnitInfo.
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public Integer createOutput(DPUInstanceRecord dpuInstance,
            String name,
            ManagableDataUnit.Type type) {
        return getContext(dpuInstance).addDataUnit(name, type, false);
    }

    /**
     * Delete all data about execution except {@link #id} Use to start execution
     * from the very beginning.
     * 
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public void reset() {
        contexts.clear();
    }

    /**
     * Generate unique id for given DataUnit. If call multiple times for the
     * same dpuInstance and DataUnit's index it return the same id. The id has
     * following format: exec_{exec_id}_dpu_{dpu_id}_du_{du_id}.
     * 
     * @param dpuInstance
     *            Owner of the DataUnit.
     * @param index
     *            DataUnit's index assigned to the DataUnit by context.
     * @return Unique id.
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public String generateDataUnitId(DPUInstanceRecord dpuInstance,
            Integer index) {
        return "exec_" + execution.getId().toString() + "_dpu_"
                + dpuInstance.getId().toString() + "_du_" + index.toString();
    }

    public String generatePipelineId() {
        return "exec_" + String.valueOf(execution.getId());
    }

    public Long getExecutionId() {
        return execution.getId();
    }

    /**
     * Return context information class {@link ProcessingUnitInfo} for given
     * DPU. If the context does not exist, then create new.
     * 
     * @param dpuInstance
     *            Instance of DPU for which retrieve context info.
     * @return {@link ProcessingUnitInfo}
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public ProcessingUnitInfo createDPUInfo(DPUInstanceRecord dpuInstance) {
        return getContext(dpuInstance);
    }

    /**
     * @return set of indexes of stored DPU's execution information.
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public Set<DPUInstanceRecord> getDPUIndexes() {
        return contexts.keySet();
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Return context information class {@link ProcessingUnitInfo} for given
     * DPU.
     * 
     * @param dpuInstance
     *            Instance of DPU for which retrieve context info.
     * @return {@link ProcessingUnitInfo} or null if no records for given
     *         dpuInstance exist.
     * @deprecated use {@link ExecutionInfo} instead
     */
    @Deprecated
    public ProcessingUnitInfo getDPUInfo(DPUInstanceRecord dpuInstance) {
        if (contexts.containsKey(dpuInstance)) {
            return contexts.get(dpuInstance);
        } else {
            return null;
        }
    }

    /**
     * @return respective pipeline execution.
     */
    public PipelineExecution getExecution() {
        return this.execution;
    }

    Map<DPUInstanceRecord, ProcessingUnitInfo> getContexts() {
        return this.contexts;
    }

    /**
     * Returns true if two objects represent the same pipeline. This holds if
     * and only if <code>this.id == null ? this == obj : this.id == o.id</code>.
     * 
     * @param obj
     * @return true if both objects represent the same pipeline
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final DataObject other = (DataObject) obj;
        if (this.getId() == null) {
            return super.equals(other);
        }

        return Objects.equals(this.getId(), other.getId());
    }

    /**
     * Hashcode is compatible with {@link #equals(java.lang.Object)}.
     * 
     * @return The value of hash code.
     */
    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return super.hashCode();
        }
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public String toString() {
        return "ExecutioContextInfo{" +
                "id=" + id +
                '}';
    }

}
