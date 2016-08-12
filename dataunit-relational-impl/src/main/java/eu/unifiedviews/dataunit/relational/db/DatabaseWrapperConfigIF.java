package eu.unifiedviews.dataunit.relational.db;

/**
 * Database wrapper configuration interface
 * Provides all configuration parameters needed to create a database
 * and provide database connections
 * 
 * @author Tomas
 */
public interface DatabaseWrapperConfigIF {

    /**
     * Get database name used to create the internal database
     * 
     * @return database name
     */
    String getDatabaseURL();

    /**
     * User name to connect to the database
     * 
     * @return user name
     */
    String getUserName();

    /**
     * User password for the database
     * 
     * @return password
     */
    String getPassword();

    /**
     * Get max. pooled connections to the database
     * 
     * @return Limit of pooled database connections
     */
    int getMaxPooledConnections();

    /**
     * Return name of JDBC driver which can be used to connect to the underlying relational database
     * 
     * @return Name of JDBC driver used to connect to database
     */
    String getJDBCDriverName();

}
