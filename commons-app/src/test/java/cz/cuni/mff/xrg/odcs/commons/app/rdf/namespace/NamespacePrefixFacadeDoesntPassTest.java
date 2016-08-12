package cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.facade.NamespacePrefixFacade;

/**
 * Test suite for RDF namespace prefixes facade.
 * 
 * @author Jan Vojt
 */
@ContextConfiguration(locations = { "classpath:commons-app-test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class NamespacePrefixFacadeDoesntPassTest {

    private static final String TEST_NAME = "ex";

    private static final String TEST_URI = "http://example.com/";

    @Autowired
    private NamespacePrefixFacade npf;

    /**
     * Test of createPrefix method, of class NamespacePrefixFacade.
     */
    @Test
    public void testCreatePrefix() {
        NamespacePrefix prefix = npf.createPrefix(TEST_NAME, TEST_URI);
        assertNotNull(prefix);
        assertEquals(prefix.getName(), TEST_NAME);
        assertEquals(prefix.getPrefixURI(), TEST_URI);
    }

    /**
     * Test of getAllPrefixes method, of class NamespacePrefixFacade.
     */
    @Test
    public void testGetAllPrefixes() {
        List<NamespacePrefix> allPrefixes = npf.getAllPrefixes();
        assertNotNull(allPrefixes);
        assertEquals(2, allPrefixes.size());
    }

    /**
     * Test of getPrefix method, of class NamespacePrefixFacade.
     */
    @Test
    public void testGetPrefix() {
        // fetch existing prefix
        NamespacePrefix prefix = npf.getPrefix(1L);
        assertNotNull(prefix);

        // try to fetch non-existent prefix
        NamespacePrefix nPrefix = npf.getPrefix(0L);
        assertNull(nPrefix);
    }

    /**
     * Test of getPrefixByName method, of class NamespacePrefixFacade.
     */
    @Test
    public void testGetPrefixByName() {
        // fetch existing prefix
        NamespacePrefix prefix = npf.getPrefixByName("ex1");
        assertNotNull(prefix);

        // try to fetch non-existent prefix
        NamespacePrefix nPrefix = npf.getPrefixByName("non-existent");
        assertNull(nPrefix);
    }

    /**
     * Test of save method, of class NamespacePrefixFacade.
     */
    @Test
    @Transactional
    public void testSave() {
        NamespacePrefix prefix = new NamespacePrefix(TEST_NAME, TEST_URI);
        npf.save(prefix);

        NamespacePrefix nPrefix = npf.getPrefixByName(TEST_NAME);

        assertSame(prefix.getName(), nPrefix.getName());
        assertSame(prefix.getPrefixURI(), nPrefix.getPrefixURI());
    }

    /**
     * Test of delete method, of class NamespacePrefixFacade.
     */
    @Test
    @Transactional
    public void testDelete() {
        NamespacePrefix prefix = new NamespacePrefix();
        prefix.setId(1L);
        npf.delete(prefix);
        assertNull(npf.getPrefix(1L));
    }
}
