package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Product;
import com.warehousewms.service.ProductService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ProductManagementFrame extends JFrame {

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
    private JTable productTable;
    private JLabel statusLabel;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "SKU", "Name", "Weight (kg)", "Volume (m\u00B3)", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    public ProductManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Products");
        setSize(900, 560);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Configure table
        productTable.setModel(tableModel);
        productTable.setRowSorter(sorter);
        ThemeConfig.styleTable(productTable);

        // Apply theme
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");

        ThemeConfig.styleButton(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER, "add");
        ThemeConfig.styleButton(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "edit");
        ThemeConfig.styleButton(deleteButton, ThemeConfig.DANGER, ThemeConfig.DANGER.brighter(), "delete");
        ThemeConfig.styleButton(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER, "refresh");

        // Search filter
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

        // Listeners
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> loadProducts());

        loadProducts();

        ThemeConfig.addHelpMenu(this, "Product Catalog Management\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "Before any inventory can be physically stored, purchased, or sold, it must exist in the Product Catalog. " +
            "This module acts as the master database for all SKU (Stock Keeping Unit) definitions. It defines what " +
            "an item is, its barcode, weight, and volume, which are critical for calculating shipping costs and bin capacities.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Search: Filter the catalog by SKU or Name to quickly locate an item.\n" +
            "• Add Product: Register a new SKU into the system so it can be procured and tracked.\n" +
            "• Edit/Delete: Maintain data accuracy by updating product dimensions or retiring obsolete items.");
    }

    

    private void loadProducts() {
        statusLabel.setText("Loading products...");
        new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                try (ProductService svc = new ProductService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.listAll();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Product> list = get();
                    tableModel.setRowCount(0);
                    for (Product p : list) {
                        tableModel.addRow(new Object[]{p.getProductId(), p.getSku(), p.getName(), p.getUnitWeightKg(), p.getUnitVolumeM3(), p.isActive() ? "Yes" : "No"});
                    }
                    statusLabel.setText("Loaded " + list.size() + " products.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load products: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addProduct() {
        Product result = showProductDialog(null);
        if (result == null) return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (ProductService svc = new ProductService(new DatabaseManager().getDataSourceWithFallback())) {
                    if (svc.skuExists(result.getSku())) return false;
                    svc.add(result);
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        statusLabel.setText("SKU already exists.");
                        return;
                    }
                    statusLabel.setText("Product created.");
                    loadProducts();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editProduct() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            statusLabel.setText("Select a product to edit.");
            return;
        }
        Product result = showProductDialog(selected);
        if (result == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (ProductService svc = new ProductService(new DatabaseManager().getDataSourceWithFallback())) {
                    if (!selected.getSku().equals(result.getSku()) && svc.skuExists(result.getSku()))
                        throw new IllegalArgumentException("SKU already exists.");
                    svc.update(result);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Product updated.");
                    loadProducts();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteProduct() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            statusLabel.setText("Select a product to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + selected.getName() + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (ProductService svc = new ProductService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.delete(selected.getProductId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean d = get();
                    statusLabel.setText(d ? "Deleted." : "Not found.");
                    if (d) loadProducts();
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Product getSelectedProduct() {
        int vr = productTable.getSelectedRow();
        if (vr < 0) return null;
        int mr = productTable.convertRowIndexToModel(vr);
        Product p = new Product();
        p.setProductId((int) tableModel.getValueAt(mr, 0));
        p.setSku((String) tableModel.getValueAt(mr, 1));
        p.setName((String) tableModel.getValueAt(mr, 2));
        p.setUnitWeightKg((double) tableModel.getValueAt(mr, 3));
        p.setUnitVolumeM3((double) tableModel.getValueAt(mr, 4));
        p.setActive("Yes".equals(tableModel.getValueAt(mr, 5)));
        return p;
    }

    private Product showProductDialog(Product existing) {
        JTextField skuF = new JTextField(), nameF = new JTextField();
        JTextField weightF = new JTextField(), volumeF = new JTextField();
        JCheckBox activeBox = new JCheckBox("Active", true);
        if (existing != null) {
            skuF.setText(existing.getSku());
            nameF.setText(existing.getName());
            weightF.setText(String.valueOf(existing.getUnitWeightKg()));
            volumeF.setText(String.valueOf(existing.getUnitVolumeM3()));
            activeBox.setSelected(existing.isActive());
        }
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(new JLabel("SKU"));
        p.add(skuF);
        p.add(new JLabel("Name"));
        p.add(nameF);
        p.add(new JLabel("Unit weight (kg)"));
        p.add(weightF);
        p.add(new JLabel("Unit volume (m\u00B3)"));
        p.add(volumeF);
        p.add(activeBox);
        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Add product" : "Edit product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;
        String sku = skuF.getText().trim(), name = nameF.getText().trim();
        if (sku.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "SKU and name required.");
            return null;
        }
        double weight, volume;
        try {
            weight = Double.parseDouble(weightF.getText().trim());
            volume = Double.parseDouble(volumeF.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid numbers.");
            return null;
        }
        Product product = existing != null ? existing : new Product();
        product.setSku(sku);
        product.setName(name);
        product.setImagePath(null);
        product.setUnitWeightKg(weight);
        product.setUnitVolumeM3(volume);
        product.setActive(activeBox.isSelected());
        return product;
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new ProductManagementFrame().setVisible(true));
    }
}
