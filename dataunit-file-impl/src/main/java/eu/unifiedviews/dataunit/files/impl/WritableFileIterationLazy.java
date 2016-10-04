package eu.unifiedviews.dataunit.files.impl;

import java.util.NoSuchElementException;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 * Must be used with reliable repository, can handle large data as loads only one entity at a time.
 *
 * @author Michal Klempa
 */
class WritableFileIterationLazy implements FilesDataUnit.Iteration {

    private RepositoryConnection connection = null;

    private RepositoryConnection connection2 = null;

    private RepositoryResult<Statement> result = null;

    private MetadataDataUnit backingStore = null;

    public WritableFileIterationLazy(MetadataDataUnit backingStore) {
        this.backingStore = backingStore;
    }

    @Override
    public FilesDataUnit.Entry next() throws DataUnitException {
        if (result == null) {
            init();
        }
        RepositoryResult<Statement> result2 = null;
        try {
            Statement statement = result.next();
            result2 = connection2.getStatements(statement.getSubject(),
                    connection.getValueFactory().createURI(FilesDataUnit.PREDICATE_FILE_URI),
                    null, false, backingStore.getMetadataGraphnames().toArray(new URI[0]));
            Statement filesytemURIStatement = result2.next();
            return new FilesDataUnitEntryImpl(statement.getObject().stringValue(), filesytemURIStatement.getObject().stringValue());
        } catch (RepositoryException ex) {
            throw new DataUnitException("Error iterating underlying repository", ex);
        } catch (NoSuchElementException ex) {
            this.close();
            throw ex;
        } finally {
            try {
                if (result2 != null) {
                    result2.close();
                }
            } catch (RepositoryException ex) {
                throw new DataUnitException("Error closing result", ex);
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
            throw new DataUnitException("Error in hasNext", ex);
        }
    }

    @Override
    public void close() throws DataUnitException {
        try {
            result.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException("Error closing result", ex);
        }
        try {
            connection.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException("Error closing connection", ex);
        }
        try {
            connection2.close();
        } catch (RepositoryException ex) {
            throw new DataUnitException("Error closing connection", ex);
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
                result = connection.getStatements(null, connection.getValueFactory().createURI(FilesDataUnit.PREDICATE_SYMBOLIC_NAME), null, false, backingStore.getMetadataGraphnames().toArray(new URI[0]));
            } catch (RepositoryException ex) {
                throw new DataUnitException("Error obtaining file list.", ex);
            }
        }
    }

}
