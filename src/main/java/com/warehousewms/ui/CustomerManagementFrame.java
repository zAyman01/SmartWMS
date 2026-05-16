package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Customer;
import com.warehousewms.service.CustomerService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CustomerManagementFrame extends JFrame {

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
    private JTable customerTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Id", "Name", "Contact", "Email", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    public CustomerManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Customers");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Configure table
        customerTable.setModel(tableModel);
        customerTable.setRowSorter(sorter);
        ThemeConfig.styleTable(customerTable);

        // Apply theme
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search customers...");

        applyButtonTheme(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);
        applyButtonTheme(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);
        applyButtonTheme(deleteButton, ThemeConfig.DANGER, ThemeConfig.DANGER.brighter());
        applyButtonTheme(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);

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
        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadCustomers());

        loadCustomers();
    }

    private void applyButtonTheme(JButton btn, Color bg, Color hover) {
        btn.setFont(ThemeConfig.FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(bg.equals(ThemeConfig.BG_CARD) ? ThemeConfig.TEXT_PRIMARY : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    private void loadCustomers() {
        statusLabel.setText("Loading customers...");
        new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                try (CustomerService svc = new CustomerService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.listAll();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Customer> list = get();
                    tableModel.setRowCount(0);
                    for (Customer c : list) {
                        tableModel.addRow(new Object[]{c.getCustomerId(), c.getName(), c.getContactName(), c.getEmail(), c.getPhone()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " customers.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load customers: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addCustomer() {
        Customer result = showCustomerDialog(null);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (CustomerService svc = new CustomerService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.add(result);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Customer created.");
                    loadCustomers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            statusLabel.setText("Select a customer to edit.");
            return;
        }
        Customer result = showCustomerDialog(selected);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (CustomerService svc = new CustomerService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.update(result);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Customer updated.");
                    loadCustomers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            statusLabel.setText("Select a customer to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + selected.getName() + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (CustomerService svc = new CustomerService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.delete(selected.getCustomerId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean d = get();
                    statusLabel.setText(d ? "Deleted." : "Not found.");
                    if (d) loadCustomers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Customer getSelectedCustomer() {
        int vr = customerTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = customerTable.convertRowIndexToModel(vr);
        Customer c = new Customer();
        c.setCustomerId((int) tableModel.getValueAt(mr, 0));
        c.setName((String) tableModel.getValueAt(mr, 1));
        c.setContactName((String) tableModel.getValueAt(mr, 2));
        c.setEmail((String) tableModel.getValueAt(mr, 3));
        c.setPhone((String) tableModel.getValueAt(mr, 4));
        return c;
    }

    private Customer showCustomerDialog(Customer existing) {
        JTextField nameF = new JTextField(), contactF = new JTextField();
        JTextField emailF = new JTextField(), phoneF = new JTextField();
        if (existing != null) {
            nameF.setText(existing.getName());
            contactF.setText(existing.getContactName());
            emailF.setText(existing.getEmail());
            phoneF.setText(existing.getPhone());
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Name"));
        p.add(nameF);
        p.add(new JLabel("Contact name"));
        p.add(contactF);
        p.add(new JLabel("Email"));
        p.add(emailF);
        p.add(new JLabel("Phone"));
        p.add(phoneF);
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Add customer" : "Edit customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;
        String name = nameF.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return null;
        }
        Customer customer = existing != null ? existing : new Customer();
        customer.setName(name);
        customer.setContactName(contactF.getText().trim());
        customer.setEmail(emailF.getText().trim());
        customer.setPhone(phoneF.getText().trim());
        return customer;
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new CustomerManagementFrame().setVisible(true));
    }
}
