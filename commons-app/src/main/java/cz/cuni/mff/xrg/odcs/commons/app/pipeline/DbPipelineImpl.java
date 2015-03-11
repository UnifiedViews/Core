package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * Implementation for accessing {@link Pipeline} data objects.
 * 
 * @author Petyr
 * @author Jan Vojt
 * @author Martin Virag
 */
@Transactional(propagation = Propagation.MANDATORY)
class DbPipelineImpl extends DbAccessBase<Pipeline> implements DbPipeline {

    protected DbPipelineImpl() {
        super(Pipeline.class);
    }

    @Override
    public List<Pipeline> getAll() {
        final String queryStr = "SELECT e FROM Pipeline e";
        return executeList(queryStr);
    }

    @Override
    public List<Pipeline> getPipelinesForOrganization(Long organizationExternalId) {
        final String queryStr = "SELECT e FROM Pipeline e WHERE e.organization.id = :organizationExternalId";
        TypedQuery<Pipeline> query = createTypedQuery(queryStr);
        query.setParameter("organizationExternalId", organizationExternalId);
        return executeList(query);
    }

    @Override
    public List<Pipeline> getPipelinesUsingDPU(DPUTemplateRecord dpu) {
        final String stringQuery = "SELECT e FROM Pipeline e"
                + " LEFT JOIN e.graph g"
                + " LEFT JOIN g.nodes n"
                + " LEFT JOIN n.dpuInstance i"
                + " LEFT JOIN i.template t"
                + " WHERE t.id = :dpuid";
        TypedQuery<Pipeline> query = createTypedQuery(stringQuery);
        query.setParameter("dpuid", dpu.getId());
        return executeList(query);
    }

    @Override
    public Pipeline getPipelineByName(String name) {
        final String stringQuery = "SELECT e FROM Pipeline e"
                + " WHERE e.name = :name";
        TypedQuery<Pipeline> query = createTypedQuery(stringQuery);
        query.setParameter("name", name);
        return execute(query);
    }

    @Override
    public boolean hasModified(Date since) {
        final String stringQuery = "SELECT MAX(e.lastChange)"
                + " FROM Pipeline e";

        TypedQuery<Date> query = em.createQuery(stringQuery, Date.class);
        Date lastModified = (Date) query.getSingleResult();

        if (lastModified == null) {
            // there are no executions in DB
            return false;
        }

        return lastModified.after(since);
    }

    @Override
    public boolean hasDeletedPipelines(List<Long> pipelinesIds) {
        if (pipelinesIds == null || pipelinesIds.isEmpty()) {
            return false;
        }
        final String stringQuery = "SELECT COUNT(e) FROM Pipeline e"
                + " WHERE e.id IN :ids";
        TypedQuery<Long> query = createCountTypedQuery(stringQuery);
        query.setParameter("ids", pipelinesIds);
        Long number = (Long) query.getSingleResult();
        return !number.equals((long) pipelinesIds.size());
    }
}
