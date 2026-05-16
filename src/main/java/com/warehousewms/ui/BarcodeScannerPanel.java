package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import com.warehousewms.model.Product;
import com.warehousewms.service.ProductService;
import com.warehousewms.util.CameraSupport;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class BarcodeScannerPanel extends JPanel {
    private final JTextField scanField;
    private final JLabel iconLabel;
    private final JLabel productLabel;
    private final JLabel statusDot;
    private final JButton cameraButton;
    private JFrame parentFrame;
    private Product lastScannedProduct;
    private BarcodeScanListener listener;

    @FunctionalInterface
    public interface BarcodeScanListener {
        void onProductScanned(Product product);
    }

    public BarcodeScannerPanel() {
        setLayout(new BorderLayout(8, 0));
        setBackground(ThemeConfig.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        iconLabel = new JLabel("\uD83D\uDCF7");
        iconLabel.setFont(ThemeConfig.FONT_HEADING);
        iconLabel.setForeground(ThemeConfig.TEXT_MUTED);

        scanField = new JTextField();
        scanField.putClientProperty("JTextField.placeholderText", "Type or scan barcode...");
        scanField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        scanField.setForeground(ThemeConfig.TEXT_PRIMARY);
        scanField.setBackground(ThemeConfig.BG_SECONDARY);
        scanField.setCaretColor(ThemeConfig.ACCENT);
        scanField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        scanField.addActionListener(this::onScan);
        scanField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                scanField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeConfig.ACCENT),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                scanField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeConfig.BORDER),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });

        cameraButton = new JButton("\uD83D\uDCF7");
        cameraButton.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        cameraButton.setBackground(ThemeConfig.BG_SECONDARY);
        cameraButton.setForeground(ThemeConfig.TEXT_PRIMARY);
        cameraButton.setFocusPainted(false);
        cameraButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cameraButton.setToolTipText("Scan with camera");
        cameraButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        cameraButton.addActionListener(e -> openCameraScanner());
        cameraButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                cameraButton.setBackground(ThemeConfig.BG_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                cameraButton.setBackground(ThemeConfig.BG_SECONDARY);
            }
        });

        productLabel = new JLabel(" ");
        productLabel.setFont(ThemeConfig.FONT_BODY);
        productLabel.setForeground(ThemeConfig.TEXT_MUTED);

        statusDot = new JLabel("\u25CF");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusDot.setForeground(ThemeConfig.TEXT_MUTED);

        JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(iconLabel, BorderLayout.WEST);
        leftPanel.add(scanField, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(leftPanel, BorderLayout.CENTER);
        centerPanel.add(productLabel, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout(4, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(cameraButton, BorderLayout.CENTER);
        rightPanel.add(statusDot, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setPreferredSize(new Dimension(0, 52));

        if (!CameraSupport.isCameraAvailable()) {
            cameraButton.setVisible(false);
            cameraButton.setEnabled(false);
        }
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    private void openCameraScanner() {
        JFrame owner = parentFrame;
        if (owner == null) {
            owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        }
        CameraScannerDialog dialog = new CameraScannerDialog(owner);
        dialog.setVisible(true);

        String barcode = dialog.getDecodedBarcode();
        if (barcode != null && !barcode.isEmpty()) {
            scanField.setText(barcode);
            onScan(null);
        }
    }

    public void setScanListener(BarcodeScanListener listener) {
        this.listener = listener;
    }

    public Product getLastScannedProduct() {
        return lastScannedProduct;
    }

    public void clearScan() {
        lastScannedProduct = null;
        productLabel.setText(" ");
        productLabel.setForeground(ThemeConfig.TEXT_MUTED);
        statusDot.setForeground(ThemeConfig.TEXT_MUTED);
    }

    public void requestScanFocus() {
        scanField.requestFocusInWindow();
    }

    private void onScan(ActionEvent e) {
        String barcode = scanField.getText().trim();
        if (barcode.isEmpty()) return;
        scanField.setText("");
        statusDot.setForeground(ThemeConfig.WARNING);
        productLabel.setText("Looking up...");
        productLabel.setForeground(ThemeConfig.TEXT_MUTED);

        new SwingWorker<Product, Void>() {
            @Override
            protected Product doInBackground() throws Exception {
                ProductService svc = new ProductService(new DatabaseManager().getDataSourceWithFallback());
                return svc.findByBarcode(barcode);
            }

            @Override
            protected void done() {
                try {
                    Product product = get();
                    if (product != null) {
                        lastScannedProduct = product;
                        productLabel.setText(product.getSku() + " \u2013 " + product.getName());
                        productLabel.setForeground(ThemeConfig.SUCCESS);
                        statusDot.setForeground(ThemeConfig.SUCCESS);
                        flashBorder(ThemeConfig.SUCCESS);
                        if (listener != null) {
                            listener.onProductScanned(product);
                        }
                    } else {
                        productLabel.setText("Product not found: " + barcode);
                        productLabel.setForeground(ThemeConfig.DANGER);
                        statusDot.setForeground(ThemeConfig.DANGER);
                        flashBorder(ThemeConfig.DANGER);
                    }
                } catch (Exception ex) {
                    productLabel.setText("Error: " + ex.getMessage());
                    productLabel.setForeground(ThemeConfig.DANGER);
                    statusDot.setForeground(ThemeConfig.DANGER);
                    flashBorder(ThemeConfig.DANGER);
                }

                Timer clearTimer = new Timer(3000, ev -> {
                    productLabel.setText(" ");
                    productLabel.setForeground(ThemeConfig.TEXT_MUTED);
                    statusDot.setForeground(ThemeConfig.TEXT_MUTED);
                });
                clearTimer.setRepeats(false);
                clearTimer.start();

                requestScanFocus();
            }
        }.execute();
    }

    private void flashBorder(Color color) {
        scanField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        Timer resetTimer = new Timer(600, ev -> {
            if (scanField.hasFocus()) {
                scanField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeConfig.ACCENT),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            } else {
                scanField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeConfig.BORDER),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }
}
