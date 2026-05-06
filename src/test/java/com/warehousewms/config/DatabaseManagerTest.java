package com.warehousewms.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {
    @AfterEach
    void tearDown() {
        ConnectionPool.shutdown();
        System.clearProperty("wms.useSqlServer");
    }

    @Test
    void h2SchemaIsInitialized() throws SQLException {
        System.setProperty("wms.useSqlServer", "false");
        DatabaseManager manager = new DatabaseManager();
        DataSource dataSource = manager.getConfiguredDataSource();

        assertTrue(manager.testConnection(dataSource));

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(usersTableExists(conn));
            assertTrue(adminUserExists(conn));
        }
    }

    @Test
    void fallsBackToH2WhenSqlServerUnavailable() throws SQLException {
        System.setProperty("wms.useSqlServer", "true");
        DatabaseManager manager = new DatabaseManager();
        DataSource dataSource = manager.getDataSourceWithFallback();

        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            assertTrue(url.startsWith("jdbc:h2:"));
        }
    }

    private static boolean usersTableExists(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "USERS", null)) {
            return rs.next();
        }
    }

    private static boolean adminUserExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

