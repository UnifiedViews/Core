package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.commons.dao.view.PipelineView;

/**
 * Accessor for {@link Pipeline}s.
 *
 * @author Škoda Petr
 */
public class PipelineViewAccessor implements ClassAccessor<PipelineView> {

    public static final String COLUMN_ID = "id";

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_CREATED_BY = "usrFullName";

    public static final String COLUMN_DURATION = "duration";

    public static final String COLUMN_START = "start";

    public static final String COLUMN_STATUS = "status";

    public static final String COLUMN_ACTOR_NAME = "userActorName";

    @Autowired
    AppConfig appConfig;

    private final List<String> all = Arrays.asList(COLUMN_ID, COLUMN_NAME, COLUMN_CREATED_BY, COLUMN_DURATION,
            COLUMN_START, COLUMN_STATUS, COLUMN_ACTOR_NAME);

    private final List<String> visible = Arrays.asList(COLUMN_NAME, COLUMN_CREATED_BY, COLUMN_START, COLUMN_DURATION, COLUMN_STATUS);

    private final List<String> sortable = Arrays.asList(COLUMN_NAME, COLUMN_CREATED_BY, COLUMN_START, COLUMN_STATUS);

    private final List<String> filterable = Arrays.asList(COLUMN_NAME, COLUMN_CREATED_BY, COLUMN_START, COLUMN_STATUS);

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
    public Class<PipelineView> getEntityClass() {
        return PipelineView.class;
    }

    @Override
    public String getColumnName(String id) {
        switch (id) {
            case COLUMN_ID:
                return Messages.getString("PipelineViewAccessor.id");
            case COLUMN_NAME:
                return Messages.getString("PipelineViewAccessor.name");
            case COLUMN_DURATION:
                return Messages.getString("PipelineViewAccessor.lastRun");
            case COLUMN_START:
                return Messages.getString("PipelineViewAccessor.lastExecution");
            case COLUMN_STATUS:
                return Messages.getString("PipelineViewAccessor.lastStatus");
            case COLUMN_CREATED_BY:
                return Messages.getString("PipelineViewAccessor.createdBy");
            default:
                return id;
        }
    }

    @Override
    public Object getValue(PipelineView pipeline, String id) {
        switch (id) {
            case COLUMN_ID:
                return pipeline.getId();
            case COLUMN_NAME:
                String name = pipeline.getName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            case COLUMN_DURATION:
                return DecorationHelper.formatDuration(pipeline.getDuration());
            case COLUMN_START:
                return pipeline.getStart();
            case COLUMN_STATUS:
                return pipeline.getStatus();
            case COLUMN_CREATED_BY:
                return getPipelineCreatedByDisplayName(pipeline);
            default:
                return null;
        }
    }

    private static String getPipelineCreatedByDisplayName(PipelineView pipeline) {
        String pipelineOwnerName = (pipeline.getUsrFullName() != null && !pipeline.getUsrFullName().equals(""))
                ? pipeline.getUsrFullName() : pipeline.getUsrName();
        if (pipeline.getUserActorName() != null && !pipeline.getUserActorName().equals("")) {
            return pipelineOwnerName + " (" + pipeline.getUserActorName() + ")";
        }
        return pipelineOwnerName;
    }

    @Override
    public Class<?> getType(String id) {
        switch (id) {
            case COLUMN_ID:
                return Integer.class;
            case COLUMN_NAME:
            case COLUMN_DURATION:
                return String.class;
            case COLUMN_START:
                return Timestamp.class;
            case COLUMN_STATUS:
                return PipelineExecutionStatus.class;
            case COLUMN_CREATED_BY:
                return String.class;
            default:
                return null;
        }
    }
}
