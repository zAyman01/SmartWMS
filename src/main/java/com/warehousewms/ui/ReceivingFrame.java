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

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"PO Line Id", "Product Id", "Ordered", "Received", "To Receive (Input)"}, 0) {
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
        poIdField.putClientProperty("JTextField.placeholderText", "PO ID");

        applyButtonTheme(fetchButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);
        applyButtonTheme(receiveButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);

        scannerPanel = new BarcodeScannerPanel();
        scannerPanel.setParentFrame(this);
        mainPanel.add(scannerPanel, BorderLayout.NORTH);
        scannerPanel.setScanListener(this::onBarcodeScan);

        fetchButton.addActionListener(e -> fetchLines());
        receiveButton.addActionListener(e -> receiveSelected());
    }

    private void onBarcodeScan(com.warehousewms.model.Product product) {
        int pid = product.getProductId();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int rowPid = (int) tableModel.getValueAt(i, 1);
            if (rowPid == pid) {
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

    private void applyButtonTheme(JButton btn, Color bg, Color hover) {
        btn.setFont(ThemeConfig.FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(bg.equals(ThemeConfig.BG_CARD) ? ThemeConfig.TEXT_PRIMARY : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void fetchLines() {
        String poIdStr = poIdField.getText().trim();
        if (poIdStr.isEmpty()) return;
        try {
            int poId = Integer.parseInt(poIdStr);
            statusLabel.setText("Fetching...");
            new SwingWorker<List<PurchaseOrderLine>, Void>() {
                @Override protected List<PurchaseOrderLine> doInBackground() throws Exception {
                    PurchaseOrderService svc = new PurchaseOrderService(new DatabaseManager().getDataSourceWithFallback());
                    return svc.getLinesForPO(poId);
                }
                @Override protected void done() {
                    try {
                        List<PurchaseOrderLine> lines = get();
                        tableModel.setRowCount(0);
                        for (PurchaseOrderLine l : lines) {
                            tableModel.addRow(new Object[]{
                                    l.getPoLineId(), l.getProductId(), l.getQuantityOrdered(), l.getQuantityReceived(), 0
                            });
                        }
                        statusLabel.setText("Fetched " + lines.size() + " lines.");
                    } catch (Exception ex) {
                        statusLabel.setText("Failed: " + ex.getMessage());
                    }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid PO Id");
        }
    }

    private void receiveSelected() {
        if (linesTable.isEditing()) linesTable.getCellEditor().stopCellEditing();
        String poIdStr = poIdField.getText().trim();
        if (poIdStr.isEmpty()) return;
        
        int poId = Integer.parseInt(poIdStr);
        String binIdStr = JOptionPane.showInputDialog(this, "Enter Bin ID to receive into:", "Bin Selection", JOptionPane.QUESTION_MESSAGE);
        if (binIdStr == null || binIdStr.trim().isEmpty()) return;
        
        try {
            int binId = Integer.parseInt(binIdStr.trim());
            List<ReceiptLine> receipts = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int toReceive = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                if (toReceive > 0) {
                    ReceiptLine rl = new ReceiptLine();
                    rl.setProductId((int) tableModel.getValueAt(i, 1));
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
            JOptionPane.showMessageDialog(this, "Invalid Bin Id or Quantity");
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new ReceivingFrame().setVisible(true));
    }
}
