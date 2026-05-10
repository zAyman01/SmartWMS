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
    private JScrollPane tableScrollPane;
    private JTable inventoryTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Inventory Id", "Product Id", "Bin Id", "Quantity"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

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

        applyButtonTheme(adjustButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);
        applyButtonTheme(transferButton, ThemeConfig.WARNING, ThemeConfig.WARNING.brighter());
        applyButtonTheme(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String t = searchField.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        adjustButton.addActionListener(e -> doAdjust());
        transferButton.addActionListener(e -> doTransfer());
        refreshButton.addActionListener(e -> loadInventory());

        loadInventory();
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

    private void loadInventory() {
        statusLabel.setText("Loading Inventory...");
        new SwingWorker<List<Inventory>, Void>() {
            @Override protected List<Inventory> doInBackground() throws Exception {
                InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                return svc.getAllInventory();
            }
            @Override protected void done() {
                try {
                    List<Inventory> list = get();
                    tableModel.setRowCount(0);
                    for (Inventory inv : list) {
                        tableModel.addRow(new Object[]{inv.getInventoryId(), inv.getProductId(),
                                inv.getBinId(), inv.getQuantity()});
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
        if (vr < 0) { statusLabel.setText("Select an item to adjust."); return; }
        int mr = inventoryTable.convertRowIndexToModel(vr);
        int pId = (int) tableModel.getValueAt(mr, 1);
        int bId = (int) tableModel.getValueAt(mr, 2);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity delta (e.g., -5 or 10):", "Adjust Stock", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int delta = Integer.parseInt(qtyStr.trim());
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                    svc.adjustStock(pId, bId, delta, userId, "Manual Adjustment");
                    return null;
                }
                @Override protected void done() {
                    try { get(); statusLabel.setText("Stock adjusted."); loadInventory(); }
                    catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                }
            }.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    private void doTransfer() {
        int vr = inventoryTable.getSelectedRow();
        if (vr < 0) { statusLabel.setText("Select an item to transfer."); return; }
        int mr = inventoryTable.convertRowIndexToModel(vr);
        int pId = (int) tableModel.getValueAt(mr, 1);
        int fromBId = (int) tableModel.getValueAt(mr, 2);

        JTextField toBinF = new JTextField();
        JTextField qtyF = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("To Bin Id:")); p.add(toBinF);
        p.add(new JLabel("Quantity to Transfer:")); p.add(qtyF);

        if (JOptionPane.showConfirmDialog(this, p, "Transfer Stock", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            int toBinId = Integer.parseInt(toBinF.getText().trim());
            int qty = Integer.parseInt(qtyF.getText().trim());
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    InventoryService svc = new InventoryService(new DatabaseManager().getDataSourceWithFallback());
                    svc.transferStock(pId, fromBId, toBinId, qty, userId);
                    return null;
                }
                @Override protected void done() {
                    try { get(); statusLabel.setText("Stock transferred."); loadInventory(); }
                    catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                }
            }.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }
}
