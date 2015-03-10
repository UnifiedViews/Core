package cz.cuni.mff.xrg.odcs.frontend.gui.views;

import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.QUEUED;
import static cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus.RUNNING;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.vaadin.dialogs.ConfirmDialog;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthAwarePermissionEvaluator;
import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Position;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportService;
import cz.cuni.mff.xrg.odcs.commons.app.user.Role;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.MaxLengthValidator;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.PipelineHelper;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.RefreshManager;
import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DPUTree;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DebuggingView;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.PipelineConflicts;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.DetailClosedEvent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.FormattingEnabledEvent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.GraphChangedEvent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.PipelineCanvas;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.ResizedEvent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.ShowDebugEvent;
import cz.cuni.mff.xrg.odcs.frontend.gui.dialog.PipelineExport;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.executionlist.ExecutionListPresenterImpl;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.navigation.Address;

/**
 * Page for creating new pipeline or editing existing pipeline.
 * 
 * @author Bogo
 */
@org.springframework.stereotype.Component
@Scope("prototype")
@Address(url = "PipelineEdit")
public class PipelineEdit extends ViewComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineEdit.class);

    private VerticalLayout mainLayout;

    private GridLayout formattingBar;

    private Label lblPipelineName;

    private Label readOnlyLabel;

    private Label idLabel;

    private Label idValue;

    private Label author;

    private TextField pipelineName;

    private TextArea pipelineDescription;

    private OptionGroup pipelineVisibility;

    private Pipeline pipeline = null;

    PipelineCanvas pipelineCanvas;

    @Autowired
    DPUTree dpuTree;

    TabSheet tabSheet;

    DragAndDropWrapper dadWrapper;

    Panel canvasPanel;

    Button undo;

    /**
     * Constant representing standard mode of pipeline edit.
     */
    public final static String STANDARD_MODE = "standard_mode";

    /**
     * Constant representing develop mode of pipeline edit.
     */
    public final static String DEVELOP_MODE = "develop_mode";

    private String canvasMode = DEVELOP_MODE;

    private Tab standardTab;

    private Tab developTab;

    Button buttonSave;

    Button buttonSaveAndClose;

    Button buttonSaveAndCloseAndDebug;

    Button buttonCancel;

    Button buttonConflicts;

    Button buttonCopy;

    Button buttonCopyAndClose;

    Button buttonExport;

    private Button btnMinimize;

    private Button btnExpand;

    //Paralel editing components
    private Label editConflicts;

    private HorizontalLayout paralelInfoLayout;

    private Button buttonRefresh;

    private boolean isExpanded = true;

    private GridLayout pipelineSettingsLayout;

    HorizontalLayout buttonBar;

    private ShowDebugEvent sde;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private DPUFacade dpuFacade;

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    private RefreshManager refreshManager;

    @Autowired
    private PipelineHelper pipelineHelper;

    @Autowired
    private PipelineConflicts conflictDialog;

    @Autowired
    private AuthenticationContext authCtx;

    /**
     * Evaluates permissions of currently logged in user.
     */
    @Autowired
    private AuthAwarePermissionEvaluator permissions;

    /**
     * Access to the application context in order to provide possiblity to
     * create dialogs. TODO: This is give us more power then we need, we should
     * use some dialog factory instead.
     */
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ExportService exportService;

    /**
     * Empty constructor.
     */
    public PipelineEdit() {
        // put init code into enter method
    }

    /**
     * Enter method for PIPELINE_EDIT view.
     * 
     * @param event
     *            {@link ViewChangeEvent}
     */
    @Override
    public void enter(ViewChangeEvent event) {
        refreshManager = ((AppEntry) UI.getCurrent()).getRefreshManager();
        buildMainLayout();
        UI.getCurrent().getPage().addBrowserWindowResizeListener(new Page.BrowserWindowResizeListener() {

            @Override
            public void browserWindowResized(Page.BrowserWindowResizeEvent event) {
                setupComponentSize();
            }
        });
        setCompositionRoot(mainLayout);
        // ..
        this.loadPipeline(event);
        // or use this.entity.getEntity();

        if (this.pipeline == null) {
            return;
        } else {
            setMode(hasPermission("pipeline.save"));
            updateLblPipelineName();
        }

        refreshManager.addListener(RefreshManager.PIPELINE_EDIT, new Refresher.RefreshListener() {

            private long lastRefreshFinished = 0;

            @Override
            public void refresh(Refresher source) {
                if (pipeline != null && new Date().getTime() - lastRefreshFinished > RefreshManager.MIN_REFRESH_INTERVAL) {
                    LOG.trace("refresh - 0");
                    pipelineFacade.createOpenEvent(pipeline);
                    LOG.trace("refresh - 1");
                    List<OpenEvent> openEvents = pipelineFacade.getOpenPipelineEvents(pipeline);
                    LOG.trace("refresh - 2");
                    if (!pipelineFacade.isUpToDate(pipeline)) {
                        editConflicts.setValue(Messages.getString("PipelineEdit.conflict.another.user"));
                        paralelInfoLayout.setVisible(true);
                        buttonRefresh.setVisible(true);
                    } else if (openEvents.isEmpty()) {
                        paralelInfoLayout.setVisible(false);
                    } else {
                        String message;
                        if (openEvents.size() == 1) {
                            message = Messages.getString("PipelineEdit.user.browsing", openEvents.get(0).getUser().getUsername());
                        } else {
                            String userList = "";
                            for (OpenEvent openEvent : openEvents) {
                                userList += String.format("%s %s", openEvents.indexOf(openEvent) == 0 ? "" : ",", openEvent.getUser().getUsername());
                            }
                            message = Messages.getString("PipelineEdit.users.browsing", userList);
                        }
                        editConflicts.setValue(message);
                        paralelInfoLayout.setVisible(true);
                        buttonRefresh.setVisible(false);
                    }
                    lastRefreshFinished = new Date().getTime();
                }
                LOG.debug("Open pipelines checked.");
            }
        });

        //Resizing canvas
        UI.getCurrent().setImmediate(true);
    }

    /**
     * Builds main layout of the page.
     * 
     * @return {@link VerticalLayout} is the main layout of the view.
     */
    private VerticalLayout buildMainLayout() {
        isExpanded = true;

        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setImmediate(true);

        // top-level component properties
        //setSizeUndefined();
        // label
        lblPipelineName = new Label();
        lblPipelineName.setImmediate(false);
        lblPipelineName.setWidth("-1px");
        lblPipelineName.setHeight("-1px");
        lblPipelineName.setContentMode(ContentMode.HTML);

        readOnlyLabel = new Label(Messages.getString("PipelineEdit.read-only.mode"));
        readOnlyLabel.setStyleName("readOnlyLabel");
        readOnlyLabel.setVisible(false);

        HorizontalLayout topLine = new HorizontalLayout(lblPipelineName, readOnlyLabel);
        topLine.setComponentAlignment(readOnlyLabel, Alignment.MIDDLE_CENTER);
        btnMinimize = new Button();
        btnMinimize.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setDetailState(false);
            }
        });
        btnMinimize.setStyleName(BaseTheme.BUTTON_LINK);
        btnMinimize.addStyleName("expand-minimize");
        btnMinimize.setIcon(new ThemeResource("icons/collapse.svg"));
        btnMinimize.setDescription(Messages.getString("PipelineEdit.minimize"));
        btnMinimize.setVisible(isExpanded);
        topLine.addComponent(btnMinimize);
        topLine.setExpandRatio(btnMinimize, 1.0f);
        topLine.setComponentAlignment(btnMinimize, Alignment.MIDDLE_RIGHT);
        btnExpand = new Button();
        btnExpand.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setDetailState(true);
            }
        });
        btnExpand.setStyleName(BaseTheme.BUTTON_LINK);
        btnExpand.addStyleName("expand-minimize");
        btnExpand.setIcon(new ThemeResource("icons/expand.svg"));
        btnExpand.setDescription(Messages.getString("PipelineEdit.exapand"));
        btnExpand.setVisible(false);
        topLine.addComponent(btnExpand);
        topLine.setExpandRatio(btnExpand, 1.0f);
        topLine.setComponentAlignment(btnExpand, Alignment.MIDDLE_RIGHT);

        btnExpand.setVisible(!isExpanded);
        //topLine.setWidth("100%");
        mainLayout.addComponent(topLine);

        pipelineSettingsLayout = buildPipelineSettingsLayout();
        mainLayout.addComponent(pipelineSettingsLayout);

        CssLayout layout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof TabSheet) {
                    return "margin-left: 0px; margin-top: 0px;";
                } else if (c instanceof Panel) {
                    return "position: fixed; left: 20px; top: 300px; max-height:600px; overflow-y:auto; overflow-x: hidden; max-width: 375px";
                } else if (c instanceof HorizontalLayout) {
                    if (c.equals(buttonBar)) {
                        return "position: fixed; bottom: 0px; left: 20px; background: #eee;";
                    } else if (c.equals(paralelInfoLayout)) {
                        return "position: fixed; left:400px; top: 300px;";
                    }
                } else if (c instanceof VerticalLayout) {
                    return "position: fixed; right: 40px; top: 300px;";
                }
                return null;
            }
        };

        //layout.setMargin(true);
        pipelineCanvas = ((AppEntry) UI.getCurrent()).getBean(PipelineCanvas.class);
        pipelineCanvas.setImmediate(true);
        pipelineCanvas.setWidth(1060, Unit.PIXELS);
        pipelineCanvas.setHeight(630, Unit.PIXELS);
        pipelineCanvas.init();
        pipelineCanvas.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
                if (event.getClass() != DetailClosedEvent.class) {
                    return;
                }
                DetailClosedEvent dce = (DetailClosedEvent) event;

                Class klass = dce.getDetailClass();
                if (klass == Node.class) {
                    dpuTree.refresh();
                    dpuTree.markAsDirty();
                    setupButtons();
                } else if (klass == Edge.class) {
                    setupButtons();
                }
            }
        });

        pipelineCanvas.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
                if (event.getClass() != ShowDebugEvent.class) {
                    return;
                }
                sde = (ShowDebugEvent) event;
                savePipeline("debug");
            }
        });
        pipelineCanvas.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
                if (event.getClass() != GraphChangedEvent.class) {
                    return;
                }

                if (((GraphChangedEvent) event).isUndoable()) {
                    undo.setEnabled(true);
                }
                setupButtons();

            }
        });
        pipelineCanvas.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
                if (event.getClass() != FormattingEnabledEvent.class) {
                    return;
                }
                formattingBar.setEnabled(((FormattingEnabledEvent) event).isEnabled());
            }
        });
        pipelineCanvas.addListener(new Listener() {

            @Override
            public void componentEvent(Event event) {
                if (event.getClass() != ResizedEvent.class) {
                    return;
                }
                ResizedEvent resizedEvent = (ResizedEvent) event;
                calculateCanvasDimensions(resizedEvent.getWidth(), resizedEvent.getHeight());
            }
        });

        dadWrapper = new DragAndDropWrapper(pipelineCanvas);
        dadWrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.NONE);
        dadWrapper.setWidth(1060, Unit.PIXELS);
        dadWrapper.setHeight(630, Unit.PIXELS);
        dadWrapper.setDropHandler(new DropHandler() {
            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            @Override
            public void drop(DragAndDropEvent event) {
                if (canvasMode.equals(STANDARD_MODE)) {
                    return;
                }
                Transferable t = (Transferable) event.getTransferable();
                DragAndDropWrapper.WrapperTargetDetails details = (DragAndDropWrapper.WrapperTargetDetails) event.getTargetDetails();
                MouseEventDetails mouse = details.getMouseEvent();

                Object obj = t.getData("itemId");

                if (obj.getClass() == DPUTemplateRecord.class) {
                    DPUTemplateRecord dpu = (DPUTemplateRecord) obj;
                    if (dpuFacade.getAllTemplates().contains(dpu)) {
                        pipelineCanvas.addDpu(dpu, mouse.getClientX() - 20, mouse.getClientY() - (isExpanded ? 350 : 150));
                    } else {
                        LOG.warn("Invalid drop operation.");
                    }
                }

            }
        });

        tabSheet = new TabSheet();
        standardTab = tabSheet.addTab(new Label(Messages.getString("PipelineEdit.under.construction")), Messages.getString("PipelineEdit.tab.standard"));
        standardTab.setEnabled(true);

        //canvasPanel = new Panel(dadWrapper);
        developTab = tabSheet.addTab(dadWrapper, Messages.getString("PipelineEdit.tab.develop"));
        tabSheet.setSelectedTab(developTab);
        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                if (event.getTabSheet().getSelectedTab().getClass() != DragAndDropWrapper.class) {
                    if (canvasMode.equals(STANDARD_MODE)) {
                        canvasMode = DEVELOP_MODE;
                        developTab.setCaption(Messages.getString("PipelineEdit.standardMode.caption.develop"));
                        standardTab.setCaption(Messages.getString("PipelineEdit.standardMode.caption.standard"));
                        tabSheet.setTabPosition(developTab, 1);
                    } else {
                        canvasMode = STANDARD_MODE;
                        standardTab.setCaption(Messages.getString("PipelineEdit.developMode.caption.develop"));
                        developTab.setCaption(Messages.getString("PipelineEdit.developMode.caption.standard"));
                        tabSheet.setTabPosition(developTab, 0);
                    }
                    pipelineCanvas.changeMode(canvasMode);
                    tabSheet.setSelectedTab(developTab);
                }
            }
        });
        tabSheet.setImmediate(true);

        layout.addComponent(tabSheet);

        Panel leftPanel = new Panel();
        //VerticalLayout left = new VerticalLayout();
        leftPanel.setStyleName("changingposition");
        //left.setWidth(250, Unit.PIXELS);
        dpuTree.setExpandable(true);
        dpuTree.setStyleName("dpuTree");
        dpuTree.setSizeUndefined();
        dpuTree.setDragable(true);
        dpuTree.fillTree();
        //left.addComponentAsFirst(dpuTree);
        leftPanel.setContent(dpuTree);
        leftPanel.setSizeUndefined();
        layout.addComponent(leftPanel);

        editConflicts = new Label();
        editConflicts.setImmediate(true);

        buttonRefresh = new Button(Messages.getString("PipelineEdit.refresh"));
        buttonRefresh.setHeight("25px");
        buttonRefresh.setWidth("100px");
        buttonRefresh.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                refreshPipeline();
                setFields();
                paralelInfoLayout.setVisible(false);
            }
        });

        paralelInfoLayout = new HorizontalLayout(editConflicts, buttonRefresh);
        paralelInfoLayout.setSpacing(true);
        paralelInfoLayout.setVisible(false);
        paralelInfoLayout.addStyleName("editConflicts");
        paralelInfoLayout.addStyleName("changingposition");
        paralelInfoLayout.setSizeUndefined();
        layout.addComponent(paralelInfoLayout);

        Button zoomIn = new Button();
        zoomIn.setDescription(Messages.getString("PipelineEdit.zoomIn"));
        zoomIn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Position bounds = pipelineCanvas.zoom(true);
                calculateCanvasDimensions(bounds.getX(), bounds.getY());
            }
        });
        zoomIn.setIcon(new ThemeResource("icons/zoom_in.svg"), Messages.getString("PipelineEdit.icon.zoomIn"));
        //zoomIn.setWidth("110px");
        Button zoomOut = new Button();
        zoomOut.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Position bounds = pipelineCanvas.zoom(false);
                calculateCanvasDimensions(bounds.getX(), bounds.getY());
            }
        });
        zoomOut.setDescription(Messages.getString("PipelineEdit.zoomOut"));
        zoomOut.setIcon(new ThemeResource("icons/zoom_out.svg"), Messages.getString("PipelineEdit.icon.zoomOut"));
        //zoomOut.setWidth("110px");
        undo = new Button();
        undo.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (!pipelineCanvas.undo()) {
                    event.getButton().setEnabled(false);
                }
            }
        });
        undo.setEnabled(false);
        undo.setImmediate(true);
        undo.setDescription(Messages.getString("PipelineEdit.undo"));
        undo.setIcon(new ThemeResource("icons/undo.svg"), Messages.getString("PipelineEdit.icon.undo"));
        //undo.setWidth("110px");
        HorizontalLayout topActions = new HorizontalLayout(zoomIn, zoomOut, undo);

        formattingBar = createFormattingBar();
        formattingBar.setEnabled(false);
        VerticalLayout actionBar = new VerticalLayout(topActions, formattingBar);
        actionBar.setStyleName("changingposition");
        actionBar.setSizeUndefined();

        layout.addComponent(actionBar);

        buttonBar = new HorizontalLayout();

        Button buttonRevert = new Button(Messages.getString("PipelineEdit.revert"));
        buttonRevert.setHeight("25px");
        buttonRevert.setWidth("100px");
        buttonRevert.setEnabled(false);
        buttonRevert.setVisible(false);
        buttonRevert.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
            }
        });
        buttonBar.addComponent(buttonRevert);

        HorizontalLayout leftPartOfButtonBar = new HorizontalLayout();
        leftPartOfButtonBar.setSpacing(true);
        leftPartOfButtonBar.setMargin(new MarginInfo(false, true, false, false));

        Button buttonValidate = new Button(Messages.getString("PipelineEdit.validate"));
        buttonValidate.setHeight("25px");
        buttonValidate.setWidth("100px");
        buttonValidate.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                pipelineCanvas.validateGraph();
            }
        });
        leftPartOfButtonBar.addComponent(buttonValidate);

        buttonConflicts = new Button(Messages.getString("PipelineEdit.conflicts"));
        buttonConflicts.setHeight("25px");
        buttonConflicts.setWidth("100px");
        buttonConflicts.setImmediate(true);
        buttonConflicts.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                showConflictPipeline();

            }
        });
        leftPartOfButtonBar.addComponent(buttonConflicts);
        buttonBar.addComponent(leftPartOfButtonBar);

        Button buttonCommit = new Button(Messages.getString("PipelineEdit.save.and.commit"));
        buttonCommit.setHeight("25px");
        buttonCommit.setWidth("100px");
        buttonCommit.setEnabled(false);
        buttonCommit.setVisible(false);
        buttonCommit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // save current pipeline
                savePipeline("none");
            }
        });
        buttonBar.addComponent(buttonCommit);

        HorizontalLayout rightPartOfButtonBar = new HorizontalLayout();
        rightPartOfButtonBar.setSpacing(true);
        rightPartOfButtonBar.setMargin(new MarginInfo(false, false, false, true));

        HorizontalLayout copyLayout = new HorizontalLayout();

        buttonCopy = new Button(Messages.getString("PipelineEdit.copy"));
        buttonCopy.setHeight("25px");
        buttonCopy.setWidth("100px");
        buttonCopy.setImmediate(true);
        buttonCopy.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // save current pipeline
                if (!pipelineFacade.isUpToDate(pipeline)) {
                    ConfirmDialog.show(UI.getCurrent(),
                            Messages.getString("PipelineEdit.copy.notActual"), Messages.getString("PipelineEdit.copy.notActual.description"), Messages.getString("PipelineEdit.copy.notActual.copyAnyway"), Messages.getString("PipelineEdit.copy.notActual.cancel"), new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {
                                        savePipelineAsNew();
                                        paralelInfoLayout.setVisible(false);
                                    }
                                }
                            });
                } else {
                    savePipelineAsNew();
                }
            }
        });
        copyLayout.addComponent(buttonCopy);
        buttonCopyAndClose = new Button(Messages.getString("PipelineEdit.copy.and.close"));

        buttonCopyAndClose.setHeight("25px");
        buttonCopyAndClose.setWidth("100px");
        buttonCopyAndClose.setImmediate(true);
        buttonCopyAndClose.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (!pipelineFacade.isUpToDate(pipeline)) {
                    ConfirmDialog.show(
                            UI.getCurrent(),
                            Messages.getString("PipelineEdit.copyClose.notActual"), Messages.getString("PipelineEdit.copyClose.notActual.description"), Messages.getString("PipelineEdit.copyClose.notActual.copyAnyway"), Messages.getString("PipelineEdit.copyClose.notActual.cancel"), new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {
                                        savePipelineAsNew();
                                        closeView();
                                    }
                                }
                            });
                } else {
                    savePipelineAsNew();
                    closeView();
                }
            }
        });
        copyLayout.addComponent(buttonCopyAndClose);
        rightPartOfButtonBar.addComponent(copyLayout);

        HorizontalLayout saveLayout = new HorizontalLayout();

        buttonSave = new Button(Messages.getString("PipelineEdit.save"));
        buttonSave.setHeight("25px");
        buttonSave.setWidth("100px");
        buttonSave.setImmediate(true);
        buttonSave.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // save current pipeline
                savePipeline("reload");
            }
        });
        saveLayout.addComponent(buttonSave);

        buttonSaveAndClose = new Button(Messages.getString("PipelineEdit.save.and.close"));
        buttonSaveAndClose.setHeight("25px");
        buttonSaveAndClose.setWidth("100px");
        buttonSaveAndClose.setImmediate(true);
        buttonSaveAndClose.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // save current pipeline
                savePipeline("close");
            }
        });
        saveLayout.addComponent(buttonSaveAndClose);
        buttonSaveAndCloseAndDebug = new Button(Messages.getString("PipelineEdit.save.close.debug"));
        buttonSaveAndCloseAndDebug.setHeight("25px");
        //buttonSaveAndCloseAndDebug.setWidth("100px");
        buttonSaveAndCloseAndDebug.setImmediate(true);
        buttonSaveAndCloseAndDebug.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // save current pipeline
                savePipeline("close&debug");
            }
        });
        saveLayout.addComponent(buttonSaveAndCloseAndDebug);

        rightPartOfButtonBar.addComponent(saveLayout);

        buttonCancel = new Button(Messages.getString("PipelineEdit.close"));

        buttonCancel.setHeight("25px");
        buttonCancel.setWidth("100px");
        buttonCancel.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                //pipelineName.discard();
                //pipelineDescription.discard();
                //pipelineCanvas.cancelChanges();
                closeView();
            }
        });
        rightPartOfButtonBar.addComponent(buttonCancel);

        buttonExport = new Button(Messages.getString("PipelineEdit.export"));
        buttonExport.setHeight("25px");
        buttonExport.setWidth("100px");
        buttonExport.addClickListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                final PipelineExport dialog = new PipelineExport(exportService, pipeline, authCtx);
                UI.getCurrent().addWindow(dialog);
                dialog.bringToFront();
            }
        });

        rightPartOfButtonBar.addComponent(buttonExport);
        buttonBar.addComponent(rightPartOfButtonBar);

        buttonBar.setSpacing(true);
        layout.addComponent(buttonBar);

        mainLayout.addComponent(layout);
        Position bounds = pipelineCanvas.zoom(true);

        calculateCanvasDimensions(bounds.getX(), bounds.getY());
        return mainLayout;
    }

    private void setupComponentSize() {
        int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth() - 30;
        int browserHeight = UI.getCurrent().getPage().getBrowserWindowHeight();
        if (pipelineCanvas.getCanvasWidth() < browserWidth) {
            tabSheet.setWidth(pipelineCanvas.getCanvasWidth() + 40, Unit.PIXELS);
        } else {
            tabSheet.setWidth(100, Unit.PERCENTAGE);
        }
        int tabSheetHeight = browserHeight - (isExpanded ? 305 : 115);
        tabSheet.setHeight(Math.min(tabSheetHeight, pipelineCanvas.getCanvasHeight() + 60), Unit.PIXELS);
        tabSheet.markAsDirty();
    }

    private void showConflictPipeline() {

        // open scheduler dialog
        if (!conflictDialog.isInitialized()) {
            conflictDialog.init();
            conflictDialog.addCloseListener(new CloseListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void windowClose(CloseEvent e) {
                    setupButtons(conflictDialog.getResult());
                }
            });
        }

        // in every case set the data
        conflictDialog.setData(pipeline);

        if (!UI.getCurrent().getWindows().contains(conflictDialog)) {
            UI.getCurrent().addWindow(conflictDialog);
        }
    }

    /**
     * Check for permission.
     * 
     * @param type
     *            Required permission.
     * @return If the user has given permission
     */
    public boolean hasPermission(String type) {
        return permissions.hasPermission(pipeline, type);
    }

    private void setDetailState(boolean expand) {
        isExpanded = expand;
        btnMinimize.setVisible(expand);
        btnExpand.setVisible(!expand);
        pipelineSettingsLayout.setVisible(expand);
        setupComponentSize();
    }

    /**
     * Builds part of layout with pipeline settings.
     * 
     * @return {@link GridLayout} contains controls with information about
     *         pipeline settings.
     * @throws com.vaadin.ui.GridLayout.OverlapsException
     * @throws com.vaadin.ui.GridLayout.OutOfBoundsException
     */
    private GridLayout buildPipelineSettingsLayout() throws OverlapsException, OutOfBoundsException {

        pipelineSettingsLayout = new GridLayout(3, 5);
        pipelineSettingsLayout.setWidth(600, Unit.PIXELS);
        idLabel = new Label(Messages.getString("PipelineEdit.idLabel.id"));
        idLabel.setSizeUndefined();
        pipelineSettingsLayout.addComponent(idLabel, 0, 4);
        idValue = new Label(Messages.getString("PipelineEdit.idValue.id"));
        idValue.setSizeUndefined();
        pipelineSettingsLayout.addComponent(idValue, 1, 4);
        Label nameLabel = new Label(Messages.getString("PipelineEdit.name"));
        nameLabel.setImmediate(false);
        nameLabel.setSizeUndefined();
        pipelineSettingsLayout.addComponent(nameLabel, 0, 0);
        pipelineName = new TextField();
        pipelineName.setImmediate(true);
        pipelineName.setWidth("400px");
        pipelineName.setHeight("-1px");
        pipelineName.setBuffered(true);
        pipelineName.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException(Messages.getString("PipelineEdit.name.empty"));
            }
        });
        pipelineName.addValidator(new MaxLengthValidator(LenghtLimits.PIPELINE_NAME));
        pipelineName.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                setupButtons(true);
            }
        });
        pipelineSettingsLayout.addComponent(pipelineName, 1, 0);
        Label descriptionLabel = new Label(Messages.getString("PipelineEdit.description"));
        descriptionLabel.setImmediate(false);
        descriptionLabel.setWidth("-1px");
        descriptionLabel.setHeight("-1px");
        pipelineSettingsLayout.addComponent(descriptionLabel, 0, 1);
        pipelineDescription = new TextArea();
        pipelineDescription.setImmediate(true);
        pipelineDescription.setWidth("400px");
        pipelineDescription.setHeight("60px");
        pipelineDescription.setBuffered(true);
        pipelineDescription.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                setupButtons(true);
            }
        });
        pipelineSettingsLayout.addComponent(pipelineDescription, 1, 1);

        Label visibilityLabel = new Label(Messages.getString("PipelineEdit.visibility"));
        if (!"organization".equals(appConfig.getString(ConfigProperty.OWNERSHIP_TYPE))) {
            pipelineSettingsLayout.addComponent(visibilityLabel, 0, 2);
        }

        pipelineVisibility = new OptionGroup();
        pipelineVisibility.addStyleName("horizontalgroup");
        pipelineVisibility.addItem(ShareType.PRIVATE);
        pipelineVisibility.setItemCaption(ShareType.PRIVATE, Messages.getString(ShareType.PRIVATE.name()));
        pipelineVisibility.addItem(ShareType.PUBLIC_RO);
        pipelineVisibility.setItemCaption(ShareType.PUBLIC_RO, Messages.getString(ShareType.PUBLIC_RO.name()));
        pipelineVisibility.addItem(ShareType.PUBLIC_RW);
        pipelineVisibility.setItemCaption(ShareType.PUBLIC_RW, Messages.getString(ShareType.PUBLIC_RW.name()));
        pipelineVisibility.setImmediate(true);
        pipelineVisibility.setBuffered(true);
        pipelineVisibility.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                setupButtons(true);
            }
        });

        if (!"organization".equals(appConfig.getString(ConfigProperty.OWNERSHIP_TYPE))) {
            pipelineSettingsLayout.addComponent(pipelineVisibility, 1, 2);
        }
        pipelineSettingsLayout.addComponent(new Label(Messages.getString("PipelineEdit.created.by")), 0, 3);

        author = new Label();
        pipelineSettingsLayout.addComponent(author, 1, 3);

        pipelineSettingsLayout.setStyleName("pipelineSettingsLayout");
        pipelineSettingsLayout.setMargin(true);
        pipelineSettingsLayout.setSpacing(true);

        //pipelineSettingsLayout.setWidth("100%");
        return pipelineSettingsLayout;
    }

    @Override
    public boolean isModified() {
        return (pipelineName.isModified() || pipelineDescription.isModified() || pipelineCanvas.isModified() || pipelineVisibility.isModified()) && hasPermission("pipeline.save");
    }

    @Override
    public boolean saveChanges() {
        return savePipeline("none");
    }

    private void setupButtons() {
        setupButtons(isModified());
    }

    private void setupButtons(boolean isModified) {
        setupButtons(isModified, this.pipeline.getId() == null);
    }

    private void savePipelineAsNew() {
        if (!pipelineFacade.isUpToDate(pipeline)) {
        }
        if (!validate()) {
            return;
        }
        pipeline.setName(pipelineName.getValue());
        pipelineCanvas.saveGraph(pipeline);
        Pipeline copiedPipeline = pipelineFacade.copyPipeline(pipeline);
        pipelineName.setValue(copiedPipeline.getName());
        setIdLabel(copiedPipeline.getId());
        author.setValue(copiedPipeline.getOwner().getUsername());
        pipeline = copiedPipeline;
        finishSavePipeline(false, ShareType.PRIVATE, "reload");
        setMode(true);
    }

    /**
     * Return true if given string is positive number.
     * 
     * @param str
     *            {@link String} to check
     * @return True if given string is positive number, false otherwise.
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(str.charAt(i))) {
            } else {
                return false;
            }
        }
        return true;
    }

    private void setupButtons(boolean enabled, boolean isNew) {
        buttonSave.setEnabled(enabled && hasPermission("pipeline.save"));
        buttonSaveAndClose.setEnabled(enabled && hasPermission("pipeline.save"));
        buttonSaveAndCloseAndDebug.setEnabled(enabled && hasPermission("pipeline.save") && hasPermission("pipeline.runDebug"));
        buttonCopy.setEnabled(!isNew && hasPermission("pipeline.copy"));
        buttonCopyAndClose.setEnabled(!isNew && hasPermission("pipeline.copy"));
        // we reuse copy permision for exportPipeline
        buttonExport.setEnabled(hasPermission("pipeline.export"));
    }

    /**
     * Closes the view and returns to View which user came from, if any.
     */
    private void closeView() {
        ((AppEntry) UI.getCurrent()).navigateToLastView();
    }

    /**
     * Opens given {@link DebuggingView} in new window.
     * 
     * @param debug
     *            {@link DebuggingView} to show.
     */
    private void openDebug(PipelineExecution pExec, final Pipeline pip, final Node debugNode) {
        if (pExec == null) {
            //Solved by dialog if backend is offline in method runPipeline.
            //Notification.show("Pipeline execution failed!", Notification.Type.ERROR_MESSAGE);
            return;
        }
        final DPUInstanceRecord instance = debugNode.getDpuInstance();
        final DebuggingView debug = context.getBean(DebuggingView.class);

        debug.initialize(pExec, instance, true, true);
        debug.setExecution(pExec, instance);

        final Window debugWindow = new Window(Messages.getString("PipelineEdit.debug.window"));
        HorizontalLayout buttonLine = new HorizontalLayout();
        buttonLine.setSpacing(true);
        buttonLine.setWidth(100, Unit.PERCENTAGE);
        Button rerunButton = new Button(Messages.getString("PipelineEdit.rerun"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                PipelineExecution pExec = pipelineHelper.runPipeline(pip, true, debugNode);
                if (pExec == null) {
                    //Solved by dialog if backend is offline in method runPipeline.
                    return;
                }
                debug.setExecution(pExec, instance);
            }
        });
        rerunButton.setWidth(100, Unit.PIXELS);
        buttonLine.addComponent(rerunButton);
        Button closeButton = new Button(Messages.getString("PipelineEdit.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                debugWindow.close();
            }
        });
        closeButton.setWidth(100, Unit.PIXELS);
        buttonLine.addComponent(closeButton);
        Label topLineFiller = new Label();
        buttonLine.addComponentAsFirst(topLineFiller);
        buttonLine.setExpandRatio(topLineFiller, 1.0f);

        VerticalLayout layout = new VerticalLayout(debug, buttonLine);
        debugWindow.setContent(layout);

        debugWindow.setImmediate(true);
        debugWindow.setWidth("700px");
        debugWindow.setHeight("850px");
        debugWindow.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                refreshManager.removeListener(RefreshManager.DEBUGGINGVIEW);
            }
        });
        debugWindow.addResizeListener(new Window.ResizeListener() {
            @Override
            public void windowResized(Window.ResizeEvent e) {
                debug.resize(e.getWindow().getHeight());
            }
        });

        if (pExec.getStatus() == RUNNING || pExec.getStatus() == QUEUED) {
            refreshManager.addListener(RefreshManager.DEBUGGINGVIEW, RefreshManager.getDebugRefresher(debug, pExec, pipelineFacade));
        }
        UI.getCurrent().addWindow(debugWindow);
    }

    /**
     * Loads pipeline with given id from database.
     * 
     * @param id
     *            {@link String} with id of {@link Pipeline} to load
     * @return {@link Pipeline} with given id.
     */
    protected Pipeline loadPipeline(String id) {
        // get data from DB ..
        try {
            this.pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
        } catch (AccessDeniedException e) {
            Notification.show(Messages.getString("PipelineEdit.opening.error"), Messages.getString("PipelineEdit.opening.error.description"), Type.ERROR_MESSAGE);
            closeView();
            return null;
        }
        if (this.pipeline == null) {
            Notification.show(Messages.getString("PipelineEdit.opening.error.notExisting"), Messages.getString("PipelineEdit.opening.error.notExisting.description"), Type.ERROR_MESSAGE);
            closeView();
            return null;
        }
        setIdLabel(pipeline.getId());
        author.setValue(pipeline.getOwner().getUsername());
        pipelineName.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getName()));
        pipelineDescription.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getDescription()));
        pipelineVisibility.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getShareType()));
        setupVisibilityOptions(this.pipeline.getShareType());
        setupButtons(false);
        return pipeline;
    }

    /**
     * Loads pipeline to edit or create. Pipeline entity is loaded into
     * this.entity. If /New parameter is passed in url, create just
     * representation for pipeline.
     * 
     * @param event
     *            {@link ViewChangeEvent} passed from enter method.
     * @return Loaded pipeline class instance or null.
     */
    protected Pipeline loadPipeline(ViewChangeEvent event) {
        // some information text ...
        String pipeIdstr = event.getParameters();
        if (isInteger(pipeIdstr)) {
            // use pipeIdstr as id
            this.pipeline = loadPipeline(pipeIdstr);
            // hide details
            setDetailState(false);
        } else {
            // create empty, for new record
            this.pipeline = pipelineFacade.createPipeline();
            pipeline.setName("");
            pipeline.setDescription("");
            pipeline.setShareType(ShareType.PRIVATE);
            pipelineName.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getName()));
            setIdLabel(null);
            author.setValue(authCtx.getUsername());
            pipelineDescription.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getDescription()));
            pipelineVisibility.setPropertyDataSource(new ObjectProperty<>(this.pipeline.getShareType()));
            setupButtons(false);
            pipelineName.setInputPrompt(Messages.getString("PipelineEdit.pipeline.insert"));
            pipelineDescription.setInputPrompt(Messages.getString("PipelineEdit.pipeline.insert.description"));
        }

        if (pipeline != null) {
            pipelineCanvas.showPipeline(pipeline);
        }
        return this.pipeline;
    }

    /**
     * Saves current pipeline.
     * 
     * @param successAction
     * @return If current pipeline was saved
     */
    protected boolean savePipeline(final String successAction) {
        if (!validate()) {
            return false;
        }

        final boolean doCleanup = pipelineCanvas.saveGraph(pipeline);

        final ShareType visibility = (ShareType) pipelineVisibility.getValue();
        if (!pipelineFacade.isUpToDate(pipeline)) {
            ConfirmDialog.show(UI.getCurrent(),
                    Messages.getString("PipelineEdit.pipeline.overwrite"), Messages.getString("PipelineEdit.pipeline.overwrite.description"), Messages.getString("PipelineEdit.pipeline.overwrite.saveAnyway"), Messages.getString("PipelineEdit.pipeline.overwrite.cancel"), new ConfirmDialog.Listener() {
                        @Override
                        public void onClose(ConfirmDialog cd) {
                            if (cd.isConfirmed()) {
                                finishSavePipeline(doCleanup, visibility, successAction);
                                paralelInfoLayout.setVisible(false);
                            }
                        }
                    });
            return false;
        } else if (pipeline.getShareType() == ShareType.PRIVATE && ShareType.PUBLIC.contains(visibility) && !pipelineFacade.getPrivateDPUs(pipeline).isEmpty()) {
            ConfirmDialog
                    .show(UI.getCurrent(),
                            Messages.getString("PipelineEdit.pipeline.save.public"), Messages.getString("PipelineEdit.pipeline.save.public.description"), Messages.getString("PipelineEdit.pipeline.save.public.save"), Messages.getString("PipelineEdit.pipeline.save.public.cancel"), new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {
                                        finishSavePipeline(doCleanup, visibility, successAction);
                                    }
                                }
                            });
            return false;
        } else {
            return finishSavePipeline(doCleanup, visibility, successAction);
        }
    }

    private boolean finishSavePipeline(boolean doCleanup, ShareType visibility, String successAction) {
        setupVisibilityOptions(visibility);

        undo.setEnabled(false);
        this.pipeline.setName(pipelineName.getValue());
        pipelineName.commit();
        this.pipeline.setDescription(pipelineDescription.getValue());
        pipelineDescription.commit();

        this.pipeline.setShareType(visibility);
        pipelineVisibility.commit();

        pipelineFacade.save(this.pipeline);
        if (doCleanup) {
            pipelineCanvas.afterSaveCleanUp();
        }

        Notification.show(Messages.getString("PipelineEdit.save.successfull"), Notification.Type.HUMANIZED_MESSAGE);
        setupButtons();

        // Update pipeline name.
        updateLblPipelineName();

        switch (successAction) {
            case "debug":
                refreshPipeline();
                PipelineExecution pExec = pipelineHelper.runPipeline(pipeline, true, sde.getDebugNode());
                openDebug(pExec, pipeline, sde.getDebugNode());
                break;
            case "close":
                closeView();
                break;
            case "reload":
                refreshPipeline();
                break;
            case "close&debug":
                PipelineExecution exec = pipelineHelper.runPipeline(pipeline, true);
                if (exec != null) {
                    ((AppEntry) UI.getCurrent()).getNavigation().navigateTo(ExecutionListPresenterImpl.class, String.format("exec=%s", exec.getId()));
                }
                break;
            default:
                return true;
        }
        return true;
    }

    /**
     * Calculates and sets canvas dimensions according to current size of
     * browser window and pipeline graph's bounds.
     * 
     * @param zoomBounds
     *            {@link Position} with bounds of pipeline graph.
     */
    private void calculateCanvasDimensions(int width, int height) {
//		int minWidth = UI.getCurrent().getPage().getBrowserWindowWidth() - 100;
//		int minHeight = (int)tabSheet.getHeight() - 60;
//		if (width < minWidth) {
//			width = minWidth;
//			//enable horizontal scrollbar
//		}
//		if (height < minHeight) {
//			height = minHeight;
//			//enable vertical scrollbar
//		}
        pipelineCanvas.setWidth(width, Unit.PIXELS);
        pipelineCanvas.setHeight(height, Unit.PIXELS);
        dadWrapper.setSizeUndefined();
        setupComponentSize();
    }

    /**
     * Validates fields with requirements on input. Shows errors as
     * notification.
     * 
     * @return validation result
     */
    private boolean validate() {
        try {
            pipelineName.validate();
            pipelineDescription.validate();
            pipelineVisibility.validate();
            if (pipelineFacade.hasPipelineWithName(pipelineName.getValue(), pipeline)) {
                throw new Validator.InvalidValueException(Messages.getString("PipelineEdit.pipeline.duplicate"));
            }
        } catch (Validator.InvalidValueException e) {
            Notification.show(Messages.getString("PipelineEdit.pipeline.error.saving"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void setMode(boolean isDevelop) {
        readOnlyLabel.setVisible(!isDevelop);
        if (isDevelop) {
            canvasMode = DEVELOP_MODE;
            standardTab.setCaption(Messages.getString("PipelineEdit.setMode.isDevelop.standard"));
            standardTab.setEnabled(false);
            developTab.setCaption(Messages.getString("PipelineEdit.setMode.isDevelop.develop"));
            tabSheet.setTabPosition(developTab, 0);
            pipelineCanvas.changeMode(canvasMode);
        } else {
            canvasMode = STANDARD_MODE;
            standardTab.setCaption(Messages.getString("PipelineEdit.setMode.isStandard.develop"));
            standardTab.setEnabled(false);
            developTab.setCaption(Messages.getString("PipelineEdit.setMode.isStandard.standard"));
            tabSheet.setTabPosition(developTab, 0);
            pipelineCanvas.changeMode(canvasMode);
        }
    }

    private void refreshPipeline() {
        pipeline = pipelineFacade.getPipeline(pipeline.getId());
        setIdLabel(pipeline.getId());
        author.setValue(pipeline.getOwner().getUsername());
        pipelineCanvas.showPipeline(pipeline);
    }

    private void setFields() {
        setIdLabel(pipeline.getId());
        pipelineName.setValue(this.pipeline.getName());
        pipelineDescription.setValue(this.pipeline.getDescription());
        pipelineVisibility.setValue(this.pipeline.getShareType());
        setupVisibilityOptions(this.pipeline.getShareType());
        setupButtons(false);
    }

    private GridLayout createFormattingBar() {

        ClickListener listener = new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                pipelineCanvas.formatAction((String) event.getButton().getData());
            }
        };

        GridLayout bar = new GridLayout(3, 3);
        Button topAlign = new Button();
        topAlign.setData("align_top");
        topAlign.setDescription(Messages.getString("PipelineEdit.align.top"));
        topAlign.setIcon(new ThemeResource("icons/arrow_top.svg"), Messages.getString("PipelineEdit.icon.align.top"));
        topAlign.addClickListener(listener);
        bar.addComponent(topAlign, 1, 0);

        Button bottomAlign = new Button();
        bottomAlign.setData("align_bottom");
        bottomAlign.setDescription(Messages.getString("PipelineEdit.align.bottom"));
        bottomAlign.setIcon(new ThemeResource("icons/arrow_bottom.svg"), Messages.getString("PipelineEdit.icon.align.bottom"));
        bottomAlign.addClickListener(listener);
        bar.addComponent(bottomAlign, 1, 2);

        Button leftAlign = new Button();
        leftAlign.setData("align_left");
        leftAlign.setDescription(Messages.getString("PipelineEdit.align.left"));
        leftAlign.setIcon(new ThemeResource("icons/arrow_left.svg"), Messages.getString("PipelineEdit.icon.align.left"));
        leftAlign.addClickListener(listener);
        bar.addComponent(leftAlign, 0, 1);

        Button rightAlign = new Button();
        rightAlign.setData("align_right");
        rightAlign.setDescription(Messages.getString("PipelineEdit.align.right"));
        rightAlign.setIcon(new ThemeResource("icons/arrow_right.svg"), Messages.getString("PipelineEdit.icon.align.right"));
        rightAlign.addClickListener(listener);
        bar.addComponent(rightAlign, 2, 1);

        Button distributeHorizontal = new Button();
        distributeHorizontal.setData("distribute_horizontal");
        distributeHorizontal.setDescription(Messages.getString("PipelineEdit.distribute.horizontally"));
        distributeHorizontal.setIcon(new ThemeResource("icons/distribute.svg"), Messages.getString("PipelineEdit.icon.distribute.horizontally"));
        distributeHorizontal.addClickListener(listener);
        bar.addComponent(distributeHorizontal, 2, 0);

        Button distributeVertical = new Button();
        distributeVertical.setData("distribute_vertical");
        distributeVertical.setDescription(Messages.getString("PipelineEdit.distribute.vertically"));
        distributeVertical.setIcon(new ThemeResource("icons/distribute_v.svg"), Messages.getString("PipelineEdit.icon.distribute.vertically"));
        distributeVertical.addClickListener(listener);
        bar.addComponent(distributeVertical, 2, 2);

        return bar;
    }

    private void setIdLabel(Long id) {
        boolean hasId = id != null;
        if (id != null) {
            idValue.setValue(id.toString());
        }
        idValue.setVisible(hasId);
        idLabel.setVisible(hasId);
    }

    private void setupVisibilityOptions(ShareType visibility) {
        pipelineVisibility.setItemEnabled(ShareType.PRIVATE, visibility == ShareType.PRIVATE);
        boolean publicRoAvalilable = visibility != ShareType.PUBLIC_RW || authCtx.getUser().equals(this.pipeline.getOwner()) || authCtx.getUser().getRoles().contains(Role.ROLE_ADMIN);
        pipelineVisibility.setItemEnabled(ShareType.PUBLIC_RO, publicRoAvalilable);
    }

    private void updateLblPipelineName() {
        if (this.pipeline == null) {
        } else {
            lblPipelineName.setValue(Messages.getString("PipelineEdit.pipeline.detail") + this.pipeline.getName() + "' <h3>");
        }
    }

}
