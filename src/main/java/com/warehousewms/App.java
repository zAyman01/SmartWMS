package com.warehousewms;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.warehousewms.ui.LoginFrame;

public class App {
    public static void main(String[] args) {
        System.setProperty("flatlaf.useNativeLibrary", "false");
        // Set modern look and feel
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

