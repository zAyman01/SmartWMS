package com.warehousewms.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Centralised FlatLaf Dark theme configuration and reusable UI components.
 */
public final class ThemeConfig {

    // ── Palette ──────────────────────────────────────────────────────────
    public static final Color BG_PRIMARY   = new Color(18, 18, 24);
    public static final Color BG_SECONDARY = new Color(28, 28, 38);
    public static final Color BG_CARD      = new Color(35, 35, 48);
    public static final Color BG_HOVER     = new Color(45, 45, 62);
    public static final Color ACCENT       = new Color(99, 102, 241);   // indigo-500
    public static final Color ACCENT_HOVER = new Color(129, 132, 255);
    public static final Color SUCCESS      = new Color(34, 197, 94);
    public static final Color WARNING      = new Color(250, 204, 21);
    public static final Color DANGER       = new Color(239, 68, 68);
    public static final Color TEXT_PRIMARY  = new Color(226, 232, 240);
    public static final Color TEXT_MUTED    = new Color(148, 163, 184);
    public static final Color BORDER       = new Color(55, 55, 72);

    // ── Font ─────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);

    private ThemeConfig() {}

    /** Install FlatLaf Dark + custom overrides. Call once at startup. */
    public static void install() {
        System.setProperty("flatlaf.useNativeLibrary", "false");

        FlatDarkLaf.setup();

        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionBackground", ACCENT);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", BG_SECONDARY);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("Tree.selectionBackground", ACCENT);
        UIManager.put("Tree.selectionForeground", Color.WHITE);
    }

    // ── Styled Button Factory ────────────────────────────────────────────

    /** Primary accent button. */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 36));
        addHover(btn, ACCENT, ACCENT_HOVER);
        return btn;
    }

    /** Danger (red) button. */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 36));
        addHover(btn, DANGER, DANGER.brighter());
        return btn;
    }

    /** Ghost / secondary button (transparent bg). */
    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(BG_CARD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 36));
        addHover(btn, BG_CARD, BG_HOVER);
        return btn;
    }

    private static void addHover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    // ── Styled Table ─────────────────────────────────────────────────────

    /** Apply premium dark styling to any JTable. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setBackground(BG_SECONDARY);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? BG_SECONDARY : BG_CARD);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BUTTON);
        header.setBackground(BG_PRIMARY);
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                c.setBackground(BG_PRIMARY);
                c.setForeground(TEXT_MUTED);
                c.setFont(FONT_BUTTON);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT),
                        new EmptyBorder(0, 10, 0, 10)));
                return c;
            }
        });
    }

    /** Wraps a JTable inside a scroll pane with themed borders. */
    public static JScrollPane themedScrollPane(JTable table) {
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(BG_SECONDARY);
        return sp;
    }

    // ── Toolbar panel with buttons ───────────────────────────────────────

    /** Creates a toolbar-like panel that hosts action buttons + optional search field. */
    public static JPanel toolbarPanel(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setOpaque(false);
        for (JButton b : buttons) {
            bar.add(b);
        }
        return bar;
    }

    /** Status bar at the bottom of management frames. */
    public static JLabel statusLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
        return lbl;
    }

    /** Search text field placeholder. */
    public static JTextField searchField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(FONT_BODY);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setPreferredSize(new Dimension(220, 36));
        return field;
    }
}
