package eu.unifiedviews.dataunit.rdf.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit.Entry;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * Load list of graphs at once. This class does not need reliable repository, but it's not suitable
 * for larger number of graphs.
 *
 * @author Škoda Petr
 */
class RDFDataUnitIterationEager implements RDFDataUnit.Iteration {

    private static final Logger LOG = LoggerFactory.getLogger(RDFDataUnitIterationEager.class);

    /**
     * Iterator for internal storage.
     */
    private Iterator<RDFDataUnit.Entry> iterator = null;

    protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    protected static final String GRAPH_URI_BINDING = "graphpUri";

    protected static final String SELECT = "SELECT ?" + SYMBOLIC_NAME_BINDING + " ?" + GRAPH_URI_BINDING + " %s WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "<" + RDFDataUnit.PREDICATE_DATAGRAPH_URI + "> ?" + GRAPH_URI_BINDING + ". "
            + "}";

    public RDFDataUnitIterationEager(MetadataDataUnit metadataDataUnit, ConnectionSource connectionSource, FaultTolerant faultTolerant) throws DataUnitException {
        // We can select from multiple graphs.
        final StringBuilder fromPart = new StringBuilder();
        for (URI graph : metadataDataUnit.getMetadataGraphnames()) {
            fromPart.append("FROM <");
            fromPart.append(graph.stringValue());
            fromPart.append("> ");
        }
        // Prepare query.
        final String selectQuery = String.format(SELECT, fromPart.toString());
        // Execute and gather data.
        final List<RDFDataUnit.Entry> internalCollection = new LinkedList<>();
        try {
            faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    TupleQuery query;
                    try {
                        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery);
                    } catch (MalformedQueryException ex) {
                        throw new DataUnitException(Messages.getString("RDFDataUnitIterationEager.system.query.problem"), ex);
                    }
                    // Clear set and load.
                    internalCollection.clear();
                    TupleQueryResult queryResult = null;
                    try {
                        queryResult = query.evaluate();
                        while (queryResult.hasNext()) {
                            BindingSet item = queryResult.next();
                            internalCollection.add(new RDFDataUnitEntryImpl(item.getValue(SYMBOLIC_NAME_BINDING).stringValue(), new URIImpl(item.getValue(GRAPH_URI_BINDING).stringValue())));
                        }
                    } catch (QueryEvaluationException ex) {
                        throw new DataUnitException(Messages.getString("RDFDataUnitIterationEager.could.not.select.all.files"), ex);
                    } finally {
                        if (queryResult != null) {
                            try {
                                queryResult.close();
                            } catch (QueryEvaluationException ex) {
                                LOG.warn("Error in close.", ex);
                            }
                        }
                    }
                }
            });
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationEager.repository.problem"), ex);
        }
        iterator = internalCollection.iterator();
    }

    @Override
    public boolean hasNext() throws DataUnitException {
        return iterator.hasNext();
    }

    @Override
    public void close() throws DataUnitException {
        // No operation here.
    }

    @Override
    public Entry next() throws DataUnitException {
        return iterator.next();
    }

}
