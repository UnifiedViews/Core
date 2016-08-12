package eu.unifiedviews.dataunit.relational.repository;

import java.sql.SQLException;

import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;

/**
 * Interface for relational database repository used by relational data units
 */
public interface ManagableRelationalRepository {

    public static final String BASE_DATABASE_NAME = "dataUnitDb";

    /**
     * Type of relational repository
     */
    public static enum Type {
        FILE, IN_MEMORY
    }

    /**
     * Get SQL connection provider to the underlying database
     * 
     * @return Implementation of {@link DataUnitDatabaseConnectionProvider} which provides connections to the underlying database
     * @throws SQLException
     */
    DataUnitDatabaseConnectionProvider getDatabaseConnectionProvider();

    /**
     * Terminate all connections to the database and shutdown the database
     * 
     * @throws Exception
     */
    void release() throws Exception;

}
