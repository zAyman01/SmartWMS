package com.warehousewms.service;

import com.warehousewms.config.DatabaseManager;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class ReportService {

    public void generateInventoryReport() throws Exception {
        // Load JRXML
        InputStream stream = getClass().getResourceAsStream("/reports/InventoryReport.jrxml");
        if (stream == null) {
            throw new Exception("InventoryReport.jrxml not found in resources/reports/");
        }

        // Compile
        JasperReport report = JasperCompileManager.compileReport(stream);

        // Fill
        try (Connection conn = new DatabaseManager().getDataSourceWithFallback().getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);
            // Display
            JasperViewer.viewReport(print, false); // false means do not exit app on close
        }
    }
}
