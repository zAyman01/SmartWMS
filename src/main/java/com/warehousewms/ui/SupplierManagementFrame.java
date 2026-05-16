package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Supplier;
import com.warehousewms.service.SupplierService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class SupplierManagementFrame extends JFrame {

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
    private JTable supplierTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Id", "Name", "Contact", "Email", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    public SupplierManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Suppliers");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Configure table
        supplierTable.setModel(tableModel);
        supplierTable.setRowSorter(sorter);
        ThemeConfig.styleTable(supplierTable);

        // Apply theme
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search suppliers...");

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
        addButton.addActionListener(e -> addSupplier());
        editButton.addActionListener(e -> editSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        refreshButton.addActionListener(e -> loadSuppliers());

        loadSuppliers();
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

    private void loadSuppliers() {
        statusLabel.setText("Loading suppliers...");
        new SwingWorker<List<Supplier>, Void>() {
            @Override
            protected List<Supplier> doInBackground() throws Exception {
                try (SupplierService svc = new SupplierService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.listAll();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Supplier> list = get();
                    tableModel.setRowCount(0);
                    for (Supplier s : list) {
                        tableModel.addRow(new Object[]{s.getSupplierId(), s.getName(), s.getContactName(), s.getEmail(), s.getPhone()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " suppliers.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load suppliers: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addSupplier() {
        Supplier result = showSupplierDialog(null);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (SupplierService svc = new SupplierService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.add(result);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Supplier created.");
                    loadSuppliers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editSupplier() {
        Supplier selected = getSelectedSupplier();
        if (selected == null) {
            statusLabel.setText("Select a supplier to edit.");
            return;
        }
        Supplier result = showSupplierDialog(selected);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (SupplierService svc = new SupplierService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.update(result);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Supplier updated.");
                    loadSuppliers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteSupplier() {
        Supplier selected = getSelectedSupplier();
        if (selected == null) {
            statusLabel.setText("Select a supplier to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + selected.getName() + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (SupplierService svc = new SupplierService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.delete(selected.getSupplierId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean d = get();
                    statusLabel.setText(d ? "Deleted." : "Not found.");
                    if (d) loadSuppliers();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Supplier getSelectedSupplier() {
        int vr = supplierTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = supplierTable.convertRowIndexToModel(vr);
        Supplier s = new Supplier();
        s.setSupplierId((int) tableModel.getValueAt(mr, 0));
        s.setName((String) tableModel.getValueAt(mr, 1));
        s.setContactName((String) tableModel.getValueAt(mr, 2));
        s.setEmail((String) tableModel.getValueAt(mr, 3));
        s.setPhone((String) tableModel.getValueAt(mr, 4));
        return s;
    }

    private Supplier showSupplierDialog(Supplier existing) {
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
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Add supplier" : "Edit supplier", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;
        String name = nameF.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return null;
        }
        Supplier supplier = existing != null ? existing : new Supplier();
        supplier.setName(name);
        supplier.setContactName(contactF.getText().trim());
        supplier.setEmail(emailF.getText().trim());
        supplier.setPhone(phoneF.getText().trim());
        return supplier;
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new SupplierManagementFrame().setVisible(true));
    }
}
