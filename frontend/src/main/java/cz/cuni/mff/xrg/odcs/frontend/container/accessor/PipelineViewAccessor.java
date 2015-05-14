package cz.cuni.mff.xrg.odcs.frontend.container.accessor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Embedded;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
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

    private static final String SHARE_MODE_ORG = "organization";
    
    @Autowired
    AppConfig appConfig;

    private final List<String> all = Arrays.asList("id", "name", "createdBy", "duration", "lastExecTime", "lastExecStatus");

    private final List<String> visible = Arrays.asList("name", "createdBy", "duration", "lastExecTime", "lastExecStatus");

    private final List<String> sortable = Arrays.asList("name", "createdBy");

    private final List<String> filterable = Arrays.asList("name", "createdBy");

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
            case "id":
                return Messages.getString("PipelineViewAccessor.id");
            case "name":
                return Messages.getString("PipelineViewAccessor.name");
            case "duration":
                return Messages.getString("PipelineViewAccessor.lastRun");
            case "lastExecTime":
                return Messages.getString("PipelineViewAccessor.lastExecution");
            case "lastExecStatus":
                return Messages.getString("PipelineViewAccessor.lastStatus");
            case "createdBy":
                return Messages.getString("PipelineViewAccessor.createdBy");
            default:
                return id;
        }
    }

    @Override
    public Object getValue(PipelineView object, String id) {
        switch (id) {
            case "id":
                return object.getId();
            case "name":
                String name = object.getName();
                return name.length() > Utils.getColumnMaxLenght() ? name.substring(0, Utils.getColumnMaxLenght() - 3) + "..." : name;
            case "duration":
                return DecorationHelper.formatDuration(object.getDuration());
            case "lastExecTime":
                return object.getStart();
            case "lastExecStatus":
                final PipelineExecutionStatus type = object.getStatus();
                if (type != null) {
                    ThemeResource img = DecorationHelper.getIconForExecutionStatus(type);
                    Embedded emb = new Embedded(type.name(), img);
                    emb.setDescription(type.name());
                    return emb;
                } else {
                    return null;
                }
            case "createdBy":
                if(SHARE_MODE_ORG.equals(appConfig.getString(ConfigProperty.OWNERSHIP_TYPE)))
                    return object.getOrgName() + " (" + object.getUsrName() + ")";
                else
                    return object.getUsrName();
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
            case "createdBy":
                return String.class;
            default:
                return null;
        }
    }
}
