package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents node in the dependency graph. Contains links to its dependencies.
 * 
 * @author Jan Vojt
 */
public class ExecutedNode {

    /**
     * The node in pipeline graph represented by this dependency.
     */
    private Node node;

    /**
     * The dpu instance record referred.
     */
    private DPUInstanceRecord dpuInstance;

    /**
     * List of dependencies of this node.
     */
    private List<ExecutedNode> dependencies = new ArrayList<>();

    /**
     * List of nodes that depend on this node
     */
    private List<ExecutedNode> dependants = new ArrayList<>();

    /**
     * Tells whether this dependency is already satisfied.
     */
    private boolean executed = false;

    /**
     *  Flag whether output data units of this DPU were already analysed whether they are consumed by multiple following DPUs
     */
    private boolean outputsAnalysedWhetherConsumedByMultipleDPUs = false;

    //to hold set of outgoing edges as needed by backend. This may not be equal to all outgoing edges in the graph (if debugging only part of the pipeline).
    private Set<ExecutedEdge> outgoingEdges = new HashSet<>();


    //to hold set of incoming edges as needed by backend. This may not be equal to all incoming edges in the graph (if debugging only part of the pipeline)
    private Set<ExecutedEdge> incomingEdges = new HashSet<>();

    /**
     * Constructs dependency from the dpu instance
     * 
     * @param node
     */
    public ExecutedNode(Node node) {
        this.node = node;
        this.dpuInstance = node.getDpuInstance();
    }

    /**
     * @return whether all dependencies for this node are already met.
     */
    public boolean hasMetDependencies() {
        for (ExecutedNode d : dependencies) {
            if (!d.isExecuted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the dependencies
     */
    public List<ExecutedNode> getDependencies() {
        return dependencies;
    }

    /**
     * @param dependencies
     */
    public void setDependencies(List<ExecutedNode> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Checks whether this node already has given dependency
     * 
     * @param node
     *            dependency to check
     * @return whether this node already has given dependency
     */
    public boolean hasDependency(ExecutedNode node) {
        return dependencies.contains(node);
    }

    /**
     * Add a new dependency
     * 
     * @param node
     *            dependency to add
     */
    public void addDependency(ExecutedNode node) {
        if (!hasDependency(node)) {
            dependencies.add(node);
        }
    }

    /**
     * @return the dependants
     */
    public List<ExecutedNode> getDependants() {
        return dependants;
    }

    /**
     * @param dependants
     *            the dependants to set
     */
    public void setDependants(List<ExecutedNode> dependants) {
        this.dependants = dependants;
    }

    /**
     * Adds dependent node
     * 
     * @param node
     */
    public void addDependant(ExecutedNode node) {
        if (!hasDependant(node)) {
            dependants.add(node);
        }
    }

    /**
     * Tells whether this node is directly dependent on given node
     * 
     * @param node
     * @return whether this node is directly dependent on given node
     */
    public boolean hasDependant(ExecutedNode node) {
        return dependants.contains(node);
    }

    /**
     * @return the executed
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * @param executed
     *            the executed to set
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    /**
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    public DPUInstanceRecord getDpuInstance() {
        return dpuInstance;
    }


    public void addOutgoingEdge(Edge e, ExecutedNode target) {
        outgoingEdges.add(new ExecutedEdge(this,target,e));
    }

    public void addIncomingEdge(Edge e, ExecutedNode source) {
        incomingEdges.add(new ExecutedEdge(source,this,e));
    }

    public Set<ExecutedEdge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public Set<ExecutedEdge> getIncomingEdges() {
        return incomingEdges;
    }


    public boolean isOutputsAnalysedWhetherConsumedByMultipleDPUs() {
        return outputsAnalysedWhetherConsumedByMultipleDPUs;
    }

    public void setOutputsAnalysedWhetherConsumedByMultipleDPUs(boolean outputsAnalysedWhetherConsumedByMultipleDPUs) {
        this.outputsAnalysedWhetherConsumedByMultipleDPUs = outputsAnalysedWhetherConsumedByMultipleDPUs;
    }



}
