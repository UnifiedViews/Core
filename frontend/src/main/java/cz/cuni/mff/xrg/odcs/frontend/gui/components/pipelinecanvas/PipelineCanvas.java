package cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeCompiler;
import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeFormater;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Position;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.PipelineValidator;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.PipelineValidator.PipelineValidationException;
import cz.cuni.mff.xrg.odcs.frontend.gui.dialogs.DPUDetail;
import cz.cuni.mff.xrg.odcs.frontend.gui.dialogs.EdgeDetail;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PipelineEdit;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Component for visualization of the pipeline.
 *
 * @author Bogo
 */
@Component
@Scope("prototype")
@SuppressWarnings("serial")
@JavaScript({ "js_pipelinecanvas.js", "kinetic-v4.5.4.min.js", "jquery-2.0.0.min.js", "jquery.i18n.properties.js" })
public class PipelineCanvas extends AbstractJavaScriptComponent {

    final int DPU_WIDTH = 120;

    final int DPU_HEIGHT = 100;

    int dpuCount = 0;

    int connCount = 0;

    float currentZoom = 1.0f;

    private int currentHeight = 630;

    private int currentWidth = 1240;

    public static final int MIN_DISTANCE_FROM_BORDER = 100;

    public static final int SIZE_INCREASE = 200;

    public static final int MIN_X_CANVAS = 400;

    private PipelineGraph graph;

    private Stack<PipelineGraph> historyStack;

    private Stack<DPUInstanceRecord> dpusToDelete = new Stack<>();

    private boolean isModified = false;

    @Autowired
    private DPUExplorer dpuExplorer;

    @Autowired
    private PipelineValidator pipelineValidator;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PermissionUtils permissionUtils;

    @Autowired
    private Utils utils;

    private static final Logger LOG = LoggerFactory.getLogger(PipelineCanvas.class);

    private DPUDetail detailDialog;

    private Window.CloseListener detailCloseListener;

    private String canvasMode = PipelineEdit.DEVELOP_MODE;

    private final EdgeFormater edgeFormater = new EdgeFormater();

    private final int MAX_HISTORY_SIZE = 10;

    /**
     * Initial constructor with registering of server side RPC.
     */
    public PipelineCanvas() {
        this.historyStack = new Stack();

        this.setId("container");
        this.setStyleName("pipelineContainer");

        registerRpc(new PipelineCanvasServerRpc() {

            @Override
            public void onDetailRequested(int dpuId) {
                Node node = graph.getNodeById(dpuId);
                if (node != null) {
                    showDPUDetail(node);
                }
            }

            @Override
            public void onConnectionRemoved(int connectionId) {
                storeHistoryGraph();
                graph.removeEdge(connectionId);
            }

            @Override
            public void onConnectionAdded(int dpuFrom, int dpuTo) {
                storeHistoryGraph();
                addConnection(dpuFrom, dpuTo);
            }

            @Override
            public void onDpuRemoved(int dpuId) {
                storeHistoryGraph();
                Node removedNode = graph.removeDpu(dpuId);
                dpusToDelete.add(removedNode.getDpuInstance());
            }

            @Override
            public void onDpuMoved(int dpuId, int newX, int newY, boolean autoAction) {
                //storeHistoryGraph();
                dpuMoved(dpuId, newX, newY, autoAction);
                fireEvent(new GraphChangedEvent(PipelineCanvas.this, false));
            }

            @Override
            public void onLogMessage(String message) {
                //TODO: Log JS messages
            }

            @Override
            public void onDebugRequested(int dpuId) {
                showDebugWindow(dpuId);
            }

            @Override
            public void onDataUnitNameEditRequested(int edgeId) {
                Edge edge = graph.getEdgeById(edgeId);
                showEdgeDetail(edge);
            }

            @Override
            public void onStoreHistory() {
                storeHistoryGraph();
            }

            @Override
            public void onDpuCopyRequested(int dpuId, int x, int y) {
                storeHistoryGraph();
                copyDpu(dpuId, x, y);
            }

            @Override
            public void onMultipleDPUsSelected(boolean selected) {
                fireEvent(new FormattingEnabledEvent(PipelineCanvas.this, selected));
            }
        });

    }

    /**
     * Method initializing client side RPC.
     */
    public void init() {
        detailDialog = new DPUDetail(this.dpuFacade, this.appConfig, this.utils, this.permissionUtils);
        getRpcProxy(PipelineCanvasClientRpc.class).init(currentWidth, currentHeight,
                LocaleHolder.getLocale().getLanguage(), appConfig.getString(ConfigProperty.FRONTEND_THEME),
                this.permissionUtils.hasUserAuthority(EntityPermissions.PIPELINE_RUN_DEBUG));
    }

    /**
     * Saves graph from graph canvas.
     *
     * @param pipeline
     *            {@link Pipeline} where graph should be saved.
     * @return If after save clean up is needed.
     */
    public boolean saveGraph(Pipeline pipeline) {
        historyStack.clear();

        pipeline.setGraph(graph);
        isModified = false;
        return !dpusToDelete.isEmpty();
    }

    /**
     * Cleans up removed DPU Instances, as Nodes dependency doesn't take care of
     * this. Always call after saving pipeline if saveGraph return True.
     */
    public void afterSaveCleanUp() {
        for (DPUInstanceRecord instance : dpusToDelete) {
            dpuFacade.delete(instance);
        }
        dpusToDelete.clear();
    }

    /**
     * Adds new DPUTemplateRecord to graph canvas.
     *
     * @param dpu
     *            Id of {@link DPUTemplateRecord} which should be added.
     * @param x
     *            X coordinate of position, where dpu should be added.
     * @param y
     *            Y coordinate of position, where dpu should be added.
     */
    public void addDpu(DPUTemplateRecord dpu, int x, int y) {
        storeHistoryGraph();
        DPUInstanceRecord dpuInstance = dpuFacade.createInstanceFromTemplate(dpu);
        Node node = graph.addDpuInstance(dpuInstance);
        getRpcProxy(PipelineCanvasClientRpc.class)
                .addNode(node.hashCode(), dpu.getName(), dpu.getDescription(), dpu.getType().name(), (int) (x / currentZoom), (int) (y / currentZoom), true);
    }

    /**
     * Adds new edge to graph canvas.
     *
     * @param dpuFrom
     *            Id of Node, where edge starts.
     * @param dpuTo
     *            Id of Node, where edge ends.
     */
    public void addConnection(int dpuFrom, int dpuTo) {
        String result = graph.validateNewEdge(dpuFrom, dpuTo);
        Node to = graph.getNodeById(dpuTo);

        if (result == null) {
            int connectionId = graph.addEdge(dpuFrom, dpuTo);
            EdgeCompiler edgeCompiler = new EdgeCompiler();
            Edge edge = graph.getEdgeById(connectionId);
            DPUInstanceRecord from = graph.getNodeById(dpuFrom).getDpuInstance();

            edgeCompiler.createDefaultMapping(dpuExplorer, edge, from, to.getDpuInstance());

            getRpcProxy(PipelineCanvasClientRpc.class).addEdge(connectionId, dpuFrom, dpuTo, edgeFormater.format(edge.getScript()));
        } else {
            Notification.show(Messages.getString("PipelineCanvas.edge.failed"), result, Notification.Type.WARNING_MESSAGE);
        }

    }

    /**
     * Shows given pipeline on graph canvas.
     *
     * @param pipeline
     *            {@link Pipeline} to show on graph canvas.
     */
    public void showPipeline(Pipeline pipeline) {
        setGraph(pipeline.getGraph());
    }

    /**
     * Shows detail of given {@link DPUInstanceRecord} in new sub-window.
     *
     * @param node
     *            {@link Node} containing DPU, which detail should be showed.
     */
    public void showDPUDetail(final Node node) {
        final DPUInstanceRecord dpu = node.getDpuInstance();
        detailDialog.showDpuDetail(dpu, canvasMode.equals(PipelineEdit.STANDARD_MODE));
        if (detailCloseListener != null) {
            detailDialog.removeCloseListener(detailCloseListener);
        }
        detailCloseListener = new Window.CloseListener() {
            @Override
            public void windowClose(CloseEvent e) {
                DPUDetail source = (DPUDetail) e.getSource();
                if (source.getResult()) {
                    isModified = true;
                    fireEvent(new DetailClosedEvent(PipelineCanvas.this, Node.class));
                    getRpcProxy(PipelineCanvasClientRpc.class).updateNode(node.hashCode(), dpu.getName(), dpu.getDescription());
                    boolean isValid = pipelineValidator.checkDPUValidity(dpu);
                    getRpcProxy(PipelineCanvasClientRpc.class).setDpuValidity(node.hashCode(), isValid);
                }
            }
        };

        detailDialog.addCloseListener(detailCloseListener);
        if (!UI.getCurrent().getWindows().contains(detailDialog)) {
            UI.getCurrent().addWindow(detailDialog);
        }
    }

    /**
     * Change canvas size.
     *
     * @param height
     *            New height of canvas in pixels.
     * @param width
     *            New width of canvas in pixels.
     */
    public void resizeCanvas(int width, int height) {
        getRpcProxy(PipelineCanvasClientRpc.class).resizeStage(width, height);
    }

    /**
     * Zoom the canvas.
     *
     * @param isZoomIn
     *            +/- zoom.
     * @return {@link Position} with new size of canvas.
     */
    public Position zoom(boolean isZoomIn) {

        if (isZoomIn && currentZoom < 2) {
            if (currentZoom < 1.5) {
                currentZoom = 1.5f;
            } else {
                currentZoom = 2.0f;
            }
        } else if (!isZoomIn && currentZoom > 1.0f) {
            if (currentZoom > 1.5f) {
                currentZoom = 1.5f;
            } else {
                currentZoom = 1.0f;
            }
        }
        getRpcProxy(PipelineCanvasClientRpc.class).zoomStage(currentZoom);
        if (currentZoom < 1.5f) {
            int minWidth = Page.getCurrent().getBrowserWindowWidth() - 60;
            if (currentWidth < minWidth) {
                currentWidth = minWidth;
                getRpcProxy(PipelineCanvasClientRpc.class).resizeStage(currentWidth, currentHeight);
            }
        }
        //return new canvas size;
        return new Position((int) (currentWidth * currentZoom), (int) (currentHeight * currentZoom));
    }

    /**
     * Undo changes on canvas.
     *
     * @return History stack contains another graph.
     */
    public boolean undo() {
        if (!historyStack.isEmpty()) {
            PipelineGraph restoredGraph = historyStack.pop();
            setGraph(restoredGraph);
        }
        return !historyStack.isEmpty();
    }

    /**
     * Changes mode of the pipeline canvas.
     *
     * @param newMode
     */
    public void changeMode(String newMode) {
        canvasMode = newMode;
        getRpcProxy(PipelineCanvasClientRpc.class).setStageMode(newMode);
    }

    /**
     * Returns if PipelineCanvas was modified since last save.
     *
     * @return Is modified?
     */
    public boolean isModified() {
        return isModified;
    }

    /**
     * Cancel unsaved changes.
     */
    public void cancelChanges() {
        isModified = false;
    }

    @Override
    protected PipelineCanvasState getState() {
        return (PipelineCanvasState) super.getState();
    }

    /**
     * Inform listeners, about supplied event.
     *
     * @param event
     */
    protected void fireEvent(Event event) {
        Collection<Listener> ls = (Collection<Listener>) this.getListeners(com.vaadin.ui.Component.Event.class);
        for (Listener l : ls) {
            l.componentEvent(event);
        }
    }

    /**
     * Initializes the canvas with given graph.
     *
     * @param pg
     *            {@link PipelineGraph} to show on canvas.
     */
    private void setGraph(PipelineGraph pg) {
        if (this.graph != null) {
            getRpcProxy(PipelineCanvasClientRpc.class).clearStage();
        }
        this.graph = pg;
        setupCanvasSize(graph);

        LOG.debug("DPU mandatory fields check starting");
        for (Node node : graph.getNodes()) {
            DPUInstanceRecord dpu = node.getDpuInstance();
            //boolean isValid = checkDPUValidity(dpu);
            getRpcProxy(PipelineCanvasClientRpc.class).addNode(node.hashCode(), dpu.getName(), dpu.getDescription(), dpu.getType().name(), node.getPosition().getX(), node.getPosition().getY(), false);
        }
        LOG.debug("DPU mandatory fields check completed");
        EdgeCompiler edgeCompiler = new EdgeCompiler();
        boolean hadInvalidMappings = false;
        String message = Messages.getString("PipelineCanvas.pipeline.invalid");
        for (Edge edge : graph.getEdges()) {
            List<String> invalidMappings = edgeCompiler.update(edge, dpuExplorer.getOutputs(edge.getFrom().getDpuInstance()), dpuExplorer.getInputs(edge.getTo().getDpuInstance()));
            if (!invalidMappings.isEmpty()) {
                hadInvalidMappings = true;
                message += Messages.getString("PipelineCanvas.edge.from", edge.getFrom().getDpuInstance().getName(), edge.getTo().getDpuInstance().getName(), invalidMappings.toString());
            }
            getRpcProxy(PipelineCanvasClientRpc.class).addEdge(edge.hashCode(), edge.getFrom().hashCode(), edge.getTo().hashCode(), edgeFormater.format(edge.getScript()));
        }
        if (hadInvalidMappings) {
            Notification.show(Messages.getString("PipelineCanvas.invalid.mappings"), message, Notification.Type.WARNING_MESSAGE);
        }
    }

    /**
     * Method updating node position on server side.
     *
     * @param dpuId
     *            Id of {@link Node} which was moved.
     * @param newX
     *            New X coordinate.
     * @param newY
     *            New Y coordinate.
     * @param autoAction
     */
    private void dpuMoved(int dpuId, int newX, int newY, boolean autoAction) {
        isModified = graph.moveNode(dpuId, newX, newY);
        if (!autoAction) {
            checkForResize(newX, newY);
        }
    }

    /**
     * Shows detail of given {@link Edge} in new sub-window.
     *
     * @param edge
     *            {@link Edge} which detail should be showed.
     */
    private void showEdgeDetail(final Edge edge) {
        EdgeDetail edgeDetailDialog = new EdgeDetail(edge, dpuExplorer, canvasMode.equals(PipelineEdit.STANDARD_MODE));
        edgeDetailDialog.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(CloseEvent e) {
                isModified = true;
                fireEvent(new DetailClosedEvent(PipelineCanvas.this, Edge.class));
                getRpcProxy(PipelineCanvasClientRpc.class).updateEdge(edge.hashCode(), edgeFormater.format(edge.getScript()));
            }
        });
        UI.getCurrent().addWindow(edgeDetailDialog);
    }

    /**
     * Store graph in stack for undo.
     */
    private void storeHistoryGraph() {
        PipelineGraph clonedGraph = graph.cloneGraph();
        isModified = true;
        if (historyStack.isEmpty()) {
            //Make undo button enabled.
            fireEvent(new GraphChangedEvent(this, true));
        }
        while (historyStack.size() >= MAX_HISTORY_SIZE) {
            historyStack.remove(0);
        }
        historyStack.push(clonedGraph);
    }

    /**
     * Copy DPURecord on canvas.
     *
     * @param nodeId
     *            Id of Node, which DPURecord should be copied.
     */
    private void copyDpu(int nodeId, int x, int y) {
        storeHistoryGraph();
        Node node = graph.getNodeById(nodeId);

        DPUInstanceRecord dpu = new DPUInstanceRecord(node.getDpuInstance());
        Node copyNode = graph.addDpuInstance(dpu);
        graph.moveNode(copyNode.hashCode(), x, y);
        getRpcProxy(PipelineCanvasClientRpc.class)
                .addNode(copyNode.hashCode(), dpu.getName(), dpu.getDescription(), dpu.getType().name(), x, y, true);
    }

    /**
     * Start pipeline in debug mode and show debug window.
     *
     * @param dpuId
     *            {@Link int} id of dpu, where debug should end.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    private void showDebugWindow(int dpuId) throws IllegalArgumentException, NullPointerException {
        Node debugNode = graph.getNodeById(dpuId);
        fireEvent(new ShowDebugEvent(this, debugNode));
    }

    /**
     * Validate graph.
     */
    public boolean validateGraph() {
        boolean isGraphValid = true;
        if (graph.getNodes().isEmpty()) {
            Notification.show(Messages.getString("PipelineCanvas.pipeline.invalid.blank.pipeline"), Notification.Type.WARNING_MESSAGE);
            return false;
        }
        for (Node node : graph.getNodes()) {
            DPUInstanceRecord dpu = node.getDpuInstance();
            boolean isValid = pipelineValidator.checkDPUValidity(dpu);
            isGraphValid &= isValid;
            getRpcProxy(PipelineCanvasClientRpc.class).setDpuValidity(node.hashCode(), isValid);
        }
        try {
            isGraphValid &= pipelineValidator.validateGraphEdges(graph);
            if (isGraphValid) {
                return true;
            }
        } catch (PipelineValidationException ex) {
            Notification.show(Messages.getString("PipelineCanvas.mandatory.missing"), ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }
        return false;
    }

    /**
     * Invoke formatting action.
     *
     * @param action
     *            Formatting action.
     */
    public void formatAction(String action) {
        getRpcProxy(PipelineCanvasClientRpc.class).formatDPUs(action);
    }

    /**
     * Resizes canvas, incerases the size by given number of pixels in given
     * direction.
     *
     * @param direction
     *            Direction to enlarge cavnas in
     * @param pixels
     *            Number of pixels to enlarge canvas by
     */
    private void resize(String direction, int pixels) {
        getRpcProxy(PipelineCanvasClientRpc.class).enlargeCanvas(direction, pixels);
    }

    private void checkForResize(int newX, int newY) {
        //Evaluate if the DPU position is near the borders
        //If it is, enlarge canvas in given direction
        boolean resized = false;

//        if (newX < MIN_DISTANCE_FROM_BORDER) {
//            resized = true;
//            currentWidth += SIZE_INCREASE;
//            resize("left", SIZE_INCREASE);
//        } else
        if (currentWidth - (newX + DPU_WIDTH) < MIN_DISTANCE_FROM_BORDER) {
            resized = true;
            currentWidth += SIZE_INCREASE;
            resize("right", SIZE_INCREASE);
        }

//        if (newY < MIN_DISTANCE_FROM_BORDER) {
//            resized = true;
//            currentHeight += SIZE_INCREASE;
//            resize("top", SIZE_INCREASE);
//        } else
        if (currentHeight - (newY + DPU_HEIGHT) < MIN_DISTANCE_FROM_BORDER) {
            resized = true;
            currentHeight += SIZE_INCREASE;
            resize("bottom", SIZE_INCREASE);
        }

        if (resized) {
            fireEvent(new ResizedEvent(this, (int) (currentWidth * currentZoom), (int) (currentHeight * currentZoom)));
        }

    }

    private void setupCanvasSize(PipelineGraph graph) {
        int minX = Integer.MAX_VALUE;
        int maxX = 0;
        int minY = Integer.MAX_VALUE;
        int maxY = 0;

        //Get min and max bounds of the graph
        for (Node node : graph.getNodes()) {
            Position nodePosition = node.getPosition();
            if (nodePosition == null) {
                continue;
            }
            int nodeX = nodePosition.getX();
            if (nodeX < minX) {
                minX = nodeX;
            }
            if (nodeX > maxX) {
                maxX = nodeX;
            }
            int nodeY = nodePosition.getY();
            if (nodeY < minY) {
                minY = nodeY;
            }
            if (nodeY > maxY) {
                maxY = nodeY;
            }
        }

        //Add dimensions of DPUs
        maxX += DPU_WIDTH;
        maxY += DPU_HEIGHT;

        //TODO: Add check for pipeline being bigger than computed canvas size, if not, do not reposition
        //TODO: Confirm repositioning of pipeline...
//        boolean translate = false;
//        int overX = 0;
//        int overY = 0;
//        if (minX > MIN_X_CANVAS) {
//            translate = true;
//            overX = minX - MIN_X_CANVAS;
//        }
//        if (minY > MIN_DISTANCE_FROM_BORDER) {
//            translate = true;
//            overY = minY - MIN_DISTANCE_FROM_BORDER;
//        }
//
//        if (translate) {
//            translateGraph(graph, overX, overY);
//        }
        int proposedWidth = maxX + MIN_DISTANCE_FROM_BORDER;
        int proposedHeight = maxY + MIN_DISTANCE_FROM_BORDER;

        boolean sizeChanged = false;
        if (proposedWidth > currentWidth) {
            sizeChanged = true;
            currentWidth = proposedWidth;
        }
        if (proposedHeight > currentHeight) {
            sizeChanged = true;
            currentHeight = proposedHeight;
        }

        if (sizeChanged) {
            getRpcProxy(PipelineCanvasClientRpc.class).resizeStage(currentWidth, currentHeight);
            fireEvent(new ResizedEvent(this, (int) (currentWidth * currentZoom), (int) (currentHeight * currentZoom)));
        }

    }

    private void translateGraph(PipelineGraph graph, int overX, int overY) {
        for (Node node : graph.getNodes()) {
            Position origPosition = node.getPosition();
            node.setPosition(new Position(origPosition.getX() - overX, origPosition.getY() - overY));
        }
    }

    public int getCanvasWidth() {
        return (int) (currentWidth * currentZoom);
    }

    public int getCanvasHeight() {
        return (int) (currentHeight * currentZoom);
    }

}
