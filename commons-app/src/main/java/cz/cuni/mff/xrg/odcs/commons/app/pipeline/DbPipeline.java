package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * Interface for access to {@link Pipeline}s.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
public interface DbPipeline extends DbAccess<Pipeline> {

    /**
     * @return List of all pipelines in DB.
     * @deprecated performance intensive for many pipelines
     */
    @Deprecated
    public List<Pipeline> getAll();

    /**
     * @return List of all pipelines in DB.
     * @deprecated performance intensive for many pipelines
     */
    @Deprecated
    public List<Pipeline> getPipelinesForOrganization(Long organizationExternalId);

    /**
     * Fetches all pipelines using given DPU template.
     * 
     * @param dpu
     *            DPU template.
     * @return List of pipelines, that's contains instance of DPU of given
     *         template.
     */
    public List<Pipeline> getPipelinesUsingDPU(DPUTemplateRecord dpu);

    /**
     * @param name
     *            pipeline name
     * @return pipeline with given name or null
     */
    public Pipeline getPipelineByName(String name);

    /**
     * Tells whether there were any changes to pipeline since the
     * last load.
     * <p>
     * 
     * @param since
     * @return whether any pipeline was created / updated since given date
     */
    public boolean hasModified(Date since);

    /**
     * Tells whether one of pipelines was deleted
     * <p>
     * 
     * @param pipelinesIds
     * @return true if one or more pipelines with provided ids were deleted, otherwise false
     */
    public boolean hasDeletedPipelines(List<Long> pipelinesIds);
}
