package com.quiz.ui;

import com.quiz.dao.QuestionDAO;
import com.quiz.model.*;
import com.quiz.util.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Super attractive modern Score Card and answer review screen.
 */
public class ResultFrame extends JFrame {

    public ResultFrame(User user, Category cat, List<Question> qs, int[] answers, QuizResult r) {
        super("Quiz Result - " + cat.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // ------------------ HEADER HERO SECTION ------------------
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Hero Card
        JPanel heroCard = Theme.card(16);
        heroCard.setLayout(new BorderLayout(15, 10));

        JPanel heroLeft = new JPanel();
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));
        heroLeft.setOpaque(false);

        JLabel statusBadge = new JLabel(r.passed ? "  🎉 PASSED SUCCESSFULLY  " : "  ⚠️ ASSESSMENT NOT CLEARED  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusBadge.setOpaque(true);
        statusBadge.putClientProperty("keepColor", Boolean.TRUE);
        if (r.passed) {
            statusBadge.setBackground(new Color(0xDC, 0xFC, 0xE7)); // soft green
            statusBadge.setForeground(new Color(0x16, 0x65, 0x34)); // dark green
        } else {
            statusBadge.setBackground(new Color(0xFE, 0xE2, 0xE2)); // soft red
            statusBadge.setForeground(new Color(0x99, 0x1B, 0x1B)); // dark red
        }
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel perfTitle = new JLabel(r.performance());
        perfTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        perfTitle.setForeground(Theme.fg());
        perfTitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        perfTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaInfo = new JLabel(String.format("📚 Category: %s    •    👤 Candidate: %s    •    🎯 Pass Criterion: 40%%",
                cat.getName(), user.getFullName()));
        metaInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metaInfo.setForeground(Theme.mutedFg());
        metaInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroLeft.add(statusBadge);
        heroLeft.add(perfTitle);
        heroLeft.add(metaInfo);
        heroCard.add(heroLeft, BorderLayout.CENTER);

        topContainer.add(heroCard);
        topContainer.add(Box.createVerticalStrut(12));

        // ------------------ 4 STAT CARDS ------------------
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 12));
        statsRow.setOpaque(false);

        statsRow.add(Theme.statCard(
            "Final Score",
            String.format("%.2f / %d", r.score, r.total),
            Theme.PRIMARY,
            "Marking (+1.0 / -0.25)"
        ));

        statsRow.add(Theme.statCard(
            "Percentage",
            String.format("%.1f%%", r.percentage),
            r.passed ? Theme.SUCCESS : Theme.DANGER,
            r.passed ? "Cleared Benchmark" : "Below 40% Target"
        ));

        statsRow.add(Theme.statCard(
            "Accuracy",
            String.format("%d / %d", r.correct, r.total),
            Theme.SUCCESS,
            String.format("%d Wrong • %d Skipped", r.wrong, r.unanswered)
        ));

        statsRow.add(Theme.statCard(
            "Status",
            r.passed ? "PASSED" : "FAILED",
            r.passed ? Theme.SUCCESS : Theme.DANGER,
            r.percentage >= 80 ? "Grade: Excellent (A)" : (r.passed ? "Grade: Qualified (B)" : "Needs Retake")
        ));

        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(10));

        // Progress Bar
        JProgressBar pbar = new JProgressBar(0, 100);
        pbar.setValue((int) Math.round(Math.max(0, Math.min(100, r.percentage))));
        pbar.setStringPainted(true);
        pbar.setString(String.format("Overall Accuracy: %.1f%%", r.percentage));
        pbar.setForeground(r.passed ? Theme.SUCCESS : Theme.DANGER);
        pbar.setPreferredSize(new Dimension(0, 20));
        topContainer.add(pbar);

        root.add(topContainer, BorderLayout.NORTH);

        // ------------------ TABLE / ANSWER REVIEW ------------------
        JPanel tableContainer = new JPanel(new BorderLayout(6, 6));
        tableContainer.setOpaque(false);

        JLabel tableTitle = new JLabel("📝 Detailed Question & Answer Breakdown");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(Theme.fg());
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[]{"#", "Question", "Your Answer", "Correct Answer", "Result"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i);
            String mine = answers[i] < 0 ? "Skipped" : (char) ('A' + answers[i]) + ". " + q.getOptions()[answers[i]];
            String right = q.getCorrect() + ". " + q.getOptions()[q.getCorrectIndex()];
            String res;
            if (answers[i] < 0) {
                res = "Skipped (0.0)";
            } else if (answers[i] == q.getCorrectIndex()) {
                res = "Correct (+1.0)";
            } else {
                res = "Wrong (-0.25)";
            }
            m.addRow(new Object[]{i + 1, q.getText(), mine, right, res});
        }

        JTable table = new JTable(m);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setMaxWidth(150);

        // Custom chip badge renderer for Result column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String text = val == null ? "" : val.toString();
                if (text.startsWith("Correct")) {
                    l.setForeground(Theme.SUCCESS);
                } else if (text.startsWith("Wrong")) {
                    l.setForeground(Theme.DANGER);
                } else {
                    l.setForeground(Theme.WARNING);
                }
                return l;
            }
        });

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(tableContainer, BorderLayout.CENTER);

        // ------------------ BOTTOM BUTTONS ------------------
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        btns.setOpaque(false);

        JButton retake = Theme.secondaryButton("🔄 Retake Quiz");
        JButton again = Theme.button("🏠 Back to Dashboard");
        JButton exit = Theme.secondaryButton("Exit");

        btns.add(retake);
        btns.add(again);
        btns.add(exit);
        root.add(btns, BorderLayout.SOUTH);

        retake.addActionListener(e -> {
            try {
                var newQs = new QuestionDAO().randomByCategory(cat.getId(), qs.size());
                if (newQs.isEmpty()) newQs = qs;
                new QuizFrame(user, cat, newQs).setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not retake quiz: " + ex.getMessage());
            }
        });

        again.addActionListener(e -> {
            new DashboardFrame(user).setVisible(true);
            dispose();
        });
        exit.addActionListener(e -> System.exit(0));

        setContentPane(root);
        Theme.apply(root);
        setSize(920, 680);
        setLocationRelativeTo(null);
    }
}
