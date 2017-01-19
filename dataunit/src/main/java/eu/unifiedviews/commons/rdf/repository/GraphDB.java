package eu.unifiedviews.commons.rdf.repository;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by tomasknap on 09/01/17.
 */
public class GraphDB implements ManagableRepository {

    private static final Logger log = LoggerFactory.getLogger(GraphDB.class);

    private Repository repository = null;


    public GraphDB(String url, String user, String password) throws RDFException {

//        String port = url.substring(url.lastIndexOf(":")+1);
//        int portNumber = Integer.valueOf(port);
//        log.info("Port number: {}", portNumber);
//
//        String host = url.substring(0,url.lastIndexOf(":"));
//        log.info("Host: {}", host);

//        Repository repository = new StardogRepository(ConnectionConfiguration.to("uv").credentials(user, password));

        // Instantiate a local repository manager and initialize it
        RepositoryManager repositoryManager = new RemoteRepositoryManager(url);
        try {
            repositoryManager.initialize();

            Collection<Repository> allRepositories = repositoryManager.getAllRepositories();

            this.repository = repositoryManager.getRepository("uv");

//            Repository repository = new HTTPRepository(url, "uv");

        } catch (RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(),e);
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
            repository.shutDown();
    } catch (RepositoryException ex) {
         throw new RDFException("Can't shutDown repository.", ex);
    }

    }

    @Override
    public void delete() throws RDFException {
        // Do nothing here.
    }
}
