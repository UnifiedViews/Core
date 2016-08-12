package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link PipelineGraph}.
 * 
 * @author Jan Vojt
 */
public class PipelineGraphTest {

    /**
     * Tested instance.
     */
    private PipelineGraph instance;

    @Before
    public void setUp() {
        instance = new PipelineGraph();
    }

    @Test
    public void testSetEdges() {
        Node nodeA = new Node();
        Node nodeB = new Node();
        Edge edge = new Edge(nodeA, nodeB);
        LinkedHashSet<Edge> edges = new LinkedHashSet<>(Arrays.asList(edge));
        instance.setEdges(edges);

        assertEquals(edges, instance.getEdges());
        assertEquals(2, instance.getNodes().size());
        assertTrue(instance.getNodes().contains(nodeA));
        assertTrue(instance.getNodes().contains(nodeB));
    }

    @Test
    public void testAddEdge() {
        Node nodeA = new Node();
        Node nodeB = new Node();

        instance.addNode(nodeB);
        instance.addEdge(nodeA, nodeB);

        assertEquals(1, instance.getEdges().size());
        assertEquals(2, instance.getNodes().size());

        // by adding an edge, both its nodes need to be members of graph
        assertTrue(instance.getNodes().contains(nodeA));
        assertTrue(instance.getNodes().contains(nodeB));
    }

    @Test
    public void testCopy() {
        instance.addEdge(new Node(), new Node());

        PipelineGraph copy = new PipelineGraph(instance);

        assertNotSame(instance, copy);
        assertEquals(instance.getEdges().size(), copy.getEdges().size());
        assertEquals(instance.getNodes().size(), copy.getNodes().size());

        for (Node oNode : instance.getNodes()) {
            assertFalse(copy.getNodes().contains(oNode));
        }
        for (Edge oEdge : instance.getEdges()) {
            assertFalse(copy.getEdges().contains(oEdge));
        }
    }
}
