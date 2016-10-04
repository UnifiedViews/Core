package eu.unifiedviews.dataunit.relational.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for database connection provider to the underlying relational database for relational data unit
 * It is used to provide database connection provider service to relational database data unit via {@link eu.unifiedviews.commons.dataunit.core.CoreServiceBus) 
 * @author Tomas
 *
 */
public interface DataUnitDatabaseConnectionProvider {

    /**
     * Get SQL connection to the underlying data unit database
     * 
     * @return Connection SQL connection to the database
     * @throws SQLException
     */
    Connection getDatabaseConnection() throws SQLException;

    /**
     * Get SQL connection to the underlying data unit database for specific user
     * 
     * @return SQL connection to the database
     * @throws SQLException
     */
    Connection getDatabaseConnectionForUser(String userName, String password) throws SQLException;

    /**
     * Shuts down the database connection provider
     * 
     * @throws Exception
     */
    void release() throws Exception;

    /**
     * Check if the database connection provider is still active
     * 
     * @return true if active, false if inactive
     */
    boolean isActive();

}
