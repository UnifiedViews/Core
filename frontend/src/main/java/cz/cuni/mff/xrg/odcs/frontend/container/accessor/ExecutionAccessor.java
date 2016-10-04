package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Accessor for {@link PipelineExecution}s.
 * 
 * @author Bogo
 */
public class ExecutionAccessor implements ClassAccessor<PipelineExecution> {

    private final List<String> all = Arrays.asList("id", "start", "pipeline.name", "duration", "status", "isDebugging", "schedule", "pipeline.id", "owner.username");

    private final List<String> visible = Arrays.asList("status", "pipeline.name", "start", "duration", "isDebugging", "schedule", "owner.username");

    private final List<String> sortable = Arrays.asList("pipeline.name", "status", "start", "isDebugging", "schedule", "owner.username");

    private final List<String> filterable = Arrays.asList("pipeline.name", "status", "start", "isDebugging", "schedule", "owner.username");

    private final List<String> toFetch = Arrays.asList("pipeline", "owner");

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
    public Class<PipelineExecution> getEntityClass() {
        return PipelineExecution.class;
    }

    @Override
    public String getColumnName(String id) {
        switch (id) {
            case "id":
                return Messages.getString("ExecutionAccessor.id");
            case "start":
                return Messages.getString("ExecutionAccessor.started");
            case "pipeline.name":
                return Messages.getString("ExecutionAccessor.pipeline");
            case "duration":
                return Messages.getString("ExecutionAccessor.duration");
            case "owner.username":
                return Messages.getString("ExecutionAccessor.owner");
            case "status":
                return Messages.getString("ExecutionAccessor.status");
            case "isDebugging":
                return Messages.getString("ExecutionAccessor.isDebugging");
            case "lastChange":
                return Messages.getString("ExecutionAccessor.lastChange");
            case "schedule":
                return Messages.getString("ExecutionAccessor.schedule");
            default:
                return null;
        }
    }

    @Override
    public Object getValue(PipelineExecution object, String id) {
        switch (id) {
            case "id":
                return object.getId();
            case "start":
                return object.getStart();
            case "pipeline.id":
                return object.getPipeline().getId();
            case "pipeline.name":
                String name = object.getPipeline().getName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            case "owner.username":
                return object.getOwner().getUsername();
            case "duration":
                return object.getDuration();
            case "status":
                PipelineExecutionStatus status = object.getStatus();
                return object.getStop() && status == PipelineExecutionStatus.RUNNING ? PipelineExecutionStatus.CANCELLING : status;
            case "isDebugging":
                return object.isDebugging();
            case "lastChange":
                return object.getLastChange();
            case "schedule":
                return object.getSchedule() != null;
            default:
                return null;
        }
    }

    @Override
    public Class<?> getType(String id) {
        switch (id) {
            case "id":
            case "pipeline.id":
                return Long.class;
            case "start":
                return Timestamp.class;
            case "pipeline.name":
                return String.class;
            case "duration":
                return Long.class;
            case "status":
                return PipelineExecutionStatus.class;
            case "isDebugging":
                return Boolean.class;
            case "lastChange":
                return Timestamp.class;
            case "schedule":
                return Boolean.class;
            case "owner.username":
                return String.class;
            default:
                return null;
        }
    }
}
