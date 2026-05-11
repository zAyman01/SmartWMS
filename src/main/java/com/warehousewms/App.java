package com.warehousewms;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.LoginService;
import com.warehousewms.ui.LoginFrame;
import com.warehousewms.ui.ThemeConfig;

import javax.swing.*;

public class App {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Global Exception Handler
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
        LoginService loginService = new LoginService(dbManager.getDataSourceWithFallback());

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(loginService);
            loginFrame.setVisible(true);
        });
    }
}
