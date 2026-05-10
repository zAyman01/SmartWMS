package com.warehousewms;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.LoginService;
import com.warehousewms.ui.LoginFrame;
import com.warehousewms.ui.ThemeConfig;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        ThemeConfig.install();

        DatabaseManager dbManager = new DatabaseManager();
        LoginService loginService = new LoginService(dbManager.getDataSourceWithFallback());

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(loginService);
            loginFrame.setVisible(true);
        });
    }
}
