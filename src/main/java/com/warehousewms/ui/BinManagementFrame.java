package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Bin;
import com.warehousewms.service.BinService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class BinManagementFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JPanel toolbarPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JScrollPane treeScrollPane;
    private JTree binTree;
    private JLabel statusLabel;

    private static final String[] BIN_TYPES = {"Zone", "Aisle", "Rack", "Shelf", "Location"};
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Warehouse");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

    public BinManagementFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Bins / Locations");
        setSize(800, 540);
        setMinimumSize(new Dimension(650, 440));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Configure tree
        binTree.setModel(treeModel);
        binTree.setRootVisible(true);
        binTree.setShowsRootHandles(true);
        binTree.setRowHeight(32);
        binTree.setFont(ThemeConfig.FONT_BODY);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(ThemeConfig.BG_SECONDARY);
        renderer.setBackgroundSelectionColor(ThemeConfig.ACCENT);
        renderer.setTextNonSelectionColor(ThemeConfig.TEXT_PRIMARY);
        renderer.setTextSelectionColor(Color.WHITE);
        renderer.setBorderSelectionColor(ThemeConfig.ACCENT);
        binTree.setCellRenderer(renderer);

        // Apply theme
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        toolbarPanel.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);
        treeScrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.BORDER));
        treeScrollPane.getViewport().setBackground(ThemeConfig.BG_SECONDARY);
        binTree.setBackground(ThemeConfig.BG_SECONDARY);

        applyButtonTheme(addButton, ThemeConfig.ACCENT, ThemeConfig.ACCENT_HOVER);
        applyButtonTheme(editButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);
        applyButtonTheme(deleteButton, ThemeConfig.DANGER, ThemeConfig.DANGER.brighter());
        applyButtonTheme(refreshButton, ThemeConfig.BG_CARD, ThemeConfig.BG_HOVER);

        // Listeners
        addButton.addActionListener(e -> addBin());
        editButton.addActionListener(e -> editBin());
        deleteButton.addActionListener(e -> deleteBin());
        refreshButton.addActionListener(e -> loadBins());

        loadBins();
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

    private void loadBins() {
        statusLabel.setText("Loading bins...");
        new SwingWorker<List<Bin>, Void>() {
            @Override
            protected List<Bin> doInBackground() throws Exception {
                try (BinService svc = new BinService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.listAll();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Bin> bins = get();
                    rootNode.removeAllChildren();
                    buildTreeNodes(rootNode, null, bins);
                    treeModel.reload();
                    expandAllNodes();
                    statusLabel.setText("Loaded " + bins.size() + " bins.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load bins: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void buildTreeNodes(DefaultMutableTreeNode parent, Integer parentId, List<Bin> allBins) {
        for (Bin bin : allBins) {
            if (parentId == null && bin.getParentBinId() == null || parentId != null && bin.getParentBinId() != null && bin.getParentBinId().equals(parentId)) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(bin);
                parent.add(node);
                buildTreeNodes(node, bin.getBinId(), allBins);
            }
        }
    }

    private void expandAllNodes() {
        for (int i = 0; i < binTree.getRowCount(); i++) {
            binTree.expandRow(i);
        }
    }

    private Bin getSelectedBin() {
        TreePath path = binTree.getSelectionPath();
        if (path == null) return null;
        Object comp = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        return comp instanceof Bin ? (Bin) comp : null;
    }

    private DefaultMutableTreeNode getSelectedTreeNode() {
        TreePath path = binTree.getSelectionPath();
        if (path == null) return null;
        return (DefaultMutableTreeNode) path.getLastPathComponent();
    }

    private void addBin() {
        DefaultMutableTreeNode parentNode = getSelectedTreeNode();
        Bin parentBin = parentNode != null && parentNode.getUserObject() instanceof Bin ? (Bin) parentNode.getUserObject() : null;
        Bin bin = showBinDialog(null, parentBin);
        if (bin == null) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (BinService svc = new BinService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.add(bin);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Bin created.");
                    loadBins();
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void editBin() {
        Bin selected = getSelectedBin();
        if (selected == null) {
            statusLabel.setText("Select a bin.");
            return;
        }
        Bin bin = showBinDialog(selected, null);
        if (bin == null) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (BinService svc = new BinService(new DatabaseManager().getDataSourceWithFallback())) {
                    svc.update(bin);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Bin updated.");
                    loadBins();
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteBin() {
        Bin selected = getSelectedBin();
        if (selected == null) {
            statusLabel.setText("Select a bin.");
            return;
        }
        DefaultMutableTreeNode node = getSelectedTreeNode();
        if (node != null && node.getChildCount() > 0) {
            statusLabel.setText("Cannot delete a bin with children.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete '" + selected.getName() + "'?", "Confirm delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (BinService svc = new BinService(new DatabaseManager().getDataSourceWithFallback())) {
                    return svc.delete(selected.getBinId());
                }
            }

            @Override
            protected void done() {
                try {
                    boolean d = get();
                    statusLabel.setText(d ? "Deleted." : "Not found.");
                    if (d) loadBins();
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private Bin showBinDialog(Bin existing, Bin parent) {
        JTextField nameF = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(BIN_TYPES);
        JTextField weightF = new JTextField(), volumeF = new JTextField(), sortF = new JTextField("0");

        if (existing != null) {
            nameF.setText(existing.getName());
            typeBox.setSelectedItem(existing.getBinType());
            weightF.setText(String.valueOf(existing.getMaxWeightKg()));
            volumeF.setText(String.valueOf(existing.getMaxVolumeM3()));
            sortF.setText(String.valueOf(existing.getSortOrder()));
        }

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        if (parent != null) p.add(new JLabel("Parent: " + parent.getName()));
        else if (existing == null) p.add(new JLabel("Parent: (root)"));
        p.add(new JLabel("Name"));
        p.add(nameF);
        p.add(new JLabel("Bin type"));
        p.add(typeBox);
        p.add(new JLabel("Max weight (kg)"));
        p.add(weightF);
        p.add(new JLabel("Max volume (m\u00B3)"));
        p.add(volumeF);
        p.add(new JLabel("Sort order"));
        p.add(sortF);

        if (JOptionPane.showConfirmDialog(this, p, existing == null ? "Add bin" : "Edit bin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return null;

        String name = nameF.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return null;
        }

        double weight, volume;
        int sortOrder;
        try {
            weight = Double.parseDouble(weightF.getText().trim());
            volume = Double.parseDouble(volumeF.getText().trim());
            sortOrder = Integer.parseInt(sortF.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid numbers.");
            return null;
        }

        Bin bin = existing != null ? existing : new Bin();
        bin.setName(name);
        bin.setBinType((String) typeBox.getSelectedItem());
        bin.setMaxWeightKg(weight);
        bin.setMaxVolumeM3(volume);
        bin.setSortOrder(sortOrder);
        if (existing == null) bin.setParentBinId(parent != null ? parent.getBinId() : null);
        return bin;
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new BinManagementFrame().setVisible(true));
    }
}
