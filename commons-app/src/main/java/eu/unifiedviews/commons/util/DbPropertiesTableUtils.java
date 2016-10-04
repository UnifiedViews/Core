package eu.unifiedviews.commons.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DbPropertiesTableUtils {

    private static final String SELECT_SQL = "SELECT * FROM properties";

    private static final String INSERT_SQL = "INSERT INTO properties VALUES (?,?)";

    private static final String DELETE_SQL = "DELETE FROM properties WHERE value = ?";

    private static final Logger LOG = LoggerFactory.getLogger(DbPropertiesTableUtils.class);

    @Autowired
    private DataSource dataSource;

    public boolean trySelectInsertDeleteInDb() {
        boolean isRunning = true;
        Connection conn = null;
        Statement stmnt = null;
        PreparedStatement insert = null;
        PreparedStatement delete = null;

        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(false);

            stmnt = conn.createStatement();
            stmnt.execute(SELECT_SQL);

            String key = generateStringGUIID();
            String value = generateStringGUIID();
            insert = conn.prepareStatement(INSERT_SQL);
            insert.setString(1, key);
            insert.setString(2, value);
            insert.execute();

            delete = conn.prepareStatement(DELETE_SQL);
            delete.setString(1, value);
            delete.execute();
        } catch (Exception e) {
            LOG.error("Connection to database could not be obtained", e);
            isRunning = false;
        } finally {
            tryRollbackConnection(conn);
            tryCloseDbResources(conn, stmnt, insert, delete);
        }

        return isRunning;
    }

    private static void tryRollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception e) {
                LOG.warn("Failed to rollback connection", e);
            }
        }
    }

    private static void tryCloseDbResources(Connection conn, Statement... statements) {
        for (Statement stmnt : statements)
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (Exception e) {
                    LOG.warn("Failed to close statement", e);
                }
            }

        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                LOG.warn("Failed to close connection", e);
            }
        }
    }

    private static String generateStringGUIID() {
        return UUID.randomUUID().toString();
    }

}
