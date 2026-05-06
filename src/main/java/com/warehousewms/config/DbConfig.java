package com.warehousewms.config;

import java.util.Properties;
import java.io.InputStream;

public class DbConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = DbConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("Missing db.properties on classpath");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    private static final String HOST = props.getProperty("db.sqlserver.host");
    private static final String PORT = props.getProperty("db.sqlserver.port");
    private static final String DATABASE = props.getProperty("db.sqlserver.database");
    private static final String USER = props.getProperty("db.sqlserver.user");
    private static final String PASSWORD = props.getProperty("db.sqlserver.password");
    private static final String H2_URL = props.getProperty("db.h2.url");
    private static final String H2_USER = props.getProperty("db.h2.user", "sa");
    private static final String H2_PASSWORD = props.getProperty("db.h2.password", "");

    public static boolean isSqlServerEnabled() {
        return Boolean.parseBoolean(System.getProperty("wms.useSqlServer", "false"));
    }

    public static String getSqlServerUser() {
        return USER;
    }

    public static String getSqlServerPassword() {
        return PASSWORD;
    }

    public static String getH2User() {
        return H2_USER;
    }

    public static String getH2Password() {
        return H2_PASSWORD;
    }

    public static String getJdbcUrl(boolean useSqlServer) {
        if (useSqlServer) {
            return "jdbc:sqlserver://" + HOST + ":" + PORT + ";databaseName=" + DATABASE
                    + ";encrypt=false;trustServerCertificate=true";
        } else {
            return H2_URL;
        }
    }
}

