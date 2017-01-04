package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link Position} class.
 * 
 * @author Jan Vojt
 */
public class PositionTest {

    /**
     * Tested instance.
     */
    private Position instance;

    private int x = 3;

    private int y = 4;

    @Before
    public void setUp() {
        instance = new Position(x, y);
    }

    @Test
    public void testCopy() {
        Position copy = new Position(instance);
        assertNotSame(instance, copy);
        assertEquals(x, copy.getX());
        assertEquals(y, copy.getY());
    }
}
