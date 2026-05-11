package com.warehousewms.service;

import com.warehousewms.config.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseAdminService {

    public String createBackup(String targetDirectory) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = targetDirectory + "/backup_" + timestamp + ".zip";

        // Check if H2 or SQL Server
        // We will just execute a command. For H2, BACKUP TO works. 
        // For SQL Server, this will fail but we'll try to catch it and notify.
        try (Connection conn = new DatabaseManager().getDataSourceWithFallback().getConnection();
             Statement stmt = conn.createStatement()) {
             
             String dbUrl = conn.getMetaData().getURL();
             if (dbUrl.startsWith("jdbc:h2")) {
                 stmt.execute("BACKUP TO '" + backupFile.replace("\\", "/") + "'");
                 return backupFile;
             } else {
                 throw new Exception("Automated SQL Server backup from client not supported without sysadmin privileges. Please use SSMS.");
             }
        }
    }
}
