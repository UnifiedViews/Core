package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;

/**
 * Test suite for {@link Node} class.
 * 
 * @author Jan Vojt
 */
public class NodeTest {

    /**
     * Tested instance.
     */
    private Node instance;

    @Before
    public void setUp() {
        instance = new Node();
    }

    @Test
    public void testCopy() {
        // initialize contained objects
        int posX = 5;
        int posY = 6;
        Position position = new Position(posX, posY);
        DPUInstanceRecord dpu = new DPUInstanceRecord();
        PipelineGraph graph = new PipelineGraph();

        instance.setDpuInstance(dpu);
        instance.setGraph(graph);
        instance.setPosition(position);

        Node copy = new Node(instance);

        assertNotSame(instance, copy);
        assertNotSame(dpu, copy.getDpuInstance());
        assertNotSame(position, copy.getPosition());
        assertEquals(posX, copy.getPosition().getX());
        assertEquals(posY, copy.getPosition().getY());

        // GRAPH CANNOT BE COPIED!!
        assertNull(copy.getGraph());
    }
}
