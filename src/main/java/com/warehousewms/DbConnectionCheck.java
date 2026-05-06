package com.warehousewms;

import com.warehousewms.config.ConnectionPool;
import com.warehousewms.config.DatabaseManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DbConnectionCheck {
    public static void main(String[] args) throws Exception {
        DatabaseManager manager = new DatabaseManager();
        DataSource dataSource = manager.getDataSourceWithFallback();
        try {
            boolean ok = manager.testConnection(dataSource);
            System.out.println("Database connection OK: " + ok);
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("Database URL: " + conn.getMetaData().getURL());
                System.out.println("Users table exists: " + usersTableExists(conn));
                System.out.println("Admin user present: " + adminUserExists(conn));
            }
        } finally {
            ConnectionPool.shutdown();
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

