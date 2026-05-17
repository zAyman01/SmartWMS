package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.OrderLine;
import com.warehousewms.model.PickRunItem;
import com.warehousewms.repository.OrderRepository;
import com.warehousewms.repository.PickRunRepository;
import com.warehousewms.service.FulfillmentService;
import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FulfillmentFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField orderIdField;
    private JButton createRunButton;
    private JTextField pickRunIdField;
    private JButton completePickButton;
    private JPanel toolbarPanel;
    private JScrollPane tableScrollPane;
    private JTable pickTable;
    private JLabel statusLabel;
    private BarcodeScannerPanel scannerPanel;

    private final java.util.List<PickRunItem> currentItems = new java.util.ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Product Name", "Bin Name", "To Pick", "Picked (Input)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }
    };

    public FulfillmentFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Fulfillment");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pickTable.setModel(tableModel);
        ThemeConfig.styleTable(pickTable);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        orderIdField.putClientProperty("JTextField.placeholderText", "Enter Order #");
        pickRunIdField.putClientProperty("JTextField.placeholderText", "Enter Pick Run #");

        ThemeConfig.styleButton(createRunButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "add");
        ThemeConfig.styleButton(completePickButton, ThemeConfig.SUCCESS, ThemeConfig.SUCCESS.brighter(), "check");

        scannerPanel = new BarcodeScannerPanel();
        scannerPanel.setParentFrame(this);
        rootPanel.setLayout(new BorderLayout());
        rootPanel.remove(mainPanel);
        rootPanel.add(scannerPanel, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);
        scannerPanel.setScanListener(this::onBarcodeScan);

        createRunButton.addActionListener(e -> createPickRun());
        completePickButton.addActionListener(e -> completePickRun());

        ThemeConfig.addHelpMenu(this, "Fulfillment & Picking\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Fulfillment is the physical process of locating and retrieving items from the warehouse shelves (Bins) " +
            "to satisfy a Customer Order. It ensures that the exact right items are packed to avoid expensive returns " +
            "and customer dissatisfaction.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Create Pick Run: Enter a Customer Order ID to automatically generate a picking list of items required.\n" +
            "• Execute Pick: Load the Pick Run ID, walk the warehouse floor, and scan items as you pull them from bins.\n" +
            "• Complete: Once the 'Picked' column matches the required amounts, click Complete to deduct the inventory " +
            "and mark the order as fulfilled.");
    }

    private void onBarcodeScan(com.warehousewms.model.Product product) {
        if (tableModel.getRowCount() == 0) {
            statusLabel.setText("No pick items loaded. Load a Pick Run first.");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.warehousewms.repository.OrderRepository oRepo =
                        new com.warehousewms.repository.OrderRepository(
                                new com.warehousewms.config.DatabaseManager().getDataSourceWithFallback());
                int pid = product.getProductId();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (i >= currentItems.size()) break;
                    PickRunItem item = currentItems.get(i);
                    if (item == null) continue;
                    com.warehousewms.model.OrderLine ol = oRepo.findOrderLineById(item.getOrderLineId());
                    if (ol != null && ol.getProductId() == pid) {
                        int toPick = (int) tableModel.getValueAt(i, 3);
                        int alreadyPicked = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                        int remaining = toPick - alreadyPicked;
                        if (remaining > 0) {
                            final int row = i;
                            SwingUtilities.invokeLater(() -> {
                                tableModel.setValueAt(toPick, row, 4);
                                statusLabel.setText("Scanned: " + product.getSku() + " \u2013 set Qty " + toPick);
                            });
                        }
                        return null;
                    }
                }
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Product " + product.getSku() + " not found in this Pick Run"));
                return null;
            }
        }.execute();
    }

    

    private void createPickRun() {
        String oIdStr = orderIdField.getText().trim();
        if (oIdStr.isEmpty()) {
            statusLabel.setText("Enter an Order # first.");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(oIdStr);

            // Load available bins for selection
            java.util.Map<String, Integer> binLookup = new java.util.LinkedHashMap<>();
            try {
                com.warehousewms.service.BinService bs = new com.warehousewms.service.BinService(
                        new com.warehousewms.config.DatabaseManager().getDataSourceWithFallback());
                for (com.warehousewms.model.Bin b : bs.listAll()) {
                    binLookup.put(b.getName(), b.getBinId());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load bins: " + ex.getMessage());
                return;
            }

            if (binLookup.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No bins available. Please create bins first.");
                return;
            }

            String[] binNameArray = binLookup.keySet().stream().sorted().toArray(String[]::new);
            JComboBox<String> binCombo = new JComboBox<>(binNameArray);
            JPanel binPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 4));
            binPanel.add(new JLabel("Pick from Bin:"));
            binPanel.add(binCombo);
            if (JOptionPane.showConfirmDialog(this, binPanel, "Bin Selection", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                return;

            String selectedBinName = (String) binCombo.getSelectedItem();
            int binId = binLookup.getOrDefault(selectedBinName, 0);
            if (binId == 0) {
                JOptionPane.showMessageDialog(this, "Invalid bin selection.");
                return;
            }

            statusLabel.setText("Creating Pick Run...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    OrderRepository oRepo = new OrderRepository(new DatabaseManager().getDataSourceWithFallback());
                    List<OrderLine> lines = oRepo.findLinesByOrderId(orderId);
                    List<PickRunItem> itemsToPick = new ArrayList<>();
                    
                    for (OrderLine l : lines) {
                        PickRunItem pi = new PickRunItem();
                        pi.setOrderLineId(l.getOrderLineId());
                        pi.setBinId(binId);
                        pi.setQuantityToPick(l.getQuantityOrdered() - l.getQuantityPicked());
                        pi.setQuantityPicked(0);
                        if (pi.getQuantityToPick() > 0) itemsToPick.add(pi);
                    }

                    if (itemsToPick.isEmpty()) throw new Exception("No items left to pick for order.");

                    FulfillmentService svc = new FulfillmentService(new DatabaseManager().getDataSourceWithFallback());
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    svc.createPickRun(orderId, userId, itemsToPick);
                    return null;
                }
                @Override protected void done() {
                    try { get(); statusLabel.setText("Pick Run created."); }
                    catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Order #. Please enter a number.");
        }
    }

    private void completePickRun() {
        if (pickTable.isEditing()) pickTable.getCellEditor().stopCellEditing();
        String prIdStr = pickRunIdField.getText().trim();
        if (prIdStr.isEmpty()) {
            // First load it
            String loadIdStr = JOptionPane.showInputDialog(this, "Enter Pick Run # to load:", "Load Pick Run", JOptionPane.QUESTION_MESSAGE);
            if (loadIdStr == null || loadIdStr.trim().isEmpty()) return;
            try {
                int prId = Integer.parseInt(loadIdStr.trim());
                pickRunIdField.setText(String.valueOf(prId));
                statusLabel.setText("Loading items...");
                new SwingWorker<List<PickRunItem>, Void>() {
                    private java.util.Map<Integer, String> productNames = new java.util.HashMap<>();
                    private java.util.Map<Integer, String> binNames = new java.util.HashMap<>();

                    @Override protected List<PickRunItem> doInBackground() throws Exception {
                        PickRunRepository repo = new PickRunRepository(new DatabaseManager().getDataSourceWithFallback());
                        List<PickRunItem> items = repo.findItemsByPickRunId(prId);
                        OrderRepository oRepo = new OrderRepository(new DatabaseManager().getDataSourceWithFallback());
                        com.warehousewms.service.ProductService ps = new com.warehousewms.service.ProductService(new DatabaseManager().getDataSourceWithFallback());
                        com.warehousewms.service.BinService bs = new com.warehousewms.service.BinService(new DatabaseManager().getDataSourceWithFallback());
                        java.util.Map<Integer, com.warehousewms.model.Product> pMap = new java.util.HashMap<>();
                        for(com.warehousewms.model.Product p : ps.listAll()) pMap.put(p.getProductId(), p);
                        for(com.warehousewms.model.Bin b : bs.listAll()) binNames.put(b.getBinId(), b.getName());

                        for (PickRunItem item : items) {
                            com.warehousewms.model.OrderLine ol = oRepo.findOrderLineById(item.getOrderLineId());
                            if (ol != null && pMap.containsKey(ol.getProductId())) {
                                productNames.put(item.getPickRunItemId(), pMap.get(ol.getProductId()).getName());
                            }
                        }
                        return items;
                    }
                    @Override protected void done() {
                        try {
                            List<PickRunItem> items = get();
                            tableModel.setRowCount(0);
                            currentItems.clear();
                            for (PickRunItem it : items) {
                                currentItems.add(it);
                                String pName = productNames.getOrDefault(it.getPickRunItemId(), "Unknown");
                                String bName = binNames.getOrDefault(it.getBinId(), "Unknown");
                                tableModel.addRow(new Object[]{it.getPickRunItemId(), pName, bName, it.getQuantityToPick(), 0});
                            }
                            statusLabel.setText("Loaded " + items.size() + " items. Enter picked quantities and click Complete.");
                        } catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                    }
                }.execute();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid number"); }
            return;
        }

        try {
            int prId = Integer.parseInt(prIdStr);
            List<PickRunItem> pickedItems = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int picked = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                if (picked > 0 && i < currentItems.size()) {
                    PickRunItem original = currentItems.get(i);
                    PickRunItem it = new PickRunItem();
                    it.setPickRunItemId(original.getPickRunItemId());
                    it.setOrderLineId(original.getOrderLineId());
                    it.setBinId(original.getBinId());
                    it.setQuantityPicked(picked);
                    pickedItems.add(it);
                }
            }

            statusLabel.setText("Completing Pick Run...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    FulfillmentService svc = new FulfillmentService(new DatabaseManager().getDataSourceWithFallback());
                    int userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getUserId() : 1;
                    svc.completePick(prId, pickedItems, userId);
                    return null;
                }
                @Override protected void done() {
                    try { get(); statusLabel.setText("Pick Run completed successfully."); tableModel.setRowCount(0); }
                    catch (Exception ex) { statusLabel.setText("Failed: " + ex.getMessage()); }
                }
            }.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in processing completion.");
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new FulfillmentFrame().setVisible(true));
    }
}

