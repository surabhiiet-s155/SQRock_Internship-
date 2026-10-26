package com.quiz.ui;

import com.quiz.dao.ResultDAO;
import com.quiz.model.User;
import com.quiz.util.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Quiz history tracking for the logged-in user.
 */
public class HistoryFrame extends JFrame {
    public HistoryFrame(User user) {
        super("Quiz History - " + user.getFullName());
        DefaultTableModel m = new DefaultTableModel(
            new String[]{"Date Taken", "Category", "Questions", "Correct", "Wrong", "Score", "Percentage"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            for (Object[] row : new ResultDAO().history(user.getId())) m.addRow(row);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage());
        }

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel topCard = Theme.card(14);
        topCard.setLayout(new BorderLayout());
        topCard.add(Theme.title("📜 Quiz History — " + user.getFullName()), BorderLayout.WEST);
        root.add(topCard, BorderLayout.NORTH);

        JTable table = new JTable(m);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnClose = Theme.secondaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        Theme.apply(root);
        setSize(820, 480);
        setLocationRelativeTo(null);
    }
}
