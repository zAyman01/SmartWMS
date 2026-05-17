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

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Supplier Name", "Date", "Status", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final java.util.Map<Integer, String> supplierNames = new java.util.HashMap<>();
    private java.util.List<PurchaseOrder> loadedPOs = new java.util.ArrayList<>();

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

        ThemeConfig.styleButton(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "add");
        ThemeConfig.styleButton(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "edit");
        ThemeConfig.styleButton(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "refresh");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String t = searchField.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(t)));
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

        ThemeConfig.addHelpMenu(this, "Purchase Orders (Inbound)\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "A Purchase Order (PO) is a legally binding contract sent to a supplier to procure goods. In the WMS workflow, " +
            "the PO informs the Receiving team what products to expect and in what quantities. It is the very first step " +
            "in the inbound inventory cycle.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Create PO: Initiate a new order by selecting a Supplier and an Expected Delivery Date.\n" +
            "• Manage Lines: After creating the header, click 'Manage Lines' to specify exactly which products and quantities you are buying.\n" +
            "• Next Steps: Once a PO is generated and sent to the supplier, the warehouse awaits the physical delivery, " +
            "which will be processed in the 'Receiving' module against this PO ID.");
    }

    

    private void loadPOs() {
        statusLabel.setText("Loading POs...");
        new SwingWorker<List<PurchaseOrder>, Void>() {
            @Override
            protected List<PurchaseOrder> doInBackground() throws Exception {
                PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                com.warehousewms.service.SupplierService ss = new com.warehousewms.service.SupplierService(new DatabaseManager().getDataSourceWithFallback());
                supplierNames.clear();
                for(com.warehousewms.model.Supplier s : ss.listAll()) supplierNames.put(s.getSupplierId(), s.getName());
                return svc.getAllPOs();
            }

            @Override
            protected void done() {
                try {
                    List<PurchaseOrder> list = get();
                    loadedPOs = list;
                    tableModel.setRowCount(0);
                    for (PurchaseOrder po : list) {
                        String sName = supplierNames.getOrDefault(po.getSupplierId(), "Unknown");
                        tableModel.addRow(new Object[]{po.getPoId(), sName, po.getOrderDate(), po.getStatus(), po.getNotes()});
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
        if (mr < loadedPOs.size()) return loadedPOs.get(mr);
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderDate(new Date());
        return po;
    }

    private PurchaseOrder showPODialog(PurchaseOrder existing) {
        // Build supplier name array from loaded map
        String[] supplierNamesList = supplierNames.values().stream().sorted().toArray(String[]::new);
        JComboBox<String> supplierCombo = new JComboBox<>(supplierNamesList);
        JTextField statusF = new JTextField(), notesF = new JTextField();
        if (existing != null) {
            String existingSupName = supplierNames.getOrDefault(existing.getSupplierId(), "");
            supplierCombo.setSelectedItem(existingSupName);
            statusF.setText(existing.getStatus());
            notesF.setText(existing.getNotes());
        } else {
            statusF.setText("Open");
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Supplier"));
        p.add(supplierCombo);
        p.add(new JLabel("Status"));
        p.add(statusF);
        p.add(new JLabel("Notes"));
        p.add(notesF);
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Create PO" : "Edit PO", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;

        try {
            String selectedSupName = (String) supplierCombo.getSelectedItem();
            if (selectedSupName == null || selectedSupName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a supplier.");
                return null;
            }
            // Reverse lookup: find supplier ID from name
            int supId = 0;
            for (var entry : supplierNames.entrySet()) {
                if (entry.getValue().equals(selectedSupName)) {
                    supId = entry.getKey();
                    break;
                }
            }
            if (supId == 0) {
                JOptionPane.showMessageDialog(this, "Supplier not found. Please create the supplier first.");
                return null;
            }
            PurchaseOrder po = existing != null ? existing : new PurchaseOrder();
            po.setSupplierId(supId);
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
