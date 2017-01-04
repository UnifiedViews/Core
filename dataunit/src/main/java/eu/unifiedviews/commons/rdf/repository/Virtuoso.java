package eu.unifiedviews.commons.rdf.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.openrdf.IsolationLevel;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import info.aduna.iteration.Iteration;
import virtuoso.sesame2.driver.VirtuosoRepository;

/**
 *
 * @author Škoda Petr
 */
class Virtuoso implements ManagableRepository {

    private final Repository repository;

    /**
     * Wrap that ignores begin, commit functions as they are not supported by Virtuoso.
     */
    private class RepositoryConnectionWrap implements RepositoryConnection {

        private final RepositoryConnection connection;

        public RepositoryConnectionWrap(RepositoryConnection connection) {
            this.connection = connection;
        }
        
        @Override
        public Repository getRepository() {
            return connection.getRepository();
        }

        @Override
        public void setParserConfig(ParserConfig pc) {
            connection.setParserConfig(pc);
        }

        @Override
        public ParserConfig getParserConfig() {
            return connection.getParserConfig();
        }

        @Override
        public ValueFactory getValueFactory() {
            return connection.getValueFactory();
        }

        @Override
        public boolean isOpen() throws RepositoryException {
            return connection.isOpen();
        }

        @Override
        public void close() throws RepositoryException {
            connection.close();
        }

        @Override
        public Query prepareQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
            return connection.prepareBooleanQuery(ql, string);
        }

        @Override
        public Query prepareQuery(QueryLanguage ql, String string, String string1) throws RepositoryException, MalformedQueryException {
            return connection.prepareQuery(ql, string, string1);
        }

        @Override
        public TupleQuery prepareTupleQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
            return connection.prepareTupleQuery(ql, string);
        }

        @Override
        public TupleQuery prepareTupleQuery(QueryLanguage ql, String string, String string1) throws RepositoryException, MalformedQueryException {
            return connection.prepareTupleQuery(ql, string, string1);
        }

        @Override
        public GraphQuery prepareGraphQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
            return connection.prepareGraphQuery(ql, string);
        }

        @Override
        public GraphQuery prepareGraphQuery(QueryLanguage ql, String string, String string1) throws RepositoryException, MalformedQueryException {
            return connection.prepareGraphQuery(ql, string, string1);
        }

        @Override
        public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
            return connection.prepareBooleanQuery(ql, string);
        }

        @Override
        public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String string, String string1) throws RepositoryException, MalformedQueryException {
            return connection.prepareBooleanQuery(ql, string, string1);
        }

        @Override
        public Update prepareUpdate(QueryLanguage ql, String string) throws RepositoryException, MalformedQueryException {
            return connection.prepareUpdate(ql, string);
        }

        @Override
        public Update prepareUpdate(QueryLanguage ql, String string, String string1) throws RepositoryException, MalformedQueryException {
            return connection.prepareUpdate(ql, string, string1);
        }

        @Override
        public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
            return connection.getContextIDs();
        }

        @Override
        public RepositoryResult<Statement> getStatements(Resource rsrc, URI uri, Value value, boolean bln,
                Resource... rsrcs) throws RepositoryException {
            return connection.getStatements(rsrc, uri, value, bln, rsrcs);
        }

        @Override
        public boolean hasStatement(Resource rsrc, URI uri, Value value, boolean bln, Resource... rsrcs)
                throws RepositoryException {
            return connection.hasStatement(rsrc, uri, value, bln, rsrcs);
        }

        @Override
        public boolean hasStatement(Statement stmnt, boolean bln, Resource... rsrcs) throws RepositoryException {
            return connection.hasStatement(stmnt, bln, rsrcs);
        }

        @Override
        public void exportStatements(Resource rsrc, URI uri, Value value, boolean bln, RDFHandler rdfh,
                Resource... rsrcs) throws RepositoryException, RDFHandlerException {
            connection.exportStatements(rsrc, uri, value, bln, rdfh, rsrcs);
        }

        @Override
        public void export(RDFHandler rdfh, Resource... rsrcs) throws RepositoryException, RDFHandlerException {
            connection.export(rdfh, rsrcs);
        }

        @Override
        public long size(Resource... rsrcs) throws RepositoryException {
            return connection.size(rsrcs);
        }

        @Override
        public boolean isEmpty() throws RepositoryException {
            return connection.isEmpty();
        }

        @Override
        public void setAutoCommit(boolean bln) throws RepositoryException {
            connection.setAutoCommit(bln);
        }

        @Override
        public boolean isAutoCommit() throws RepositoryException {
            return connection.isAutoCommit();
        }

        @Override
        public boolean isActive() throws UnknownTransactionStateException, RepositoryException {
            return connection.isActive();
        }

        @Override
        public void begin() throws RepositoryException {
            // Ignore as it's not supported by Virutoso.
        }

        @Override
        public void begin(IsolationLevel level) throws RepositoryException {
            // Ignore as it's not supported by Virutoso.
        }

        @Override
        public void commit() throws RepositoryException {
            // Ignore as it's not suported by Virtuoso.
        }

        @Override
        public void rollback() throws RepositoryException {
            connection.rollback();
        }

        @Override
        public void add(InputStream in, String string, RDFFormat rdff, Resource... rsrcs) throws IOException, RDFParseException, RepositoryException {
            connection.add(in, string, rdff, rsrcs);
        }

        @Override
        public void add(Reader reader, String string, RDFFormat rdff, Resource... rsrcs) throws IOException, RDFParseException, RepositoryException {
            connection.add(reader, string, rdff, rsrcs);
        }

        @Override
        public void add(URL url, String string, RDFFormat rdff, Resource... rsrcs) throws IOException, RDFParseException, RepositoryException {
            connection.add(url, string, rdff, rsrcs);
        }

        @Override
        public void add(File file, String string, RDFFormat rdff, Resource... rsrcs) throws IOException, RDFParseException, RepositoryException {
            connection.add(file, string, rdff, rsrcs);
        }

        @Override
        public void add(Resource rsrc, URI uri, Value value, Resource... rsrcs) throws RepositoryException {
            connection.add(rsrc, uri, value, rsrcs);
        }

        @Override
        public void add(Statement stmnt, Resource... rsrcs) throws RepositoryException {
            connection.add(stmnt, rsrcs);
        }

        @Override
        public void add(
                Iterable<? extends Statement> itrbl, Resource... rsrcs) throws RepositoryException {
            connection.add(itrbl, rsrcs);
        }

        @Override
        public <E extends Exception> void add(
                Iteration<? extends Statement, E> itrtn, Resource... rsrcs) throws RepositoryException, E {
            connection.add(itrtn, rsrcs);
        }

        @Override
        public void remove(Resource rsrc, URI uri, Value value, Resource... rsrcs) throws RepositoryException {
            connection.remove(rsrc, uri, value, rsrcs);
        }

        @Override
        public void remove(Statement stmnt, Resource... rsrcs) throws RepositoryException {
            connection.remove(stmnt, rsrcs);
        }

        @Override
        public void remove(
                Iterable<? extends Statement> itrbl, Resource... rsrcs) throws RepositoryException {
            connection.remove(itrbl, rsrcs);
        }

        @Override
        public <E extends Exception> void remove(
                Iteration<? extends Statement, E> itrtn, Resource... rsrcs) throws RepositoryException, E {
            connection.remove(itrtn, rsrcs);
        }

        @Override
        public void clear(Resource... rsrcs) throws RepositoryException {
            connection.clear(rsrcs);
        }

        @Override
        public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
            return connection.getNamespaces();
        }

        @Override
        public String getNamespace(String string) throws RepositoryException {
            return connection.getNamespace(string);
        }

        @Override
        public void setNamespace(String string, String string1) throws RepositoryException {
            connection.setNamespace(string, string1);
        }

        @Override
        public void removeNamespace(String string) throws RepositoryException {
            connection.removeNamespace(string);
        }

        @Override
        public void clearNamespaces() throws RepositoryException {
            connection.clearNamespaces();
        }

        @Override
        public void setIsolationLevel(IsolationLevel il) throws IllegalStateException {
            connection.setIsolationLevel(il);
        }

        @Override
        public IsolationLevel getIsolationLevel() {
            return connection.getIsolationLevel();
        }
    
    }

    private class ConnectionSourceWrap extends ConnectionSourceImpl {

        public ConnectionSourceWrap(Repository repository) {
            super(repository, true);
        }

        @Override
        public RepositoryConnection getConnection() throws RepositoryException {
            return new RepositoryConnectionWrap(super.getConnection());
        }
    }

    public Virtuoso(String url, String user, String password) throws RDFException {
        repository = new VirtuosoRepository(url, user, password);
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw new RDFException("Could not initialize repository", ex);
        }
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return new ConnectionSourceWrap(repository);
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
