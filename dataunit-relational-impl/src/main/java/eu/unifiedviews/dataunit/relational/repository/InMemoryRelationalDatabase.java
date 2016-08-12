package eu.unifiedviews.dataunit.relational.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.db.DatabaseWrapperConfigIF;
import eu.unifiedviews.dataunit.relational.db.DatabaseWrapperIF;
import eu.unifiedviews.dataunit.relational.db.DefaultDatabaseConfig;
import eu.unifiedviews.dataunit.relational.db.RelationalDatabaseWrapper;

/**
 * In memory implementation of relational database repository used by data units.
 * This relational repository is shared by all DPUs and data units within one pipeline run
 * It provides the connections to the underlying database and also handles creating and releasing of the underlying relational database
 * <p/>
 * In memory implementation by default uses H2 database engine. This can be configured in program properties file. For details, see
 * {@link cz.cuni.mff.xrg.odcs.commons.app.dataunit.relational.RelationalRepositoryManager}
 */
public class InMemoryRelationalDatabase implements ManagableRelationalRepository {

    public static final String DEFAULT_BASE_DB_URL = "jdbc:h2:mem:";

    public static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";

    private static final String USER_NAME = "inMemoryUser";

    private static final String PASSWORD = "dummyPsw";

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryRelationalDatabase.class);

    private String baseDatabaseURL;

    private String jdbcDriverName;

    private long executionId;

    private String databaseURL;

    private DatabaseWrapperIF databaseWrapper;

    /**
     * Creates new {@link FilesRelationalDatabase} repository. Creates a database wrapper to a relational database defined by connection parameters
     * 
     * @param baseDatabaseURL
     *            The first part of the JDBC URL after which the database name should follow; It is database engine specific, e.g. for H2 it's 'jdbc:h2:mem:'
     * @param jdbcDriverName
     *            JDBC driver name to be used to connect to the underlying database
     * @param executionId
     *            Id of executing pipeline
     * @throws DataUnitException
     */
    public InMemoryRelationalDatabase(String baseDatabaseURL, String jdbcDriverName, long executionId) throws DataUnitException {
        // if not configured, use default values
        this.baseDatabaseURL = (baseDatabaseURL != null && baseDatabaseURL.length() > 1) ? baseDatabaseURL : DEFAULT_BASE_DB_URL;
        this.jdbcDriverName = (jdbcDriverName != null && jdbcDriverName.length() > 1) ? jdbcDriverName : DEFAULT_JDBC_DRIVER;
        this.executionId = executionId;
        this.databaseURL = this.baseDatabaseURL + ManagableRelationalRepository.BASE_DATABASE_NAME
                + "_" + String.valueOf(this.executionId);

        LOG.debug("Using {} driver to connect to database", this.jdbcDriverName);
        LOG.debug("Creating dataunit in memory database with URL: {} and dummy user name: {}", this.databaseURL, USER_NAME);
        this.databaseWrapper = createDatabaseWrapper();
    }

    private DatabaseWrapperIF createDatabaseWrapper() throws DataUnitException {
        DatabaseWrapperConfigIF config = new DefaultDatabaseConfig(this.databaseURL, USER_NAME, PASSWORD, this.jdbcDriverName);
        DatabaseWrapperIF wrapper = null;
        try {
            wrapper = new RelationalDatabaseWrapper(config);
        } catch (Exception e) {
            throw new DataUnitException("Failed to create database wrapper for internal relational database", e);
        }

        return wrapper;
    }

    @Override
    public void release() throws Exception {
        LOG.debug("Releasing data unit relational repository");
        this.databaseWrapper.shutdown();
    }

    @Override
    public DataUnitDatabaseConnectionProvider getDatabaseConnectionProvider() {
        return new DataUnitDatabaseConnectionProviderImpl(this.databaseWrapper);
    }

}
