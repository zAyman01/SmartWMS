package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.PurchaseOrderLine;
import com.warehousewms.model.ReceiptLine;
import com.warehousewms.service.PurchaseOrderService;
import com.warehousewms.service.ReceivingService;
import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReceivingFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField poIdField;
    private JPanel toolbarPanel;
    private JButton fetchButton;
    private JButton receiveButton;
    private JScrollPane tableScrollPane;
    private JTable linesTable;
    private JLabel statusLabel;
    private BarcodeScannerPanel scannerPanel;
    private final java.util.Map<String, Integer> productNameToId = new java.util.HashMap<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Product Name", "Ordered", "Received", "To Receive (Input)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }
    };

    public ReceivingFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Receiving");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        linesTable.setModel(tableModel);
        ThemeConfig.styleTable(linesTable);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        poIdField.putClientProperty("JTextField.placeholderText", "Enter PO #");

        ThemeConfig.styleButton(fetchButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "fetch");
        ThemeConfig.styleButton(receiveButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "check");

        scannerPanel = new BarcodeScannerPanel();
        scannerPanel.setParentFrame(this);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.remove(mainPanel);
        rootPanel.add(scannerPanel, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);
        scannerPanel.setScanListener(this::onBarcodeScan);

        fetchButton.addActionListener(e -> fetchLines());
        receiveButton.addActionListener(e -> receiveSelected());

        ThemeConfig.addHelpMenu(this, "Receiving Dock Operations\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Receiving is the critical process of accepting physical goods into the warehouse. When a truck arrives, " +
            "the receiving clerk must match the physical items against the original Purchase Order. This module acts as " +
            "the gatekeeper, turning expected inbound shipments into real, sellable on-hand inventory.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Fetch PO: Enter the ID of the Purchase Order that the truck is delivering against to load the expected items.\n" +
            "• Scan & Verify: Use a barcode scanner to scan incoming items. This ensures you are receiving the correct SKUs.\n" +
            "• Putaway: Once quantities are verified in the 'To Receive' column, click 'Receive Items' to officially " +
            "inject the stock into a specific warehouse Bin. This updates the ledger instantly.");
    }

    private void onBarcodeScan(com.warehousewms.model.Product product) {
        int pid = product.getProductId();
        String pName = product.getName();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowPName = (String) tableModel.getValueAt(i, 1);
            if (pName.equals(rowPName)) {
                int ordered = (int) tableModel.getValueAt(i, 2);
                int received = (int) tableModel.getValueAt(i, 3);
                int remaining = ordered - received;
                if (remaining > 0) {
                    tableModel.setValueAt(remaining, i, 4);
                    statusLabel.setText("Scanned: " + product.getSku() + " \u2013 set Qty " + remaining);
                } else {
                    statusLabel.setText("Scanned: " + product.getSku() + " (already fully received)");
                }
                return;
            }
        }
        statusLabel.setText("Scanned product not in this PO: " + product.getSku());
    }

    

    private void fetchLines() {
        String poIdStr = poIdField.getText().trim();
        if (poIdStr.isEmpty()) return;
        try {
            int poId = Integer.parseInt(poIdStr);
            statusLabel.setText("Fetching...");
            new SwingWorker<List<PurchaseOrderLine>, Void>() {
                private java.util.Map<Integer, String> productNames = new java.util.HashMap<>();

                @Override protected List<PurchaseOrderLine> doInBackground() throws Exception {
                    PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                    com.warehousewms.service.ProductService ps = new com.warehousewms.service.ProductService(new DatabaseManager().getDataSourceWithFallback());
                    productNameToId.clear();
                    for(com.warehousewms.model.Product p : ps.listAll()) {
                        productNames.put(p.getProductId(), p.getName());
                        productNameToId.put(p.getName(), p.getProductId());
                    }
                    return svc.getLinesForPO(poId);
                }
                @Override protected void done() {
                    try {
                        List<PurchaseOrderLine> lines = get();
                        tableModel.setRowCount(0);
                        for (PurchaseOrderLine l : lines) {
                            String pName = productNames.getOrDefault(l.getProductId(), "Unknown");
                            tableModel.addRow(new Object[]{
                                    l.getPoLineId(), pName, l.getQuantityOrdered(), l.getQuantityReceived(), 0
                            });
                        }
                        statusLabel.setText("Fetched " + lines.size() + " lines.");
                    } catch (Exception ex) {
                        statusLabel.setText("Failed: " + ex.getMessage());
                    }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid PO number");
        }
    }

    private void receiveSelected() {
        if (linesTable.isEditing()) linesTable.getCellEditor().stopCellEditing();
        String poIdStr = poIdField.getText().trim();
        if (poIdStr.isEmpty()) return;
        
        int poId;
        try {
            poId = Integer.parseInt(poIdStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid PO number.");
            return;
        }

        // Load available bins for selection
        String[] binNames;
        java.util.Map<String, Integer> binLookup = new java.util.HashMap<>();
        try {
            com.warehousewms.service.BinService bs = new com.warehousewms.service.BinService(new DatabaseManager().getDataSourceWithFallback());
            for (com.warehousewms.model.Bin b : bs.listAll()) {
                binLookup.put(b.getName(), b.getBinId());
            }
            binNames = binLookup.keySet().stream().sorted().toArray(String[]::new);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load bins: " + ex.getMessage());
            return;
        }

        if (binNames.length == 0) {
            JOptionPane.showMessageDialog(this, "No bins available. Please create bins first.");
            return;
        }

        JComboBox<String> binCombo = new JComboBox<>(binNames);
        JPanel binPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        binPanel.add(new JLabel("Receive into Bin:"));
        binPanel.add(binCombo);
        if (JOptionPane.showConfirmDialog(this, binPanel, "Bin Selection", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;

        String selectedBinName = (String) binCombo.getSelectedItem();
        int binId = binLookup.getOrDefault(selectedBinName, 0);
        if (binId == 0) {
            JOptionPane.showMessageDialog(this, "Invalid bin selection.");
            return;
        }
        
        try {
            List<ReceiptLine> receipts = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int toReceive = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                if (toReceive > 0) {
                    String rlPName = (String) tableModel.getValueAt(i, 1);
                    ReceiptLine rl = new ReceiptLine();
                    rl.setProductId(productNameToId.getOrDefault(rlPName, 0));
                    rl.setBinId(binId);
                    rl.setQuantity(toReceive);
                    receipts.add(rl);
                }
            }

            if (receipts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No quantities entered to receive.");
                return;
            }

            statusLabel.setText("Receiving...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    ReceivingService svc = new ReceivingService(new DatabaseManager().getDataSourceWithFallback());
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    svc.receivePO(poId, receipts, "Received via UI", userId);
                    return null;
                }
                @Override protected void done() {
                    try { get(); statusLabel.setText("Received successfully."); fetchLines(); }
                    catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                }
            }.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity value in table.");
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new ReceivingFrame().setVisible(true));
    }
}
