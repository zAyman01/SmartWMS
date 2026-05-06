package com.warehousewms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class ConnectionPool {
    private static HikariDataSource sqlServerPool;
    private static HikariDataSource h2Pool;

    private static synchronized HikariDataSource initSqlServerPool() {
        if (sqlServerPool != null) {
            return sqlServerPool;
        }
        HikariConfig sqlConfig = new HikariConfig();
        sqlConfig.setJdbcUrl(DbConfig.getJdbcUrl(true));
        sqlConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        sqlConfig.setUsername(DbConfig.getSqlServerUser());
        sqlConfig.setPassword(DbConfig.getSqlServerPassword());
        sqlConfig.setMaximumPoolSize(10);
        sqlConfig.setInitializationFailTimeout(-1);
        sqlServerPool = new HikariDataSource(sqlConfig);
        return sqlServerPool;
    }

    private static synchronized HikariDataSource initH2Pool() {
        if (h2Pool != null) {
            return h2Pool;
        }
        HikariConfig h2Config = new HikariConfig();
        h2Config.setJdbcUrl(DbConfig.getJdbcUrl(false));
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setUsername(DbConfig.getH2User());
        h2Config.setPassword(DbConfig.getH2Password());
        h2Config.setMaximumPoolSize(5);
        h2Config.setInitializationFailTimeout(-1);
        h2Pool = new HikariDataSource(h2Config);
        return h2Pool;
    }

    public static DataSource getSqlServerDataSource() {
        return initSqlServerPool();
    }

    public static DataSource getH2DataSource() {
        return initH2Pool();
    }

    public static synchronized void shutdown() {
        if (sqlServerPool != null) {
            sqlServerPool.close();
            sqlServerPool = null;
        }
        if (h2Pool != null) {
            h2Pool.close();
            h2Pool = null;
        }
    }
}

