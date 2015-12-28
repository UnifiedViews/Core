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
package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.commons.dao.view.ExecutionView;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Accessor for {@link Pipeline}s.
 * 
 * @author Škoda Petr
 */
public class ExecutionViewAccessor implements ClassAccessor<ExecutionView> {

    public static final String COLUMN_ID = "id";

    public static final String COLUMN_START = "start";

    public static final String COLUMN_PIPELINE_NAME = "pipelineName";

    public static final String COLUMN_DURATION = "duration";

    public static final String COLUMN_STATUS = "status";

    public static final String COLUMN_DEBUGGING = "isDebugging";

    public static final String COLUMN_SCHEDULE = "schedule";

    public static final String COLUMN_PIPELINE_ID = "pipelineId";

    public static final String COLUMN_EXECUTED_BY = "ownerFullName";

    public static final String COLUMN_ACTOR_NAME = "userActorName";

    private final List<String> all = Arrays.asList(COLUMN_ID, COLUMN_START, COLUMN_PIPELINE_NAME, COLUMN_DURATION,
            COLUMN_STATUS, COLUMN_DEBUGGING, COLUMN_SCHEDULE, COLUMN_PIPELINE_ID, COLUMN_EXECUTED_BY, COLUMN_ACTOR_NAME);

    private final List<String> visible = Arrays.asList(COLUMN_STATUS, COLUMN_PIPELINE_NAME, COLUMN_START, COLUMN_DURATION,
            COLUMN_DEBUGGING, COLUMN_SCHEDULE, COLUMN_EXECUTED_BY);

    private final List<String> sortable = Arrays.asList(COLUMN_PIPELINE_NAME, COLUMN_STATUS, COLUMN_START, COLUMN_DEBUGGING,
            COLUMN_SCHEDULE, COLUMN_EXECUTED_BY);

    private final List<String> filterable = Arrays.asList(COLUMN_PIPELINE_NAME, COLUMN_STATUS, COLUMN_START, COLUMN_DEBUGGING,
            COLUMN_SCHEDULE, COLUMN_EXECUTED_BY);

    private final List<String> toFetch = new LinkedList<>();

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
    public Class<ExecutionView> getEntityClass() {
        return ExecutionView.class;
    }

    @Override
    public String getColumnName(String id) {
        switch (id) {
            case COLUMN_ID:
                return Messages.getString("ExecutionViewAccessor.id");
            case COLUMN_START:
                return Messages.getString("ExecutionViewAccessor.started");
            case COLUMN_PIPELINE_NAME:
                return Messages.getString("ExecutionViewAccessor.pipeline");
            case COLUMN_DURATION:
                return Messages.getString("ExecutionViewAccessor.duration");
            case COLUMN_EXECUTED_BY:
                return Messages.getString("ExecutionViewAccessor.owner");
            case COLUMN_STATUS:
                return Messages.getString("ExecutionViewAccessor.status");
            case COLUMN_DEBUGGING:
                return Messages.getString("ExecutionViewAccessor.isDebugging");
            case "lastChange":
                return Messages.getString("ExecutionViewAccessor.lastChange");
            case COLUMN_SCHEDULE:
                return Messages.getString("ExecutionViewAccessor.schedule");
            default:
                return null;
        }
    }

    @Override
    public Object getValue(ExecutionView execution, String id) {
        switch (id) {
            case COLUMN_ID:
                return execution.getId();
            case COLUMN_START:
                return execution.getStart();
            case COLUMN_PIPELINE_ID:
                return execution.getPipelineId();
            case COLUMN_PIPELINE_NAME:
                String name = execution.getPipelineName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            case COLUMN_EXECUTED_BY:
                return getPipelineCreatedByDisplayName(execution);
            case COLUMN_DURATION:
                return execution.getDuration();
            case COLUMN_STATUS:
                PipelineExecutionStatus status = execution.getStatus();
                return execution.isStop() && status == PipelineExecutionStatus.RUNNING ? PipelineExecutionStatus.CANCELLING : status;
            case COLUMN_DEBUGGING:
                return execution.isDebugging();
            case "lastChange":
                return execution.getLastChange();
            case COLUMN_SCHEDULE:
                return execution.getScheduleId() != null;
            default:
                return null;
        }
    }

    private static String getPipelineCreatedByDisplayName(ExecutionView execution) {
        String executionOwnerName = (execution.getOwnerFullName() != null && !execution.getOwnerFullName().equals(""))
                ? execution.getOwnerFullName() : execution.getOwnerName();
        if (execution.getUserActorName() != null && !execution.getUserActorName().equals("")) {
            return executionOwnerName + " (" + execution.getUserActorName() + ")";
        }
        return executionOwnerName;
    }

    @Override
    public Class<?> getType(String id) {
        switch (id) {
            case COLUMN_ID:
            case COLUMN_PIPELINE_ID:
                return Long.class;
            case COLUMN_START:
                return Timestamp.class;
            case COLUMN_PIPELINE_NAME:
                return String.class;
            case COLUMN_DURATION:
                return Long.class;
            case COLUMN_STATUS:
                return PipelineExecutionStatus.class;
            case COLUMN_DEBUGGING:
                return Boolean.class;
            case "lastChange":
                return Timestamp.class;
            case COLUMN_SCHEDULE:
                return Boolean.class;
            case COLUMN_EXECUTED_BY:
                return String.class;
            default:
                return null;
        }
    }

}
