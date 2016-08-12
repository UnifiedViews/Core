package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Embedded;

import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.container.DataTimeCache;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Accessor for {@link Pipeline}s.
 * 
 * @author Bogo
 */
public class PipelineAccessor implements ClassAccessor<Pipeline> {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineAccessor.class);

    private final List<String> all = Arrays.asList("id", "name", "duration", "lastExecTime", "lastExecStatus");

    private final List<String> visible = Arrays.asList("name", "duration", "lastExecTime", "lastExecStatus");

    private final List<String> sortable = Arrays.asList("name");

    private final List<String> filterable = Arrays.asList("name");

    private final List<String> toFetch = new LinkedList<>();

    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * Cache for last pipeline execution, so we do not load from DB every time
     * table cell with duration, start, ..., is needed.
     */
    private DataTimeCache<PipelineExecution> execCache = new DataTimeCache<>();

    @Override
    public List<String> all() {
        return all;
    }

    @Override
    public List<String> sortable() {
        return sortable;
    }

    @Override
    public List<String> filterable() {
        return filterable;
    }

    @Override
    public List<String> visible() {
        return visible;
    }

    @Override
    public List<String> toFetch() {
        return toFetch;
    }

    @Override
    public Class<Pipeline> getEntityClass() {
        return Pipeline.class;
    }

    @Override
    public String getColumnName(String id) {
        switch (id) {
            case "id":
                return Messages.getString("PipelineAccessor.id");
            case "name":
                return Messages.getString("PipelineAccessor.name");
            case "duration":
                return Messages.getString("PipelineAccessor.lastRun");
            case "lastExecTime":
                return Messages.getString("PipelineAccessor.lastExecution");
            case "lastExecStatus":
                return Messages.getString("PipelineAccessor.lastStatus");
            default:
                return id;
        }
    }

    @Override
    public Object getValue(Pipeline object, String id) {
        switch (id) {
            case "id":
                return object.getId();
            case "name":
                String name = object.getName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            case "duration":
                PipelineExecution latestExec = pipelineFacade.getLastExec(object, PipelineExecutionStatus.FINISHED);
                return DecorationHelper.getDuration(latestExec);
            case "lastExecTime":
                return getLastExecutionTime(object);
            case "lastExecStatus":
                return getLastExecutionStatus(object);
            default:
                return null;
        }
    }

    @Override
    public Class<?> getType(String id) {
        switch (id) {
            case "id":
                return Integer.class;
            case "name":
            case "duration":
            case "lastExecTime":
                return String.class;
            case "lastExecStatus":
                return Embedded.class;
            default:
                return null;
        }
    }

    /**
     * Clears the pipeline cache.
     */
    public void clearExecCache() {
        execCache.invalidate();
    }

    /**
     * Get last pipeline execution from cache. If execution is not found in
     * cache, it is loaded from DB and cached.
     * 
     * @param ppl
     *            pipeline
     * @return last execution for given pipeline
     */
    private PipelineExecution getLastExecution(Pipeline ppl) {
        PipelineExecution exec = execCache.get(ppl.getId());
        if (exec == null) {
            exec = pipelineFacade.getLastExec(ppl);
            execCache.set(ppl.getId(), exec);
        }
        return exec;
    }

    Object getLastExecutionTime(Pipeline ppl) {
        PipelineExecution latestExec = getLastExecution(ppl);
        if (latestExec != null) {
            if (latestExec.getStart() == null) {
                // no start time for last execution
                LOG.warn("The start time for execuiton id: {} is null", latestExec.getId());
                return null;
            }
            SimpleDateFormat df = new SimpleDateFormat("d.M.yyyy H:mm:ss");
            //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
            return df.format(latestExec.getStart());
        } else {
            return null;
        }
    }

    Object getLastExecutionStatus(Pipeline ppl) {
        PipelineExecution latestExec = getLastExecution(ppl);
        if (latestExec != null) {
            PipelineExecutionStatus type = latestExec.getStatus();
            if (type != null) {
                ThemeResource img = DecorationHelper.getIconForExecutionStatus(type);
                Embedded emb = new Embedded(type.name(), img);
                emb.setDescription(type.name());
                return emb;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }
}
