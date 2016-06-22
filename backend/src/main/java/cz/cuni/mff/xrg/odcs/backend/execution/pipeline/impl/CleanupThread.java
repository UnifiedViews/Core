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
package cz.cuni.mff.xrg.odcs.backend.execution.pipeline.impl;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.facade.LogFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;

/**
 * This class periodically (every 24h) checks database for old executions executed by this backend
 * and deletes all executions older than the defined count of days
 * Temporary data for all failed and canceled non-debugged executions are also cleaned up during this check (regardless the execution age)
 * as these data cannot be accessed from UV in any way and thus just take up place on disk
 */
@Component
public class CleanupThread {

    private static Logger LOG = LoggerFactory.getLogger(CleanupThread.class);

    @Autowired
    private ResourceManager resourceManager;

    private int executionsDaysLimitForCleanup = -1;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private LogFacade logFacade;

    private String backendId;

    @PostConstruct
    public void init() {
        this.backendId = this.appConfig.getString(ConfigProperty.BACKEND_ID);
        if (this.appConfig.contains(ConfigProperty.BACKEND_EXECUTION_CLEANUP_DAYS_LIMIT)) {
            this.executionsDaysLimitForCleanup = this.appConfig.getInteger(ConfigProperty.BACKEND_EXECUTION_CLEANUP_DAYS_LIMIT);
        }
    }

    /**
     * Periodically (every 24 hours) cleanup the executions
     */
    @Async
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    protected void cleanupExecutions() {
        LOG.info("Going to cleanup executions");
        if (this.executionsDaysLimitForCleanup != -1) {
            LOG.info("Deleting all executions older than {} days", this.executionsDaysLimitForCleanup);
            deleteExecutions();
        }

        LOG.info("Deleting temp data for all failed/cancelled executions");
        deleteTempDataForFailedExecutions();
        LOG.info("Executions cleanup successfully finished");
    }

    /**
     * Delete all finished executions older than the defined count of days and all its data and files
     * Deletes execution from DB along with logs, events and deletes execution files from disk
     * Note: This code duplicates the code also defined in {@link cz.cuni.mff.xrg.odcs.frontend.gui.views.Settings.PipelineExecutionDeleterThread} (frontend)
     */
    private void deleteExecutions() {
        List<PipelineExecution> finishedPipelineExecutions = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.CANCELLED, this.backendId);
        finishedPipelineExecutions.addAll(this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.FAILED, this.backendId));
        finishedPipelineExecutions.addAll(this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.FINISHED_SUCCESS, this.backendId));
        finishedPipelineExecutions.addAll(this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.FINISHED_WARNING, this.backendId));

        int recordsDeleted = 0;
        int recordsDeleteFailed = 0;

        Calendar now = Calendar.getInstance();
        now.add(java.util.Calendar.HOUR, -24 * this.executionsDaysLimitForCleanup);
        for (PipelineExecution fex : finishedPipelineExecutions) {
            Calendar executionEnd = Calendar.getInstance();
            executionEnd.setTime(fex.getEnd());
            try {
                if (executionEnd.before(now)) {
                    try {
                        final File executionDir = this.resourceManager.getExecutionDir(fex);
                        CleanupUtils.deleteDirectory(executionDir);
                    } catch (MissingResourceException ex) {
                        LOG.warn("No resources to delete for Pipeline execution id: {}", fex.getId(), ex);
                    }
                    this.logFacade.deleteLogs(fex);
                    this.pipelineFacade.delete(fex);
                    recordsDeleted++;
                }
            } catch (Exception e) {
                LOG.error("Failed to cleanup execution {}", fex.getId(), e);
                recordsDeleteFailed++;
            }
        }
        LOG.info("Executions cleanup finished, {} executions successfully cleaned, failed to clean {} executions", recordsDeleted, recordsDeleteFailed);
    }

    /**
     * Delete temp execution files for non debugging failed and canceled executions
     * Note: Check whether this code is needed. Because there is CleanUp PostExecutor which, for every finished pipeline
     * (even those which fail or are cancelled) cleans up the working directory.
     */
    private void deleteTempDataForFailedExecutions() {
        List<PipelineExecution> failedExecutions = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.FAILED, this.backendId);
        failedExecutions.addAll(this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.CANCELLED, this.backendId));
        int cleanedExecutions = 0;
        for (PipelineExecution failed : failedExecutions) {
            if (!failed.isDebugging()) {
                try {
                    final File executionDir = this.resourceManager.getExecutionDir(failed);
                    CleanupUtils.deleteDirectory(executionDir);
                    cleanedExecutions++;
                } catch (MissingResourceException e) {
                    LOG.info("No resources to delete for Pipeline execution id: {}", failed.getId(), e);
                } catch (Exception e) {
                    LOG.error("Failed to delete temp data for execution {}", failed.getId(), e);
                }
            }
        }
        LOG.info("Deleted temp execution data for {} failed/cancelled executions", cleanedExecutions);
    }

}
