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
import eu.unifiedviews.dataunit.DataUnit;

import javax.persistence.*;
import java.util.Objects;

/**
 * Edge represents oriented connection between nodes of the graph.
 *
 * @author Bogo
 */
@Entity
@Table(name = "ppl_edge")
public class Edge implements DataObject {

    /**
     * Primary key of graph stored in db
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ppl_edge")
    @SequenceGenerator(name = "seq_ppl_edge", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "node_from_id")
    private Node from;

    @ManyToOne(optional = false)
    @JoinColumn(name = "node_to_id")
    private Node to;

    /**
     * Reference to owning graph
     */
    @ManyToOne
    @JoinColumn(name = "graph_id")
    private PipelineGraph graph;

    @Column(name = "data_unit_name", nullable = true, length = 2048)
    private String script;

    /**
     * Set script defining outputs to inputs mappings.
     *
     * @param script
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Get script defining outputs to inputs mappings.
     *
     * @return script defining outputs to inputs mappings.
     */
    public String getScript() {
        return script;
    }

    /**
     * No-arg public constructor for JPA
     */
    public Edge() {
    }

    /**
     * Constructor with specification of connecting nodes.
     *
     * @param from
     * @param to
     */
    public Edge(Node from, Node to) {
        this(from, to, "");
    }

    /**
     * Constructor with specification of connecting nodes and {@link DataUnit} name.
     *
     * @param from
     * @param to
     * @param script
     */
    public Edge(Node from, Node to, String script) {
        this.script = script;

        setFrom(from);
        setTo(to);
    }

    /**
     * @return start node of edge
     */
    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;

        if (from != null) from.getStartNodeOfEdges().add(this);
    }

    /**
     * @return end node of edge
     */
    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;

        if (to != null) to.getEndNodeOfEdges().add(this);
    }

    /**
     * Get graph this edge is in.
     *
     * @return graph
     */
    public PipelineGraph getGraph() {
        return graph;
    }

    /**
     * Set graph this edge is in.
     *
     * @param graph
     */
    public void setGraph(PipelineGraph graph) {
        this.graph = graph;

        if (graph != null) graph.getEdges().add(this);
    }

    @Override
    public int hashCode() {
        if (this.id == null) {
            return super.hashCode();
        }
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final Edge other = (Edge) o;
        if (this.id == null) {
            return super.equals(other);
        }

        return Objects.equals(this.id, other.id);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
