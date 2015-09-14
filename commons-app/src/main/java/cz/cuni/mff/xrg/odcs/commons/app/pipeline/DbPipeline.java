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

    /**
     * Fetches all pipelines for given user (user is owner of the pipelines)
     * 
     * @param externalUserId
     *            User ID
     * @return List of pipelines
     */
    public List<Pipeline> getPipelinesForUser(String externalUserId);

    
    /**
     * Fetches all pipelines that are visible to user, so:
     * <br/>
     * - he is owner that pipeline
     * - pipelines that are not private (owner is someone else) 
     * 
     * @param userExternalId
     * @return
     */
    public List<Pipeline> getAllVisiblePipelines(String userExternalId);

}
