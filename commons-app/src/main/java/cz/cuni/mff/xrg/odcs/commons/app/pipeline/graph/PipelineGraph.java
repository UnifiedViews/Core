/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;

import javax.persistence.*;
import java.util.*;

/**
 * Oriented acyclic graph representation of pipeline. Each Node represents a
 * DPURecord instance, and each edge represents data flow.
 *
 * @author Jiri Tomes
 * @author Bogo
 * @author Jan Vojt
 */
@Entity
@Table(name = "ppl_graph")
public class PipelineGraph implements DataObject {

    /**
     * Primary key of graph stored in db
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ppl_graph")
    @SequenceGenerator(name = "seq_ppl_graph", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Pipeline this graph belongs to
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pipeline_id", unique = true, nullable = false)
    private Pipeline pipeline;

    /**
     * Set of nodes which represent DPUs
     * <p>
     * Nodes are eagerly loaded, because they are needed every time graph is loaded.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "graph", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Node> nodes = new HashSet<>();

    /**
     * Set of edges which represent data flow between DPUs.
     * <p>
     * Edges are eagerly loaded, because they are needed every time graph is loaded.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "graph", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Edge> edges = new HashSet<>();

    /**
     * Empty constructor for JPA.
     */
    public PipelineGraph() {
    }

    /**
     * Copy constructor. Note that newly created graph is NOT associated with
     * any pipeline, as graph should always be unique.
     *
     * @param graph
     *            the value of pipeline graph.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public PipelineGraph(PipelineGraph graph) {

        // create mapping from old nodes to new nodes to be able to correctly
        // reference source and target nodes for new edges
        Map<Node, Node> nMap = new HashMap<>(graph.nodes.size());
        for (Node oldNode : graph.getNodes()) {
            Node newNode = new Node(oldNode);
            newNode.setGraph(this);
            nMap.put(oldNode, newNode);
        }

        // create edges
        edges = new HashSet<>();
        for (Edge oldEdge : graph.getEdges()) {
            Edge newEdge = new Edge(
                    nMap.get(oldEdge.getFrom()),
                    nMap.get(oldEdge.getTo()),
                    oldEdge.getScript());
            newEdge.setGraph(this);
            edges.add(newEdge);
        }

        // assign nodes
        nodes = new HashSet<>(nMap.values());
    }

    /**
     * Add node to pipeline graph.
     *
     * @param node
     *            The value of node will be added to pipeline graph.
     */
    public void addNode(Node node) {
        nodes.add(node);
        node.setGraph(this);
    }

    /**
     * Removes node from graph.
     *
     * @param node
     *            the node will be removed.
     * @return <tt>true</tt> if graph contained given node
     */
    public boolean removeNode(Node node) {
        node.setGraph(null);
        return nodes.remove(node);
    }

    /**
     * Returns the set of nodes for pipeline graph.
     *
     * @return the set of nodes for pipeline graph.
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * Set new set of nodes for this pipeline graph.
     *
     * @param newNodes
     *            set of nodes for pipeline graph.
     */
    public void setNodes(Set<Node> newNodes) {
        nodes = newNodes;
        // update on owning side
        for (Node node : nodes) {
            node.setGraph(this);
        }
    }

    /**
     * Returns the set of edges for this pipeline graph.
     *
     * @return the set of edges for this pipeline graph.
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * Set new set of edges for this pipeline graph
     *
     * @param edges
     *            set of edges for pipeline graph.
     */
    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
        for (Edge edge : edges) {
            edge.setGraph(this);
            nodes.add(edge.getFrom());
            nodes.add(edge.getTo());
        }
    }

    /**
     * Adds a new node created from given DPUInstance.
     *
     * @param dpuInstance
     *            DPU instance
     * @return node instance
     */
    public Node addDpuInstance(DPUInstanceRecord dpuInstance) {
        Node node = new Node(dpuInstance);
        addNode(node);
        return node;
    }

    /**
     * Removes DPURecord from graph.
     *
     * @param dpuId
     *            value of DPU id.
     * @return removed node
     */
    public Node removeDpu(int dpuId) {
        Node node = getNodeById(dpuId);
        if (node != null) {
            nodes.remove(node);
        }
        return node;
    }

    /**
     * Adds a single edge into pipeline graph, unless it exists already.
     *
     * @param from
     *            source DPURecord
     * @param to
     *            target DPURecord
     * @return newly created edge or null
     */
    public Edge addEdge(Node from, Node to) {
        Edge edge = new Edge(from, to);

        // adds unless it is present already
        boolean added = edges.add(edge);

        // make sure nodes are members of this graph
        nodes.add(from);
        nodes.add(to);

        // update on owning side
        edge.setGraph(this);

        //TODO Find existing edge for this connection
        return added ? edge : null;
    }

    /**
     * Removes an egde from graph. Reference to graph in edge is also cleared.
     *
     * @param edge
     *            The value of edge that will be removed.
     * @return true if edge was removed, false if no such edge was in the graph
     */
    public boolean removeEdge(Edge edge) {
        edge.setGraph(null);
        return edges.remove(edge);
    }

    /**
     * Duplicate method from adding edge to graph. Probably only one shall
     * remain.
     *
     * @param fromId
     *            node id
     * @param toId
     *            node id
     * @return hashcode as identification of new edge
     */
    public int addEdge(int fromId, int toId) {
        Node dpuFrom = getNodeById(fromId);
        Node dpuTo = getNodeById(toId);

        Edge edge = addEdge(dpuFrom, dpuTo);
        return edge == null ? 0 : edge.hashCode();
    }

    /**
     * Removes edge from graph.
     *
     * @param edgeId
     *            id of edge to be removed
     * @return edge with given id that was removed
     */
    public Edge removeEdge(int edgeId) {
        Edge edge = getEdgeById(edgeId);
        if (edge != null) {
            boolean result = removeEdge(edge);
        }
        return edge;
    }

    /**
     * Gets edge with given id.
     *
     * @param id
     *            edge id
     * @return edge with given id
     */
    public Edge getEdgeById(int id) {
        for (Edge el : edges) {
            if (el.hashCode() == id) {
                return el;
            }
        }
        return null;
    }

    /**
     * Gets node with given id.
     *
     * @param id
     *            node id
     * @return node with given id
     */
    public Node getNodeById(int id) {
        for (Node el : nodes) {
            if (el.hashCode() == id) {
                return el;
            }
        }
        return null;
    }

    /**
     * Updates Node position in graph.
     *
     * @param dpuId
     *            The DPU ID will that will be moved.
     * @param newX
     *            The value of X point of node.
     * @param newY
     *            The value of Y point of node
     */
    public boolean moveNode(int dpuId, int newX, int newY) {
        Node node = getNodeById(dpuId);
        if (node == null) {
            throw new IllegalArgumentException(
                    "Node with supplied id was not found!");
        }
        if ((node.getPosition() != null)&&(node.getPosition().getX() == newX)&&(node.getPosition().getY() == newY)) {
            return false;
        }
        node.setPosition(new Position(newX, newY));
        return true;
    }

    /**
     * Returns the pipeline for this pipeline graph.
     *
     * @return the pipeline for this pipeline graph.
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Set the new pipeline set for this pipeline graph.
     *
     * @param pipeline
     *            the new pipeline set for this pipeline graph.
     */
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Validates new edge in graph. TODO refractor and use <code>Exception</code>s instead of <code>String</code>s.
     *
     * @param fromId
     *            ID of node where the edge starts
     * @param toId
     *            ID of node where the edge ends
     * @return null on success, error message otherwise
     */
    public String validateNewEdge(int fromId, int toId) {
        Node from = getNodeById(fromId);
        Node to = getNodeById(toId);

        // Rules validation with corresponding error messages.
        if (from.equals(to)) {
            return Messages.getString("PipelineGraph.validation.loops");
        }
        //Same edge check
        if (sameEdgeExists(from, to)) {
            return Messages.getString("PipelineGraph.validation.exists");
        }

        //Same edge, other direction check
        for (Edge e : edges) {
            if (e.getFrom().equals(to) && e.getTo().equals(from)) {
                return Messages.getString("PipelineGraph.validation.twoway");
            }
        }

        //Cycle check
        Edge newEdge = new Edge(from, to);
        if (newEdgeCreateCycle(newEdge)) {
            return Messages.getString("PipelineGraph.validation.cycles");
        }

        return null;
    }

    private boolean newEdgeCreateCycle(Edge newEdge) {
        List<Node> v = new ArrayList<>(nodes);
        List<Edge> e = new ArrayList<>(edges);
        e.add(newEdge);

        while (!v.isEmpty()) {
            Node candidate = null;
            //Find vertex with no out edges
            for (Node vertex : v) {
                boolean hasOutEdge = false;
                for (Edge edge : e) {
                    if (edge.getFrom().equals(vertex)) {
                        hasOutEdge = true;
                        break;
                    }
                }
                if (!hasOutEdge) {
                    candidate = vertex;
                    break;
                }
            }

            if (candidate == null) {
                return true;
            }

            //Remove candidate and edges with candidate
            v.remove(candidate);
            for (int i = 0; i < e.size();) {
                Edge edge = e.get(i);
                if (edge.getTo().equals(candidate)) {
                    e.remove(i);
                } else {
                    ++i;
                }

            }
        }
        return false;
    }

    private boolean sameEdgeExists(Node from, Node to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns maximum coordinates of DPUs in graph.
     *
     * @return maximum coordinates of DPUs in graph.
     */
    public Position getBounds() {
        Position bounds = new Position(0, 0);
        for (Node node : nodes) {
            Position nodePosition = node.getPosition();
            if (nodePosition == null) {
                continue;
            }
            if (nodePosition.getX() > bounds.getX()) {
                bounds.setX(nodePosition.getX());
            }
            if (nodePosition.getY() > bounds.getY()) {
                bounds.setY(nodePosition.getY());
            }
        }
        return bounds;
    }

    /**
     * Clones the graph into a new object with the same id. Internally calls the
     * copy constructor and sets the same id as in original graph.
     *
     * @return cloned graph
     */
    public PipelineGraph cloneGraph() {
        PipelineGraph clone = new PipelineGraph(this);
        clone.id = this.id;
        return clone;
    }

    /**
     * Returns the set ID of pipeline graph as {@link Long} value.
     *
     * @return the set ID of pipeline graph as {@link Long} value.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Get all edges pointing to given node.
     *
     * @param node
     *            The value of node.
     * @return list of edges
     */
    public List<Edge> getEdgesTo(Node node) {
        List<Edge> edgesTo = new LinkedList<>();
        for (Edge e : edges) {
            if (e.getTo().equals(node)) {
                edgesTo.add(e);
            }
        }
        return edgesTo;
    }

    /**
     * Get all edges starting at given node.
     *
     * @param node
     *            The value of node.
     * @return list of edges
     */
    public List<Edge> getEdgesFrom(Node node) {
        List<Edge> edgesFrom = new LinkedList<>();
        for (Edge e : edges) {
            if (e.getFrom().equals(node)) {
                edgesFrom.add(e);
            }
        }
        return edgesFrom;
    }

    /**
     * Returns true if two objects represent the same pipeline. This holds if
     * and only if <code>this.id == null ? this == obj : this.id == o.id</code>.
     *
     * @param obj
     * @return true if both objects represent the same pipeline
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final DataObject other = (DataObject) obj;
        if (this.getId() == null) {
            return super.equals(other);
        }

        return Objects.equals(this.getId(), other.getId());
    }

    /**
     * Hashcode is compatible with {@link #equals(java.lang.Object)}.
     *
     * @return The value of hashcode.
     */
    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return super.hashCode();
        }
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        return hash;
    }

}
