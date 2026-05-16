package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.User;
import com.warehousewms.service.AuthService;
import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class UserManagementFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField searchField;
    private JPanel toolbarPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JScrollPane tableScrollPane;
    private JTable userTable;
    private JLabel statusLabel;

    private static final String[] ROLES = {"Admin", "Supervisor", "Picker", "Operator"};
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Id", "Username", "Full Name", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    public UserManagementFrame() {
        if (!SessionContext.isAdmin()) {
            JOptionPane.showMessageDialog(null, "Access denied. Admin role required.",
                    "Access denied", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 User Management");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Configure table
        userTable.setModel(tableModel);
        userTable.setRowSorter(sorter);
        ThemeConfig.styleTable(userTable);

        // Apply theme
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search users...");

        ThemeConfig.styleButton(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "add");
        ThemeConfig.styleButton(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "edit");
        ThemeConfig.styleButton(deleteButton, ThemeConfig.DANGER, ThemeConfig.DANGER.brighter(), "delete");
        ThemeConfig.styleButton(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "refresh");

        // Search filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String t = searchField.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        // Listeners
        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());
        refreshButton.addActionListener(e -> loadUsers());

        loadUsers();

        ThemeConfig.addHelpMenu(this, "System Security & Users\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Controlling access to the warehouse management system is critical for operational integrity and security. " +
            "The User Management module ensures that only authorized personnel can log in, edit ledgers, or perform " +
            "sensitive administrative tasks.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Provision Accounts: Add new warehouse staff and assign them appropriate roles (Admin vs User).\n" +
            "• Manage Access: Deactivate accounts for departing employees to prevent unauthorized access while preserving their historical audit trail.");
    }

    

    private void loadUsers() {
        statusLabel.setText("Loading users...");
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                try (AuthService svc = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.listUsers();
                }
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    tableModel.setRowCount(0);
                    for (User u : list) {
                        tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " users.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load users: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addUser() {
        UserFormResult result = showUserDialog(null);
        if (result == null) return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService svc = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    if (svc.usernameExists(result.user.getUsername())) return false;
                    result.user.setPasswordHash(result.newPassword);
                    svc.register(result.user);
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        statusLabel.setText("Username exists.");
                        return;
                    }
                    statusLabel.setText("User created.");
                    loadUsers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
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
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (AuthService svc = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.updateUser(result.user);
                    if (result.newPassword != null && !result.newPassword.isEmpty()) {
                        svc.updatePassword(result.user.getUserId(), result.newPassword);
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
                    statusLabel.setText("Failed: " + ex.getMessage());
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
            statusLabel.setText("Cannot delete yourself.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + selected.getUsername() + "'?",
                "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (AuthService svc = new AuthService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.deleteUser(selected.getUserId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean d = get();
                    statusLabel.setText(d ? "Deleted." : "Not found.");
                    if (d) loadUsers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private User getSelectedUser() {
        int vr = userTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = userTable.convertRowIndexToModel(vr);
        User u = new User();
        u.setUserId((int) tableModel.getValueAt(mr, 0));
        u.setUsername((String) tableModel.getValueAt(mr, 1));
        u.setFullName((String) tableModel.getValueAt(mr, 2));
        u.setRole((String) tableModel.getValueAt(mr, 3));
        return u;
    }

    private UserFormResult showUserDialog(User existing) {
        JTextField usernameF = new JTextField(), fullNameF = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(ROLES);
        JPasswordField passF = new JPasswordField(), confirmF = new JPasswordField();
        if (existing != null) {
            usernameF.setText(existing.getUsername());
            usernameF.setEnabled(false);
            fullNameF.setText(existing.getFullName());
            roleBox.setSelectedItem(existing.getRole());
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Username"));
        p.add(usernameF);
        p.add(new JLabel("Full name"));
        p.add(fullNameF);
        p.add(new JLabel("Role"));
        p.add(roleBox);
        p.add(new JLabel(existing == null ? "Password" : "New password (blank to keep)"));
        p.add(passF);
        p.add(new JLabel("Confirm password"));
        p.add(confirmF);

        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Add user" : "Edit user",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;

        String username = usernameF.getText().trim(), fullName = fullNameF.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String pass = new String(passF.getPassword()), confirm = new String(confirmF.getPassword());

        if (username.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and name required.");
            return null;
        }
        if (existing == null && username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username min 3 chars.");
            return null;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords don't match.");
            return null;
        }
        if (existing == null && pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password min 6 chars.");
            return null;
        }
        if (existing != null && !pass.isEmpty() && pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password min 6 chars.");
            return null;
        }

        User u = existing != null ? existing : new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(role);
        return new UserFormResult(u, pass.isEmpty() ? null : pass);
    }

    private static class UserFormResult {
        final User user;
        final String newPassword;

        UserFormResult(User u, String p) {
            user = u;
            newPassword = p;
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new UserManagementFrame().setVisible(true));
    }
}
