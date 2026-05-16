package com.warehousewms.ui;

import com.warehousewms.service.DatabaseAdminService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BackupFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JPanel toolbarPanel;
    private JButton backupButton;
    private JLabel statusLabel;

    public BackupFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Backup");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);

        applyButtonTheme(backupButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);

        backupButton.addActionListener(e -> doBackup());
    }

    private void applyButtonTheme(JButton btn, Color bg, Color hover) {
        btn.setFont(ThemeConfig.FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(bg.equals(ThemeConfig.BG_CARD) ? ThemeConfig.TEXT_PRIMARY : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void doBackup() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Backup Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File targetDir = chooser.getSelectedFile();
        statusLabel.setText("Creating backup...");
        
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                DatabaseAdminService svc = new DatabaseAdminService();
                return svc.createBackup(targetDir.getAbsolutePath());
            }
            @Override protected void done() {
                try {
                    String file = get();
                    statusLabel.setText("Backup successfully created at: " + file);
                    JOptionPane.showMessageDialog(BackupFrame.this, "Backup saved to:\n" + file, "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    statusLabel.setText("Backup failed.");
                    JOptionPane.showMessageDialog(BackupFrame.this, "Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new BackupFrame().setVisible(true));
    }
}
