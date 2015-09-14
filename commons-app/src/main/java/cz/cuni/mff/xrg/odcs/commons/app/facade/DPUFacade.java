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
package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUType;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Facade for working with DPUs.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
public interface DPUFacade extends Facade {

    /* ******************* Methods for DPUTemplateRecord management *********************** */

    /**
     * Creates DPU template with given name of given type and assigns currently
     * logged in user as owner.
     * 
     * @param name
     * @param type
     * @return newly created DPU template
     */
    DPUTemplateRecord createTemplate(String name, DPUType type);

    /**
     * Create copy of DPU template, as the owner the current user is set.
     * 
     * @param original
     *            DPU template
     * @return newly created copy of DPU template
     */
    DPUTemplateRecord createCopy(DPUTemplateRecord original);

    /**
     * Creates a new DPU template with the same properties and configuration as
     * in given DPU instance. Note that newly created DPU template is only
     * returned, but not managed by database. To persist it, {@link #save(cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord) save} must be called
     * explicitly.
     * 
     * @param instance
     * @return new DPURecord
     */
    DPUTemplateRecord createTemplateFromInstance(DPUInstanceRecord instance);

    /**
     * Returns list of all {@link DPUTemplateRecord}s currently persisted in
     * database.
     * 
     * @return list of DPU templates
     */
    List<DPUTemplateRecord> getAllTemplates();

    /**
     * Find DPUTemplateRecord in database by ID and return it.
     * 
     * @param id
     * @return fetched DPU template or null
     */
    DPUTemplateRecord getTemplate(long id);

    /**
     * Saves any modifications made to the DPUTemplateRecord into the database.
     * 
     * @param dpu
     */
    void save(DPUTemplateRecord dpu);

    /**
     * Deletes DPUTemplateRecord from the database.
     * 
     * @param dpu
     */
    void delete(DPUTemplateRecord dpu);

    /**
     * Fetch all child DPU templates for a given DPU template.
     * 
     * @param parent
     *            DPU template
     * @return list of child DPU templates or empty collection
     */
    List<DPUTemplateRecord> getChildDPUs(DPUTemplateRecord parent);

    DPUTemplateRecord getByDirectory(String jarDirectory);
    
    DPUTemplateRecord getByJarName(String jarName);

    DPUTemplateRecord getByName(String name);

    /**
     * Fetch DPU template using jarDirectory and DPU name.
     * Good to use when searching for DPU of the same jar file without version restriction.
     * 
     * @param jarDirectory
     * @param name
     * @return
     */
    DPUTemplateRecord getByDirectoryAndName(String jarDirectory, String name);


    /* **************** Methods for DPUInstanceRecord Instance management ***************** */

    /**
     * Creates DPUInstanceRecord with configuration copied from template without
     * persisting it.
     * 
     * @param dpuTemplate
     *            to create from
     * @return newly created DPU instance
     */
    DPUInstanceRecord createInstanceFromTemplate(DPUTemplateRecord dpuTemplate);

    /**
     * Returns list of all DPUInstanceRecord currently persisted in database.
     * 
     * @return DPUInstance list
     */
    List<DPUInstanceRecord> getAllDPUInstances();

    /**
     * Find DPUInstanceRecord in database by ID and return it.
     * 
     * @param id
     * @return DPU instance
     */
    DPUInstanceRecord getDPUInstance(long id);

    /**
     * Saves any modifications made to the DPUInstanceRecord into the database.
     * 
     * @param dpu
     */
    void save(DPUInstanceRecord dpu);

    /**
     * Deletes DPUInstance from the database.
     * 
     * @param dpu
     */
    void delete(DPUInstanceRecord dpu);

    /* **************** Methods for Record (messages) management ***************** */

    /**
     * Fetches all DPURecords emitted by given PipelineExecution.
     * 
     * @param pipelineExec
     * @return all DPURecords emitted by given PipelineExecution.
     */
    List<MessageRecord> getAllDPURecords(PipelineExecution pipelineExec);

    /**
     * Find Record in database by ID and return it.
     * 
     * @param id
     * @return DPU record
     */
    MessageRecord getDPURecord(long id);

    /**
     * Saves any modifications made to the Record into the database.
     * 
     * @param record
     */
    void save(MessageRecord record);

    /**
     * Deletes Record from the database.
     * 
     * @param record
     */
    void delete(MessageRecord record);

}
