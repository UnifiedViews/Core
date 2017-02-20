package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Graph of DPURecord dependencies.
 * 
 * @author Jan Vojt
 */
public class DependencyGraph implements Iterable<ExecutedNode> {

    /**
     * Structure for building dependency graph mapping nodes to dependency
     * nodes.
     */
    private Map<Node, ExecutedNode> dGraph = new LinkedHashMap<>();

    /**
     * List of Extractor nodes - nodes without dependencies
     */
    private List<ExecutedNode> starters = new ArrayList<>();

    /**
     * Cache used for fast searching of node ancestors. A {@link Node} with no
     * ancestor (no incoming {@link Edge}) is not indexed here at all.
     */
    private Map<Node, List<Node>> ancestorCache = new LinkedHashMap<>();

    /**
     * Constructs dependency graph from given pipeline graph.
     * 
     * @param graph
     *            pipeline graph
     */
    public DependencyGraph(PipelineGraph graph) {
        buildDependencyGraph(graph);
        findStarters();
    }

    /**
     * Constructs dependency graph containing only dependencies required to run
     * given debugNode.
     * 
     * @param graph
     *            pipeline graph to build dependencies from
     * @param debugNode
     *            node whose dependencies are used exclusively
     */
    public DependencyGraph(PipelineGraph graph, Node debugNode) {

        // first build complete dependency graph
        buildDependencyGraph(graph);

        // now create trimmed PipelineGraph containing only nodes needed to run
        // debugNode
        List<Node> oNodes = getAllAncestors(debugNode);
        oNodes.add(debugNode);

        Set<Edge> nEdges = new HashSet<>();
        List<Edge> edges = new ArrayList<>(graph.getEdges());
        Collections.sort(edges, new Comparator<Edge>() {

            @Override
            public int compare(Edge o1, Edge o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        for (Edge edge : edges) {
            if (oNodes.contains(edge.getFrom())
                    && oNodes.contains(edge.getTo())) {
                // Copy edge so we do not work with the edge from original
                // graph. Otherwise calling persist/merge on pipeline will
                // cascade to edge where the trimmed graph might be found
                // as a new entity. See GH-1156.
                Edge edge2 = new Edge(edge.getFrom(), edge.getTo(), edge.getScript());
                edge2.setId(edge.getId());
                nEdges.add(edge2);
            }
        }

        PipelineGraph tGraph = new PipelineGraph();
        tGraph.setEdges(nEdges);

        // If the debug node has no dependencies, it is the only node to be run,
        // in which case it cannot be found through edges and we need to add
        // it as a dependency manually (exclusive dependency).
        if (tGraph.getNodes().isEmpty()) {
            tGraph.addNode(debugNode);
        }

        // rebuild dependencies in trimmed graph
        buildDependencyGraph(tGraph);
        findStarters();
    }

    /**
     * Returns iterator, which iterates over pipeline graph in a way that all
     * dependencies come before the nodes they depend on.
     */
    @Override
    public GraphIterator iterator() {
        return new GraphIterator(this);
    }

    /**
     * @return the extractors without inputs = the nodes which may be run first
     */
    public List<ExecutedNode> getStarters() {
        return starters;
    }

    /**
     * Return all direct ancestors to the given node.
     * 
     * @param node
     * @return set of ancestors
     */
    public List<Node> getAncestors(Node node) {
        return ancestorCache.get(node);
    }

    /**
     * Finds extractors without input in the dependency graph. Starter node is
     * a node which does not have any dependencies and thus may be run first.
     * Always call after dependency graph is built!
     */
    private void findStarters() {
        starters = new ArrayList<>();
        for (ExecutedNode node : dGraph.values()) {
            // extractors have no dependencies
            if (node.getDependencies().isEmpty()) {
                starters.add(node);
            }
        }
    }

    /**
     * Builds dependency graph consisting of mapping from {@link Node}s to
     * their corresponding newly created {@link ExecutedNode}s.
     * 
     * @param graph
     *            to build dependencies from
     */
    private void buildDependencyGraph(PipelineGraph graph) {

        // clear all previous data
        int noOfNodes = graph.getNodes().size();
        dGraph = new LinkedHashMap<>(noOfNodes);
        ancestorCache = new LinkedHashMap<>(noOfNodes);

        // initialize map for dependency nodes
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        Collections.sort(nodes, new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        for (Node node : nodes) {
            dGraph.put(node, new ExecutedNode(node));
        }

        List<Edge> edges = new ArrayList<>(graph.getEdges());
        Collections.sort(edges, new Comparator<Edge>() {

            @Override
            public int compare(Edge o1, Edge o2) {
                return o1.getId().compareTo(o2.getId());
            }

        });
        // iterate over all edges and reflect them in dependency nodes
        for (Edge e : edges) {

            // find the target node in the dependency graph
            ExecutedNode tNode = dGraph.get(e.getTo());

            // find the source node in the dependency graph
            ExecutedNode sNode = dGraph.get(e.getFrom());

            // add the dependency
            tNode.addDependency(sNode);
            sNode.addDependant(tNode);

            // cache ancestors
            cacheAncestor(e.getFrom(), e.getTo());

            //mark also outgoing/incoming edges for a node
            sNode.addOutgoingEdge(e, tNode);
            tNode.addIncomingEdge(e, sNode);
        }
    }

    /**
     * Adds a single source node to cache indexed by target nodes.
     * Used during build process to create a cache for fast searching of {@link Node}s direct ancestors.
     * 
     * @param sNode
     * @param tNode
     */
    private void cacheAncestor(Node sNode, Node tNode) {
        List<Node> nodes = ancestorCache.get(tNode);
        if (nodes == null) {
            nodes = new ArrayList<>();
            ancestorCache.put(tNode, nodes);
        }
        nodes.add(sNode);
    }

    /**
     * Returns all nodes on which given node depends. In other words, it returns
     * all nodes that need to be run before given node. This does not include
     * given node itself.
     * 
     * @param node
     * @return all dependencies of given node
     */
    private List<Node> getAllAncestors(Node node) {

        List<Node> oAncestors = ancestorCache.get(node);
        List<Node> ancestors = new ArrayList<>();

        if (oAncestors != null) {
            ancestors.addAll(oAncestors);
            for (Node ancestor : oAncestors) {
                ancestors.addAll(getAllAncestors(ancestor));
            }
        }

        return ancestors;
    }
}
