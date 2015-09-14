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
package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Interface providing access to {@link DPUTemplateRecord} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbDPUTemplateRecord extends DbAccess<DPUTemplateRecord> {

    /**
     * @return DPURecord list of all DPUTemplateRecords currently persisted in
     *         database.
     */
    public List<DPUTemplateRecord> getAll();

    /**
     * @return DPURecord list of all DPUTemplateRecords currently persisted in
     *         database.
     */
    public List<DPUTemplateRecord> getAllVisible(User user);

    
    /**
     * Fetch DPU template using given DPU directory.
     * 
     * @param directory
     *            name where JAR file is located
     * @return DPU template
     */
    public DPUTemplateRecord getByDirectory(String directory);

    
    /**
     * Fetch DPU template using given DPU jar.
     * 
     * @param jarName
     *            name of dpu's jar
     * @return DPU template
     */
    public DPUTemplateRecord getByJarName(String jarName);

    /**
     * Fetch DPU template using given DPU name.
     *
     * @param name
     *            name of dpu's jar
     * @return DPU template
     */
    public DPUTemplateRecord getByName(String name);
    
    /**
     * Fetch DPU template using jarDirectory and DPU name.
     * Good to use when searching for DPU of the same jar file without version restriction.
     * 
     * @param jarDirectory
     * @param name
     * @return
     */
    public DPUTemplateRecord getByDirectoryAndName(String jarDirectory, String name);
    
    /**
     * Fetch all child DPU templates for a given DPU template.
     * 
     * @param parentDpu
     *            DPU template
     * @return list of child DPU templates or empty collection
     */
    public List<DPUTemplateRecord> getChilds(DPUTemplateRecord parentDpu);

}
