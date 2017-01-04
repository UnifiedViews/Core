package cz.cuni.mff.xrg.odcs.commons.app.rdf;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import eu.unifiedviews.commons.rdf.repository.ManagableRepository;
import eu.unifiedviews.commons.rdf.repository.RDFException;
import eu.unifiedviews.commons.rdf.repository.RepositoryFactory;

/**
 * Provide access to repositories.
 * 
 * @author Škoda Petr
 */
public class RepositoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryManager.class);

    /**
     * Locks used to synchronise access to {@link Repository}.
     */
    private final Map<Long, Object> locks = new HashMap<>();
    
    /**
     * Repositories.
     */
    private final Map<Long, ManagableRepository> repositories = Collections.synchronizedMap(new HashMap<Long, ManagableRepository>());

    /**
     * Repository factory.
     */
    private final RepositoryFactory factory = new RepositoryFactory();

    /**
     * Type of used repositories.
     */
    private ManagableRepository.Type repositoryType;

    @Value("${database.rdf.platform}")
    private String repositoryTypeString;

    /**
     * URL of remote repository.
     */
    @Value("${database.rdf.url:}")
    private String url;

    /**
     * User.
     */
    @Value("${database.rdf.user:}")
    private String user;

    /**
     * Password.
     */
    @Value("${database.rdf.password:}")
    private String password;

    @Autowired
    private ResourceManager resourceManager;

    @PostConstruct
    protected void init() {
        factory.setRemoteParameters(url, user, password);
        switch (repositoryTypeString) {
            case "inMemoryRDF":
                repositoryType = ManagableRepository.Type.INMEMORY_RDF;
                break;
            case "localRDF":
                repositoryType = ManagableRepository.Type.LOCAL_RDF;
                break;
            case "virtuoso":
                repositoryType = ManagableRepository.Type.VIRTUOSO;
                break;
            case "remoteRDF":
                repositoryType = ManagableRepository.Type.REMOTE_RDF;
                break;
            default:
                throw new RuntimeException("Unknown repository type.");
        }
    }

    /**
     * Return repository for given execution, if repository doesn't exists then it's created.
     *
     * @param executionId
     * @return
     * @throws eu.unifiedviews.commons.rdf.repository.RDFException
     */
    public ManagableRepository get(Long executionId) throws RDFException {
        synchronized (getLock(executionId)) {
            ManagableRepository repository = repositories.get(executionId);
            if (repository != null) {
                // Return existing repository.
                return repository;
            }
            // Prepare directory.
            final String directory;
            try {
                directory = resourceManager.getExecutionRepositoryDir(executionId).toString() + File.separator + "rdf";
            } catch (MissingResourceException ex) {
                throw new RDFException("Can't initialize repository.", ex);
            }
            // We need to create a new repository.
            repository = factory.create(executionId, repositoryType, directory);
            // Add to list and return it.
            repositories.put(executionId, repository);
            return repository;
        }
    }

    /**
     * Release repository for given pipeline, if that repository is loaded.
     *
     * @param executionId
     * @throws RDFException
     */
    public void release(Long executionId) throws RDFException {
        synchronized (getLock(executionId)) {
            final ManagableRepository repository = repositories.get(executionId);
            if (repository != null) {
                repository.release();
            }
            // Remove from list.
            repositories.remove(executionId);
        }
    }

    /**
     * Delete repository for given pipeline if the repository is already loaded.
     * 
     * @param executionId
     * @throws RDFException
     */
    public void delete(Long executionId) throws RDFException {
        synchronized (getLock(executionId)) {
            final ManagableRepository repository = repositories.get(executionId);
            if (repository != null) {
                repository.delete();
            }
            // Remove from list.
            repositories.remove(executionId);
        }
    }

    /**
     *
     * @param executionId
     * @return Lock object for given pipeline.
     */
    private synchronized Object getLock(Long executionId) {
        if (locks.containsKey(executionId)) {
            return locks.get(executionId);
        }
        final Object lock = new Object();
        locks.put(executionId, lock);
        return lock;
    }

}
