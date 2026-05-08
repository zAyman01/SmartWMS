package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.User;
import com.warehousewms.service.AuthService;
import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

public class UserManagementFrame extends JFrame {
    private static final String[] ROLES = {"Admin", "Supervisor", "Picker", "Operator"};

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Id", "Username", "Full Name", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable userTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel(" ");

    public UserManagementFrame() {
        if (!SessionContext.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access denied. Admin role required.", "Access denied",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setTitle("Smart WMS – User Management");
        setSize(750, 500);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel actions = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        add(new JScrollPane(userTable), BorderLayout.CENTER);
        add(actions, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());
        refreshButton.addActionListener(e -> loadUsers());

        loadUsers();
    }

    private void loadUsers() {
        statusLabel.setText("Loading users...");
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return authService.listUsers();
                }
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User user : users) {
                        tableModel.addRow(new Object[]{user.getUserId(), user.getUsername(), user.getFullName(), user.getRole()});
                    }
                    statusLabel.setText("Loaded " + users.size() + " users.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load users: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addUser() {
        UserFormResult result = showUserDialog(null);
        if (result == null) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    if (authService.usernameExists(result.user.getUsername())) {
                        return false;
                    }
                    result.user.setPasswordHash(result.newPassword);
                    authService.register(result.user);
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean created = get();
                    if (!created) {
                        statusLabel.setText("Username already exists.");
                        return;
                    }
                    statusLabel.setText("User created.");
                    loadUsers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed to create user: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editUser() {
        User selected = getSelectedUser();
        if (selected == null) {
            statusLabel.setText("Select a user to edit.");
            return;
        }

        UserFormResult result = showUserDialog(selected);
        if (result == null) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    authService.updateUser(result.user);
                    if (result.newPassword != null && !result.newPassword.isEmpty()) {
                        authService.updatePassword(result.user.getUserId(), result.newPassword);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("User updated.");
                    loadUsers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed to update user: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteUser() {
        User selected = getSelectedUser();
        if (selected == null) {
            statusLabel.setText("Select a user to delete.");
            return;
        }
        if (selected.getUserId() == SessionContext.getCurrentUser().getUserId()) {
            statusLabel.setText("You cannot delete your own account.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete user '" + selected.getUsername() + "'?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService authService = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return authService.deleteUser(selected.getUserId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        statusLabel.setText("User deleted.");
                        loadUsers();
                    } else {
                        statusLabel.setText("User not found.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to delete user: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        User user = new User();
        user.setUserId((int) tableModel.getValueAt(row, 0));
        user.setUsername((String) tableModel.getValueAt(row, 1));
        user.setFullName((String) tableModel.getValueAt(row, 2));
        user.setRole((String) tableModel.getValueAt(row, 3));
        return user;
    }

    private UserFormResult showUserDialog(User existing) {
        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(ROLES);
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        if (existing != null) {
            usernameField.setText(existing.getUsername());
            usernameField.setEnabled(false);
            fullNameField.setText(existing.getFullName());
            roleBox.setSelectedItem(existing.getRole());
        }

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Full name"));
        panel.add(fullNameField);
        panel.add(new JLabel("Role"));
        panel.add(roleBox);
        panel.add(new JLabel(existing == null ? "Password" : "New password (leave blank to keep)"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm password"));
        panel.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(this, panel,
                existing == null ? "Add user" : "Edit user", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and full name are required.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (existing == null && username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (existing == null && password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (existing != null && !password.isEmpty() && password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation error",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        User user = existing != null ? existing : new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);

        String newPassword = password.isEmpty() ? null : password;
        return new UserFormResult(user, newPassword);
    }

    private static class UserFormResult {
        private final User user;
        private final String newPassword;

        private UserFormResult(User user, String newPassword) {
            this.user = user;
            this.newPassword = newPassword;
        }
    }
}

