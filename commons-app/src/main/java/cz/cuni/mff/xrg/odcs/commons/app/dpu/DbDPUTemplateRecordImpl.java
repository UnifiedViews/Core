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
