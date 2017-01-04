package eu.unifiedviews.dataunit.rdf.impl;

import java.util.NoSuchElementException;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * Must be used with reliable repository, can handle large data as loads only one entity at a time.
 *
 * @author Michal Klempa
 */
class RDFDataUnitIterationLazy implements RDFDataUnit.Iteration {

    private RepositoryConnection connection = null;

    private RepositoryConnection connection2 = null;

    private RepositoryResult<Statement> result = null;

    private MetadataDataUnit backingStore = null;

    public RDFDataUnitIterationLazy(MetadataDataUnit backingStore) {
        this.backingStore = backingStore;
    }

    @Override
    public RDFDataUnit.Entry next() throws DataUnitException {
        if (result == null) {
            init();
        }
        RepositoryResult<Statement> result2 = null;
        try {
            Statement statement = result.next();
            result2 = connection2.getStatements(statement.getSubject(), connection.getValueFactory().createURI(RDFDataUnit.PREDICATE_DATAGRAPH_URI), null, false, backingStore.getMetadataGraphnames().toArray(new URI[0]));
            Statement rdfDataGraphURIStatement = result2.next();
            return new RDFDataUnitEntryImpl(statement.getObject().stringValue(), new URIImpl(rdfDataGraphURIStatement.getObject().stringValue()));
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.iterating.error"), ex);
        } catch (NoSuchElementException ex) {
            this.close();
            throw ex;
        } finally {
            try {
                if (result2 != null) {
                    result2.close();
                }
            } catch (RepositoryException ex) {
                throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.closing.error"), ex);
            }
        }
    }

    @Override
    public boolean hasNext() throws DataUnitException {
        if (result == null) {
            init();
        }

        try {
            boolean hasNext = result.hasNext();
            if (!hasNext) {
                this.close();
            }
            return hasNext;
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.hasNext.error"), ex);
        }
    }

    @Override
    public void close() throws DataUnitException {
        try {
            result.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.closing.error"), ex);
        }
        try {
            connection.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.connection.closing.error"), ex);
        }
        try {
            connection2.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.connection.closing.error"), ex);
        }
    }

    private void init() throws DataUnitException {
        if (result == null) {
            if (connection == null) {
                connection = backingStore.getConnection();
            }
            if (connection2 == null) {
                connection2 = backingStore.getConnection();
            }
            try {
                result = connection.getStatements(null, connection.getValueFactory().createURI(MetadataDataUnit.PREDICATE_SYMBOLIC_NAME), null, false, backingStore.getMetadataGraphnames().toArray(new URI[0]));
            } catch (RepositoryException ex) {
                throw new DataUnitException(Messages.getString("RDFDataUnitIterationLazy.obtaining.entryList.error"), ex);
            }
        }
    }
}
