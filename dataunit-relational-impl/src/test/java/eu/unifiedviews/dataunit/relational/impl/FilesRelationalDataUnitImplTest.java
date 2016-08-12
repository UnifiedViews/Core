package eu.unifiedviews.dataunit.relational.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.commons.rdf.repository.ManagableRepository;
import eu.unifiedviews.commons.rdf.repository.RDFException;
import eu.unifiedviews.commons.rdf.repository.RepositoryFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.repository.ManagableRelationalRepository;
import eu.unifiedviews.dataunit.relational.repository.RelationalException;
import eu.unifiedviews.dataunit.relational.repository.RelationalRepositoryFactory;

public class FilesRelationalDataUnitImplTest {

    private Path rootDir;

    private Path rootDirFile;

    private ManagableRepository repository;

    private ManagableRelationalRepository dataUnitDatabase;

    private ManageableWritableRelationalDataUnit dataUnit;

    private static final String[] TABLE_COLUMNS_1 = new String[] { "name", "surname", "age" };

    private static final String[] TABLE_COLUMN_TYPES_1 = new String[] { "VARCHAR(255)", "VARCHAR(255)", "INTEGER" };

    private static final String[] TABLE_COLUMNS_2 = new String[] { "street", "city", "number" };

    private static final String[] TABLE_COLUMN_TYPES_2 = new String[] { "VARCHAR(255)", "VARCHAR(255)", "INTEGER" };

    @Before
    public void prepare() throws IOException, DataUnitException, RDFException, RelationalException {
        final RepositoryFactory factory = new RepositoryFactory();
        final RelationalRepositoryFactory relFactory = new RelationalRepositoryFactory();

        this.rootDir = Files.createTempDirectory(FileUtils.getTempDirectory().toPath(), "uv-relationalDataUnit-");
        this.rootDirFile = (new File(this.rootDir.toFile(), "storage")).toPath();
        final String directory = this.rootDir.toAbsolutePath().toString() + File.separator + "1";

        this.repository = factory.create(1l, ManagableRepository.Type.LOCAL_RDF, directory);
        this.dataUnitDatabase = relFactory.create(1L, this.rootDir.toFile(), ManagableRelationalRepository.Type.FILE);

        final RelationalDataUnitFactory dataUnitfactory = new RelationalDataUnitFactory();
        this.dataUnit = (ManageableWritableRelationalDataUnit) dataUnitfactory.create("test",
                "http://unifiedviews.eu/test/dataUnitDb",
                this.rootDirFile.toFile().toURI().toString(),
                createSimpleCoreServiceBusImplementation());
    }

    @After
    public void cleanUp() throws Exception {
        this.repository.delete();
        this.dataUnitDatabase.release();
        Files.deleteIfExists(this.rootDirFile);
        // There should be no data as we called clear.
        Assert.assertTrue("Failed to delete data directory", Files.deleteIfExists(this.rootDir));
    }

    @Test
    public void addAndIterateTest() throws DataUnitException {
        // Add initial tables.
        this.dataUnit.addNewDatabaseTable("table1");
        this.dataUnit.addNewDatabaseTable("table2");
        // Just check for size.
        int counter = 0;
        RelationalDataUnit.Iteration iter = this.dataUnit.getIteration();
        while (iter.hasNext()) {
            iter.next();
            counter++;
        }
        Assert.assertEquals(2, counter);

        this.dataUnit.clear();
        counter = 0;
        this.dataUnit.addNewDatabaseTable("table3");
        iter = this.dataUnit.getIteration();
        while (iter.hasNext()) {
            iter.next();
            counter++;
        }
        Assert.assertEquals(1, counter);
        this.dataUnit.clear();
        this.dataUnit.release();
    }

    @Test
    public void addCreateAndIterateTest() throws DataUnitException, SQLException {
        // Add initial tables.
        String tableName1 = this.dataUnit.addNewDatabaseTable("table1");
        String tableName2 = this.dataUnit.addNewDatabaseTable("table2");
        // Just check for size.
        try {
            int counter = 0;
            Connection connection = this.dataUnit.getDatabaseConnection();
            createTable(connection, tableName1, TABLE_COLUMNS_1, TABLE_COLUMN_TYPES_1);
            createTable(connection, tableName2, TABLE_COLUMNS_2, TABLE_COLUMN_TYPES_2);
            connection.close();

            connection = this.dataUnit.getDatabaseConnection();
            Assert.assertEquals(true, checkTableExists(connection, tableName1));
            Assert.assertEquals(true, checkTableExists(connection, tableName2));
            connection.close();

            RelationalDataUnit.Iteration iter = this.dataUnit.getIteration();
            while (iter.hasNext()) {
                iter.next();
                counter++;
            }
            Assert.assertEquals(2, counter);

            this.dataUnit.clear();
            counter = 0;
            tableName1 = this.dataUnit.addNewDatabaseTable("table1");
            connection = this.dataUnit.getDatabaseConnection();
            createTable(connection, tableName1, TABLE_COLUMNS_1, TABLE_COLUMN_TYPES_1);
            connection.close();

            connection = this.dataUnit.getDatabaseConnection();
            Assert.assertEquals(true, checkTableExists(connection, tableName1));

            iter = this.dataUnit.getIteration();
            while (iter.hasNext()) {
                iter.next();
                counter++;
            }
            Assert.assertEquals(1, counter);
            connection.close();
        } finally {
            this.dataUnit.clear();
            this.dataUnit.release();
        }
    }

    @Test
    public void addAndCheckTablesExist() throws DataUnitException, SQLException {
        // Add initial tables.
        String tableName1 = this.dataUnit.addNewDatabaseTable("table1");
        String tableName2 = this.dataUnit.addNewDatabaseTable("table2");

        try {
            Connection connection = this.dataUnit.getDatabaseConnection();
            createTable(connection, tableName1, TABLE_COLUMNS_1, TABLE_COLUMN_TYPES_1);
            createTable(connection, tableName2, TABLE_COLUMNS_2, TABLE_COLUMN_TYPES_2);
            connection.close();

            connection = this.dataUnit.getDatabaseConnection();
            Assert.assertEquals(true, checkTableExists(connection, tableName1));
            Assert.assertEquals(true, checkTableExists(connection, tableName2));

            connection.close();

        } finally {
            this.dataUnit.clear();
            this.dataUnit.release();
        }
    }

    @Test
    public void createTableCheckColumns() throws DataUnitException, SQLException {
        String tableName = this.dataUnit.addNewDatabaseTable("table1");

        try {
            Connection connection = this.dataUnit.getDatabaseConnection();
            createTable(connection, tableName, TABLE_COLUMNS_1, TABLE_COLUMN_TYPES_1);

            Assert.assertEquals(true, checkColumns(connection, tableName, TABLE_COLUMNS_1, TABLE_COLUMN_TYPES_1));

            connection.close();
        } finally {
            this.dataUnit.clear();
            this.dataUnit.release();
        }
    }

    private static final void createTable(Connection conn, String tableName, String[] columnNames, String[] columnTypes) throws SQLException {
        StringBuilder query = new StringBuilder("CREATE TABLE ");
        query.append(tableName);
        query.append(" (");
        for (int i = 0; i < columnNames.length; i++) {
            query.append(columnNames[i]);
            query.append(" ");
            query.append(columnTypes[i]);
            query.append(", ");
        }
        query.setLength(query.length() - 2);
        query.append(")");

        Statement stmnt = null;
        try {
            stmnt = conn.createStatement();
            stmnt.execute(query.toString());
            conn.commit();
        } finally {
            try {
                if (stmnt != null) {
                    stmnt.close();
                }
            } catch (SQLException ignore) {
            }
        }
    }

    private boolean checkColumns(Connection conn, String tableName, String[] columns, String[] columnTypes) throws SQLException {
        boolean bColumnsEqual = true;
        Set<String> columnSet = new HashSet<>();
        ResultSet rs = null;
        Statement statement = null;

        ResultSetMetaData meta = null;
        for (String column : columns) {
            columnSet.add(column.toUpperCase());
        }

        try {
            statement = conn.createStatement();
            rs = statement.executeQuery("SELECT * FROM " + tableName);
            meta = rs.getMetaData();

            if (columns.length != meta.getColumnCount()) {
                return false;
            }
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (!columnSet.contains(meta.getColumnName(i).toUpperCase())) {
                    bColumnsEqual = false;
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }

            } catch (Exception ignore) {
            }
        }

        return bColumnsEqual;
    }

    private boolean checkTableExists(Connection connection, String dbTableName) throws SQLException {
        boolean bTableExists = false;
        DatabaseMetaData dbm = null;
        ResultSet tables = null;
        try {
            dbm = connection.getMetaData();
            tables = dbm.getTables(null, null, dbTableName, null);
            if (tables.next()) {
                bTableExists = true;
            }
        } finally {
            try {
                if (tables != null) {
                    tables.close();
                }
            } catch (SQLException ignore) {
            }
        }
        return bTableExists;
    }

    private CoreServiceBus createSimpleCoreServiceBusImplementation() {
        return new CoreServiceBus() {

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getService(Class<T> serviceClass) throws IllegalArgumentException {
                // Simple test implementation of bus service
                if (serviceClass.isAssignableFrom(ConnectionSource.class)) {
                    return (T) FilesRelationalDataUnitImplTest.this.repository.getConnectionSource();
                } else if (serviceClass.isAssignableFrom(FaultTolerant.class)) {
                    return (T) new FaultTolerant() {

                        @Override
                        public void execute(FaultTolerant.Code codeToExecute)
                                throws RepositoryException, DataUnitException {
                            final RepositoryConnection conn =
                                    FilesRelationalDataUnitImplTest.this.repository.getConnectionSource().getConnection();
                            try {
                                codeToExecute.execute(conn);
                            } finally {
                                conn.close();
                            }
                        }
                    };
                } else if (serviceClass.isAssignableFrom(DataUnitDatabaseConnectionProvider.class)) {
                    return (T) FilesRelationalDataUnitImplTest.this.dataUnitDatabase.getDatabaseConnectionProvider();
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }

}
