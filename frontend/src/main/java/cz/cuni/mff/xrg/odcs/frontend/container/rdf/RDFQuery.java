package cz.cuni.mff.xrg.odcs.frontend.container.rdf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.NestingBeanItem;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.ui.Notification;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.rdf.enums.SPARQLQueryType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
import cz.cuni.mff.xrg.odcs.rdf.help.RDFTriple;
import cz.cuni.mff.xrg.odcs.rdf.query.utils.QueryPart;
import cz.cuni.mff.xrg.odcs.rdf.query.utils.QueryRestriction;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;

/**
 * Implementation of {@link Query} interface for RDF queries. Just read-only
 * access to data is supported.
 *
 * @author Bogo
 */
public class RDFQuery implements Query {

    private static final Logger LOG = LoggerFactory.getLogger(RDFQuery.class);

    private static final String ALL_STATEMENT_QUERY = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";

    private final String baseQuery;

    private final int batchSize;

    private final RDFQueryDefinition queryDefinition;

    private ArrayList<Item> cachedItems;

    /**
     * Constructor.
     *
     * @param queryDefinition
     *            Query definition.
     */
    public RDFQuery(RDFQueryDefinition queryDefinition) {
        this.baseQuery = queryDefinition.getBaseQuery();
        this.batchSize = queryDefinition.getBatchSize();
        this.queryDefinition = queryDefinition;
    }

    /**
     * Size of the query result.
     *
     * @return Size of the result.
     */
    @Override
    public int size() {
        ManageableWritableRDFDataUnit repository = RepositoryFrontendHelper.getRepository(queryDefinition.getInfo(), queryDefinition.getDpu(), queryDefinition.getDataUnit());
        if (repository == null) {
            throw new RuntimeException(Messages.getString("RDFQuery.size.exception"));
        }
        try {
            int count;
            String filteredQuery = setWhereCriteria(baseQuery);
            LOG.debug("Size query started...");
            if (isAllStatementQuery(filteredQuery)) {
                count = (int) this.getResultSizeForDataCollection(repository);
            } else {
                count = (int) this.getResultSizeForQuery(repository, filteredQuery);
            }
            LOG.debug("Size query finished!");
            return count;
        } catch (InvalidQueryException ex) {
            Notification.show(Messages.getString("RDFQuery.size.invalidQuery.name"),
                    Messages.getString("RDFQuery.size.invalidQuery.description")
                            + ex.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            LOG.debug("Size query exception", ex);
        } finally {
            try {
                repository.release();
            } catch (DataUnitException ex) {
                LOG.error("Faild to release repository.", ex);
            }
        }
        return 0;
    }

    private boolean isAllStatementQuery(String query) {
        return query.equalsIgnoreCase(ALL_STATEMENT_QUERY);
    }

    /**
     * Load batch of items.
     *
     * @param startIndex
     *            Starting index of the item list.
     * @param count
     *            Count of the items to be retrieved.
     * @return List of items.
     */
    @Override
    public List<Item> loadItems(int startIndex, int count) {
        LOG.debug(String.format("Loading %d items from %d started...", count,
                startIndex));
        if (cachedItems != null) {
            LOG.debug("Using cached items.");
            return cachedItems.subList(startIndex, startIndex + count);
        }

        ManageableWritableRDFDataUnit repository = RepositoryFrontendHelper.getRepository(queryDefinition.getInfo(), queryDefinition.getDpu(), queryDefinition.getDataUnit());
        if (repository == null) {
            LOG.debug("Unable to load RDFDataUnit.");
            throw new RuntimeException(Messages.getString("RDFQuery.loadItems.exception"));
        }

        String filteredQuery = setWhereCriteria(baseQuery);

        QueryRestriction restriction = new QueryRestriction(filteredQuery);
        restriction.setLimit(batchSize);
        //String query = baseQuery + String.format(" LIMIT %d", batchSize);
        int offset = startIndex / batchSize;
        if (offset > 0) {
            //query += String.format(" OFFSET %d", offset * batchSize);
            restriction.setOffset(offset * batchSize);
        }
        String query = restriction.getRestrictedQuery();
        RepositoryConnection connection = null;
        try {
            SPARQLQueryType type = getQueryType(query);
            Object data;
            Graph graph;
            connection = repository.getConnection();
            switch (type) {
                case SELECT:
                    data = RepositoryFrontendHelper.executeSelectQueryAsTuples(connection, query, RDFHelper.getGraphsURISet(repository));
                    break;
                case CONSTRUCT:
                    graph = RepositoryFrontendHelper.executeConstructQuery(connection, query, RDFHelper.getDatasetWithDefaultGraphs(repository));
                    data = getRDFTriplesData(graph);
                    break;
                case DESCRIBE:
                    String resource = query.substring(query.indexOf('<') + 1,
                            query.indexOf('>'));
                    URIImpl uri = new URIImpl(resource);
                    graph = RepositoryFrontendHelper.describeURI(connection, RDFHelper.getGraphsURISet(repository), uri);
                    data = getRDFTriplesData(graph);
                    break;
                default:
                    return null;
            }

            List<Item> items = new ArrayList<>();
            switch (type) {
                case SELECT:
                    TupleQueryResult result = (TupleQueryResult) data;
                    int id = 0;
                    if (result != null) {
                        while (result.hasNext()) {
                            List<String> names = result.getBindingNames();
                            if (names.size() > 0) {
                                Item item = toItem(names, result.next(), ++id);
                                if (item != null) {
                                    items.add(item);
                                }
                            }
                        }
                        result.close();
                    }
                    break;
                case CONSTRUCT:
                    for (RDFTriple triple : (List<RDFTriple>) data) {
                        items.add(toItem(triple));
                    }
                    break;
                case DESCRIBE:
                    cachedItems = new ArrayList<>();
                    for (RDFTriple triple : (List<RDFTriple>) data) {
                        cachedItems.add(toItem(triple));
                    }
                    LOG.debug("Loading of items finished, whole result preloaded and cached.");
                    return cachedItems.subList(startIndex, startIndex + count);
            }

            LOG.debug("Loading of items finished.");
            return items;
        } catch (InvalidQueryException ex) {
            Notification.show(Messages.getString("RDFQuery.loadItems.invalidQuery.name"),
                    Messages.getString("RDFQuery.loadItems.invalidQuery.description")
                            + ex.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } catch (QueryEvaluationException ex) {
            Notification.show(Messages.getString("RDFQuery.loadItems.queryEvaluation.name"),
                    Messages.getString("RDFQuery.loadItems.queryEvaluation.description")
                            + ex.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } catch (DataUnitException e) {
            Notification.show(Messages.getString("RDFQuery.loadItems.dataUnit.name"),
                    Messages.getString("RDFQuery.loadItems.dataUnit.description") + e.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
        return null;
    }

    private String setWhereCriteria(String query) {
        List<Filter> filters = queryDefinition.getFilters();
        return RepositoryFrontendHelper.filterRDFQuery(query, filters);
    }

    private Item toItem(RDFTriple triple) {
        return new NestingBeanItem(triple, queryDefinition.getMaxNestedPropertyDepth(), queryDefinition
                .getPropertyIds());
    }

    private Item toItem(List<String> headers, BindingSet binding, int id) {
        return new BindingSetItem(headers, binding, id);
    }

    /**
     * Override from {@link Query}. Not applicable for our use.
     *
     * @param list
     * @param list1
     * @param list2
     */
    @Override
    public void saveItems(List<Item> list, List<Item> list1, List<Item> list2) {
        throw new UnsupportedOperationException(
                Messages.getString("RDFQuery.saveItems.unsupported"));
    }

    /**
     * Override from {@link Query}. Not applicable for our use.
     *
     * @return not supported
     */
    @Override
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException(
                Messages.getString("RDFQuery.deleteAllItems.unsupported"));
    }

    /**
     * Override from {@link Query}. Not applicable for our use.
     *
     * @return not supported
     */
    @Override
    public Item constructItem() {
        throw new UnsupportedOperationException(
                Messages.getString("RDFQuery.constructItem.unsupported"));
    }

    private List<RDFTriple> getRDFTriplesData(Graph graph) {

        List<RDFTriple> triples = new ArrayList<>();

        int count = 0;

        for (Statement next : graph) {
            String subject = next.getSubject().stringValue();
            String predicate = next.getPredicate().stringValue();
            String object = next.getObject().stringValue();

            count++;

            RDFTriple triple = new RDFTriple(count, subject, predicate, object);
            triples.add(triple);
        }

        return triples;
    }

    private SPARQLQueryType getQueryType(String query) throws InvalidQueryException {
        if (query.length() < 9) {
            //Due to expected exception format in catch block
            throw new InvalidQueryException(new InvalidQueryException(
                    Messages.getString("RDFQuery.queryType.invalidQuery") + query));
        }
        QueryPart queryPart = new QueryPart(query);
        SPARQLQueryType type = queryPart.getSPARQLQueryType();

        return type;
    }

    private long getSizeForConstruct(RDFDataUnit rdfDataUnit, String constructQuery) throws InvalidQueryException {
        long size = 0;

        RepositoryConnection connection = null;

        try {
            connection = rdfDataUnit.getConnection();

            GraphQuery graphQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL,
                    constructQuery);
            graphQuery.setDataset(RDFHelper.getDatasetWithDefaultGraphs(rdfDataUnit));
            try {
                GraphQueryResult result = graphQuery.evaluate();

                // its quicker to iterate through result, than init model and ask model the size 
                while (result.hasNext()) {
                    ++size;
                    result.next();
                }
                result.close();

            } catch (QueryEvaluationException ex) {

                throw new InvalidQueryException(
                        Messages.getString("RDFQuery.constructSize.invalidQuery") + ex
                                .getMessage(),
                        ex);
            }

        } catch (MalformedQueryException ex) {
            throw new InvalidQueryException(
                    Messages.getString("RDFQuery.constructSize.malformedQuery")
                            + ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            LOG.error("Connection to RDF repository failed. {}",
                    ex.getMessage(), ex);
        } catch (DataUnitException ex) {
            LOG.error("DataUnit problem. {}",
                    ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Failed to close connection to RDF repository while querying. {}",
                            ex.getMessage(), ex);
                }
            }
        }

        return size;
    }

    private long getSizeForSelect(RDFDataUnit rdfDataUnit, QueryPart queryPart) throws InvalidQueryException {

        final String sizeVar = "selectSize";

        final String sizeQuery = String.format("%s SELECT (count(*) AS ?%s) WHERE {%s}", queryPart
                .getQueryPrefixes(), sizeVar,
                queryPart.getQueryWithoutPrefixes());
        RepositoryConnection connection = null;
        try {
            connection = rdfDataUnit.getConnection();

            TupleQuery tupleQuery = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, sizeQuery);
            tupleQuery.setDataset(RDFHelper.getDatasetWithDefaultGraphs(rdfDataUnit));
            try {
                TupleQueryResult tupleResult = tupleQuery.evaluate();
                if (tupleResult.hasNext()) {
                    String selectSize = tupleResult.next()
                            .getValue(sizeVar).stringValue();
                    long resultSize = Long.parseLong(selectSize);

                    tupleResult.close();
                    return resultSize;
                }
                throw new InvalidQueryException(
                        Messages.getString("RDFQuery.selectSize.invalidQuery", queryPart.getQuery()));
            } catch (QueryEvaluationException ex) {
                throw new InvalidQueryException(
                        Messages.getString("RDFQuery.selectSize.notValid") + ex
                                .getMessage(),
                        ex);
            }

        } catch (MalformedQueryException ex) {
            throw new InvalidQueryException(
                    Messages.getString("RDFQuery.selectSize.malformedQuery")
                            + ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            LOG.error("Connection to RDF repository failed. {}",
                    ex.getMessage(), ex);
        } catch (DataUnitException ex) {
            LOG.error("DataUnit problem. {}",
                    ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Failed to close connection to RDF repository while querying. {}",
                            ex.getMessage(), ex);
                }
            }
        }

        return 0;
    }

    /**
     * For Browsing all data in graph return its size {count of rows}.
     */
    private long getResultSizeForDataCollection(RDFDataUnit rdfDataUnit) throws InvalidQueryException {
        final String selectQuery = "SELECT ?x ?y ?z WHERE {?x ?y ?z}";
        return getSizeForSelect(rdfDataUnit, new QueryPart(selectQuery));
    }

    /**
     * For given valid SELECT of CONSTRUCT query return its size {count of rows
     * returns for given query).
     *
     * @param query
     *            Valid SELECT/CONTRUCT query for asking.
     * @return size for given valid query as long.
     * @throws InvalidQueryException
     *             if query is not valid.
     */
    private long getResultSizeForQuery(RDFDataUnit rdfDataUnit, String query) throws InvalidQueryException {

        long size = 0;

        QueryPart queryPart = new QueryPart(query);
        SPARQLQueryType type = queryPart.getSPARQLQueryType();

        switch (type) {
            case SELECT:
                size = getSizeForSelect(rdfDataUnit, queryPart);
                break;
            case CONSTRUCT:
            case DESCRIBE:
                size = getSizeForConstruct(rdfDataUnit, query);
                break;
            case UNKNOWN:
                throw new InvalidQueryException(
                        Messages.getString("RDFQuery.resultSize.invalidQuery", query));
        }

        return size;

    }
}
