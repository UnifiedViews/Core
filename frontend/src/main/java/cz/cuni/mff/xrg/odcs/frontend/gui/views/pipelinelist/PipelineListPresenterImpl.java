package cz.cuni.mff.xrg.odcs.frontend.gui.views.pipelinelist;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tepi.filtertable.numberfilter.NumberInterval;
import org.vaadin.dialogs.ConfirmDialog;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthAwarePermissionEvaluator;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbPipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportService;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.PipelineHelper;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.RefreshManager;
import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;
import cz.cuni.mff.xrg.odcs.frontend.container.accessor.PipelineAccessor;
import cz.cuni.mff.xrg.odcs.frontend.container.accessor.PipelineViewAccessor;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.db.DbCachedSource;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.SchedulePipeline;
import cz.cuni.mff.xrg.odcs.frontend.gui.dialog.PipelineImport;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PipelineEdit;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PostLogoutCleaner;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.executionlist.ExecutionListPresenterImpl;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.navigation.Address;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigator;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ParametersHandler;
import eu.unifiedviews.commons.dao.DBPipelineView;
import eu.unifiedviews.commons.dao.view.PipelineView;

/**
 * Implementation of {@link PipelineListPresenter}.
 * 
 * @author Bogo
 */
@Component
@Scope("session")
@Address(url = "PipelineList")
public class PipelineListPresenterImpl implements PipelineListPresenter, PostLogoutCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineListPresenterImpl.class);

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private DbPipeline dbPipeline;

    @Autowired
    private DBPipelineView dbPipelineView;

    @Autowired
    private PipelineAccessor pipelineAccessor;

    @Autowired
    private PipelineViewAccessor pipelineViewAccessor;

    @Autowired
    private PipelineListView view;

    @Autowired
    private SchedulePipeline schedulePipeline;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private PipelineHelper pipelineHelper;

    @Autowired
    private Utils utils;

    @Autowired
    private ImportService importService;

    private ClassNavigator navigator;

    private PipelineListData dataObject;

    private DbCachedSource<PipelineView> cachedSource;

    private RefreshManager refreshManager;

    private Date lastLoad = new Date(0L);

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;
    
    /**
     * Evaluates permissions of currently logged in user.
     */
    @Autowired
    private AuthAwarePermissionEvaluator permissions;

    private boolean isInitialized = false;

    @Override
    public Object enter() {
        if (isInitialized) {
            navigator = ((AppEntry) UI.getCurrent()).getNavigation();
            addRefreshManager();
            return view.enter(this);
        }

        navigator = ((AppEntry) UI.getCurrent()).getNavigation();
        // prepare data object
//        cachedSource = new DbCachedSource<>(dbPipeline, pipelineAccessor, utils.getPageLength());
        cachedSource = new DbCachedSource<>(dbPipelineView, pipelineViewAccessor, utils.getPageLength());

        dataObject = new PipelineListPresenter.PipelineListData(new ReadOnlyContainer<>(cachedSource));

        // prepare view
        Object viewObject = view.enter(this);
        addRefreshManager();

        // set data object
        view.setDisplay(dataObject);

        // add initial name filter
        view.setFilter("owner.username", utils.getUserName());

        isInitialized = true;

        // return main component
        return viewObject;
    }

    private void addRefreshManager() {
        refreshManager = ((AppEntry) UI.getCurrent()).getRefreshManager();
        refreshManager.addListener(RefreshManager.PIPELINE_LIST, new Refresher.RefreshListener() {
            private long lastRefreshFinished = 0;

            @Override
            public void refresh(Refresher source) {
                if (new Date().getTime() - lastRefreshFinished > RefreshManager.MIN_REFRESH_INTERVAL) {
                    boolean hasModifiedPipelinesOrExecutions = pipelineFacade.hasModifiedPipelines(lastLoad)
                            || pipelineFacade.hasModifiedExecutions(lastLoad)
                            || (cachedSource.size() > 0 &&
                            pipelineFacade.hasDeletedPipelines((List<Long>) cachedSource.getItemIds(0, cachedSource.size())));
                    if (hasModifiedPipelinesOrExecutions) {
                        lastLoad = new Date();
                        refreshEventHandler();
                    }
                    LOG.debug("Pipeline list refreshed.");
                    lastRefreshFinished = new Date().getTime();
                }
            }
        });
        refreshManager.triggerRefresh();
    }

    @Override
    public void setParameters(Object configuration) {
        if (configuration != null && Map.class.isAssignableFrom(configuration.getClass())) {
            int pageNumber = 0;
            Map<String, String> config = (Map<String, String>) configuration;
            for (Entry<String, String> entry : config.entrySet()) {
                switch (entry.getKey()) {
                    case "page":
                        pageNumber = Integer.parseInt(entry.getValue());
                        break;
                    case "id":
                        view.setFilter(entry.getKey(), ParametersHandler.getInterval(entry.getValue()));
                        break;
                    default:
                        view.setFilter(entry.getKey(), entry.getValue());
                        break;
                }
            }
            if (pageNumber != 0) {
                //Page number is set as last, because filtering automatically moves table to first page.
                view.setPage(pageNumber);
            }
        }
    }

    @Override
    public void refreshEventHandler() {
        pipelineAccessor.clearExecCache();
        cachedSource.invalidate();
        dataObject.getContainer().refresh();
        view.refreshTableControls();
    }

    @Override
    public void copyEventHandler(long id) {
        Pipeline pipeline = getLightPipeline(id);
        pipelineFacade.copyPipeline(pipeline);
        Notification.show(Messages.getString("PipelineListPresenterImpl.copy.successfull", pipeline.getName()),
                Notification.Type.HUMANIZED_MESSAGE);
        refreshEventHandler();
    }

    @Override
    public void deleteEventHandler(long id) {
        final Pipeline pipeline = getLightPipeline(id);
        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline, PipelineExecutionStatus.QUEUED);
        if (executions.isEmpty()) {
            executions = pipelineFacade.getExecutions(pipeline, PipelineExecutionStatus.RUNNING);
        }
        if (!executions.isEmpty()) {
            Notification.show(Messages.getString("PipelineListPresenterImpl.pipeline.running", pipeline.getName()), Notification.Type.WARNING_MESSAGE);
            return;
        }
        String message = Messages.getString("PipelineListPresenterImpl.delete.dialog", pipeline.getName());
        List<Schedule> schedules = scheduleFacade.getSchedulesFor(pipeline);
        if (!schedules.isEmpty()) {
            HashSet<String> usersWithSchedules = new HashSet<>();
            for (Schedule schedule : schedules) {
                usersWithSchedules.add(schedule.getOwner().getUsername());
            }
            Iterator<String> it = usersWithSchedules.iterator();
            String users = it.next();
            while (it.hasNext()) {
                users = users + ", " + it.next();
            }
            String scheduleMessage = Messages.getString("PipelineListPresenterImpl.pipeline.scheduled", users);
            message = message + scheduleMessage;
        }
        ConfirmDialog.show(UI.getCurrent(),
                Messages.getString("PipelineListPresenterImpl.delete.confirmation"), message, Messages.getString("PipelineListPresenterImpl.delete.confirmation.deleteButton"), Messages.getString("PipelineListPresenterImpl.delete.confirmation.cancelButton"), new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog cd) {
                        if (cd.isConfirmed()) {
                            pipelineFacade.delete(pipeline);
                            refreshEventHandler();
                        }
                    }
                });
    }

    @Override
    public boolean canDeletePipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        String adminPermission = appConfig.getString(ConfigProperty.ADMIN_PERMISSION);
        boolean isAdmin = permissions.hasPermission(pipeline, adminPermission);
        boolean canDelete = permissions.hasPermission(pipeline, "pipeline.delete");
        return isAdmin || canDelete;
    }

    @Override
    public boolean canEditPipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        return permissions.hasPermission(pipeline, "pipeline.edit");
    }

    @Override
    public boolean canDebugPipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        return permissions.hasPermission(pipeline, "pipeline.runDebug");
    }

    @Override
    public boolean canSchedulePipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        return permissions.hasPermission(pipeline, "pipeline.schedule");
    }

    @Override
    public boolean canCopyPipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        return permissions.hasPermission(pipeline, "pipeline.copy");
    }

    @Override
    public boolean canRunPipeline(long pipelineId) {
        Pipeline pipeline = getLightPipeline(pipelineId);
        return permissions.hasPermission(pipeline, "pipeline.run");
    }

    @Override
    public void scheduleEventHandler(long id) {
        Pipeline pipeline = getLightPipeline(id);
        // open scheduler dialog
        if (!schedulePipeline.isInitialized()) {
            schedulePipeline.init();
        }
        schedulePipeline.setSelectePipeline(pipeline);
        UI.getCurrent().addWindow(schedulePipeline);
    }

    @Override
    public void runEventHandler(long id, boolean inDebugMode) {
        PipelineExecution exec = pipelineHelper.runPipeline(getLightPipeline(id), inDebugMode);
        if (inDebugMode && exec != null) {
            navigator.navigateTo(ExecutionListPresenterImpl.class, String.format("exec=%s", exec.getId()));
        }
    }

    @Override
    public void navigateToEventHandler(Class where, Object param) {
        if (param == null) {
            navigator.navigateTo(where);
        } else {
            navigator.navigateTo(where, param.toString());
        }
    }

    private Pipeline getLightPipeline(long pipelineId) {
        return pipelineFacade.getPipeline(pipelineId);
    }

    @Override
    public void pageChangedHandler(Integer newPageNumber) {
        String uriFragment = Page.getCurrent().getUriFragment();
        ParametersHandler handler = new ParametersHandler(uriFragment);
        handler.addParameter("page", newPageNumber.toString());
        ((AppEntry) UI.getCurrent()).setUriFragment(handler.getUriFragment(), false);
    }

    @Override
    public void filterParameterEventHander(String propertyId, Object filterValue) {
        String uriFragment = Page.getCurrent().getUriFragment();
        ParametersHandler handler = new ParametersHandler(uriFragment);
        if (filterValue == null || (filterValue.getClass() == String.class && ((String) filterValue).isEmpty())) {
            //Remove from URI
            handler.removeParameter(propertyId);
        } else {
            String value;
            switch (propertyId) {
                case "id":
                    value = ParametersHandler.getStringForInterval((NumberInterval) filterValue);
                    break;
                default:
                    value = filterValue.toString();
                    break;
            }
            handler.addParameter(propertyId, value);
        }
        ((AppEntry) UI.getCurrent()).setUriFragment(handler.getUriFragment(), false);
    }

    @Override
    public void importPipeline() {
        final PipelineImport dialog = new PipelineImport(importService);
        dialog.addCloseListener(new Window.CloseListener() {

            @Override
            public void windowClose(Window.CloseEvent e) {
                final Pipeline pipe = dialog.getImportedPipeline();
                if (pipe != null) {
                    // show newly added pipeline
                    navigator.navigateTo(PipelineEdit.class, pipe.getId().toString());
                }
            }
        });

        UI.getCurrent().addWindow(dialog);
        dialog.bringToFront();
    }

    @Override
    public void doAfterLogout() {
        isInitialized = false;
    }

    @Override
    public boolean isLayoutInitialized() {
        return isInitialized;
    }

}
