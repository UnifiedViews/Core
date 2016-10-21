package eu.unifiedviews.commons.rdf.repository;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.semantics.sesame.MarkLogicRepository;
import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tomasknap on 18/10/16.
 */
public class MarkLogic implements ManagableRepository {

    private static final Logger log = LoggerFactory.getLogger(RepositoryFactory.class);

    private final Repository repository;


    public MarkLogic(String url, String user, String password) throws RDFException {

        String port = url.substring(url.lastIndexOf(":")+1);
        int portNumber = Integer.valueOf(port);
        log.info("Port number: {}", portNumber);

        String host = url.substring(0,url.lastIndexOf(":"));
        log.info("Host: {}", host);

        DatabaseClient adminClient =
                DatabaseClientFactory.newClient(host, portNumber,
                        user, password, DatabaseClientFactory.Authentication.DIGEST);

        // create repo and init
        repository = new MarkLogicRepository(adminClient);

        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
    }


        @Override
        public ConnectionSource getConnectionSource() {
            return new ConnectionSourceImpl(repository);
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
