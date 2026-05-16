package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ForgetPasswordFrame extends JFrame {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel appIconLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel usernameOrEmailLabel;
    private JTextField usernameOrEmailField;
    private JButton resetButton;
    private JLabel statusLabel;
    private JLabel backToLoginLabel;

    public ForgetPasswordFrame() {
        setContentPane(rootPanel);
        setSize(500, 500);
        setMinimumSize(new Dimension(450, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Smart WMS \u2013 Forgot Password");

        applyTheme();

        // Enter key triggers reset
        getRootPane().setDefaultButton(resetButton);

        // Hover effect for "Back to sign in" label
        final Color normalColor = backToLoginLabel.getForeground();
        backToLoginLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                backToLoginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                backToLoginLabel.setForeground(ThemeConfig.ACCENT_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                backToLoginLabel.setCursor(Cursor.getDefaultCursor());
                backToLoginLabel.setForeground(normalColor);
            }
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginFrame login = new LoginFrame();
                    login.setVisible(true);
                });
            }
        });

        // Reset button action
        resetButton.addActionListener(e -> handlePasswordReset());
    }

    private void applyTheme() {
        resetButton.setBackground(ThemeConfig.ACCENT);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(ThemeConfig.FONT_BUTTON);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (resetButton.isEnabled()) resetButton.setBackground(ThemeConfig.ACCENT_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                resetButton.setBackground(ThemeConfig.ACCENT);
            }
        });

        titleLabel.setForeground(ThemeConfig.ACCENT);
        backToLoginLabel.setForeground(ThemeConfig.ACCENT);

        ThemeConfig.applyEmojiFont(appIconLabel);
    }

    private void handlePasswordReset() {
        String username = usernameOrEmailField.getText().trim();

        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            return;
        }

        resetButton.setEnabled(false);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        statusLabel.setText("Verifying account...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return authService.usernameExists(username);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exists = get();
                    if (!exists) {
                        statusLabel.setForeground(ThemeConfig.DANGER);
                        statusLabel.setText("Account not found.");
                        resetButton.setEnabled(true);
                        return;
                    }

                    String newPassword = promptForNewPassword();
                    if (newPassword == null) {
                        statusLabel.setText(" ");
                        resetButton.setEnabled(true);
                        return;
                    }

                    statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
                    statusLabel.setText("Resetting password...");

                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                                return authService.resetPassword(username, newPassword);
                            }
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean updated = get();
                                if (updated) {
                                    statusLabel.setForeground(ThemeConfig.SUCCESS);
                                    statusLabel.setText("Password updated. You can now sign in.");
                                    usernameOrEmailField.setText("");
                                } else {
                                    statusLabel.setForeground(ThemeConfig.DANGER);
                                    statusLabel.setText("Account not found.");
                                }
                            } catch (Exception ex) {
                                statusLabel.setForeground(ThemeConfig.DANGER);
                                statusLabel.setText("Error: " + ex.getMessage());
                            } finally {
                                resetButton.setEnabled(true);
                            }
                        }
                    }.execute();

                } catch (Exception ex) {
                    statusLabel.setForeground(ThemeConfig.DANGER);
                    statusLabel.setText("Error: " + ex.getMessage());
                    resetButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private String promptForNewPassword() {
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("New password"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm password"));
        panel.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(this, panel,
                "Reset password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return password;
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new ForgetPasswordFrame().setVisible(true));
    }
}