package eu.unifiedviews.commons.rdf.repository;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasknap on 09/01/17.
 */
public class GraphDB implements ManagableRepository {

    private static final Logger log = LoggerFactory.getLogger(GraphDB.class);

    private Repository repository = null;


    public GraphDB(String url, String user, String password) throws RDFException {

        log.info("Graph db is being initialized, url: {}", url);

//        String port = url.substring(url.lastIndexOf(":")+1);
//        int portNumber = Integer.valueOf(port);
//        log.info("Port number: {}", portNumber);
//
//        String host = url.substring(0,url.lastIndexOf(":"));
//        log.info("Host: {}", host);

//        Repository repository = new StardogRepository(ConnectionConfiguration.to("uv").credentials(user, password));

          this.repository = new HTTPRepository(url);

        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
    }

    @Override
    public Type getRepositoryType() {
        return Type.GRAPHDB;
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return new ConnectionSourceImpl(repository, false, Type.GRAPHDB);
    }

    @Override
    public void release() throws RDFException {
        //do nothing as the repo is not being shutDown at the end of single pipeline exec
    }

    @Override
    public void delete() throws RDFException {
        //graphs for the execution already deleted in CleanUp, as part of context cleansing
    }
}
