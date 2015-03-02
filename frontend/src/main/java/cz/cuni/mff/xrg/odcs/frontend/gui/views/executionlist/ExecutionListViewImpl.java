package cz.cuni.mff.xrg.odcs.frontend.gui.views.executionlist;

import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.RUNNING;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.paged.PagedFilterTable;
import org.tepi.filtertable.paged.PagedTableChangeEvent;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Not;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.container.ValueItem;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DebuggingView;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.ActionColumnGenerator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.ActionColumnGenerator.Action;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibFilterDecorator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PipelineEdit;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ParametersHandler;

/**
 * Implementation of view for {@link ExecutionListPresenter}.
 * 
 * @author Petyr
 */
@Component
@Scope("prototype")
public class ExecutionListViewImpl extends CustomComponent implements ExecutionListPresenter.ExecutionListView {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionListViewImpl.class);

    /**
     * Column widths for execution table.
     */
    private static final int COLUMN_SCHEDULE_WIDTH = 60;

    private static final int COLUMN_STATUS_WIDTH = 39;

    private static final int COLUMN_DEBUG_WIDTH = 42;

    private static final int COLUMN_DURATION_WIDTH = 53;

    private static final int COLUMN_START_WIDTH = 115;

    private static final int COLUMN_ACTIONS_WIDTH = 160;

    private static final int FALLBACK_WIDTH = 720;

    private IntlibPagedTable monitorTable;

    /**
     * Used to separate table from execution detail view.
     */
    private HorizontalSplitPanel hsplit;

    private VerticalLayout logLayout;

    private Panel mainLayout;

    @Autowired
    private DebuggingView debugView;

    private HashMap<Date, Label> runTimeLabels = new HashMap<>();

    private ExecutionListPresenter presenter;

    @Autowired
    private Utils utils;

    private Button showMaster;

    @Override
    public Object enter(final ExecutionListPresenter presenter) {
        // build page
        this.presenter = presenter;
        if (!presenter.isLayoutInitialized()) {
            buildPage(presenter);
        }
        debugView.restore();
        return this;
    }

    @Override
    public void setDisplay(ExecutionListPresenter.ExecutionListData dataObject) {
        monitorTable.setContainerDataSource(dataObject.getContainer());
        //monitorTable.setVisibleColumns(dataObject.getContainer().getContainerPropertyIds());
    }

    @Override
    public void showExecutionDetail(PipelineExecution execution, ExecutionListPresenter.ExecutionDetailData detailDataObject) {
        LOG.trace("showExecutionDetail()");
        presenter.stopRefreshEventHandler();
        // secure existance of detail layout
//		if (logLayout == null) {
//			buildExecutionDetail(execution);
//		} 
        // will just set the debug view content
        LOG.trace("showExecutionDetail() : buildDebugView");
        buildDebugView(execution);
        if (execution.getStatus() == PipelineExecutionStatus.RUNNING
                || execution.getStatus() == PipelineExecutionStatus.QUEUED) {
            presenter.startDebugRefreshEventHandler(debugView, execution);
        }
        // no DPU specified
        LOG.trace("showExecutionDetail() : setExecution");
        debugView.setExecution(execution, null);
        //debugView.setDisplay(detailDataObject);
        LOG.trace("showExecutionDetail() : hsplit.setSecondComponent");
        hsplit.setSecondComponent(logLayout);
        // adjust hsplit
        LOG.trace("showExecutionDetail() : adjust hsplit");
        if (hsplit.isLocked()) {
            debugView.setActiveTab(Messages.getString("DebuggingView.events"));
            if (UI.getCurrent().getPage().getBrowserWindowWidth() < FALLBACK_WIDTH) {
                hsplit.setSplitPosition(0, Unit.PERCENTAGE);
                showMaster.setEnabled(true);
            } else {
                hsplit.setSplitPosition(55, Unit.PERCENTAGE);
                showMaster.setEnabled(false);
            }
            //hsplit.setHeight("-1px");
            hsplit.setLocked(false);
        }

        LOG.trace("showExecutionDetail() -> done");
    }

    @Override
    public void refresh(boolean modified) {
        if (modified) {
            runTimeLabels.clear();
        }
        for (Map.Entry<Date, Label> entry : runTimeLabels.entrySet()) {
            long duration = (new Date()).getTime() - entry.getKey().getTime();
            entry.getValue().setValue(DecorationHelper.formatDuration(duration));
            entry.getValue().setSizeUndefined();
        }
    }

    /**
     * Build page layout.
     * 
     * @param presenter
     */
    private void buildPage(final ExecutionListPresenter presenter) {
        mainLayout = new Panel("");
        // split page into two parts
        hsplit = new HorizontalSplitPanel();
        mainLayout.setContent(hsplit);
        // set top level element properties
        setWidth("100%");
        //setHeight("100%");

        VerticalLayout monitorTableLayout = new VerticalLayout();
        monitorTableLayout.setImmediate(true);
        monitorTableLayout.setMargin(true);
        monitorTableLayout.setSpacing(true);
        monitorTableLayout.setWidth("100%");
        //monitorTableLayout.setHeight("100%");

        // Layout for buttons Refresh and Clear Filters on the top.
        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setSpacing(true);
        monitorTableLayout.addComponent(topLine);

        // Refresh button. Refreshing the table
        Button btnRefresh = new Button(Messages.getString("ExecutionListViewImpl.refresh"), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.refreshEventHandler();
            }
        });
        btnRefresh.addStyleName("v-button-primary");
        topLine.addComponent(btnRefresh);

        //Clear Filters button. Clearing filters on the table with executions.
        Button btnClearFilters = new Button();
        btnClearFilters.setCaption(Messages.getString("ExecutionListViewImpl.clear.filters"));
        btnClearFilters.setHeight("25px");
        btnClearFilters.addStyleName("v-button-primary");
        btnClearFilters.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                // TODO move this to the monitorTable
                monitorTable.resetFilters();
            }
        });
        topLine.addComponent(btnClearFilters);

        // clear table sorting
        Button btnClearSort = new Button(Messages.getString("ExecutionListViewImpl.clear.sort"));
        btnClearSort.setHeight("25px");
        btnClearSort.addStyleName("v-button-primary");
        btnClearSort.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                monitorTable.setSortContainerPropertyId(null);; // deselect column
                monitorTable.sort(new Object[] { "id" }, new boolean[] { false });
            }
        });
        topLine.addComponent(btnClearSort);

        // Table with pipeline execution records
        monitorTable = initializeExecutionTable(presenter);
        monitorTableLayout.addComponent(monitorTable);
        monitorTableLayout.addComponent(monitorTable.createControls());

        hsplit.setFirstComponent(monitorTableLayout);
        hsplit.setSecondComponent(null);
        if (146 + (utils.getPageLength() * 32) <= 850) {
            hsplit.setHeight(850, Unit.PIXELS);
        } else {
            hsplit.setHeight(-1, Unit.PIXELS);
        }

        hsplit.setSplitPosition(100, Unit.PERCENTAGE);
        hsplit.setLocked(true);

        // at the end set page root
        setCompositionRoot(mainLayout);
    }

    /**
     * Create and return {@link ActionColumnGenerator}.
     * 
     * @param presenter
     * @return {@link ActionColumnGenerator}
     */
    private ActionColumnGenerator createColumnGenerator(final ExecutionListPresenter presenter) {
        ActionColumnGenerator generator = new ActionColumnGenerator();
        // add action buttons

        generator.addButton(Messages.getString("ExecutionListViewImpl.show.log"), null, new Action() {
            @Override
            protected void action(long id) {
                changeURI(id);
                presenter.showDebugEventHandler(id);
                monitorTable.select(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                Property propStatus = source.getItem(id).getItemProperty("status");
                PipelineExecutionStatus status = (PipelineExecutionStatus) propStatus.getValue();
                boolean isDebug = (boolean) source.getItem(id).getItemProperty("isDebugging").getValue();
                // ...
                
                boolean canReadLog = presenter.canReadLog(id);
                return !isDebug && status != PipelineExecutionStatus.QUEUED && canReadLog;
            }
        }, new ThemeResource("icons/show_log.png"));

        generator.addButton(Messages.getString("ExecutionListViewImpl.debug.data"), null, new Action() {
            @Override
            protected void action(long id) {
                changeURI(id);
                presenter.showDebugEventHandler(id);
                monitorTable.select(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                Property propStatus = source.getItem(id).getItemProperty("status");
                PipelineExecutionStatus status = (PipelineExecutionStatus) propStatus.getValue();
                boolean isDebug = (boolean) source.getItem(id).getItemProperty("isDebugging").getValue();
                // ...
                boolean canDebug = presenter.canDebugData(id);
                return isDebug && status != PipelineExecutionStatus.QUEUED && canDebug;
            }
        }, new ThemeResource("icons/debug_data.png"));

        generator.addButton(Messages.getString("ExecutionListViewImpl.cancel"), null, new Action() {
            @Override
            protected void action(long id) {
                presenter.stopEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                Property propStatus = source.getItem(id).getItemProperty("status");
                PipelineExecutionStatus status = (PipelineExecutionStatus) propStatus.getValue();

                boolean stoppableStatus = status == PipelineExecutionStatus.QUEUED
                        || status == PipelineExecutionStatus.RUNNING;

                boolean userCanStop = presenter.canStopExecution(id);

                return stoppableStatus && userCanStop;
            }
        }, new ThemeResource("icons/cancelled.png"));

        generator.addButton(Messages.getString("ExecutionListViewImpl.run.pipeline"), null, new Action() {
            @Override
            protected void action(long id) {
                presenter.runEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                Property propStatus = source.getItem(id).getItemProperty("status");
                PipelineExecutionStatus status = (PipelineExecutionStatus) propStatus.getValue();
                // ...
                boolean canRunPipeline = presenter.canRunPipeline(id);
                return status != PipelineExecutionStatus.RUNNING
                        && status != PipelineExecutionStatus.CANCELLING && canRunPipeline;
            }
        }, new ThemeResource("icons/running.png"));

        generator.addButton(Messages.getString("ExecutionListViewImpl.debug.pipeline"), null, new Action() {
            @Override
            protected void action(long id) {
                presenter.debugEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                Property propStatus = source.getItem(id).getItemProperty("status");
                PipelineExecutionStatus status = (PipelineExecutionStatus) propStatus.getValue();
                // ...
                boolean canDebugPipeline = presenter.canDebugPipeline(id);
                return status != PipelineExecutionStatus.RUNNING
                        && status != PipelineExecutionStatus.CANCELLING && canDebugPipeline;
            }
        }, new ThemeResource("icons/debug.png"));

        return generator;
    }

    private void buildExecutionDetail(PipelineExecution execution) {
        logLayout = new VerticalLayout();
        logLayout.setImmediate(true);
        logLayout.setMargin(true);
        logLayout.setSpacing(true);
        logLayout.setWidth("100%");
        //logLayout.setHeight("100%");
        //debugView = new DebuggingView();

        // build the debug view
        //Recursive call
        //buildDebugView(execution);

        showMaster = new Button(Messages.getString("ExecutionListViewImpl.back.to.exexution"), new ClickListener() {

            private static final long serialVersionUID = 4723715087122769012L;

            @Override
            public void buttonClick(ClickEvent event) {
                hsplit.setSplitPosition(100, Unit.PERCENTAGE);
                hsplit.setLocked(true);
            }
        });
        showMaster.setEnabled(false);
        logLayout.addComponent(showMaster);
        logLayout.addComponent(debugView);
        logLayout.setExpandRatio(debugView, 1.0f);

        // Layout for buttons  Close and  Export on the bottom
//		HorizontalLayout buttonBar = new HorizontalLayout();
//		buttonBar.setWidth("100%");
//
//		Button buttonClose = new Button();
//		buttonClose.setCaption("Close");
//		buttonClose.setHeight("25px");
//		buttonClose.setWidth("100px");
//		buttonClose.addClickListener(new com.vaadin.ui.Button.ClickListener() {
//			@Override
//			public void buttonClick(Button.ClickEvent event) {
//				presenter.stopRefreshEventHandler();
//				hsplit.setSplitPosition(100, Unit.PERCENTAGE);
//				//hsplit.setHeight("100%");
//				hsplit.setLocked(true);
//			}
//		});
//		buttonBar.addComponent(buttonClose);
//		buttonBar.setComponentAlignment(buttonClose, Alignment.BOTTOM_RIGHT);
//
//		logLayout.addComponent(buttonBar);
//		logLayout.setExpandRatio(buttonBar, 0);

    }

    /**
     * Builds debugging view upon first call, sets the pipeline execution to
     * show inside it.
     * 
     * @param exec
     *            pipeline execution to show in debugging view
     */
    private void buildDebugView(PipelineExecution execution) {
        if (logLayout == null) {
            // secure that the debug view exist
            buildExecutionDetail(execution);
        }

        if (!debugView.isInitialized()) {
            debugView.initialize(execution, null, execution.isDebugging(), false);
        } else {
            //It is done later...
            //debugView.setExecution(execution, null);
        }
    }

    private FilterGenerator createFilterGenerator() {
        return new FilterGenerator() {
            @Override
            public Container.Filter generateFilter(Object propertyId, Object value) {
                if ("schedule".equals(propertyId)) {
                    boolean val = (boolean) value;

                    if (!val) {
                        return new IsNull(propertyId);
                    } else {
                        return new Not(new IsNull(propertyId));
                    }
                }
                return null;
            }

            @Override
            public Container.Filter generateFilter(Object propertyId, Field<?> originatingField) {
                return null;
            }

            @Override
            public AbstractField<?> getCustomFilterComponent(Object propertyId) {
                if ("schedule".equals(propertyId)) {
                    ComboBox comboScheduled = new ComboBox();
                    comboScheduled.addItem(true);
                    ThemeResource iconScheduled = new ThemeResource("icons/scheduled.png");
                    comboScheduled.setItemIcon(true, iconScheduled);
                    comboScheduled.setItemCaption(true, Messages.getString("ExecutionListViewImpl.scheduled"));
                    comboScheduled.addItem(false);
                    ThemeResource iconNotScheduled = new ThemeResource("icons/not_scheduled.png");
                    comboScheduled.setItemIcon(false, iconNotScheduled);
                    comboScheduled.setItemCaption(false, Messages.getString("ExecutionListViewImpl.manual"));
                    return comboScheduled;
                }
                return null;
            }

            @Override
            public void filterRemoved(Object propertyId) {
            }

            @Override
            public void filterAdded(Object propertyId, Class<? extends Container.Filter> filterType, Object value) {
            }

            @Override
            public Container.Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
                return null;
            }
        };
    }

    private IntlibPagedTable initializeExecutionTable(final ExecutionListPresenter presenter) {

        final IntlibPagedTable executionTable = new IntlibPagedTable();
        executionTable.setSelectable(true);
        executionTable.setWidth("100%");
        executionTable.setHeight("100%");
        executionTable.setImmediate(true);
        executionTable.setColumnCollapsingAllowed(true);

        executionTable.setColumnWidth("schedule", COLUMN_SCHEDULE_WIDTH);
        executionTable.setColumnWidth("status", COLUMN_STATUS_WIDTH);
        executionTable.setColumnWidth("isDebugging", COLUMN_DEBUG_WIDTH);
        executionTable.setColumnWidth("duration", COLUMN_DURATION_WIDTH);
        executionTable.setColumnWidth("start", COLUMN_START_WIDTH);

        //Suitable if no more than 3 buttons are available at the same time, which is true in current version.
        executionTable.setColumnWidth("actions", COLUMN_ACTIONS_WIDTH);
        executionTable.setColumnAlignment("schedule", CustomTable.Align.CENTER);
        executionTable.setColumnAlignment("isDebugging", CustomTable.Align.CENTER);
        executionTable.setColumnAlignment("status", CustomTable.Align.CENTER);
        executionTable.setColumnAlignment("duration", CustomTable.Align.RIGHT);
        executionTable.setSortEnabled(true);
        executionTable.setPageLength(utils.getPageLength());

        executionTable.setFilterGenerator(createFilterGenerator());
        executionTable.setFilterDecorator(new filterDecorator());
        executionTable.setFilterBarVisible(true);

        executionTable.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
                    private static final long serialVersionUID = 9095300568169526085L;

                    @Override
                    public void itemClick(ItemClickEvent event) {
                        ValueItem item = (ValueItem) event.getItem();
                        final long executionId = item.getId();
                        changeURI(executionId);
                        // set debug
                        presenter.showDebugEventHandler(executionId);
                    }
                });

        executionTable.addGeneratedColumn("pipelineName", new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(final CustomTable source, final Object itemId, Object columnId) {
                final Button btnEdit = new Button();
                btnEdit.setDescription(Messages.getString("ExecutionListViewImpl.detail"));
                btnEdit.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        presenter.navigateToEventHandler(PipelineEdit.class, source.getItem(itemId).getItemProperty("pipelineId").getValue());
                    }
                });
                btnEdit.addStyleName("small_button");
                btnEdit.setIcon(new ThemeResource("icons/gear.png"));
                Label lblPipelineName = new Label((String) source.getItem(itemId).getItemProperty(columnId).getValue());
                lblPipelineName.setStyleName("clickable-table-cell");
                HorizontalLayout colLayout = new HorizontalLayout(btnEdit, lblPipelineName);
                colLayout.setSpacing(true);
                colLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                    @Override
                    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                        if (event.getClickedComponent() == btnEdit) {
                            return;
                        }
                        changeURI((Long) itemId);
                        presenter.showDebugEventHandler((Long) itemId);
                        monitorTable.select(itemId);
                    }
                });
                return colLayout;
            }
        });

        //Status column. Contains status icons.
        executionTable.addGeneratedColumn("status", new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(CustomTable source, Object itemId,
                    Object columnId) {
                PipelineExecutionStatus type = (PipelineExecutionStatus) source.getItem(itemId)
                        .getItemProperty(columnId).getValue();
                if (type != null) {
                    ThemeResource img = DecorationHelper.getIconForExecutionStatus(type);
                    Embedded emb = new Embedded(type.name(), img);
                    emb.setDescription(type.name());
                    return emb;
                } else {
                    return null;
                }
            }
        });

        //Debug column. Contains debug icons.
        executionTable.addGeneratedColumn("isDebugging", new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(CustomTable source, Object itemId,
                    Object columnId) {
                boolean inDebug = (boolean) source.getItem(itemId).getItemProperty(columnId).getValue();
                Embedded emb;
                if (inDebug) {
                    emb = new Embedded(Messages.getString("ExecutionListViewImpl.inDebug.true"), new ThemeResource("icons/debug.png"));
                    emb.setDescription(Messages.getString("ExecutionListViewImpl.TRUE"));
                } else {
                    emb = new Embedded(Messages.getString("ExecutionListViewImpl.inDebug.false"), new ThemeResource("icons/no_debug.png"));
                    emb.setDescription(Messages.getString("ExecutionListViewImpl.FALSE"));
                }
                return emb;
            }
        });

        executionTable.addGeneratedColumn("duration", new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(CustomTable source, Object itemId, Object columnId) {
                long duration = (long) source.getItem(itemId).getItemProperty(columnId).getValue();
                //It is refreshed only upon change in db, so for running pipeline it is not refreshed
                PipelineExecutionStatus status = (PipelineExecutionStatus) source.getItem(itemId).getItemProperty("status").getValue();
                if (duration == -1 && (status == RUNNING || status == PipelineExecutionStatus.CANCELLING)) {
                    Date start = (Date) source.getItem(itemId).getItemProperty("start").getValue();
                    if (start != null) {
                        duration = (new Date()).getTime() - start.getTime();
                        Label durationLabel = new Label(DecorationHelper.formatDuration(duration));
                        durationLabel.setSizeUndefined();
                        durationLabel.setImmediate(true);
                        runTimeLabels.put(start, durationLabel);
                        return durationLabel;
                    }
                }
                return DecorationHelper.formatDuration(duration);
            }
        });
        executionTable.addGeneratedColumn("schedule", new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(CustomTable source, Object itemId, Object columnId) {
                boolean isScheduled = (boolean) source.getItem(itemId).getItemProperty(columnId).getValue();
                Embedded emb = DecorationHelper.getIconForScheduled(isScheduled);
                return emb;
            }
        });

        // add generated columns to the executionTable
        executionTable.addGeneratedColumn("actions", 0, createColumnGenerator(presenter));
        executionTable.setColumnHeader("actions", Messages.getString("ExecutionListViewImpl.actions"));
        executionTable.setVisibleColumns();
        executionTable.addListener(new PagedFilterTable.PageChangeListener() {
            @Override
            public void pageChanged(PagedTableChangeEvent event) {
                int newPageNumber = event.getCurrentPage();
                presenter.pageChangedHandler(newPageNumber);
            }
        });
        executionTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                for (Object id : event.getContainer().getContainerPropertyIds()) {
                    Object filterValue = executionTable.getFilterFieldValue(id);
                    presenter.filterParameterEventHander((String) id, filterValue);
                }
            }
        });

        return executionTable;
    }

    private void changeURI(long executionId) {
        String uriFragment = Page.getCurrent().getUriFragment();
        ParametersHandler handler = new ParametersHandler(uriFragment);
        handler.addParameter("exec", "" + executionId);
        ((AppEntry) UI.getCurrent()).setUriFragment(handler.getUriFragment(), false);
    }

    @Override
    public void setSelectedRow(Long execId) {
        monitorTable.select(execId);
        changeURI(execId);
    }

    @Override
    public void setFilter(String name, Object value) {
        monitorTable.setFilterFieldValue(name, value);
    }

    @Override
    public void setPage(int pageNumber) {
        monitorTable.setCurrentPage(pageNumber);
    }

    /**
     * Settings icons to the table filters "status" and "debug"
     * 
     * @author Bogo
     */
    class filterDecorator extends IntlibFilterDecorator {

        @Override
        public String getEnumFilterDisplayName(Object propertyId, Object value) {
            if (propertyId.equals("status")) {
                return ((PipelineExecutionStatus) value).name();
            }
            return super.getEnumFilterDisplayName(propertyId, value);
        }

        @Override
        public Resource getEnumFilterIcon(Object propertyId, Object value) {
            if (propertyId.equals("status")) {
                PipelineExecutionStatus type = (PipelineExecutionStatus) value;
                ThemeResource img = null;
                switch (type) {
                    case FINISHED_SUCCESS:
                        img = new ThemeResource("icons/ok.png");
                        break;
                    case FINISHED_WARNING:
                        img = new ThemeResource("icons/warning.png");
                        break;
                    case FAILED:
                        img = new ThemeResource("icons/error.png");
                        break;
                    case RUNNING:
                        img = new ThemeResource("icons/running.png");
                        break;
                    case QUEUED:
                        img = new ThemeResource("icons/queued.png");
                        break;
                    case CANCELLED:
                        img = new ThemeResource("icons/cancelled.png");
                        break;
                    case CANCELLING:
                        img = new ThemeResource("icons/cancelling.png");
                        break;
                    default:
                        //no icon
                        break;
                }
                return img;
            }
            return super.getEnumFilterIcon(propertyId, value);
        }

        @Override
        public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
            if (propertyId.equals("isDebugging")) {
                if (value) {
                    return Messages.getString("ExecutionListViewImpl.debug");
                } else {
                    return Messages.getString("ExecutionListViewImpl.run");
                }
            }
            return super.getBooleanFilterDisplayName(propertyId, value);
        }

        @Override
        public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
            if (propertyId.equals("isDebugging")) {
                if (value) {
                    return new ThemeResource("icons/debug.png");
                } else {
                    return new ThemeResource("icons/no_debug.png");
                }
            }
            return super.getBooleanFilterIcon(propertyId, value);
        }
    }

    @Override
    public int getExecPage(Long execId) {
        Iterator<?> it = monitorTable.getItemIds().iterator();
        int index = 0;
        while (it.hasNext()) {
            Long id = (Long) it.next();
            if (id.equals(execId)) {
                return (index / monitorTable.getPageLength()) + 1; // pages are from 1
            }
            index++;
        }
        return 0;
    }

    @Override
    public boolean hasExecution(long executionId) {
        return monitorTable.getItemIds().contains(executionId);
    };

    @Override
    public void resetFilters() {
        monitorTable.resetFilters();
    }
}
