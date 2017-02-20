package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

/**
 * Created by tomasknap on 16/02/17.
 */
public final class ExecutedEdge {

    private ExecutedNode from;
    private ExecutedNode to;
    private Edge edge;

    public ExecutedEdge(ExecutedNode from, ExecutedNode to, Edge edge) {
        this.from = from;
        this.to = to;
        this.edge = edge;
    }

    public ExecutedNode getFrom() {
        return from;
    }

    public ExecutedNode getTo() {
        return to;
    }

    public Edge getEdge() {
        return edge;
    }
}
