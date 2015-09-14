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

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Implementation for accessing {@link DPUTemplateRecord} data objects.
 * 
 * @author Jan Vojt
 * @author petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
public class DbDPUTemplateRecordImpl extends DbAccessBase<DPUTemplateRecord>
        implements DbDPUTemplateRecord {

    public DbDPUTemplateRecordImpl() {
        super(DPUTemplateRecord.class);
    }

    @Override
    public List<DPUTemplateRecord> getAll() {
        final String queryStr = "SELECT e FROM DPUTemplateRecord e";
        return executeList(queryStr);
    }

    @Override
    public List<DPUTemplateRecord> getAllVisible(User user) {
        final String queryStr = "SELECT e FROM DPUTemplateRecord e where (e.shareType != :sharetype) or (e.owner = :user and e.shareType = :sharetype)";

        TypedQuery<DPUTemplateRecord> query = createTypedQuery(queryStr);
        query.setParameter("sharetype", ShareType.PRIVATE);
        query.setParameter("user", user);

        return executeList(query);
    }

    @Override
    public DPUTemplateRecord getByDirectory(String directory) {
        final String stringQuery = "SELECT e FROM DPUTemplateRecord e"
                + " WHERE e.jarDirectory = :directory";

        TypedQuery<DPUTemplateRecord> query = createTypedQuery(stringQuery);
        query.setParameter("directory", directory);

        return execute(query);
    }
    
    @Override
    public DPUTemplateRecord getByDirectoryAndName(String jarDirectory, String name) {
        final String stringQuery = "SELECT e FROM DPUTemplateRecord e"
                + " LEFT JOIN DPUTemplateRecord p ON e.parent = p"
                + " WHERE ( e.jarDirectory = :directory OR p.jarDirectory = :directory)"
                + " AND e.name = :name";
        
        TypedQuery<DPUTemplateRecord> query = em.createQuery(stringQuery, DPUTemplateRecord.class);
        query.setParameter("directory", jarDirectory);
        query.setParameter("name", name);

        return execute(query);
    }

    @Override
    public List<DPUTemplateRecord> getChilds(DPUTemplateRecord parentDpu) {
        final String stringQuery = "SELECT e FROM DPUTemplateRecord e"
                + " WHERE e.parent = :tmpl";

        TypedQuery<DPUTemplateRecord> query = createTypedQuery(stringQuery);
        query.setParameter("tmpl", parentDpu);

        return executeList(query);
    }

    @Override
    public DPUTemplateRecord getByJarName(String jarName) {
        final String stringQuery = "SELECT e FROM DPUTemplateRecord e"
                + " WHERE e.jarName = :jarName";

        TypedQuery<DPUTemplateRecord> query = createTypedQuery(stringQuery);
        query.setParameter("jarName", jarName);

        return execute(query);

    }

    @Override
    public DPUTemplateRecord getByName(String name) {
        final String stringQuery = "SELECT e FROM DPUTemplateRecord e"
                + " WHERE e.name = :name";

        TypedQuery<DPUTemplateRecord> query = createTypedQuery(stringQuery);
        query.setParameter("name", name);

        return execute(query);

    }

}
