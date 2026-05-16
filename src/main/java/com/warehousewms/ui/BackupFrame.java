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

        ThemeConfig.styleButton(backupButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "backup");

        backupButton.addActionListener(e -> doBackup());

        ThemeConfig.addHelpMenu(this, "Disaster Recovery & Backups\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "The WMS database contains the lifeblood of your operation: inventory ledgers, supplier contracts, and " +
            "customer order histories. In the event of hardware failure or cyber-attacks, a recent backup is the only " +
            "way to avoid catastrophic data loss and business downtime.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Run Backup: Select a secure external directory and click 'Backup'. The system will generate a highly " +
            "compressed, secure SQL snapshot of your entire database.\n" +
            "• Best Practice: Perform this task regularly and store the resulting file off-site or in the cloud.");
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
