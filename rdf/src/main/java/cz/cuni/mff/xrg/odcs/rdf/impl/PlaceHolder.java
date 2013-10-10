package cz.cuni.mff.xrg.odcs.rdf.impl;

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
	 * @param graphName URI representation of graph for DPU defined by DPU name.
	 */
	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	/**
	 *
	 * @return DPU name.
	 */
	public String getDPUName() {
		return DPUName;
	}

	/**
	 * Return name of grapf for DPU. If graph has not been set, return
	 * tempGraphName.
	 *
	 * @return String value of URI representation of graph set for this DPU
	 *         name.
	 */
	public String getGraphName() {
		return graphName;
	}
}
