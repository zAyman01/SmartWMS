package com.warehousewms;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.LoginService;
import com.warehousewms.ui.LoginFrame;
import com.warehousewms.ui.ThemeConfig;

import javax.sql.DataSource;
import javax.swing.*;

public class App {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("An unexpected error occurred in thread " + t.getName(), e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred:\n" + e.getMessage(),
                        "System Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        });

        ThemeConfig.install();

        DatabaseManager dbManager = new DatabaseManager();
        DataSource dataSource = dbManager.getDataSourceWithFallback();

        try {
            new SeedData(dataSource).seedAll();
            logger.info("Seed data applied successfully");
        } catch (Exception e) {
            logger.warn("Seed data skipped or failed (first run already seeded): {}", e.getMessage());
        }

        LoginService loginService = new LoginService(dataSource);

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(loginService);
            loginFrame.setVisible(true);
        });
    }
}
