package com.warehousewms.ui;

import com.warehousewms.util.SessionContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main dashboard with sidebar navigation and card-based content area.
 */
public class DashboardFrame extends JFrame {

    private final JPanel contentArea;

    public DashboardFrame() {
        setTitle("Smart WMS \u2013 Dashboard");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeConfig.BG_PRIMARY);
        setLayout(new BorderLayout());

        // ── Sidebar ──────────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content ──────────────────────────────────────────────────────
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(ThemeConfig.BG_PRIMARY);
        contentArea.setBorder(new EmptyBorder(20, 24, 20, 24));
        add(contentArea, BorderLayout.CENTER);

        showHomePage();

        ThemeConfig.addHelpMenu(this, "Welcome to the SmartWMS Dashboard!\n\n" +
            "BUSINESS OVERVIEW:\n" +
            "The Dashboard serves as the central command center for your warehouse operations. In a fast-paced environment, " +
            "warehouse managers need a real-time snapshot of the facility's health. This page aggregates data across " +
            "all modules to help you make split-second decisions on inbound shipments, outbound orders, and inventory levels.\n\n" +
            "HOW TO USE THIS PAGE:\n" +
            "• Navigation: Use the left menu to access specialized operational modules (like Receiving or Fulfillment).\n" +
            "• Key Metrics: Review the stat cards to monitor pending tasks and operational bottlenecks.\n" +
            "• Quick Actions: Use the central buttons to rapidly jump into high-priority tasks.");
    }

    // ── Sidebar ──────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeConfig.BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeConfig.BORDER));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        brand.setOpaque(false);
        JLabel icon = new JLabel(ThemeConfig.getIcon("package", 26, 26, ThemeConfig.ACCENT));
        JLabel brandLabel = new JLabel("Smart WMS");
        brandLabel.setFont(ThemeConfig.FONT_TITLE);
        brandLabel.setForeground(ThemeConfig.ACCENT);
        brand.add(icon);
        brand.add(brandLabel);
        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(10));

        // Navigation items
        sidebar.add(navItem("home", "Home", this::showHomePage));
        sidebar.add(navItem("products", "Products", () -> openFrame(new ProductManagementFrame())));
        sidebar.add(navItem("suppliers", "Suppliers", () -> openFrame(new SupplierManagementFrame())));
        sidebar.add(navItem("customers", "Customers", () -> openFrame(new CustomerManagementFrame())));
        sidebar.add(navItem("bins", "Bins / Locations", () -> openFrame(new BinManagementFrame())));

        sidebar.add(Box.createVerticalStrut(8));
        JLabel inventorySection = new JLabel("  INVENTORY OPS");
        inventorySection.setFont(ThemeConfig.FONT_SMALL);
        inventorySection.setForeground(ThemeConfig.TEXT_MUTED);
        inventorySection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        inventorySection.setBorder(new EmptyBorder(4, 16, 4, 0));
        sidebar.add(inventorySection);

        sidebar.add(navItem("purchase-orders", "Purchase Orders", () -> openFrame(new PurchaseOrderManagementFrame())));
        sidebar.add(navItem("receiving", "Receiving", () -> openFrame(new ReceivingFrame())));
        sidebar.add(navItem("customer-orders", "Customer Orders", () -> openFrame(new OrderManagementFrame())));
        sidebar.add(navItem("fulfillment", "Fulfillment", () -> openFrame(new FulfillmentFrame())));
        sidebar.add(navItem("inventory", "Inventory Mgmt", () -> openFrame(new InventoryManagementFrame())));

        sidebar.add(Box.createVerticalStrut(8));
        JLabel analyticsSection = new JLabel("  REPORTS & KPI");
        analyticsSection.setFont(ThemeConfig.FONT_SMALL);
        analyticsSection.setForeground(ThemeConfig.TEXT_MUTED);
        analyticsSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        analyticsSection.setBorder(new EmptyBorder(4, 16, 4, 0));
        sidebar.add(analyticsSection);
        sidebar.add(navItem("analytics", "Analytics Dashboard", () -> openFrame(new AnalyticsFrame())));

        if (SessionContext.isAdmin()) {
            sidebar.add(Box.createVerticalStrut(8));
            JLabel adminSection = new JLabel("  ADMIN");
            adminSection.setFont(ThemeConfig.FONT_SMALL);
            adminSection.setForeground(ThemeConfig.TEXT_MUTED);
            adminSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            adminSection.setBorder(new EmptyBorder(4, 16, 4, 0));
            sidebar.add(adminSection);
            sidebar.add(navItem("users", "User Management", () -> openFrame(new UserManagementFrame())));
            sidebar.add(navItem("backup", "Database Backup", () -> openFrame(new BackupFrame())));
        }

        sidebar.add(Box.createVerticalGlue());

        // User card at bottom
        sidebar.add(buildUserCard());

        return sidebar;
    }

    private JPanel navItem(String iconName, String label, Runnable action) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setBackground(ThemeConfig.BG_SECONDARY);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setBorder(new EmptyBorder(0, 16, 0, 16));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        labelPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(ThemeConfig.getIcon(iconName, 18, 18, ThemeConfig.TEXT_PRIMARY));

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(ThemeConfig.FONT_BODY);
        textLabel.setForeground(ThemeConfig.TEXT_PRIMARY);

        labelPanel.add(iconLabel);
        labelPanel.add(textLabel);

        item.add(labelPanel, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(ThemeConfig.BG_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(ThemeConfig.BG_SECONDARY);
            }
            @Override public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
        return item;
    }

    private JPanel buildUserCard() {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        card.setBackground(ThemeConfig.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConfig.BORDER),
                new EmptyBorder(10, 14, 10, 14)));

        // Avatar circle
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConfig.ACCENT);
                g2.fillOval(0, 0, 36, 36);
                String initials = getInitials(SessionContext.getCurrentUser().getFullName());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(initials)) / 2;
                int y = (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        card.add(avatar, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLabel = new JLabel(SessionContext.getCurrentUser().getFullName());
        nameLabel.setFont(ThemeConfig.FONT_BODY);
        nameLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel roleLabel = new JLabel(SessionContext.getCurrentUser().getRole());
        roleLabel.setFont(ThemeConfig.FONT_SMALL);
        roleLabel.setForeground(ThemeConfig.TEXT_MUTED);
        info.add(nameLabel);
        info.add(roleLabel);
        card.add(info, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("\u2192");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutBtn.setToolTipText("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setForeground(ThemeConfig.TEXT_MUTED);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { logoutBtn.setForeground(ThemeConfig.DANGER); }
            @Override public void mouseExited(MouseEvent e) { logoutBtn.setForeground(ThemeConfig.TEXT_MUTED); }
        });
        logoutBtn.addActionListener(e -> {
            SessionContext.clear();
            dispose();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
        card.add(logoutBtn, BorderLayout.EAST);

        return card;
    }

    // ── Home Page ────────────────────────────────────────────────────────

    private void showHomePage() {
        contentArea.removeAll();

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);

        // Greeting
        String greeting = getGreeting();
        JLabel greetLabel = new JLabel(greeting + ", " + SessionContext.getCurrentUser().getFullName() + "!");
        greetLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        greetLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        greetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(greetLabel);

        JLabel subLabel = new JLabel("Here's your warehouse overview");
        subLabel.setFont(ThemeConfig.FONT_BODY);
        subLabel.setForeground(ThemeConfig.TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(subLabel);
        page.add(Box.createVerticalStrut(24));

        // Quick stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        cards.add(statCard("Products", "products", ThemeConfig.ACCENT));
        cards.add(statCard("Suppliers", "suppliers", ThemeConfig.SUCCESS));
        cards.add(statCard("Customers", "customers", new Color(236, 72, 153)));
        cards.add(statCard("Locations", "bins", ThemeConfig.WARNING));

        page.add(cards);
        page.add(Box.createVerticalStrut(24));

        // Quick actions
        JLabel actionsLabel = new JLabel("Quick Actions");
        actionsLabel.setFont(ThemeConfig.FONT_HEADING);
        actionsLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        actionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(actionsLabel);
        page.add(Box.createVerticalStrut(12));

        JPanel actionsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        actionsGrid.setOpaque(false);
        actionsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        actionsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton prodBtn = ThemeConfig.primaryButton("Manage Products");
        prodBtn.addActionListener(e -> openFrame(new ProductManagementFrame()));
        actionsGrid.add(prodBtn);

        JButton supBtn = ThemeConfig.ghostButton("Manage Suppliers");
        supBtn.addActionListener(e -> openFrame(new SupplierManagementFrame()));
        actionsGrid.add(supBtn);

        JButton custBtn = ThemeConfig.ghostButton("Manage Customers");
        custBtn.addActionListener(e -> openFrame(new CustomerManagementFrame()));
        actionsGrid.add(custBtn);

        JButton binBtn = ThemeConfig.ghostButton("Manage Locations");
        binBtn.addActionListener(e -> openFrame(new BinManagementFrame()));
        actionsGrid.add(binBtn);

        page.add(actionsGrid);

        contentArea.add(page, BorderLayout.NORTH);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel statCard(String label, String iconName, Color accent) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConfig.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // icon circle
        JLabel iconLabel = new JLabel(ThemeConfig.getIcon(iconName, 28, 28, accent));
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(ThemeConfig.FONT_HEADING);
        titleLabel.setForeground(accent);
        JLabel descLabel = new JLabel("Click to manage");
        descLabel.setFont(ThemeConfig.FONT_SMALL);
        descLabel.setForeground(ThemeConfig.TEXT_MUTED);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void openFrame(JFrame frame) {
        frame.setVisible(true);
    }

    private static String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0)).toUpperCase();
    }

    private static String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }
    public static void main(String[] args) {
        ThemeConfig.install();
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }
}
