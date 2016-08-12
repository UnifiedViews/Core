import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.cuni.mff.xrg.odcs.commons.app.dataunit.rdf.ManagableRdfDataUnit;
import cz.cuni.mff.xrg.odcs.commons.app.dataunit.rdf.localrdf.LocalRDFDataUnit;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RepositoryFrontendHelper;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;

public class RepositoryFrontendHelperSysTest {
    /**
     * Count of thread used in tests.
     */
    protected static final int THREAD_SIZE = 10;

    /**
     * Local repository
     */
    protected static ManagableRdfDataUnit rdfRepo;

    /**
     * Logging info about behavior method.
     */
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    final private String encode = "UTF-8";

    /**
     * Path to directory with produced data
     */
    protected Path outDir;

    /**
     * Path to directory with test input data
     */
    protected String testFileDir;

    /**
     * Path to test repository
     */
    private Path pathRepo;

    /**
     * Basic setting before test execution.
     */
    @Before
    public void setUp() {
        try {
            pathRepo = Files.createTempDirectory("intlib-repo");
            outDir = Files.createTempDirectory("intlib-out");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        rdfRepo = new LocalRDFDataUnit(pathRepo.toString(),
                "myTestName", "http://default");
    }

    /**
     * Test if repository is created.
     */
    @Test
    public void isRepositoryCreated() {
        assertNotNull(rdfRepo);
    }

    /**
     * Test SPARQL transform using SPARQL update query.
     */
    @Test
    public void transformUsingSPARQLUpdate() throws RepositoryException {

        String namespace = "http://sport/hockey/";
        String subjectName = "Jagr";
        String predicateName = "playes_in";
        String objectName = "Dalas_Stars";

        RepositoryConnection connection = rdfRepo.getConnection();
        ValueFactory factory = connection.getValueFactory();
        Resource subject = factory.createURI(namespace + subjectName);
        URI predicate = factory.createURI(namespace + predicateName);
        Value object = factory.createLiteral(objectName);

        String updateQuery = "DELETE { ?who ?what 'Dalas_Stars' }"
                + "INSERT { ?who ?what 'Boston_Bruins' } "
                + "WHERE { ?who ?what 'Dalas_Stars' }";

        connection.add(subject, predicate, object, rdfRepo.getDataGraph());

        boolean beforeUpdate = connection.hasStatement(subject, predicate, object, true, rdfRepo.getDataGraph());
        assertTrue(beforeUpdate);

        try {
            DatasetImpl dataSet = new DatasetImpl();
            dataSet.addDefaultGraph(rdfRepo.getDataGraph());
            dataSet.addNamedGraph(rdfRepo.getDataGraph());
            RepositoryFrontendHelper.executeSPARQLUpdateQuery(connection, updateQuery, rdfRepo.getDataGraph(), dataSet);
        } catch (RDFException e) {
            fail(e.getMessage());
        }

        boolean afterUpdate = connection.hasStatement(subject, predicate, object, true, rdfRepo.getDataGraph());
        assertFalse(afterUpdate);
        connection.close();
    }

    /**
     * Run 'BIG' pipeline - 3 transformer, 1 loader to N3 file.
     */
    @Test
    public void BIGDataTest() throws RepositoryException {
        BigTransformQuery1();
        BigTransformQuery2();
        BigTransformQuery3();
    }

    private void BigTransformQuery1() throws RepositoryException {

        // Dotaz nahrazuje vsechny objekty jejich spravnymi URI

        String updateQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "PREFIX ndf: <http://linked.opendata.cz/ontology/ndfrt/> "
                + "PREFIX adms: <http://www.w3.org/ns/adms#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "DELETE { "
                + "  ?s ?p ?s1 . } "
                + "INSERT { "
                + "  ?s ?p ?s2 . } "
                + "WHERE { "
                + "  ?s1 owl:sameAs ?s2 . "
                + "  ?s ?p ?s1 . }";

        RepositoryConnection connection = null;
        try {
            connection = rdfRepo.getConnection();
            DatasetImpl dataSet = new DatasetImpl();
            dataSet.addDefaultGraph(rdfRepo.getDataGraph());
            dataSet.addNamedGraph(rdfRepo.getDataGraph());
            RepositoryFrontendHelper.executeSPARQLUpdateQuery(connection, updateQuery, rdfRepo.getDataGraph(), dataSet);
        } catch (RDFException e) {
            fail(e.getMessage());
        } finally {
            connection.close();
        }
        LOG.debug("Transform Query 1 - OK");
    }

    private void BigTransformQuery2() throws RepositoryException {

        // Dotaz nahrazuje vsechny subjekty jejich spravnymi URI

        String updateQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "PREFIX ndf: <http://linked.opendata.cz/ontology/ndfrt/> "
                + "PREFIX adms: <http://www.w3.org/ns/adms#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "DELETE { "
                + "  ?s1 ?p ?o . } "
                + "INSERT { "
                + "  ?s2 ?p ?o . } "
                + "WHERE { "
                + "  ?s1 owl:sameAs ?s2 . "
                + "  ?s ?p ?o . }";

        RepositoryConnection connection = null;

        try {
            connection = rdfRepo.getConnection();
            DatasetImpl dataSet = new DatasetImpl();
            dataSet.addDefaultGraph(rdfRepo.getDataGraph());
            dataSet.addNamedGraph(rdfRepo.getDataGraph());
            RepositoryFrontendHelper.executeSPARQLUpdateQuery(connection, updateQuery, rdfRepo.getDataGraph(), dataSet);
        } catch (RDFException e) {
            fail(e.getMessage());
        } finally {
            connection.close();
        }
        LOG.debug("Transform Query 2 - OK");
    }

    private void BigTransformQuery3() throws RepositoryException {

        //Maze same-as na spatne URI

        String updateQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX dcterms: <http://purl.org/dc/terms/> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "PREFIX ndf: <http://linked.opendata.cz/ontology/ndfrt/> "
                + "PREFIX adms: <http://www.w3.org/ns/adms#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "DELETE { "
                + "  ?s1 owl:sameAs ?s2 . } "
                + "WHERE { "
                + "  ?s1 owl:sameAs ?s2 . }";

        RepositoryConnection connection = null;
        try {
            connection = rdfRepo.getConnection();
            DatasetImpl dataSet = new DatasetImpl();
            dataSet.addDefaultGraph(rdfRepo.getDataGraph());
            dataSet.addNamedGraph(rdfRepo.getDataGraph());
            RepositoryFrontendHelper.executeSPARQLUpdateQuery(connection, updateQuery, rdfRepo.getDataGraph(), dataSet);
        } catch (RDFException e) {
            fail(e.getMessage());
        } finally {
            connection.close();
        }
        LOG.debug("Transform Query 3 - OK");
    }

    /**
     * Test SPARQL transform using SPARQL update query.
     */
    @Test
    public void SecondUpdateQueryTest() throws RepositoryException {

        String updateQuery = "prefix s: <http://schema.org/> "
                + "DELETE {?s s:streetAddress ?o} "
                + "INSERT {?s s:streetAddress ?x} "
                + "WHERE {"
                + "{ SELECT ?s ?o ?x "
                + "WHERE {?s s:streetAddress ?o}} FILTER (BOUND(?x))}";

        RepositoryConnection connection = null;

        try {
            connection = rdfRepo.getConnection();
            DatasetImpl dataSet = new DatasetImpl();
            dataSet.addDefaultGraph(rdfRepo.getDataGraph());
            dataSet.addNamedGraph(rdfRepo.getDataGraph());
            RepositoryFrontendHelper.executeSPARQLUpdateQuery(connection, updateQuery, rdfRepo.getDataGraph(), dataSet);
        } catch (RDFException e) {
            fail(e.getMessage());
        } finally {
            connection.close();
        }

    }

}
