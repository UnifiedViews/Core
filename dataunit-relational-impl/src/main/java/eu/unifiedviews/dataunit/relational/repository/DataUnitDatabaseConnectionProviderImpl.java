package eu.unifiedviews.dataunit.relational.repository;

import java.sql.Connection;
import java.sql.SQLException;

import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.db.DatabaseWrapperIF;

/**
 * Implementation of {@link DataUnitDatabaseConnectionProvider) interface which provides the database
 * connections to the underlying relational database to data unit
 */
public class DataUnitDatabaseConnectionProviderImpl implements DataUnitDatabaseConnectionProvider {
    private DatabaseWrapperIF databaseWrapper;

    public DataUnitDatabaseConnectionProviderImpl(DatabaseWrapperIF databaseWrapper) {
        this.databaseWrapper = databaseWrapper;
    }

    @Override
    public Connection getDatabaseConnection() throws SQLException {
        return this.databaseWrapper.getConnection();
    }

    @Override
    public void release() throws Exception {
        this.databaseWrapper.shutdown();
    }

    @Override
    public boolean isActive() {
        return this.databaseWrapper.isActive();
    }

    @Override
    public Connection getDatabaseConnectionForUser(String userName, String password) throws SQLException {
        return this.databaseWrapper.getConnectionForUser(userName, password);
    }

}
