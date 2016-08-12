package eu.unifiedviews.dataunit.relational.repository;

import java.io.File;
import java.sql.Connection;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Factory for creating relational repositories used by relational data units
 */
public class RelationalRepositoryFactory {

    private String baseDbUrl;

    private String userName;

    private String password;

    private String jdbcDriverName;

    /**
     * Set database parameters for this factory that will be used to create relational repository
     * 
     * @param userName
     *            User name in the database
     * @param password
     *            Password in the database
     * @param baseDbUrl
     *            First part of JDBC URL of the database - it is type specific (memory, file) and database engine specific
     * @param jdbcDriverName
     *            JDBC driver name to use to connect to the underlying database
     */
    public void setDatabaseParameters(String userName, String password, String baseDbUrl, String jdbcDriverName) {
        this.baseDbUrl = baseDbUrl;
        this.userName = userName;
        this.password = password;
        this.jdbcDriverName = jdbcDriverName;
    }

    /**
     * Create relational repository of given type and for given pipeline execution.
     * Before calling this, make sure {@link setDatabaseParameters()} method was called and parameters are set
     * 
     * @param executionId
     *            Id of executing pipeline
     * @param dataUnitDirectory
     *            Directory where data unit data (if any) should be placed
     * @param type
     *            Type of relational repository. Supported types are {@link ManagableRelationalRepository.Type.IN_MEMORY} and
     *            {@link ManagableRelationalRepository.Type.FILE}
     * @return Created relational repository
     * @throws RelationalException
     * @throws DataUnitException
     */
    public ManagableRelationalRepository create(long executionId, File dataUnitDirectory, ManagableRelationalRepository.Type type) throws RelationalException, DataUnitException {
        ManagableRelationalRepository repository = null;
        switch (type) {
            case IN_MEMORY:
                repository = new InMemoryRelationalDatabase(this.baseDbUrl, this.jdbcDriverName, executionId);
                break;
            case FILE:
                repository = new FilesRelationalDatabase(this.baseDbUrl, this.jdbcDriverName, executionId, dataUnitDirectory);
                break;
            default:
                throw new RelationalException("Unsupported dataunit relational database type: " + type.toString());
        }

        Connection connection = null;
        try {
            connection = repository.getDatabaseConnectionProvider().getDatabaseConnection();
        } catch (Exception e) {
            throw new RelationalException("Failed to connect to created repository", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ignore) {
            }
        }

        return repository;

    }

}
