package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.User;
import com.warehousewms.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterFrame extends JFrame {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel appIconLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel fullNameLabel;
    private JTextField fullNameField;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel confirmPasswordLabel;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JLabel statusLabel;
    private JLabel backToLoginLabel;

    private static final String DEFAULT_ROLE = "Operator";

    public RegisterFrame() {
        setContentPane(rootPanel);
        setSize(650, 650);
        setMinimumSize(new Dimension(650, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Smart WMS – Create Account");

        // Enter key triggers registration
        getRootPane().setDefaultButton(registerButton);

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

        registerButton.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        // Basic validation
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }
        if (username.length() < 3) {
            statusLabel.setText("Username must be at least 3 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            return;
        }

        registerButton.setEnabled(false);
        statusLabel.setText("Creating account…");

        new SwingWorker<RegisterResult, Void>() {
            @Override
            protected RegisterResult doInBackground() {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    if (authService.usernameExists(username)) {
                        return RegisterResult.failure("Username is already taken.");
                    }
                    User newUser = new User();
                    newUser.setFullName(fullName);
                    newUser.setUsername(username);
                    newUser.setPasswordHash(password); // Repository hashes before persisting
                    newUser.setRole(DEFAULT_ROLE);

                    authService.register(newUser);
                    return RegisterResult.success();
                } catch (Exception ex) {
                    return RegisterResult.failure("Registration failed: " + ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    RegisterResult result = get();
                    if (result.success) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "Account created successfully! Please sign in.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            LoginFrame login = new LoginFrame();
                            login.setVisible(true);
                        });
                    } else {
                        statusLabel.setText(result.message);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                } finally {
                    registerButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private static class RegisterResult {
        private final boolean success;
        private final String message;

        private RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private static RegisterResult success() {
            return new RegisterResult(true, null);
        }

        private static RegisterResult failure(String message) {
            return new RegisterResult(false, message);
        }
    }
}