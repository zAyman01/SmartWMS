package com.warehousewms.ui;

import com.warehousewms.config.DatabaseManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AnalyticsFrame extends JFrame {

    private JPanel rootPanel;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JPanel chartContainer;
    private JPanel inventoryChartPanel;
    private JPanel ordersChartPanel;
    private JLabel statusLabel;

    public AnalyticsFrame() {
        setContentPane(rootPanel);
        setTitle("Smart WMS \u2013 Analytics");
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        mainPanel.setBackground(ThemeConfig.BG_PRIMARY);
        chartContainer.setBackground(ThemeConfig.BG_PRIMARY);
        titleLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        statusLabel.setForeground(ThemeConfig.TEXT_MUTED);

        loadCharts();
    }

    private void loadCharts() {
        statusLabel.setText("Loading charts...");
        new SwingWorker<Void, Void>() {
            private JFreeChart invChart;
            private JFreeChart ordChart;

            @Override protected Void doInBackground() throws Exception {
                // Fetch inventory data
                DefaultCategoryDataset invDataset = new DefaultCategoryDataset();
                DefaultPieDataset ordDataset = new DefaultPieDataset();

                try (Connection conn = new DatabaseManager().getDataSourceWithFallback().getConnection()) {
                    // Inventory by Product
                    String invSql = "SELECT p.Name, SUM(i.Quantity) as TotalQty FROM Inventory i JOIN Products p ON i.ProductId = p.ProductId GROUP BY p.Name";
                    try (PreparedStatement ps = conn.prepareStatement(invSql); ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            invDataset.addValue(rs.getInt("TotalQty"), "Quantity", rs.getString("Name"));
                        }
                    }

                    // Orders by Status
                    String ordSql = "SELECT Status, COUNT(*) as Count FROM Orders GROUP BY Status";
                    try (PreparedStatement ps = conn.prepareStatement(ordSql); ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ordDataset.setValue(rs.getString("Status"), rs.getInt("Count"));
                        }
                    }
                }

                invChart = ChartFactory.createBarChart("Total Stock by Product", "Product", "Quantity", invDataset, PlotOrientation.VERTICAL, false, true, false);
                ordChart = ChartFactory.createPieChart("Orders by Status", ordDataset, true, true, false);

                // Basic FlatLaf styling adjustments for JFreeChart (optional but good for dark theme)
                invChart.setBackgroundPaint(ThemeConfig.BG_CARD);
                invChart.getPlot().setBackgroundPaint(ThemeConfig.BG_PRIMARY);
                ordChart.setBackgroundPaint(ThemeConfig.BG_CARD);
                ordChart.getPlot().setBackgroundPaint(ThemeConfig.BG_PRIMARY);
                
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    ChartPanel p1 = new ChartPanel(invChart);
                    ChartPanel p2 = new ChartPanel(ordChart);
                    
                    inventoryChartPanel.removeAll();
                    inventoryChartPanel.add(p1, BorderLayout.CENTER);
                    inventoryChartPanel.revalidate();
                    
                    ordersChartPanel.removeAll();
                    ordersChartPanel.add(p2, BorderLayout.CENTER);
                    ordersChartPanel.revalidate();
                    
                    statusLabel.setText("Charts loaded.");
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load charts: " + ex.getMessage());
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new AnalyticsFrame().setVisible(true));
    }
}
