package cz.cuni.mff.xrg.odcs.commons.app.dataunit.relational;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.repository.ManagableRelationalRepository;
import eu.unifiedviews.dataunit.relational.repository.RelationalException;
import eu.unifiedviews.dataunit.relational.repository.RelationalRepositoryFactory;

/**
 * Relational repository manager holds all active relational repositories for running pipelines.
 * In current implementation, each pipeline has one underlying relational database, that is shared by all DPUs within pipeline
 * Relational repository manager is also responsible for configuration of the underlying relational database.
 * <p/>
 * Currently supported implementations of underlying database are: <br/>
 * {@link ManagableRelationalRepository.Type.FILE} - <b>default</b> <br/>
 * {@link ManagableRelationalRepository.Type.IN_MEMORY}
 * 
 * @author Tomas
 */
public class RelationalRepositoryManager {

    /**
     * Locks used to synchronize access to {@link Repository}.
     */
    private final Map<Long, Object> locks = new HashMap<>();

    /**
     * Created active repositories for pipelines
     */
    private final Map<Long, ManagableRelationalRepository> repositories = Collections.synchronizedMap(new HashMap<Long, ManagableRelationalRepository>());

    private final RelationalRepositoryFactory factory = new RelationalRepositoryFactory();

    private static final Logger LOG = LoggerFactory.getLogger(RelationalRepositoryManager.class);

    private ManagableRelationalRepository.Type repositoryType;

    /**
     * Type of used underlying database. Currently supported 2 modes: <b>file</b>(default) and <b>inMemory</b>
     */
    @Value("${database.dataunit.sql.type:}")
    private String repositoryTypeString;

    /**
     * URL of remote repository.
     */
    @Value("${database.dataunit.sql.baseurl:}")
    private String baseUrl;

    /**
     * User.
     */
    @Value("${database.dataunit.sql.user:}")
    private String user;

    /**
     * Password.
     */
    @Value("${database.dataunit.sql.password:}")
    private String password;

    /**
     * JDBC driver name class name to use to connect to the underlying database
     */
    @Value("${database.dataunit.sql.driver:}")
    private String jdbcDriverName;

    /**
     * Initialize relational data unit factory
     */
    @PostConstruct
    public void init() {
        this.factory.setDatabaseParameters(this.user, this.password, this.baseUrl, this.jdbcDriverName);
        switch (this.repositoryTypeString) {
            case "inMemory":
                this.repositoryType = ManagableRelationalRepository.Type.IN_MEMORY;
                break;
            case "file":
                this.repositoryType = ManagableRelationalRepository.Type.FILE;
                break;
            default:
                LOG.info("Unknown repository type, using default file mode");
                this.repositoryType = ManagableRelationalRepository.Type.FILE;
        }
    }

    /**
     * Get internal data unit relational database repository for given pipeline.
     * If the relational repository already exists for the given pipeline, it is returned.
     * Otherwise, new relational repository is created based on configuration and returned
     * 
     * @param executionId
     *            Id of executed pipeline
     * @return ManagableRelationalRepository relational repository for given pipeline
     * @throws RelationalException
     * @throws DataUnitException
     */
    public ManagableRelationalRepository getRepository(long executionId, File dataUnitDirectory) throws RelationalException, DataUnitException {
        synchronized (getLock(executionId)) {
            ManagableRelationalRepository repository = this.repositories.get(executionId);
            if (repository != null) {
                return repository;
            }
            // TODO: if other than in memory  / file database, here the new database should be created
            repository = this.factory.create(executionId, dataUnitDirectory, this.repositoryType);
            this.repositories.put(executionId, repository);

            return repository;
        }
    }

    /**
     * Release repository for given pipeline, if that repository is loaded.
     *
     * @param executionId
     *            Id of executed pipeline
     * @throws Exception
     */
    public void release(Long executionId) throws Exception {
        synchronized (getLock(executionId)) {
            final ManagableRelationalRepository repository = this.repositories.get(executionId);
            if (repository != null) {
                repository.release();
            }
            // Remove from list.
            this.repositories.remove(executionId);
        }
    }

    /**
     * @param executionId
     *            Id of executed pipeline
     * @return Lock object for given pipeline.
     */
    private synchronized Object getLock(Long executionId) {
        if (this.locks.containsKey(executionId)) {
            return this.locks.get(executionId);
        }
        final Object lock = new Object();
        this.locks.put(executionId, lock);
        return lock;
    }
}
