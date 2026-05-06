package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.AuthService;
import com.warehousewms.util.CredentialStorage;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel appIconLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;
    private JLabel forgotPasswordLabel;
    private JButton signInButton;
    private JButton createAccountButton;
    private JLabel statusLabel;

    private final java.awt.Color normalColor;
    private final java.awt.Color hoverColor = new java.awt.Color(0, 102, 204);  // darker blue

    public LoginFrame() {
        setContentPane(rootPanel);
        setSize(650, 650);
        setMinimumSize(new java.awt.Dimension(450, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Smart WMS – Login");

        normalColor = forgotPasswordLabel.getForeground();
        loadRememberedCredentials();

        getRootPane().setDefaultButton(signInButton);

        signInButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter both username and password.");
                return;
            }

            signInButton.setEnabled(false);
            statusLabel.setText("Signing in...");

            new SwingWorker<Boolean, Void>() {
                private String errorMessage;
                private String fullName;

                @Override
                protected Boolean doInBackground() {
                    try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                        var user = authService.login(username, password);
                        if (user == null) {
                            return false;
                        }
                        if (rememberMeCheckBox.isSelected()) {
                            CredentialStorage.saveCredentials(username, password);
                        } else {
                            CredentialStorage.clearCredentials();
                        }
                        fullName = user.getFullName();
                        return true;
                    } catch (Exception ex) {
                        errorMessage = ex.getMessage();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            statusLabel.setText("Login successful! Welcome, " + fullName + ".");
                        } else if (errorMessage != null) {
                            statusLabel.setText("Login failed: " + errorMessage);
                        } else {
                            statusLabel.setText("Invalid username or password.");
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("Login failed: " + ex.getMessage());
                    } finally {
                        signInButton.setEnabled(true);
                    }
                }
            }.execute();
        });

        // Placeholder action for Create account button
        createAccountButton.addActionListener(e -> {
            dispose();
            RegisterFrame registerFrame = new RegisterFrame();
            registerFrame.setVisible(true);
        });

        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                forgotPasswordLabel.setForeground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                forgotPasswordLabel.setCursor(Cursor.getDefaultCursor());
                forgotPasswordLabel.setForeground(normalColor);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Password reset not implemented yet.");
            }
        });
    }

    private void loadRememberedCredentials() {
        String[] saved = CredentialStorage.getSavedCredentials();
        if (saved != null) {
            usernameField.setText(saved[0]);
            passwordField.setText(saved[1]);
            rememberMeCheckBox.setSelected(true);
        }
    }

}