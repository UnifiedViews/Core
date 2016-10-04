package eu.unifiedviews.dataunit.relational.impl;

import java.util.NoSuchElementException;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit.Entry;

/**
 * Must be used with reliable repository, can handle large data as loads only one entity (database table) at a time.
 */
public class RelationalDataUnitIterationLazy implements RelationalDataUnit.Iteration {

    private RepositoryConnection connection = null;

    private RepositoryConnection connection2 = null;

    private RepositoryResult<Statement> result = null;

    private MetadataDataUnit backingStore = null;

    public RelationalDataUnitIterationLazy(MetadataDataUnit backingStore) {
        this.backingStore = backingStore;
    }

    private void init() throws DataUnitException {
        if (this.result == null) {
            if (this.connection == null) {
                this.connection = this.backingStore.getConnection();
            }
            if (this.connection2 == null) {
                this.connection2 = this.backingStore.getConnection();
            }
            try {
                this.result = this.connection.getStatements(null, this.connection.getValueFactory().createURI(RelationalDataUnit.PREDICATE_SYMBOLIC_NAME), null, false, this.backingStore.getMetadataGraphnames().toArray(new URI[0]));
            } catch (RepositoryException e) {
                throw new DataUnitException("Error obtaining file list.", e);
            }
        }
    }

    @Override
    public boolean hasNext() throws DataUnitException {
        if (this.result == null) {
            init();
        }

        try {
            boolean hasNext = this.result.hasNext();
            if (!hasNext) {
                this.close();
            }
            return hasNext;
        } catch (RepositoryException e) {
            throw new DataUnitException("Error in hasNext", e);
        }
    }

    @Override
    public void close() throws DataUnitException {
        try {
            this.result.close();
        } catch (RepositoryException e) {
            throw new DataUnitException("Error closing result", e);
        }
        try {
            this.connection.close();
        } catch (RepositoryException e) {
            throw new DataUnitException("Error closing connection", e);
        }
        try {
            this.connection2.close();
        } catch (RepositoryException e) {
            throw new DataUnitException("Error closing connection", e);
        }
    }

    @Override
    public Entry next() throws DataUnitException {
        if (this.result == null) {
            init();
        }
        RepositoryResult<Statement> result2 = null;
        try {
            Statement statement = this.result.next();
            result2 = this.connection2.getStatements(statement.getSubject(),
                    this.connection.getValueFactory().createURI(RelationalDataUnit.PREDICATE_DB_TABLE_NAME),
                    null, false, this.backingStore.getMetadataGraphnames().toArray(new URI[0]));
            Statement tableNameStatement = result2.next();
            return new RelationalDataUnitEntryImpl(statement.getObject().stringValue(), tableNameStatement.getObject().stringValue());
        } catch (RepositoryException e) {
            throw new DataUnitException("Error iterating underlying repository", e);
        } catch (NoSuchElementException e) {
            this.close();
            throw e;
        } finally {
            try {
                if (result2 != null) {
                    result2.close();
                }
            } catch (RepositoryException e) {
                throw new DataUnitException("Error closing result", e);
            }
        }
    }

}
