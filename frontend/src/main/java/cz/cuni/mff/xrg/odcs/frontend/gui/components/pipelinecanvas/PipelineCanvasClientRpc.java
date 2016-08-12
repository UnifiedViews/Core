package cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Interface for RPC calls to client = JS part of component.
 * 
 * @author Bogo
 */
public interface PipelineCanvasClientRpc extends ClientRpc {

    //public void loadPipeline(Pipeline pipeline);
    /**
     * Adds new node on graph canvas.
     * 
     * @param dpuId
     *            Id of node.
     * @param name
     *            Name of dpu.
     * @param description
     *            Description of dpu.
     * @param type
     *            {@link String} with type of dpu.
     * @param posX
     *            X coordinate of node.
     * @param posY
     *            Y coordinate of node.
     * @param isNew
     *            Is DPU new? Or only loaded?
     */
    public void addNode(int dpuId, String name, String description, String type, int posX, int posY, boolean isNew);

    /**
     * Adds new edge on graph canvas.
     * 
     * @param connId
     *            Id of edge.
     * @param dpuFrom
     *            Id of start node.
     * @param dpuTo
     *            Id of end node.
     * @param dataUnitName
     *            Name of corresponding DataUnit or null.
     */
    public void addEdge(int connId, int dpuFrom, int dpuTo, String dataUnitName);

    /**
     * Initializes JS part of graph canvas component.
     * 
     * @param width
     *            Default width of the canvas
     * @param height
     *            Default height of the canvas
     * @param language
     *            language in which should be canvas displayed
     * @param frontendTheme
     *            frontend theme name
     * @param canDebug
     *            If user has permission to debug pipeline
     */
    public void init(int width, int height, String language, String frontendTheme, boolean debug);

    /**
     * Updates information of given node's DPUInstance.
     * 
     * @param id
     *            Id of node to update.
     * @param name
     *            New name of corresponding dpu.
     * @param description
     *            New description of corresponding dpu.
     */
    public void updateNode(int id, String name, String description);

    /**
     * Updates information of given Edge.
     * 
     * @param id
     *            Id of edge.
     * @param dataUnitName
     *            New name of corresponding DataUnit or null.
     */
    public void updateEdge(int id, String dataUnitName);

    /**
     * Resizes stage.
     * 
     * @param height
     *            New height.
     * @param width
     *            New width.
     */
    public void resizeStage(int width, int height);

    /**
     * Enlarges canvas in given direction by given pixels.
     * 
     * @param direction
     *            Direction to enlarge cavnas in.
     * @param pixels
     *            Number of pixels to enlarge canvas by.
     */
    public void enlargeCanvas(String direction, int pixels);

    /**
     * Zooms the stage to given ratio.
     * 
     * @param zoom
     *            {@link Double} with ratio.(1.0 - 2.0)
     */
    public void zoomStage(double zoom);

    /**
     * Clears stage.
     */
    public void clearStage();

    /**
     * Sets new mode of the stage.
     * 
     * @param newMode
     *            New mode, either "standard_mode" or "develop_mode"
     */
    public void setStageMode(String newMode);

    /**
     * Sets DPU's validity.
     * 
     * @param id
     * @param isValid
     */
    public void setDpuValidity(int id, boolean isValid);

    /**
     * Formats selected DPUs with given action.
     * 
     * @param action
     *            Formatting action
     */
    public void formatDPUs(String action);

}
