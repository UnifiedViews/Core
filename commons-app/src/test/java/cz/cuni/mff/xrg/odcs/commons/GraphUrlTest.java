package cz.cuni.mff.xrg.odcs.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.rdf.repositories.GraphUrl;

/**
 * Test suite for {@link GraphUrl} class.
 * 
 * @author Petyr
 */
public class GraphUrlTest {

    /**
     * Execute translate test.
     */
    @Test
    public void translateTest() {
        final String input = "exec_3_dpu_4_du_0";
        final String expectedOutput =
                "http://unifiedviews.eu/resource/internal/dataunit/exec/3/dpu/4/du/0";

        assertEquals(expectedOutput, GraphUrl.translateDataUnitId(input));
    }
}
