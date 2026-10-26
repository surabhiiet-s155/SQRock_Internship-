package com.quiz.ui;

import com.quiz.dao.CategoryDAO;
import com.quiz.dao.QuestionDAO;
import com.quiz.model.Category;
import com.quiz.model.User;
import com.quiz.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modern, attractive Dashboard Screen.
 */
public class DashboardFrame extends JFrame {
    private final User user;
    private final JComboBox<Category> cmbCat = new JComboBox<>();
    private final JSpinner spnCount = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));

    public DashboardFrame(User user) {
        super("Quiz Dashboard - " + user.getFullName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        // ----------------- TOP GREETING & RULES -----------------
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JPanel userRow = new JPanel(new BorderLayout());
        userRow.setOpaque(false);

        JLabel welcomeTitle = Theme.title("Welcome back, " + user.getFullName() + "! 👋");
        JLabel roleBadge = new JLabel(user.isAdmin() ? " [ADMINISTRATOR] " : " [STUDENT] ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setOpaque(true);
        roleBadge.putClientProperty("keepColor", Boolean.TRUE);
        if (user.isAdmin()) {
            roleBadge.setBackground(new Color(0xFE, 0xF3, 0xC7));
            roleBadge.setForeground(new Color(0x92, 0x40, 0x0E));
        } else {
            roleBadge.setBackground(new Color(0xDB, 0xEA, 0xFE));
            roleBadge.setForeground(new Color(0x1E, 0x40, 0xAF));
        }
        roleBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        userRow.add(welcomeTitle, BorderLayout.WEST);
        userRow.add(roleBadge, BorderLayout.EAST);
        topPanel.add(userRow);
        topPanel.add(Box.createVerticalStrut(10));

        // Rules Banner Card
        JPanel bannerCard = Theme.card(12);
        bannerCard.setLayout(new BorderLayout());
        JLabel rules = new JLabel("⚡ Scoring: +1.0 for Correct  •  -0.25 for Wrong  •  0 for Skipped  •  Passing mark: 40%");
        rules.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rules.setForeground(Theme.mutedFg());
        bannerCard.add(rules, BorderLayout.CENTER);
        topPanel.add(bannerCard);

        root.add(topPanel, BorderLayout.NORTH);

        // ----------------- CENTER CARDS -----------------
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 18, 18));
        centerGrid.setOpaque(false);

        // Left Card: Start Quiz
        JPanel quizCard = Theme.card(20);
        quizCard.setLayout(new BoxLayout(quizCard, BoxLayout.Y_AXIS));

        JLabel quizCardTitle = new JLabel("🎯 Take A New Quiz");
        quizCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quizCardTitle.setForeground(Theme.fg());
        quizCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCat = new JLabel("Select Subject / Category:");
        lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCat.setForeground(Theme.mutedFg());
        lblCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblCount = new JLabel("Number of Questions:");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCount.setForeground(Theme.mutedFg());
        lblCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        spnCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        spnCount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnStart = Theme.button("🚀 Start Quiz Now");
        btnStart.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        quizCard.add(quizCardTitle);
        quizCard.add(Box.createVerticalStrut(14));
        quizCard.add(lblCat);
        quizCard.add(Box.createVerticalStrut(6));
        quizCard.add(cmbCat);
        quizCard.add(Box.createVerticalStrut(12));
        quizCard.add(lblCount);
        quizCard.add(Box.createVerticalStrut(6));
        quizCard.add(spnCount);
        quizCard.add(Box.createVerticalGlue());
        quizCard.add(btnStart);

        // Right Card: Quick Actions
        JPanel actionsCard = Theme.card(20);
        actionsCard.setLayout(new BoxLayout(actionsCard, BoxLayout.Y_AXIS));

        JLabel actionsTitle = new JLabel("⚡ Quick Navigation");
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        actionsTitle.setForeground(Theme.fg());
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnHistory = Theme.secondaryButton("📊 View My Quiz History");
        btnHistory.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHistory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton btnBoard = Theme.secondaryButton("🏆 Global Leaderboard");
        btnBoard.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBoard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton btnAdmin = Theme.secondaryButton("⚙️ Manage Questions & Categories");
        btnAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdmin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JCheckBox darkCheck = new JCheckBox("🌙 Dark Mode", Theme.dark);
        darkCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionsCard.add(actionsTitle);
        actionsCard.add(Box.createVerticalStrut(14));
        actionsCard.add(btnHistory);
        actionsCard.add(Box.createVerticalStrut(10));
        actionsCard.add(btnBoard);
        if (user.isAdmin()) {
            actionsCard.add(Box.createVerticalStrut(10));
            actionsCard.add(btnAdmin);
        }
        actionsCard.add(Box.createVerticalStrut(14));
        actionsCard.add(darkCheck);
        actionsCard.add(Box.createVerticalGlue());

        centerGrid.add(quizCard);
        centerGrid.add(actionsCard);
        root.add(centerGrid, BorderLayout.CENTER);

        // ----------------- BOTTOM BAR -----------------
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton btnLogout = Theme.secondaryButton("🚪 Sign Out");
        bottomBar.add(btnLogout);
        root.add(bottomBar, BorderLayout.SOUTH);

        // Actions
        btnStart.addActionListener(e -> startQuiz());
        btnHistory.addActionListener(e -> new HistoryFrame(user).setVisible(true));
        btnBoard.addActionListener(e -> new LeaderboardFrame().setVisible(true));
        btnAdmin.addActionListener(e -> new AdminFrame().setVisible(true));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        darkCheck.addActionListener(e -> {
            Theme.setDark(darkCheck.isSelected());
            SwingUtilities.updateComponentTreeUI(this);
            Theme.apply(getContentPane());
            repaint();
        });

        loadCategories();
        setContentPane(root);
        Theme.apply(root);
        setSize(780, 500);
        setLocationRelativeTo(null);
    }

    private void loadCategories() {
        try {
            List<Category> cats = new CategoryDAO().findAll();
            for (Category c : cats) cmbCat.addItem(c);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startQuiz() {
        Category c = (Category) cmbCat.getSelectedItem();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "No category available. Please add categories via admin panel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int count = (Integer) spnCount.getValue();
            var qs = new QuestionDAO().randomByCategory(c.getId(), count);
            if (qs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This category has no questions yet.", "Empty Category", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new QuizFrame(user, c, qs).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
