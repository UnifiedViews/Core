package cz.cuni.mff.xrg.odcs.rdf.help;

/**
 * Class responsible for keeping mapping between DPU name a graph name for this
 * DPU. Until the graph name for DPU is not set is used TEMP graph name
 * generated for DPU name.
 * 
 * @author Jiri Tomes
 */
public class PlaceHolder {

    private String DPUName;

    private String graphName;

    /**
     * Create new instance of {@link PlaceHolder} for given DPU name.
     * 
     * @param DPUName
     *            string value of DPU name.
     */
    public PlaceHolder(String DPUName) {
        this.DPUName = DPUName;
        setTempGraphName();
    }

    private void setTempGraphName() {
        graphName = "http://graphForDataUnit_" + DPUName;
    }

    /**
     * Set Graph mapping to DPU.
     * 
     * @param graphName
     *            URI representation of graph for DPU defined by DPU name.
     */
    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    /**
     * Returns string value of DPU name.
     * 
     * @return DPU name.
     */
    public String getDPUName() {
        return DPUName;
    }

    /**
     * Returns name of graph for DPU. If graph has not been set, the
     * tempGraphName is returned.
     * 
     * @return String value of URI representation of graph set for this DPU
     *         name.
     */
    public String getGraphName() {
        return graphName;
    }
}
