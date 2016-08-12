package eu.unifiedviews.dataunit.relational.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link DatabaseConnection} interface for providing connection to the underlying database
 */
public interface DatabaseConnection {

    /**
     * Return connection to the underlying database
     * Connection is returned opened and it is responsibility of the caller to properly close it
     * 
     * @return Connection to the database
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * Close all connections to the underlying database
     * 
     * @throws Exception
     */
    void close();
}
