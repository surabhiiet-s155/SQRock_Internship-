package com.quiz.ui;

import com.quiz.dao.CategoryDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.model.Category;
import com.quiz.util.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Modern Leaderboard screen showing top 10 performances.
 */
public class LeaderboardFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Rank", "Candidate Name", "Category", "Score", "Percentage", "Date Taken"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JComboBox<Category> cmb = new JComboBox<>();

    public LeaderboardFrame() {
        super("Global Leaderboard");
        cmb.addItem(new Category(0, "All Categories"));
        try {
            for (Category c : new CategoryDAO().findAll()) cmb.addItem(c);
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel topCard = Theme.card(14);
        topCard.setLayout(new BorderLayout(10, 0));

        JLabel title = Theme.title("🏆 Top 10 Hall of Fame");
        topCard.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterPanel.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterPanel.add(lblFilter);
        cmb.setPreferredSize(new Dimension(200, 32));
        filterPanel.add(cmb);
        topCard.add(filterPanel, BorderLayout.EAST);

        root.add(topCard, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnClose = Theme.secondaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        root.add(bottom, BorderLayout.SOUTH);

        cmb.addActionListener(e -> load());
        load();

        setContentPane(root);
        Theme.apply(root);
        setSize(800, 480);
        setLocationRelativeTo(null);
    }

    private void load() {
        model.setRowCount(0);
        Category c = (Category) cmb.getSelectedItem();
        try {
            for (Object[] row : new ResultDAO().leaderboard(c == null ? 0 : c.getId())) {
                model.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage());
        }
    }
}
