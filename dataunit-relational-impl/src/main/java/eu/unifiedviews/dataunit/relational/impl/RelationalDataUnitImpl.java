package eu.unifiedviews.dataunit.relational.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.commons.dataunit.AbstractWritableMetadataDataUnit;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;

public class RelationalDataUnitImpl extends AbstractWritableMetadataDataUnit implements ManageableWritableRelationalDataUnit {

    private final static Logger LOG = LoggerFactory.getLogger(RelationalDataUnitImpl.class);

    private DataUnitDatabaseConnectionProvider dataUnitDatabase;

    private static final String DB_TABLE_NAME_BINDING = "dbTableName";

    private Set<String> databaseTables = new HashSet<>();

    private static final String UPDATE_EXISTING_TABLE = ""
            + "DELETE "
            + "{ "
            + "?s <" + RelationalDataUnit.PREDICATE_DB_TABLE_NAME + "> ?o "
            + "} "
            + "INSERT "
            + "{ "
            + "?s <" + RelationalDataUnit.PREDICATE_DB_TABLE_NAME + "> ?" + DB_TABLE_NAME_BINDING + " "
            + "} "
            + "WHERE "
            + "{"
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " . "
            + "?s <" + RelationalDataUnit.PREDICATE_DB_TABLE_NAME + "> ?o "
            + "}";

    private AtomicInteger tableIndex = new AtomicInteger(1);

    public RelationalDataUnitImpl(String dataUnitName, String databaseURL, String writeContextString, CoreServiceBus coreServices) {
        super(dataUnitName, writeContextString, coreServices);
        this.dataUnitDatabase = coreServices.getService(DataUnitDatabaseConnectionProvider.class);
    }

    @Override
    public void addExistingDatabaseTable(final String symbolicName, final String dbTableName) throws DataUnitException {
        checkForMultithreadAccess();
        if (!checkTableExists(dbTableName)) {
            throw new DataUnitException("Database table " + dbTableName + " does not exist!");
        }
        saveTableInRepository(symbolicName, dbTableName);
        this.databaseTables.add(dbTableName);
    }

    @Override
    public String addNewDatabaseTable(String symbolicName) throws DataUnitException {
        checkForMultithreadAccess();
        // to ensure that both table name and symbolic name are unique for the pipeline, we use the same 
        // string for table name and symbolic name which is checked against the database
        String tableName = generateTableName(symbolicName);
        String newSymbolicName = tableName;
        saveTableInRepository(newSymbolicName, tableName);
        this.databaseTables.add(tableName);

        return tableName;

    }

    private void saveTableInRepository(final String symbolicName, final String dbTableName) throws DataUnitException {
        final URI entrySubject = this.creatEntitySubject();
        try {
            this.faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    addEntry(entrySubject, symbolicName, connection);
                    final ValueFactory valueFactory = connection.getValueFactory();
                    connection.add(
                            entrySubject,
                            valueFactory.createURI(RelationalDataUnitImpl.PREDICATE_DB_TABLE_NAME),
                            valueFactory.createLiteral(dbTableName),
                            getMetadataWriteGraphname()
                            );
                }
            });
        } catch (RepositoryException e) {
            throw new DataUnitException("Problem with repositry.", e);
        }
    }

    private boolean checkTableExists(String dbTableName) throws DataUnitException {
        boolean bTableExists = false;
        DatabaseMetaData dbm = null;
        ResultSet tables = null;
        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            dbm = connection.getMetaData();
            tables = dbm.getTables(null, null, dbTableName, null);
            if (tables.next()) {
                bTableExists = true;
            }
        } catch (Exception e) {
            LOG.error("Failed to check if table exists");
            throw new DataUnitException("Failed to check if table exists");
        } finally {
            try {
                if (tables != null) {
                    tables.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error("Failed to clear database resources");
            }
        }
        return bTableExists;
    }

    @Override
    public void updateExistingTableName(String symbolicName, String newDbTableName) throws DataUnitException {
        checkForMultithreadAccess();

        RepositoryConnection connection = null;

        try {
            connection = this.connectionSource.getConnection();
            connection.begin();
            ValueFactory valueFactory = connection.getValueFactory();
            Literal symbolicNameLiteral = valueFactory.createLiteral(symbolicName);
            try {
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL, UPDATE_EXISTING_TABLE);
                update.setBinding(SYMBOLIC_NAME_BINDING, symbolicNameLiteral);
                update.setBinding(DB_TABLE_NAME_BINDING, valueFactory.createLiteral(newDbTableName));

                DatasetImpl dataset = new DatasetImpl();
                dataset.addDefaultGraph(getMetadataWriteGraphname());
                dataset.setDefaultInsertGraph(getMetadataWriteGraphname());
                dataset.addDefaultRemoveGraph(getMetadataWriteGraphname());

                update.setDataset(dataset);
                update.execute();
            } catch (MalformedQueryException | UpdateExecutionException e) {
                throw new DataUnitException(e);
            }
            connection.commit();
        } catch (RepositoryException e) {
            throw new DataUnitException("Error when updating database table.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    LOG.warn("Error when closing connection", e);
                }
            }
        }
    }

    @Override
    public RelationalDataUnit.Iteration getIteration() throws DataUnitException {
        checkForMultithreadAccess();
        if (this.connectionSource.isRetryOnFailure()) {
            return new RelationalDataUnitIterationEager(this, this.connectionSource, this.faultTolerant);
        } else {
            return new RelationalDataUnitIterationLazy(this);
        }
    }

    @Override
    public Connection getDatabaseConnection() throws DataUnitException {
        try {
            return this.dataUnitDatabase.getDatabaseConnection();
        } catch (SQLException e) {
            throw new DataUnitException("Failed to obtain database connection to dataunit database");
        }
    }

    @Override
    public Type getType() {
        return ManagableDataUnit.Type.RELATIONAL;
    }

    @Override
    public boolean isType(Type dataUnitType) {
        return getType().equals(dataUnitType);
    }

    private String generateTableName(String symbolicName) throws DataUnitException {
        StringBuilder tableName = new StringBuilder();
        tableName.append(symbolicName);
        tableName.append("_");
        tableName.append(this.tableIndex.getAndIncrement());
        int iteration = 1;
        while (checkTableExists(tableName.toString().toUpperCase())) {
            LOG.debug("Table name {} already exists", tableName.toString());
            if (iteration == 1) {
                tableName.append("_");
            }
            tableName.append("X");
            iteration++;
        }

        LOG.debug("Using table name {}", tableName.toString());
        String tableNameAsString = tableName.toString();
        return tableNameAsString.toUpperCase();
    }

    @Override
    public void clear() throws DataUnitException {
        if (this.dataUnitDatabase.isActive()) {
            dropAllDatabaseTables();
        }
        this.databaseTables.clear();
        super.clear();
    }

    private void dropAllDatabaseTables() throws DataUnitException {
        Connection conn = null;
        Statement stmnt = null;
        try {
            conn = getDatabaseConnection();
            stmnt = conn.createStatement();
            for (String table : this.databaseTables) {
                stmnt.execute("DROP TABLE " + table + " IF EXISTS");
            }
            conn.commit();
        } catch (Exception e) {
            LOG.error("Failed to drop existing database tables", e);
            throw new DataUnitException("Failed to clear existing tables", e);
        } finally {
            try {
                if (stmnt != null) {
                    stmnt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.error("Failed to clear database resources", e);
            }
        }
    }

    /**
     * Release all database tables and connections. After this call, data unit is not usable anymore.
     * However, the underlying database connection is not shut down as this is shared by all data units in one pipeline
     * Therefore, the release only deletes all database tables that the data unit contains and invalidates database connection
     * so it cannot be used from this data unit.
     */
    @Override
    public void release() {
        try {
            if (this.dataUnitDatabase.isActive()) {
                dropAllDatabaseTables();
            }
            this.dataUnitDatabase = null;
        } catch (DataUnitException e) {
            LOG.error("Failure during release of relational data unit", e);
        }

        super.release();
    }

    @Override
    public Connection getDatabaseConnectionForUser(String userName, String password) throws DataUnitException {
        try {
            return this.dataUnitDatabase.getDatabaseConnectionForUser(userName, password);
        } catch (SQLException e) {
            throw new DataUnitException("Failed to obtain SQL connection for user " + userName, e);
        }
    }

}
