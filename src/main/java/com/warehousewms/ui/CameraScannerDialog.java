package com.warehousewms.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraScannerDialog extends JDialog {
    private Webcam webcam;
    private final AtomicBoolean scanning = new AtomicBoolean(true);
    private final MultiFormatReader reader = new MultiFormatReader();
    private volatile String decodedBarcode;

    public CameraScannerDialog(JFrame parent) {
        super(parent, "Scan Barcode with Camera", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);

        JLabel header = new JLabel("Point camera at a barcode", SwingConstants.CENTER);
        header.setFont(ThemeConfig.FONT_HEADING);
        header.setForeground(ThemeConfig.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        content.add(header, BorderLayout.NORTH);

        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                JOptionPane.showMessageDialog(this, "No webcam detected.",
                        "Camera Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();

            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(false);
            panel.setMirrored(true);
            content.add(panel, BorderLayout.CENTER);

        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this,
                    "Camera not available on this platform:\n" + t.getMessage(),
                    "Camera Unsupported", JOptionPane.WARNING_MESSAGE);
            if (webcam != null && webcam.isOpen()) webcam.close();
            webcam = null;
            dispose();
            return;
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.setBackground(ThemeConfig.BG_CARD);

        JLabel tip = new JLabel("Hold steady until barcode is detected");
        tip.setFont(ThemeConfig.FONT_SMALL);
        tip.setForeground(ThemeConfig.TEXT_MUTED);
        bottom.add(tip);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(ThemeConfig.FONT_BUTTON);
        cancelBtn.setBackground(ThemeConfig.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> close());
        bottom.add(cancelBtn);

        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);

        if (webcam != null) {
            startDecodingThread();
        }
    }

    private void startDecodingThread() {
        new Thread(() -> {
            while (scanning.get() && webcam != null && webcam.isOpen()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    BinaryBitmap bitmap = new BinaryBitmap(
                            new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                    Result result = reader.decodeWithState(bitmap);
                    if (result != null) {
                        decodedBarcode = result.getText();
                        scanning.set(false);
                        SwingUtilities.invokeLater(this::close);
                        return;
                    }
                    Thread.sleep(200);
                } catch (Exception ignored) {
                }
            }
        }, "barcode-decoder").start();
    }

    private void close() {
        scanning.set(false);
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        dispose();
    }

    @Override
    public void dispose() {
        scanning.set(false);
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        super.dispose();
    }

    public String getDecodedBarcode() {
        return decodedBarcode;
    }
}
