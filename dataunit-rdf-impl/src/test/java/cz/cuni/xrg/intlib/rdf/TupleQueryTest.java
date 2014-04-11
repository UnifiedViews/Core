package cz.cuni.xrg.intlib.rdf;

import cz.cuni.mff.xrg.odcs.rdf.data.RDFDataUnitFactory;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
import cz.cuni.mff.xrg.odcs.rdf.help.OrderTupleQueryResult;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.ManagableRdfDataUnit;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

import org.junit.*;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import static org.junit.Assert.*;
import org.openrdf.query.TupleQueryResult;

/**
 *
 * @author Jiri Tomes
 */
public class TupleQueryTest {

	private static ManagableRdfDataUnit repository;

	/**
	 * Basic repository inicializing before test execution.
	 */
	@BeforeClass
	public static void inicialize() {
		repository = RDFDataUnitFactory.createLocalRDFRepo("");
	}

	/**
	 * The repository is destroyed at the end of working.
	 */
	@AfterClass
	public static void deleting() {
		repository.delete();
	}

	/**
	 * Cleaning repository before each test execution.
	 */
	@Before
	public void cleaning() {
		repository.clean();
	}

	/**
	 * Test executing ordered SELECT query for object.
	 */
	@Test
	public void OrderTupleQueryResultTestForObject() throws RepositoryException {
		String orderSelectQuery = "SELECT ?x ?y ?z where {?x ?y ?z} ORDER BY ?x ?y ?z";

		String[] expectedNames = {"x", "y", "z"};

		String expectedVarName = "z";

		String[] expectedDataForVar = {
			"\"object\"", "http://object", "_:b23",
			"\"ob\"@en", "\"25\"^^<http://www.w3.org/2001/XMLSchema#integer>"};

        RepositoryConnection connection = repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
		Resource subject = factory.createURI("http://subject");
        Resource subjectBlank =  factory.createBNode("id");

		URI predicate = factory.createURI("http://predicate");

		Value objectLiteral = factory.createLiteral("object");
		Value object = factory.createURI("http://object");
		Value objectBlank =  factory.createBNode("b23");
		Value objectLanguageLiteral = factory.createLiteral("ob", "en");
		Value objectTypedLiteral = factory.createLiteral("25",
                factory.createURI("http://www.w3.org/2001/XMLSchema#integer"));


        connection.add(subject, predicate, object, repository.getDataGraph());
        connection.add(subjectBlank, predicate, object,repository.getDataGraph());
        connection.add(subjectBlank, predicate, objectBlank,repository.getDataGraph());
        connection.add(subject, predicate, objectLanguageLiteral,repository.getDataGraph());
        connection.add(subject, predicate, objectTypedLiteral,repository.getDataGraph());

        
        assertEquals(5L, connection.size(repository.getDataGraph()));

		try {
			OrderTupleQueryResult result = repository
					.executeOrderSelectQueryAsTuples(orderSelectQuery);

			List<BindingSet> bindings = new ArrayList<>();

			List<String> names = result.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (result.hasNext()) {
				bindings.add(result.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for object are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test executing ordered SELECT query for predicate.
	 */
	@Test
	public void OrderTupleQueryResultTestForPredicate() throws RepositoryException {
		String orderSelectQuery = "SELECT ?b ?c where {?a ?b ?c} ORDER BY ?a ?b ?c";
		String[] expectedNames = {"b", "c"};

		String expectedVarName = "b";
		String[] expectedDataForVar = {"http://predicate", "http://p", "http://pred"};

        RepositoryConnection connection =  repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
        Resource subjectBlank =  factory.createBNode("id");
        Resource subject = factory.createURI("http://subject");

        URI predicate = factory.createURI(expectedDataForVar[0]);
        URI p = factory.createURI(expectedDataForVar[1]);
        URI pred = factory.createURI(expectedDataForVar[2]);

        Value object = factory.createLiteral("object");
        Value objectBlank = factory.createBNode("blank");


        connection.add(subject, predicate, object, repository.getDataGraph());
        connection.add(subjectBlank, p, object, repository.getDataGraph());
        connection.add(subject, pred, objectBlank, repository.getDataGraph());

		assertEquals(3L, connection.size(repository.getDataGraph()));

		try {
			OrderTupleQueryResult result = repository
					.executeOrderSelectQueryAsTuples(orderSelectQuery);

			List<BindingSet> bindings = new ArrayList<>();

			List<String> names = result.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (result.hasNext()) {
				bindings.add(result.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for predicate are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test executing ordered SELECT query for subject.
	 */
	@Test
	public void OrderTupleQueryResultTestForSubject() throws RepositoryException {
		String orderSelectQuery = "SELECT ?x ?y WHERE {?x ?y ?z} ORDER BY ?x ?y ?z";

		String[] expectedNames = {"x", "y"};

		String expectedVarName = "x";
		String[] expectedDataForVar = {"http://subject", "http://s"};

        RepositoryConnection connection =  repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
		Resource subject = factory.createURI(expectedDataForVar[0]);
		Resource s = factory.createURI(expectedDataForVar[1]);

		URI predicate = factory.createURI("http://predicate");
		Value object = factory.createLiteral("object");


        connection.add(subject, predicate, object, repository.getDataGraph());
        connection.add(s, predicate, object, repository.getDataGraph());
		assertEquals(2L, connection.size(repository.getDataGraph()));


		try {
			OrderTupleQueryResult result = repository
					.executeOrderSelectQueryAsTuples(orderSelectQuery);

			List<BindingSet> bindings = new ArrayList<>();

			List<String> names = result.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (result.hasNext()) {
				bindings.add(result.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for subject are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test executing SELECT query for object.
	 */
	@Test
	public void TupleQueryResultTestForObject() throws RepositoryException {

		String selectQuery = "SELECT ?x ?y ?z where {?x ?y ?z}";
		String[] expectedNames = {"x", "y", "z"};

		String expectedVarName = "z";

		String[] expectedDataForVar = {
			"\"object\"", "http://object", "_:b23",
			"\"ob\"@en", "\"25\"^^<http://www.w3.org/2001/XMLSchema#integer>"};

        RepositoryConnection connection = repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
        Resource subject = factory.createURI("http://subject");


        Resource subjectBlank =  factory.createBNode("id");

        URI predicate = factory.createURI("http://predicate");
        Value objectLiteral = factory.createLiteral("object");
        Value object = factory.createURI("http://object");

        factory = connection.getValueFactory();
        Value objectBlank = factory.createBNode("b23");
        Value objectLanguageLiteral = factory.createLiteral("ob", "en");
        Value objectTypedLiteral = factory.createLiteral("25",
                factory.createURI("http://www.w3.org/2001/XMLSchema#integer"));


        connection.add(subject, predicate, objectLiteral, repository.getDataGraph());
        connection.add(subjectBlank, predicate, object, repository.getDataGraph());
        connection.add(subjectBlank, predicate, objectBlank, repository.getDataGraph());
        connection.add(subject, predicate, objectLanguageLiteral, repository.getDataGraph());
        connection.add(subject, predicate, objectTypedLiteral, repository.getDataGraph());

		assertEquals(5L, connection.size(repository.getDataGraph()));

		try {
			List<BindingSet> bindings = new ArrayList<>();
			TupleQueryResult tupleQueryResult = repository
					.executeSelectQueryAsTuples(selectQuery);

			List<String> names = tupleQueryResult.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (tupleQueryResult.hasNext()) {
				bindings.add(tupleQueryResult.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for object are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test executing SELECT query for predicate.
	 */
	@Test
	public void TupleQueryResultTestForPredicate() throws RepositoryException {

		String selectQuery = "SELECT ?b ?c where {?a ?b ?c}";
		String[] expectedNames = {"b", "c"};

		String expectedVarName = "b";
		String[] expectedDataForVar = {"http://predicate", "http://p", "http://pred"};

        RepositoryConnection connection = repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
        Resource subject = factory.createURI("http://subject");
        Resource subjectBlank =  factory.createBNode("id");

        URI predicate = factory.createURI(expectedDataForVar[0]);
        URI p = factory.createURI(expectedDataForVar[1]);
        URI pred = factory.createURI(expectedDataForVar[2]);

        Value object = factory.createLiteral("object");
        Value objectBlank = factory.createBNode("blank");

        connection.add(subject, predicate, object, repository.getDataGraph());
        connection.add(subjectBlank, p, object, repository.getDataGraph());
        connection.add(subject, pred, objectBlank, repository.getDataGraph());
		assertEquals(3L, connection.size(repository.getDataGraph()));

		try {
			List<BindingSet> bindings = new ArrayList<>();
			TupleQueryResult tupleQueryResult = repository
					.executeSelectQueryAsTuples(selectQuery);

			List<String> names = tupleQueryResult.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (tupleQueryResult.hasNext()) {
				bindings.add(tupleQueryResult.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for predicate are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test executing SELECT query for subject.
	 */
	@Test
	public void TupleQueryResultTestForSubject() throws RepositoryException {

		String selectQuery = "SELECT ?x ?y where {?x ?y ?z}";
		String[] expectedNames = {"x", "y"};

		String expectedVarName = "x";
		String[] expectedDataForVar = {"http://subject", "http://s"};

        RepositoryConnection connection = repository.getConnection();
        ValueFactory factory = connection.getValueFactory();
        Resource subject = factory.createURI(expectedDataForVar[0]);
        Resource s = factory.createURI(expectedDataForVar[1]);

        URI predicate = factory.createURI("http://predicate");
        Value object = factory.createLiteral("object");


        connection.add(subject, predicate, object, repository.getDataGraph());
        connection.add(s, predicate, object, repository.getDataGraph());

        assertEquals(2L, connection.size(repository.getDataGraph()));

		try {
			List<BindingSet> bindings = new ArrayList<>();
			TupleQueryResult tupleQueryResult = repository
					.executeSelectQueryAsTuples(selectQuery);

			List<String> names = tupleQueryResult.getBindingNames();
			boolean sameNames = sameNames(names, expectedNames);
			assertTrue("Name of variables are not same", sameNames);

			while (tupleQueryResult.hasNext()) {
				bindings.add(tupleQueryResult.next());
			}

			boolean sameData = sameData(bindings, expectedVarName,
					expectedDataForVar);
			assertTrue("Expected data for subject are not same", sameData);

		} catch (InvalidQueryException | QueryEvaluationException e) {
			fail(e.getMessage());
		}

	}

	private boolean sameData(List<BindingSet> bindings, String varName,
			String[] expectedData) {

		for (BindingSet nextBinding : bindings) {
			Binding binding = nextBinding.getBinding(varName);

			Value value = binding.getValue();
			if (value == null) {
				return false;
			} else {
				boolean dataFound = false;
				String stringValue = value.toString();

				for (String next : expectedData) {
					if (next.equals(stringValue)) {
						dataFound = true;
						break;
					}
				}
				if (!dataFound) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean sameNames(List<String> names, String[] expectedNames) {
		if (expectedNames == null) {
			return false;
		} else {

			boolean definedAll = true;

			for (String expectedName : expectedNames) {
				if (!names.contains(expectedName)) {
					definedAll = false;
					break;
				}
			}
			return definedAll;
		}
	}
}
