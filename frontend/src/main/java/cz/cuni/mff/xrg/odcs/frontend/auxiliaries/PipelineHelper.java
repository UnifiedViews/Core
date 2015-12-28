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
package cz.cuni.mff.xrg.odcs.frontend.auxiliaries;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ExecutionFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.commons.dao.view.ExecutionView;
import eu.unifiedviews.commons.dao.view.PipelineView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bogo
 */
@Component
public class PipelineHelper {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineHelper.class);

    private static final Long DEFAULT_ORDER_POSITION = ScheduledJobsPriority.IGNORE.getValue();

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private RuntimePropertiesFacade runtimePropertyFacade;

    @Autowired
    private ExecutionFacade executionFacade;

    public List<ExecutionView> getExecutionViews() {
        List<ExecutionView> executionViews = new ArrayList<>();

        for (PipelineExecution execution : pipelineFacade.getAllExecutions()) {
            executionViews.add(new ExecutionView(
                    execution.getId(),
                    execution.getStatus(),
                    execution.getPipeline().getId(),
                    execution.getPipeline().getName(),
                    execution.isDebugging(),
                    execution.getStart(),
                    execution.getEnd(),
                    execution.getSchedule() != null ? execution.getSchedule().getId() : null,
                    execution.getOwner().getUsername(),
                    execution.getOwner().getFullName(),
                    execution.getActor() != null ? execution.getActor().getName() : null,
                    execution.getStop(),
                    execution.getLastChange()));
        }

        return executionViews;
    }

    public List<PipelineView> getPipelineViews() {
        List<PipelineView> pipelineViews = new ArrayList<>();

        for (Pipeline pipeline : pipelineFacade.getAllPipelines()) {
            PipelineExecution lastExec = pipelineFacade.getLastExec(pipeline);
            User user = pipeline.getOwner();

            PipelineView pipelineView = new PipelineView(
                pipeline.getId(),
                pipeline.getName(),
                lastExec != null ? lastExec.getStart() : null,
                lastExec != null ? lastExec.getEnd() : null,
                user.getUsername(),
                user.getFullName(),
                lastExec != null ? lastExec.getStatus() : null,
                user.getUserActor() != null ? user.getUserActor().getName() : "");
            pipelineViews.add(pipelineView);
        }

        return pipelineViews;
    }

    /**
     * Sets up parameters of pipeline execution and runs the pipeline.
     * 
     * @param pipeline
     *            {@link Pipeline} to run.
     * @param inDebugMode
     *            Run in debug/normal mode.
     * @return {@link PipelineExecution} of given {@link Pipeline}.
     */
    public PipelineExecution runPipeline(Pipeline pipeline, boolean inDebugMode) {
        return runPipeline(pipeline, inDebugMode, null);
    }

    /**
     * Sets up parameters of pipeline execution and runs the pipeline.
     * 
     * @param pipeline
     *            {@link Pipeline} to run.
     * @param inDebugMode
     *            Run in debug/normal mode.
     * @param debugNode
     *            {@link Node} where debug execution should stop. Valid
     *            only for debug mode.
     * @return {@link PipelineExecution} of given {@link Pipeline} or null if backend is offline.
     */
    public PipelineExecution runPipeline(Pipeline pipeline, boolean inDebugMode, Node debugNode) {
        final boolean hasQueuedOrRunning = pipelineFacade.hasExecutionsWithStatus(pipeline,
                Arrays.asList(PipelineExecutionStatus.QUEUED, PipelineExecutionStatus.RUNNING));
        if (hasQueuedOrRunning) {
            Notification.show(Messages.getString("PipelineHelper.start.failed"), Messages.getString("PipelineHelper.start.failed.description"), Type.WARNING_MESSAGE);
            return null;
        }

        final PipelineExecution pipelineExec = pipelineFacade.createExecution(pipeline);
        pipelineExec.setDebugging(inDebugMode);
        if (inDebugMode && debugNode != null) {
            pipelineExec.setDebugNode(debugNode);
        }

        Long orderPosition = getOrderPosition();
        // run immediately - set higher priority
        pipelineExec.setOrderNumber(orderPosition);
        this.pipelineFacade.save(pipelineExec);
        if (!checkBackendActive()) {
            ConfirmDialog.show(UI.getCurrent(),
                    Messages.getString("PipelineHelper.backend.offline.dialog.name"),
                    Messages.getString("PipelineHelper.backend.offline.dialog.message"),
                    Messages.getString("PipelineHelper.backend.offline.dialog.schedule"),
                    Messages.getString("PipelineHelper.backend.offline.dialog.cancel"),
                    new ConfirmDialog.Listener() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClose(ConfirmDialog cd) {
                            PipelineExecution pplExec = pipelineFacade.getExecution(pipelineExec.getId());
                            if (pplExec != null && pplExec.getStatus() != PipelineExecutionStatus.QUEUED) {
                                Notification.show(Messages.getString("PipelineHelper.execution.state.title"),
                                        Messages.getString("PipelineHelper.execution.state.description"), Type.WARNING_MESSAGE);
                                return; // already running
                            }
                            if (cd.isConfirmed()) {
                                pipelineFacade.save(pipelineExec);
                            } else {
                                pipelineFacade.delete(pipelineExec);
                            }
                        }
                    });
            return null;
        }

        Notification.show(Messages.getString("PipelineHelper.execution.started"), Notification.Type.HUMANIZED_MESSAGE);
        return pipelineExec;
    }

    /**
     * Computes order position using runtime property
     */
    private Long getOrderPosition() {
        Long epoch = System.currentTimeMillis();
        Long priority = DEFAULT_ORDER_POSITION;

        // checking format of value
        RuntimeProperty property = runtimePropertyFacade.getByName(ConfigProperty.FRONTEND_RUN_NOW_PIPELINE_PRIORITY.toString());
        if (property != null) {
            try {
                priority = Long.parseLong(property.getValue());
            } catch (NumberFormatException e) {
                LOG.error("Value not a number of RuntimeProperty: " + ConfigProperty.FRONTEND_RUN_NOW_PIPELINE_PRIORITY.toString()
                        + ", error: " + e.getMessage());
                LOG.warn("Using default value: " + DEFAULT_ORDER_POSITION);
            }
        }

        // schould be in range IGNORE (0) - HIGHEST (3)
        if (priority < ScheduledJobsPriority.IGNORE.getValue()) {
            priority = ScheduledJobsPriority.IGNORE.getValue();
        } else if (priority > ScheduledJobsPriority.HIGHEST.getValue()) {
            priority = ScheduledJobsPriority.HIGHEST.getValue();
        }

        Long orderNumber = priority;
        if (priority != ScheduledJobsPriority.IGNORE.getValue()) {
            orderNumber = (epoch / priority);
        }
        return orderNumber;
    }

    private boolean checkBackendActive() {
        return this.executionFacade.checkAnyBackendActive();
    }

}
