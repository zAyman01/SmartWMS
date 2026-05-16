package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.PurchaseOrder;
import com.warehousewms.service.PurchaseOrderService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class PurchaseOrderManagementFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField searchField;
    private JPanel toolbarPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton refreshButton;
    private JScrollPane tableScrollPane;
    private JTable poTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"PO Id", "Supplier Id", "Date", "Status", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    public PurchaseOrderManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Purchase Orders");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        poTable.setModel(tableModel);
        poTable.setRowSorter(sorter);
        ThemeConfig.styleTable(poTable);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search POs...");

        applyButtonTheme(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);
        applyButtonTheme(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);
        applyButtonTheme(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);

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

        addButton.addActionListener(e -> addPO());
        editButton.addActionListener(e -> editPO());
        refreshButton.addActionListener(e -> loadPOs());

        loadPOs();
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

    private void loadPOs() {
        statusLabel.setText("Loading POs...");
        new SwingWorker<List<PurchaseOrder>, Void>() {
            @Override
            protected List<PurchaseOrder> doInBackground() throws Exception {
                PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                return svc.getAllPOs();
            }

            @Override
            protected void done() {
                try {
                    List<PurchaseOrder> list = get();
                    tableModel.setRowCount(0);
                    for (PurchaseOrder po : list) {
                        tableModel.addRow(new Object[]{po.getPoId(), po.getSupplierId(), po.getOrderDate(), po.getStatus(), po.getNotes()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " POs.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addPO() {
        PurchaseOrder result = showPODialog(null);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                svc.createPO(result, new ArrayList<>()); // Simplify: no lines created in this dialog
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("PO created.");
                    loadPOs();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editPO() {
        PurchaseOrder selected = getSelectedPO();
        if (selected == null) {
            statusLabel.setText("Select a PO.");
            return;
        }
        PurchaseOrder result = showPODialog(selected);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                svc.updatePO(result, new ArrayList<>());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("PO updated.");
                    loadPOs();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private PurchaseOrder getSelectedPO() {
        int vr = poTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = poTable.convertRowIndexToModel(vr);
        PurchaseOrder po = new PurchaseOrder();
        po.setPoId((int) tableModel.getValueAt(mr, 0));
        po.setSupplierId((int) tableModel.getValueAt(mr, 1));
        po.setOrderDate((Date) tableModel.getValueAt(mr, 2));
        po.setStatus((String) tableModel.getValueAt(mr, 3));
        po.setNotes((String) tableModel.getValueAt(mr, 4));
        return po;
    }

    private PurchaseOrder showPODialog(PurchaseOrder existing) {
        JTextField supplierF = new JTextField(), statusF = new JTextField(), notesF = new JTextField();
        if (existing != null) {
            supplierF.setText(String.valueOf(existing.getSupplierId()));
            statusF.setText(existing.getStatus());
            notesF.setText(existing.getNotes());
        } else {
            statusF.setText("Open");
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Supplier Id"));
        p.add(supplierF);
        p.add(new JLabel("Status"));
        p.add(statusF);
        p.add(new JLabel("Notes"));
        p.add(notesF);
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Create PO" : "Edit PO", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;

        try {
            PurchaseOrder po = existing != null ? existing : new PurchaseOrder();
            po.setSupplierId(Integer.parseInt(supplierF.getText().trim()));
            po.setStatus(statusF.getText().trim());
            po.setNotes(notesF.getText().trim());
            if (existing == null) po.setOrderDate(new Date());
            return po;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
            return null;
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new PurchaseOrderManagementFrame().setVisible(true));
    }
}
