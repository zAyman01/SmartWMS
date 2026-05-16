package com.warehousewms.service;

import com.warehousewms.config.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseAdminService {

    public String createBackup(String targetDirectory) throws Exception {
        if (targetDirectory == null || targetDirectory.isBlank()) {
            throw new IllegalArgumentException("Target directory must not be empty");
        }
        // Sanitize: remove characters that could break SQL syntax
        String sanitizedDir = targetDirectory.replace("'", "").replace(";", "").replace("--", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = sanitizedDir.replace("\\", "/") + "/backup_" + timestamp + ".zip";

        try (Connection conn = new DatabaseManager().getDataSourceWithFallback().getConnection();
             Statement stmt = conn.createStatement()) {

             String dbUrl = conn.getMetaData().getURL();
             if (dbUrl.startsWith("jdbc:h2")) {
                 stmt.execute("BACKUP TO '" + backupFile + "'");
                 return backupFile;
             } else {
                 throw new Exception("Automated SQL Server backup from client not supported without sysadmin privileges. Please use SSMS.");
             }
        }
    }
}
