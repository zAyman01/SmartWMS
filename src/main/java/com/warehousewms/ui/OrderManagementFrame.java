package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Order;
import com.warehousewms.service.OrderService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class OrderManagementFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JTextField searchField;
    private JPanel toolbarPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton refreshButton;
    private JScrollPane tableScrollPane;
    private JTable orderTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Customer Name", "Date", "Status", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final java.util.Map<Integer, String> customerNames = new java.util.HashMap<>();
    private java.util.List<Order> loadedOrders = new java.util.ArrayList<>();

    public OrderManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Customer Orders");
        setSize(850, 530);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        orderTable.setModel(tableModel);
        orderTable.setRowSorter(sorter);
        ThemeConfig.styleTable(orderTable);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search Orders...");

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

        addButton.addActionListener(e -> addOrder());
        editButton.addActionListener(e -> editOrder());
        refreshButton.addActionListener(e -> loadOrders());

        loadOrders();

        ThemeConfig.addHelpMenu(this, "Sales Orders (Outbound)\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Customer Orders represent the demand side of your warehouse. When a customer makes a purchase, a Sales Order " +
            "is generated. This document dictates what needs to be picked, packed, and shipped out of the facility. " +
            "It drives the entire outbound logistics workflow.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Create Order: Start a new order by selecting the destination Customer and setting shipping priorities.\n" +
            "• Manage Lines: Specify the products and exact quantities the customer has requested.\n" +
            "• Next Steps: Once the order is complete, it is handed over to the 'Fulfillment & Picking' module, " +
            "where warehouse staff will physically locate and pack the requested stock.");
    }

    

    private void loadOrders() {
        statusLabel.setText("Loading Orders...");
        new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() throws Exception {
                OrderService svc = new OrderService(new DatabaseManager().getDataSourceWithFallback());
                com.warehousewms.service.CustomerService cs = new com.warehousewms.service.CustomerService(new DatabaseManager().getDataSourceWithFallback());
                customerNames.clear();
                for(com.warehousewms.model.Customer c : cs.listAll()) customerNames.put(c.getCustomerId(), c.getName());
                return svc.getAllOrders();
            }

            @Override
            protected void done() {
                try {
                    List<Order> list = get();
                    loadedOrders = list;
                    tableModel.setRowCount(0);
                    for (Order o : list) {
                        String cName = customerNames.getOrDefault(o.getCustomerId(), "Unknown");
                        tableModel.addRow(new Object[]{o.getOrderId(), cName, o.getOrderDate(), o.getStatus(), o.getNotes()});
                    }
                    statusLabel.setText("Loaded " + list.size() + " orders.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addOrder() {
        Order result = showOrderDialog(null);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                OrderService svc = new OrderService(new DatabaseManager().getDataSourceWithFallback());
                svc.createOrder(result, new ArrayList<>());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Order created.");
                    loadOrders();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editOrder() {
        Order selected = getSelectedOrder();
        if (selected == null) {
            statusLabel.setText("Select an Order.");
            return;
        }
        Order result = showOrderDialog(selected);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                OrderService svc = new OrderService(new DatabaseManager().getDataSourceWithFallback());
                svc.updateOrder(result, new ArrayList<>());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Order updated.");
                    loadOrders();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Order getSelectedOrder() {
        int vr = orderTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = orderTable.convertRowIndexToModel(vr);
        if (mr < loadedOrders.size()) return loadedOrders.get(mr);
        Order o = new Order();
        o.setOrderDate(new Date());
        return o;
    }

    private Order showOrderDialog(Order existing) {
        // Build customer name array from loaded map
        String[] customerNamesList = customerNames.values().stream().sorted().toArray(String[]::new);
        JComboBox<String> custCombo = new JComboBox<>(customerNamesList);
        JTextField statusF = new JTextField(), notesF = new JTextField();
        if (existing != null) {
            String existingCustName = customerNames.getOrDefault(existing.getCustomerId(), "");
            custCombo.setSelectedItem(existingCustName);
            statusF.setText(existing.getStatus());
            notesF.setText(existing.getNotes());
        } else {
            statusF.setText("Pending");
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("Customer"));
        p.add(custCombo);
        p.add(new JLabel("Status"));
        p.add(statusF);
        p.add(new JLabel("Notes"));
        p.add(notesF);
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Create Order" : "Edit Order", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;

        try {
            String selectedCustName = (String) custCombo.getSelectedItem();
            if (selectedCustName == null || selectedCustName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a customer.");
                return null;
            }
            // Reverse lookup: find customer ID from name
            int custId = 0;
            for (var entry : customerNames.entrySet()) {
                if (entry.getValue().equals(selectedCustName)) {
                    custId = entry.getKey();
                    break;
                }
            }
            if (custId == 0) {
                JOptionPane.showMessageDialog(this, "Customer not found. Please create the customer first.");
                return null;
            }
            Order o = existing != null ? existing : new Order();
            o.setCustomerId(custId);
            o.setStatus(statusF.getText().trim());
            o.setNotes(notesF.getText().trim());
            if (existing == null) o.setOrderDate(new Date());
            return o;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
            return null;
        }
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new OrderManagementFrame().setVisible(true));
    }
}
