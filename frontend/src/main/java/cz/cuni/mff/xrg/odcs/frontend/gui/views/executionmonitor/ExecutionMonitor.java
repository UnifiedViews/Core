package cz.cuni.mff.xrg.odcs.frontend.gui.views.executionmonitor;

import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibFilterDecorator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import com.github.wolfie.refresher.Refresher;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.CANCELLED;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.FAILED;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.FINISHED_SUCCESS;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.FINISHED_WARNING;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.RUNNING;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.SCHEDULED;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.App;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.ContainerFactory;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.IntlibHelper;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.RefreshManager;
import cz.cuni.mff.xrg.odcs.frontend.container.IntlibLazyQueryContainer;
import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.*;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import ru.xpoft.vaadin.VaadinView;

/**
 * GUI for Execution Monitor page which opens from the main menu. Contains table
 * with pipeline execution records. Is the opportunity to open
 * {@link DebuggingView} from the table
 *
 * @author Maria Kukhar
 * @author Bogo
 */
@org.springframework.stereotype.Component
@Scope("prototype")
@VaadinView(ExecutionMonitor.NAME)
public class ExecutionMonitor extends ViewComponent implements ClickListener {
	
	@Autowired
	private ContainerFactory cf;

	/**
	 * View name.
	 */
	public static final String NAME = "ExecutionMonitor";
	
	@AutoGenerated
	private VerticalLayout monitorTableLayout;
	private VerticalLayout logLayout;
	private HorizontalSplitPanel hsplit;
	private Panel mainLayout;
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitor.class);
	/**
	 * Table contains pipeline executions.
	 */
	private IntlibPagedTable monitorTable;
	private Container tableData;
	private Long exeId;
	
	private static final String[] visibleCols = new String[] {
		"start", "pipeline.name", "duration", "user", "status",
		"isDebugging", "obsolete", "actions", "report"
	};
	
	static String[] headers = new String[] {
		"Date", "Name", "Run time", "User", "Status",
		"Debug", "Obsolete", "Actions", "Report"
	};
	
	private Date lastLoad;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public ExecutionMonitor() {
	}

	@Override
	public boolean isModified() {
		//There are no editable fields.
		return false;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		String strExecId = event.getParameters();
		if (strExecId == null || strExecId.isEmpty()) {
			return;
		}
		try {
			Long execId = Long.parseLong(strExecId);
			showExecutionDetail(execId);
		} catch (NumberFormatException e) {
			LOG.warn("Invalid parameter for execution monitor.", e);
		}
	}

	/**
	 * Building layout with {@link DebuggingView}.
	 *
	 * @return logLayout VerticalLayout contains {@link DebuggingView}
	 */
	private VerticalLayout buildlogLayout() {

		logLayout = new VerticalLayout();
		logLayout.setImmediate(true);
		logLayout.setMargin(true);
		logLayout.setSpacing(true);
		logLayout.setWidth("100%");
		logLayout.setHeight("100%");

		PipelineExecution pipelineExec = App.getApp().getPipelines()
				.getExecution(exeId);
		DebuggingView debugView = new DebuggingView(pipelineExec, null,
				pipelineExec.isDebugging(), false);
//        debugView.addListener(new Listener() {
//            @Override
//            public void componentEvent(Event event) {
//                if (event.getComponent().getClass() == DebuggingView.class) {
//                    refreshData();
//                }
//            }
//        });
		logLayout.addComponent(debugView);
		logLayout.setExpandRatio(debugView, 1.0f);

		//Layout for buttons  Close and  Export on the bottom
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setWidth("100%");

		Button buttonClose = new Button();
		buttonClose.setCaption("Close");
		buttonClose.setHeight("25px");
		buttonClose.setWidth("100px");
		buttonClose
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//App.getApp().getRefreshThread().refreshExecution(null, null);
				App.getApp().getRefreshManager().removeListener(RefreshManager.DEBUGGINGVIEW);

				hsplit.setSplitPosition(100, Unit.PERCENTAGE);
				hsplit.setHeight("100%");
				hsplit.setLocked(true);
			}
		});
		buttonBar.addComponent(buttonClose);
		buttonBar.setComponentAlignment(buttonClose, Alignment.BOTTOM_LEFT);

		Button buttonExport = new Button();
		buttonExport.setCaption("Export");
		buttonExport.setHeight("25px");
		buttonExport.setWidth("100px");
		buttonExport
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
			}
		});
		buttonBar.addComponent(buttonExport);
		buttonBar.setComponentAlignment(buttonExport, Alignment.BOTTOM_RIGHT);

		logLayout.addComponent(buttonBar);
		logLayout.setExpandRatio(buttonBar, 0);

		if (pipelineExec.getStatus() == RUNNING || pipelineExec.getStatus() == SCHEDULED) {
			//App.getApp().getRefreshThread().refreshExecution(pipelineExec, debugView);
			App.getApp().getRefreshManager().addListener(RefreshManager.DEBUGGINGVIEW, RefreshManager.getDebugRefresher(debugView, pipelineExec));
		}
		return logLayout;

	}

	/**
	 * Builds main layout that represent as a split panel. Right side of it
	 * contains monitor table, left side contains debugging view that shown
	 * after request only.
	 *
	 * @return mainLayout Panel with all components of Execution Monitor page.
	 */
	private Panel buildMainLayout() {
		// common part: create layout
		mainLayout = new Panel("");
		hsplit = new HorizontalSplitPanel();
		mainLayout.setContent(hsplit);

		monitorTableLayout = new VerticalLayout();
		monitorTableLayout.setImmediate(true);
		monitorTableLayout.setMargin(true);
		monitorTableLayout.setSpacing(true);
		monitorTableLayout.setWidth("100%");
		monitorTableLayout.setHeight("100%");


		// top-level component properties

		setWidth("100%");
		setHeight("100%");

		//Layout for buttons Refresh and Clear Filters on the top.
		HorizontalLayout topLine = new HorizontalLayout();
		topLine.setSpacing(true);
		//topLine.setWidth(100, Unit.PERCENTAGE);
		//Refresh button. Refreshing the table
		Button refreshButton = new Button("Refresh", new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				refresh();
			}
		});
		refreshButton.setWidth("120px");
		topLine.addComponent(refreshButton);
		//topLine.setComponentAlignment(refreshButton, Alignment.MIDDLE_RIGHT);
		//Clear Filters button. Clearing filters on the table with executions.
		Button buttonDeleteFilters = new Button();
		buttonDeleteFilters.setCaption("Clear Filters");
		buttonDeleteFilters.setHeight("25px");
		buttonDeleteFilters.setWidth("120px");
		buttonDeleteFilters
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				monitorTable.resetFilters();
				monitorTable.setFilterFieldVisible("actions", false);
				monitorTable.setFilterFieldVisible("duration", false);
			}
		});
		topLine.addComponent(buttonDeleteFilters);
		//topLine.setComponentAlignment(buttonDeleteFilters, Alignment.MIDDLE_RIGHT);

//        Label topLineFiller = new Label();
//        topLine.addComponentAsFirst(topLineFiller);
//        topLine.setExpandRatio(topLineFiller, 1.0f);
		monitorTableLayout.addComponent(topLine);

		tableData = cf.createExecutions();
		lastLoad = new Date();

		//table with pipeline execution records
		monitorTable = new IntlibPagedTable();
		monitorTable.setSelectable(true);
		monitorTable.setContainerDataSource(tableData);
		monitorTable.setWidth("100%");
		monitorTable.setHeight("100%");
		monitorTable.setImmediate(true);
		monitorTable.setColumnWidth("obsolete", 60);
		monitorTable.setColumnWidth("status", 50);
		monitorTable.setColumnWidth("isDebugging", 50);
		monitorTable.setColumnWidth("duration", 60);
		monitorTable.setColumnWidth("actions", 200);

		//sorting by execution date
		Object property = "start";
		monitorTable.setSortContainerPropertyId(property);
		monitorTable.setSortAscending(false);
		monitorTable.sort();
		//monitorTable.setSortEnabled(true);

		//Status column. Contains status icons.
		monitorTable.addGeneratedColumn("status", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId,
					Object columnId) {
				PipelineExecutionStatus type = (PipelineExecutionStatus) source.getItem(itemId)
						.getItemProperty(columnId).getValue();
				if (type != null) {
					ThemeResource img = IntlibHelper.getIconForExecutionStatus(type);
					Embedded emb = new Embedded(type.name(), img);
					emb.setDescription(type.name());
					return emb;
				} else {
					return null;
				}
			}
		});

		//Debug column. Contains debug icons.
		monitorTable.addGeneratedColumn("isDebugging", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId,
					Object columnId) {
				boolean inDebug = (boolean) source.getItem(itemId).getItemProperty(columnId).getValue();
				Embedded emb;
				if (inDebug) {
					emb = new Embedded("True", new ThemeResource("icons/debug.png"));
					emb.setDescription("TRUE");
				} else {
					emb = new Embedded("False", new ThemeResource("icons/no_debug.png"));
					emb.setDescription("FALSE");
				}
				return emb;
			}
		});

		monitorTable.addGeneratedColumn("duration", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId, Object columnId) {
				long duration = (long) source.getItem(itemId).getItemProperty(columnId).getValue();
				//It is refreshed only upon change in db, so for running pipeline it is not refreshed
//				if(duration == -1 && (PipelineExecutionStatus) source.getItem(itemId).getItemProperty("status").getValue() == RUNNING) {
//					Date start = (Date) source.getItem(itemId).getItemProperty("start").getValue();
//					duration = (new Date()).getTime() - start.getTime();
//				}
				return IntlibHelper.formatDuration(duration);
			}
		});

		//Actions column. Contains actions buttons: Debug data, Show log, Cancel.
		monitorTable.addGeneratedColumn("actions",
				new GenerateActionColumnMonitor(this));
		monitorTable.addGeneratedColumn("user", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId, Object columnId) {
				return "";
			}
		});
		monitorTable.addGeneratedColumn("obsolete", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId, Object columnId) {
				return "";
			}
		});
		monitorTable.addGeneratedColumn("report", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId, Object columnId) {
				return "";
			}
		});

		monitorTable.setVisibleColumns(visibleCols); // Set visible columns
		monitorTable.setColumnHeaders(headers);
		monitorTableLayout.addComponent(monitorTable);
		monitorTableLayout.addComponent(monitorTable.createControls());
		monitorTable.setPageLength(20);
		monitorTable.setFilterDecorator(new filterDecorator());
		monitorTable.setFilterBarVisible(true);
		monitorTable.setFilterFieldVisible("actions", false);
		monitorTable.setFilterFieldVisible("duration", false);
		monitorTable.addItemClickListener(
				new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				//debugging view open
				if (!monitorTable.isSelected(event.getItemId())) {
					Long executionId = (long) event.getItem().getItemProperty("id").getValue();
					showExecutionDetail(executionId);
				} else {
				}
			}
		});


		hsplit.setFirstComponent(monitorTableLayout);
		hsplit.setSecondComponent(null);
		hsplit.setSplitPosition(100, Unit.PERCENTAGE);
		hsplit.setLocked(true);

		monitorTable.refreshRowCache();

		App.getApp().getRefreshManager().addListener(RefreshManager.EXECUTION_MONITOR, new Refresher.RefreshListener() {
			@Override
			public void refresh(Refresher source) {
				ExecutionMonitor.this.refresh();
				LOG.debug("ExecutionMonitor refreshed.");
			}
		});

		return mainLayout;
	}

	/**
	 * Calls for refresh {@link #monitorTable}.
	 */
	public void refresh() {
		Date now = new Date();
		boolean hasModifiedExecutions = App.getApp().getPipelines().hasModifiedExecutions(lastLoad);
		if (hasModifiedExecutions) {
			int page = monitorTable.getCurrentPage();
			Object selectedRow = monitorTable.getValue();
			IntlibLazyQueryContainer c = (IntlibLazyQueryContainer) monitorTable.getContainerDataSource().getContainer();
			c.refresh();
			monitorTable.setCurrentPage(page);
			monitorTable.setValue(selectedRow);
			lastLoad = now;
		} else {
			//monitorTable.refreshRowCache();
		}
	}

	/**
	 * Identify the button type was pushed (cancel, showlog, debug ) with the
	 * help of {@link ActionButtonData}. Then produces a corresponding action.
	 * Opens {@link DebuggingView}
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button senderButton = event.getButton();
		if (senderButton != null) {
			ActionButtonData senderData = (ActionButtonData) senderButton
					.getData();
			String caption = senderData.action;
			Object itemId = senderData.data;

			Long execId = (Long) tableData.getContainerProperty(itemId, "id")
					.getValue();
			switch (caption) {
				case "cancel":
					PipelineExecution pipelineExec =
							App.getApp().getPipelines().getExecution(execId);
					pipelineExec.stop();
					App.getApp().getPipelines().save(pipelineExec);
					senderButton.setVisible(false);
					// this load new data from database
					refresh();
					Notification.show("Pipeline execution cancelled.", Notification.Type.HUMANIZED_MESSAGE);
					break;
				case "showlog":
					monitorTable.setValue(itemId);
					showExecutionDetail(execId);
					break;
				case "debug":
					monitorTable.setValue(itemId);
					showExecutionDetail(execId);
					break;
				case "rerun":
					PipelineExecution exec = App.getApp().getPipelines().getExecution(execId);
					IntlibHelper.runPipeline(exec.getPipeline(), false);
					refresh();
					break;
				case "redebug":
					PipelineExecution execDebug = App.getApp().getPipelines().getExecution(execId);
					IntlibHelper.runPipeline(execDebug.getPipeline(), true);
					refresh();
					break;
			}

		}
	}

	private void showExecutionDetail(Long executionId) {
		App.getApp().getRefreshManager().removeListener(RefreshManager.DEBUGGINGVIEW);
		exeId = executionId;
		logLayout = buildlogLayout();
		hsplit.setSplitPosition(55, Unit.PERCENTAGE);
		hsplit.setSecondComponent(logLayout);
		hsplit.setHeight("-1px");
		hsplit.setLocked(false);
	}

	/**
	 * Settings icons to the table filters "status" and "debug"
	 *
	 * @author Bogo
	 *
	 */
	class filterDecorator extends IntlibFilterDecorator {

		@Override
		public String getEnumFilterDisplayName(Object propertyId, Object value) {
			if (propertyId == "status") {
				return ((PipelineExecutionStatus) value).name();
			}
			return super.getEnumFilterDisplayName(propertyId, value);
		}

		@Override
		public Resource getEnumFilterIcon(Object propertyId, Object value) {
			if (propertyId == "status") {
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
					case SCHEDULED:
						img = new ThemeResource("icons/scheduled.png");
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
					return "Debug";
				} else {
					return "Run";
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
	};
}