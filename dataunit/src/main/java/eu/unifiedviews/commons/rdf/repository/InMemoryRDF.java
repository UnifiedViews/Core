package eu.unifiedviews.commons.rdf.repository;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.dataunit.DataUnitException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

import java.io.File;

/**
 * 
 * @author Škoda Petr
 */
class InMemoryRDF implements ManagableRepository {

    public static final String GLOBAL_REPOSITORY_ID = "uv_internal_repository_memory";

    private final Repository repository;

    /**
     *
     * @param repositoryPath Path for manager, should be the same for different pipelines.
     * @throws DataUnitException
     */
    public InMemoryRDF(String repositoryPath) throws RDFException {
        final File managerDir = new File(repositoryPath);
        if (!managerDir.isDirectory() && !managerDir.mkdirs()) {
            throw new RDFException("Could not create repository manager directory.");
        }
        try {
            final LocalRepositoryManager localRepositoryManager = RepositoryProvider.getRepositoryManager(managerDir);
            final Repository newRepository = localRepositoryManager.getRepository(GLOBAL_REPOSITORY_ID);
            if (newRepository == null) {
                // Create and add to the manager.
                localRepositoryManager.addRepositoryConfig(new RepositoryConfig(GLOBAL_REPOSITORY_ID, new SailRepositoryConfig(new MemoryStoreConfig())));
                repository = localRepositoryManager.getRepository(GLOBAL_REPOSITORY_ID);
            } else {
                repository = newRepository;
            }
        } catch (RepositoryConfigException | RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
        if (repository == null) {
            throw new RDFException("Could not initialize repository");
        }
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return new ConnectionSourceImpl(repository, false, Type.INMEMORY_RDF);
    }

    @Override
    public void release() throws RDFException {
        // Do nothing here.
    }

    @Override
    public void delete() throws RDFException {
        // Do nothing here.
    }

    @Override
    public Type getRepositoryType() {
        return Type.INMEMORY_RDF;
    }

}
