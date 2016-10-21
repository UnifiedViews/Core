package eu.unifiedviews.dataunit.files.impl;

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
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Entry;

/**
 * Load list of files at once. This class does not need reliable repository, but it's not suitable
 * for larger number of files.
 *
 * @author Škoda Petr
 */
class WritableFileIterationEager implements FilesDataUnit.Iteration {

    private static final Logger LOG = LoggerFactory.getLogger(WritableFileIterationEager.class);

    /**
     * Iterator for internal storage.
     */
    private Iterator<FilesDataUnit.Entry> iterator = null;

    protected static final String SYMBOLIC_NAME_BINDING = "symbolicName";

    protected static final String FILE_URI_BINDING = "fileUri";

    protected static final String SELECT = "SELECT ?" + SYMBOLIC_NAME_BINDING + " ?" + FILE_URI_BINDING + " %s WHERE { "
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + ";"
            + "<" + FilesDataUnit.PREDICATE_FILE_URI + "> ?" + FILE_URI_BINDING + ". "
            + "}";

    public WritableFileIterationEager(MetadataDataUnit metadataDataUnit, ConnectionSource connectionSource,
            FaultTolerant faultTolerant) throws DataUnitException {
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
        final List<FilesDataUnit.Entry> internalCollection = new LinkedList<>();
        try {
            faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    TupleQuery query;
                    try {
                        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery);
                    } catch (MalformedQueryException ex) {
                        throw new DataUnitException("Problem with system query.", ex);
                    }
                    // Clear set and load.
                    internalCollection.clear();
                    TupleQueryResult queryResult = null;
                    try {
                        queryResult = query.evaluate();
                        while (queryResult.hasNext()) {
                            BindingSet item = queryResult.next();
                            internalCollection.add(new FilesDataUnitEntryImpl(
                                    item.getValue(SYMBOLIC_NAME_BINDING).stringValue(),
                                    item.getValue(FILE_URI_BINDING).stringValue()));
                        }
                    } catch (QueryEvaluationException ex) {
                        throw new DataUnitException("Could not select all files from repository", ex);
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
            throw new DataUnitException("Problem with repository", ex);
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
