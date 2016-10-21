package eu.unifiedviews.dataunit.relational.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
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
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit.Entry;

/**
 * Load list of tables at once. This class does not need reliable repository, but it's not suitable
 * for larger number of tables.
 */
public class RelationalDataUnitIterationEager implements RelationalDataUnit.Iteration {

    private static final Logger LOG = LoggerFactory.getLogger(RelationalDataUnitIterationEager.class);

    private Iterator<RelationalDataUnit.Entry> iterator = null;

    protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    private static final String DB_TABLE_NAME_BINDING = "dbTableName";

    protected static final String SELECT = "SELECT ?" + SYMBOLIC_NAME_BINDING + " ?" + DB_TABLE_NAME_BINDING + " %s WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "<" + RelationalDataUnit.PREDICATE_DB_TABLE_NAME + "> ?" + DB_TABLE_NAME_BINDING + ". "
            + "}";

    public RelationalDataUnitIterationEager(MetadataDataUnit metadataDataUnit, ConnectionSource connectionSource, FaultTolerant faultTolerant) throws DataUnitException {
        final StringBuilder fromPart = new StringBuilder();
        for (URI graph : metadataDataUnit.getMetadataGraphnames()) {
            fromPart.append("FROM <");
            fromPart.append(graph.stringValue());
            fromPart.append("> ");
        }
        // Prepare query.
        final String selectQuery = String.format(SELECT, fromPart.toString());
        // Execute and gather data.
        final List<RelationalDataUnit.Entry> internalCollection = new LinkedList<>();
        try {
            faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    TupleQuery query;
                    try {
                        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery);
                    } catch (MalformedQueryException e) {
                        throw new DataUnitException("Problem with system query.", e);
                    }
                    // Clear set and load.
                    internalCollection.clear();
                    TupleQueryResult queryResult = null;
                    try {
                        queryResult = query.evaluate();
                        while (queryResult.hasNext()) {
                            BindingSet item = queryResult.next();
                            internalCollection.add(new RelationalDataUnitEntryImpl(
                                    item.getValue(SYMBOLIC_NAME_BINDING).stringValue(),
                                    item.getValue(DB_TABLE_NAME_BINDING).stringValue()));
                        }
                    } catch (QueryEvaluationException e) {
                        throw new DataUnitException("Could not select all files from repository", e);
                    } finally {
                        if (queryResult != null) {
                            try {
                                queryResult.close();
                            } catch (QueryEvaluationException e) {
                                LOG.warn("Error in close.", e);
                            }
                        }
                    }
                }
            });
        } catch (RepositoryException ex) {
            throw new DataUnitException("Problem with repository", ex);
        }
        this.iterator = internalCollection.iterator();
    }

    @Override
    public boolean hasNext() throws DataUnitException {
        return this.iterator.hasNext();
    }

    @Override
    public void close() throws DataUnitException {
        // no operation here
    }

    @Override
    public Entry next() throws DataUnitException {
        return this.iterator.next();
    }

}
