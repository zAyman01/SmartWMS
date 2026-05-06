package com.warehousewms;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        System.setProperty("flatlaf.useNativeLibrary", "false");
        // Set modern look and feel
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null,
                "Smart WMS application skeleton started.\nNavigate to ui package to design forms."
        ));
    }
}

