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
        setTitle("Smart WMS – Forgot Password");

        // Enter key triggers reset
        getRootPane().setDefaultButton(resetButton);

        // Hover effect for "Back to sign in" label
        final Color normalColor = backToLoginLabel.getForeground();
        final Color hoverColor = new Color(0, 102, 204);
        backToLoginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backToLoginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                backToLoginLabel.setForeground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backToLoginLabel.setCursor(Cursor.getDefaultCursor());
                backToLoginLabel.setForeground(normalColor);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
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

    private void handlePasswordReset() {
        String usernameOrEmail = usernameOrEmailField.getText().trim();

        if (usernameOrEmail.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            return;
        }

        String newPassword = promptForNewPassword();
        if (newPassword == null) {
            return;
        }

        resetButton.setEnabled(false);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Resetting password...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return authService.resetPassword(usernameOrEmail, newPassword);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean updated = get();
                    if (updated) {
                        statusLabel.setForeground(new Color(0, 128, 0));
                        statusLabel.setText("Password updated. You can now sign in.");
                        usernameOrEmailField.setText("");
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Account not found.");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                } finally {
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
}