package com.warehousewms.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final H2SchemaInitializer h2Initializer = new H2SchemaInitializer();

    public DataSource getConfiguredDataSource() {
        boolean useSqlServer = DbConfig.isSqlServerEnabled();
        if (useSqlServer) {
            return ConnectionPool.getSqlServerDataSource();
        }
        DataSource h2 = ConnectionPool.getH2DataSource();
        initH2Schema(h2);
        return h2;
    }

    public DataSource getDataSourceWithFallback() {
        if (DbConfig.isSqlServerEnabled()) {
            try {
                DataSource sqlServer = ConnectionPool.getSqlServerDataSource();
                if (testConnection(sqlServer)) {
                    return sqlServer;
                }
            } catch (SQLException ignored) {
                // Fallback to H2 when SQL Server is unreachable.
            }
        }
        DataSource h2 = ConnectionPool.getH2DataSource();
        initH2Schema(h2);
        return h2;
    }

    public boolean testConnection(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private void initH2Schema(DataSource h2DataSource) {
        try {
            h2Initializer.ensureSchema(h2DataSource);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 schema", e);
        }
    }
}

