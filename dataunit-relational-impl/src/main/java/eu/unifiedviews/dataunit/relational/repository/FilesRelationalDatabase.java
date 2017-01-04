package eu.unifiedviews.dataunit.relational.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.db.DatabaseWrapperConfigIF;
import eu.unifiedviews.dataunit.relational.db.DatabaseWrapperIF;
import eu.unifiedviews.dataunit.relational.db.DefaultDatabaseConfig;
import eu.unifiedviews.dataunit.relational.db.RelationalDatabaseWrapper;

/**
 * File implementation of relational database repository used by data units.
 * This relational repository is shared by all DPUs and data units within one pipeline execution.
 * It provides the connections to the underlying database and also handles creating and releasing of the underlying relational database.
 * <p/>
 * File implementation by default uses H2 database engine. This can be configured in program properties file. For details, see
 * {@link cz.cuni.mff.xrg.odcs.commons.app.dataunit.relational.RelationalRepositoryManager}
 */
public class FilesRelationalDatabase implements ManagableRelationalRepository {

    private static final String DEFAULT_BASE_DB_URL = "jdbc:h2:file:";

    public static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";

    private static final String USER_NAME = "filesUser";

    private static final String PASSWORD = "dummyPsw";

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryRelationalDatabase.class);

    private String baseDatabaseURL;

    private String jdbcDriverName;

    private long executionId;

    private String databaseURL;

    private File dataunitDirectory;

    private DatabaseWrapperIF databaseWrapper;

    private String databaseFileName;

    /**
     * Creates new {@link FilesRelationalDatabase} repository. Creates a database wrapper to a relational database defined by connection parameters
     * 
     * @param baseDatabaseURL
     *            The first part of the JDBC URL after which the file location should follow; It is database engine specific, e.g. for H2 it's 'jdbc:h2:file:'
     * @param jdbcDriverName
     *            JDBC driver name to be used to connect to the underlying database
     * @param executionId
     *            Id of executing pipeline
     * @param dataUnitDirectory
     *            Data unit directory to place the database file
     * @throws DataUnitException
     */
    public FilesRelationalDatabase(String baseDatabaseURL, String jdbcDriverName, long executionId, File dataUnitDirectory) throws DataUnitException {
        // if not configured, use default values
        this.baseDatabaseURL = (baseDatabaseURL != null && baseDatabaseURL.length() > 1) ? baseDatabaseURL : DEFAULT_BASE_DB_URL;
        this.jdbcDriverName = (jdbcDriverName != null && jdbcDriverName.length() > 1) ? jdbcDriverName : DEFAULT_JDBC_DRIVER;
        this.executionId = executionId;
        this.dataunitDirectory = dataUnitDirectory;

        this.databaseFileName = createDatabaseFileName();
        this.databaseURL = createDatabaseURL();

        LOG.debug("Using {} driver to connect to database", this.jdbcDriverName);
        LOG.debug("Creating dataunit file database with URL: {} and dummy user name: {}", this.databaseURL, USER_NAME);
        this.databaseWrapper = createDatabaseWrapper();
    }

    private String createDatabaseFileName() {
        StringBuilder dbFileName = new StringBuilder();

        dbFileName.append(ManagableRelationalRepository.BASE_DATABASE_NAME);
        dbFileName.append("_");
        dbFileName.append(this.executionId);

        return dbFileName.toString();
    }

    private String createDatabaseURL() {
        StringBuilder dbURL = new StringBuilder(this.baseDatabaseURL);
        String dataUnitDirectoryPath = this.dataunitDirectory.getAbsolutePath();
        dbURL.append(dataUnitDirectoryPath);
        if (!dataUnitDirectoryPath.endsWith("\\") && !dataUnitDirectoryPath.endsWith("/")) {
            dbURL.append("/");
        }
        dbURL.append(this.databaseFileName);

        return dbURL.toString();
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
    public DataUnitDatabaseConnectionProvider getDatabaseConnectionProvider() {
        return new DataUnitDatabaseConnectionProviderImpl(this.databaseWrapper);
    }

    @Override
    public void release() throws Exception {
        LOG.debug("Releasing data unit relational repository");
        this.databaseWrapper.shutdown();
        LOG.debug("Deleting database file");
        deleteDatabaseFile();
        LOG.debug("Release successful");
    }

    private void deleteDatabaseFile() throws IOException {
        Pattern filesPattern = Pattern.compile(this.databaseFileName + ".*");
        if (this.dataunitDirectory == null || !this.dataunitDirectory.exists()) {
            return;
        }
        File[] filesInDataUnitDir = this.dataunitDirectory.listFiles();
        if (filesInDataUnitDir == null) {
            return;
        }
        for (File file : filesInDataUnitDir) {
            if (filesPattern.matcher(file.getName()).matches()) {
                Files.delete(file.toPath());
            }
        }
    }

}
