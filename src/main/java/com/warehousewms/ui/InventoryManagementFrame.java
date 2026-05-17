package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Inventory;
import com.warehousewms.service.InventoryService;
import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class InventoryManagementFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField searchField;
    private JPanel toolbarPanel;
    private JButton adjustButton;
    private JButton transferButton;
    private JButton refreshButton;
    private JButton printButton;
    private JScrollPane tableScrollPane;
    private JTable inventoryTable;
    private JLabel statusLabel;
    private BarcodeScannerPanel scannerPanel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Product Name", "Bin Name", "Quantity"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final java.util.Map<Integer, String> productNames = new java.util.HashMap<>();
    private final java.util.Map<Integer, String> binNames = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> productNameToId = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> binNameToId = new java.util.HashMap<>();

    public InventoryManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Inventory");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inventoryTable.setModel(tableModel);
        inventoryTable.setRowSorter(sorter);
        ThemeConfig.styleTable(inventoryTable);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search Inventory...");

        ThemeConfig.styleButton(adjustButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "adjust");
        ThemeConfig.styleButton(transferButton, ThemeConfig.WARNING, ThemeConfig.WARNING.brighter(), "transfer");
        ThemeConfig.styleButton(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "refresh");
        ThemeConfig.styleButton(printButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "print");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String t = searchField.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(t)));
            }

            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        scannerPanel = new BarcodeScannerPanel();
        scannerPanel.setParentFrame(this);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.remove(mainPanel);
        rootPanel.add(scannerPanel, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);
        scannerPanel.setScanListener(this::onBarcodeScan);

        adjustButton.addActionListener(e -> doAdjust());
        transferButton.addActionListener(e -> doTransfer());
        refreshButton.addActionListener(e -> loadInventory());
        printButton.addActionListener(e -> printReport());

        loadInventory();

        ThemeConfig.addHelpMenu(this, "Inventory Control\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Inventory Control is the heart of the WMS. It maintains the absolute truth of what stock is physically " +
            "present in the warehouse and exactly where it is located. Accurate inventory prevents stockouts, reduces " +
            "holding costs, and ensures smooth sales operations.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Audit & Scan: Search for an item or use a barcode scanner to instantly verify its bin location and quantity.\n" +
            "• Stock Transfers: Move inventory between bins to optimize space or consolidate storage.\n" +
            "• Adjustments: Correct discrepancies discovered during physical cycle counts.\n" +
            "• Print Report: Generate physical inventory manifests for tax or auditing purposes.");
    }

    private void onBarcodeScan(com.warehousewms.model.Product product) {
        searchField.setText(product.getSku());
        statusLabel.setText("Scanned: " + product.getName());
    }

    

    private void loadInventory() {
        statusLabel.setText("Loading Inventory...");
        new SwingWorker<List<Inventory>, Void>() {
            @Override
            protected List<Inventory> doInBackground() throws Exception {
                InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                com.warehousewms.service.ProductService ps = new com.warehousewms.service.ProductService(new DatabaseManager().getDataSourceWithFallback());
                com.warehousewms.service.BinService bs = new com.warehousewms.service.BinService(new DatabaseManager().getDataSourceWithFallback());
                productNames.clear();
                binNames.clear();
                productNameToId.clear();
                binNameToId.clear();
                for(com.warehousewms.model.Product p : ps.listAll()) {
                    productNames.put(p.getProductId(), p.getName());
                    productNameToId.put(p.getName(), p.getProductId());
                }
                for(com.warehousewms.model.Bin b : bs.listAll()) {
                    binNames.put(b.getBinId(), b.getName());
                    binNameToId.put(b.getName(), b.getBinId());
                }
                return svc.getAllInventory();
            }

            @Override
            protected void done() {
                try {
                    List<Inventory> list = get();
                    tableModel.setRowCount(0);
                    for (Inventory inv : list) {
                        String pName = productNames.getOrDefault(inv.getProductId(), "Unknown");
                        String bName = binNames.getOrDefault(inv.getBinId(), "Unknown");
                        tableModel.addRow(new Object[]{inv.getInventoryId(), pName, bName, inv.getQuantity()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " inventory records.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void doAdjust() {
        int vr = inventoryTable.getSelectedRow();
        if (vr < 0) {
            statusLabel.setText("Select an item to adjust.");
            return;
        }
        int mr = inventoryTable.convertRowIndexToModel(vr);
        String pName = (String) tableModel.getValueAt(mr, 1);
        String bName = (String) tableModel.getValueAt(mr, 2);
        int pId = productNameToId.getOrDefault(pName, 0);
        int bId = binNameToId.getOrDefault(bName, 0);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity delta (e.g., -5 or 10):", "Adjust Stock: " + pName + " @ " + bName, JOptionPane.QUESTION_MESSAGE);
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int delta = Integer.parseInt(qtyStr.trim());
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                    svc.adjustStock(pId, bId, delta, userId, "Manual Adjustment");
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        statusLabel.setText("Stock adjusted.");
                        loadInventory();
                    } catch (Exception ex) {
                        statusLabel.setText("Failed: " + ex.getMessage());
                    }
                }
            }.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    private void doTransfer() {
        int vr = inventoryTable.getSelectedRow();
        if (vr < 0) {
            statusLabel.setText("Select an item to transfer.");
            return;
        }
        int mr = inventoryTable.convertRowIndexToModel(vr);
        String pName = (String) tableModel.getValueAt(mr, 1);
        String fromBName = (String) tableModel.getValueAt(mr, 2);
        int pId = productNameToId.getOrDefault(pName, 0);
        int fromBId = binNameToId.getOrDefault(fromBName, 0);
        int currentQty = (int) tableModel.getValueAt(mr, 3);

        if (binNameToId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bins available for transfer.");
            return;
        }

        // Build a combo box with all bin names except the current one
        String[] availableBins = binNameToId.keySet().stream()
                .filter(name -> !name.equals(fromBName))
                .sorted()
                .toArray(String[]::new);

        if (availableBins.length == 0) {
            JOptionPane.showMessageDialog(this, "No other bins available for transfer.");
            return;
        }

        JComboBox<String> toBinCombo = new JComboBox<>(availableBins);
        JTextField qtyF = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("From: " + pName + " @ " + fromBName + " (Qty: " + currentQty + ")"));
        p.add(new JLabel("To Bin:"));
        p.add(toBinCombo);
        p.add(new JLabel("Quantity to Transfer:"));
        p.add(qtyF);

        if (JOptionPane.showConfirmDialog(this, p, "Transfer Stock", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;

        try {
            int qty = Integer.parseInt(qtyF.getText().trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }
            if (qty > currentQty) {
                JOptionPane.showMessageDialog(this, "Cannot transfer more than available stock (" + currentQty + ").");
                return;
            }
            String selectedBinName = (String) toBinCombo.getSelectedItem();
            int toBinId = binNameToId.getOrDefault(selectedBinName, 0);
            if (toBinId == 0) {
                JOptionPane.showMessageDialog(this, "Invalid destination bin.");
                return;
            }

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                    svc.transferStock(pId, fromBId, toBinId, qty, userId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        statusLabel.setText("Stock transferred successfully.");
                        loadInventory();
                    } catch (Exception ex) {
                        statusLabel.setText("Failed: " + ex.getMessage());
                    }
                }
            }.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.");
        }
    }

    private void printReport() {
        statusLabel.setText("Generating report...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.warehousewms.service.ReportService rs = new com.warehousewms.service.ReportService();
                rs.generateInventoryReport();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Report opened in viewer.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to generate report: " + ex.getMessage());
                    JOptionPane.showMessageDialog(InventoryManagementFrame.this, "Error generating report:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new InventoryManagementFrame().setVisible(true));
    }
}
