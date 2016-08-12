package eu.unifiedviews.commons.rdf.repository;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 *
 * @author Škoda Petr
 */
class RemoteRDF implements ManagableRepository{

    private final Repository repository;

    /**
     * URL of remote RDF storage.
     */
    private final String url;

    /**
     * User for RDF storage.
     */
    private final String user;

    /**
     * User's password.
     */
    private final String password;

    /**
     * Unique execution identification.
     */
    private final String executionIdStr;

    public RemoteRDF(String url, String user, String password, Long executionId) throws RDFException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.executionIdStr = executionId.toString();
        try {
            // Connect to remote repository.
            final RepositoryManager repositoryManager = getRepositoryManager();
            // Get repository if exists.
            final Repository newRepository = repositoryManager.getRepository(executionIdStr);
            if (newRepository == null) {
                // Create new repository.
                repositoryManager.addRepositoryConfig(new RepositoryConfig(executionIdStr, new SailRepositoryConfig(new NativeStoreConfig())));
                repository = repositoryManager.getRepository(executionIdStr);
            } else {
                // Use existing one.
                repository = newRepository;
            }
        } catch (RepositoryConfigException | RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
        if (repository == null) {
            throw new RDFException("Could not initialize repository");
        }
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return new ConnectionSourceImpl(repository, false);
    }

    @Override
    public void release() throws RDFException {
        try {
            getRepositoryManager().getRepository(executionIdStr).shutDown();
        } catch (RepositoryConfigException | RepositoryException ex) {
            throw new RDFException("Can't delete repository", ex);
        }
    }

    @Override
    public void delete() throws RDFException {
        try {
            getRepositoryManager().removeRepository(executionIdStr);
        } catch (RepositoryConfigException | RepositoryException ex) {
            throw new RDFException("Can't delete repository", ex);
        }
    }

    /**
     *
     * @return Repository provider.
     * @throws DataUnitException
     */
    private RepositoryManager getRepositoryManager() throws RDFException {
        try {
            final RepositoryManager repositoryManager = RepositoryProvider.getRepositoryManager(url);
            if (repositoryManager instanceof RemoteRepositoryManager) {
                if (user != null && !user.isEmpty()) {
                    ((RemoteRepositoryManager) repositoryManager).setUsernameAndPassword(user, password);
                }
            }
            return repositoryManager;
        } catch (RepositoryConfigException | RepositoryException ex) {
            throw new RDFException("Can't get repository provider.", ex);
        }
    }

}
