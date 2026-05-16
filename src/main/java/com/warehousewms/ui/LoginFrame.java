package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.service.LoginResult;
import com.warehousewms.service.LoginService;
import com.warehousewms.util.CredentialStorage;
import com.warehousewms.util.SessionContext;

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

    private final LoginService loginService;
    private final Color normalColor;
    private final Color hoverColor = ThemeConfig.ACCENT;

    public LoginFrame() {
        this(new LoginService(new DatabaseManager().getDataSourceWithFallback()));
    }

    public LoginFrame(LoginService loginService) {
        this.loginService = loginService;

        setContentPane(rootPanel);
        setSize(650, 650);
        setMinimumSize(new Dimension(450, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Smart WMS \u2013 Login");

        // Apply theme colors to form components
        applyTheme();

        normalColor = forgotPasswordLabel.getForeground();
        loadRememberedCredentials();

        getRootPane().setDefaultButton(signInButton);

        signInButton.addActionListener(e -> performLogin());

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
                dispose();
                ForgetPasswordFrame forgetPasswordFrame = new ForgetPasswordFrame();
                forgetPasswordFrame.setVisible(true);
            }
        });
    }

    private void applyTheme() {
        // Style the sign in button as an accent button
        signInButton.setBackground(ThemeConfig.ACCENT);
        signInButton.setForeground(Color.WHITE);
        signInButton.setFont(ThemeConfig.FONT_BUTTON);
        signInButton.setFocusPainted(false);
        signInButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (signInButton.isEnabled()) signInButton.setBackground(ThemeConfig.ACCENT_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                signInButton.setBackground(ThemeConfig.ACCENT);
            }
        });

        // Style the create account button
        createAccountButton.setBackground(ThemeConfig.BG_CARD);
        createAccountButton.setForeground(ThemeConfig.TEXT_PRIMARY);
        createAccountButton.setFont(ThemeConfig.FONT_BUTTON);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Title styling
        titleLabel.setForeground(ThemeConfig.ACCENT);

        // App icon
        appIconLabel.setIcon(ThemeConfig.getIcon("package", 48, 48, ThemeConfig.ACCENT));

        // Forgot password link
        forgotPasswordLabel.setForeground(ThemeConfig.ACCENT);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        signInButton.setEnabled(false);
        statusLabel.setText("Signing in...");
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);

        boolean rememberMe = rememberMeCheckBox.isSelected();

        new SwingWorker<LoginResult, Void>() {
            @Override
            protected LoginResult doInBackground() {
                return loginService.login(username, password, rememberMe);
            }

            @Override
            protected void done() {
                try {
                    LoginResult result = get();
                    if (result.isSuccess()) {
                        statusLabel.setForeground(ThemeConfig.SUCCESS);
                        statusLabel.setText("Login successful! Welcome, " + result.getUser().getFullName() + ".");
                        SwingUtilities.invokeLater(() -> {
                            DashboardFrame dashboard = new DashboardFrame();
                            dashboard.setVisible(true);
                        });
                        dispose();
                    } else {
                        statusLabel.setForeground(ThemeConfig.DANGER);
                        statusLabel.setText(result.getErrorMessage());
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ThemeConfig.DANGER);
                    statusLabel.setText("Login failed: " + ex.getMessage());
                } finally {
                    signInButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadRememberedCredentials() {
        String[] saved = CredentialStorage.getSavedCredentials();
        if (saved != null) {
            usernameField.setText(saved[0]);
            passwordField.setText(saved[1]);
            rememberMeCheckBox.setSelected(true);
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
