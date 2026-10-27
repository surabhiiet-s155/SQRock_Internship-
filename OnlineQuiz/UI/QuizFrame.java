package com.quiz.ui;

import com.quiz.dao.ResultDAO;
import com.quiz.model.*;
import com.quiz.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Quiz module: modern interactive MCQ player with countdown timer and navigation palette.
 */
public class QuizFrame extends JFrame {
    private static final int SECONDS_PER_QUESTION = 30;

    private final User user;
    private final Category category;
    private final List<Question> questions;
    private final int[] answers;
    private int index = 0, remaining;
    private boolean loading = false, submitted = false;

    private final JLabel lblProgress = new JLabel(), lblTimer = new JLabel();
    private final JTextArea txtQuestion = new JTextArea(4, 40);
    private final JRadioButton[] opts = new JRadioButton[4];
    private final ButtonGroup group = new ButtonGroup();
    private final JButton btnPrev = Theme.secondaryButton("◀ Previous"),
                          btnNext = Theme.secondaryButton("Next ▶"),
                          btnClear = Theme.secondaryButton("Clear Answer"),
                          btnSubmit = Theme.button("✅ Submit Quiz");
    private final JButton[] navButtons;
    private final javax.swing.Timer timer;

    public QuizFrame(User user, Category category, List<Question> questions) {
        super("Quiz - " + category.getName());
        this.user = user;
        this.category = category;
        this.questions = questions;
        this.answers = new int[questions.size()];
        Arrays.fill(answers, -1);
        this.remaining = questions.size() * SECONDS_PER_QUESTION;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // ------------------ TOP BAR ------------------
        JPanel top = Theme.card(12);
        top.setLayout(new BorderLayout());

        JLabel cat = new JLabel("📚 " + category.getName());
        cat.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cat.setForeground(Theme.fg());

        lblProgress.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProgress.setHorizontalAlignment(SwingConstants.CENTER);
        lblProgress.setForeground(Theme.PRIMARY);

        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTimer.putClientProperty("keepColor", Boolean.TRUE);

        top.add(cat, BorderLayout.WEST);
        top.add(lblProgress, BorderLayout.CENTER);
        top.add(lblTimer, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        // ------------------ CENTER QUESTION CARD ------------------
        JPanel centreCard = Theme.card(20);
        centreCard.setLayout(new BoxLayout(centreCard, BoxLayout.Y_AXIS));

        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new Font("Segoe UI", Font.BOLD, 17));
        txtQuestion.setBorder(BorderFactory.createEmptyBorder(6, 6, 12, 6));
        txtQuestion.setOpaque(false);
        txtQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        centreCard.add(txtQuestion);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        centreCard.add(sep);
        centreCard.add(Box.createVerticalStrut(14));

        for (int i = 0; i < 4; i++) {
            opts[i] = new JRadioButton();
            opts[i].setFont(new Font("Segoe UI", Font.PLAIN, 15));
            opts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            opts[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            group.add(opts[i]);
            centreCard.add(opts[i]);
            centreCard.add(Box.createVerticalStrut(10));
            final int k = i;
            opts[i].addActionListener(e -> {
                if (!loading) {
                    answers[index] = k;
                    refreshNav();
                }
            });
        }
        root.add(centreCard, BorderLayout.CENTER);

        // ------------------ BOTTOM BAR ------------------
        JPanel bottom = new JPanel(new BorderLayout(8, 10));
        bottom.setOpaque(false);

        // Palette of question numbers
        JPanel numsCard = Theme.card(8);
        numsCard.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));
        navButtons = new JButton[questions.size()];
        for (int i = 0; i < navButtons.length; i++) {
            final int k = i;
            navButtons[i] = new JButton(String.valueOf(i + 1));
            navButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
            navButtons[i].setMargin(new Insets(3, 8, 3, 8));
            navButtons[i].setFocusPainted(false);
            navButtons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            navButtons[i].addActionListener(e -> go(k));
            numsCard.add(navButtons[i]);
        }
        bottom.add(numsCard, BorderLayout.NORTH);

        // Navigation controls
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        nav.setOpaque(false);
        nav.add(btnPrev);
        nav.add(btnClear);
        nav.add(btnNext);
        nav.add(btnSubmit);
        bottom.add(nav, BorderLayout.CENTER);

        root.add(bottom, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> go(index - 1));
        btnNext.addActionListener(e -> go(index + 1));
        btnClear.addActionListener(e -> {
            group.clearSelection();
            answers[index] = -1;
            refreshNav();
        });
        btnSubmit.addActionListener(e -> confirmSubmit());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if (JOptionPane.showConfirmDialog(QuizFrame.this,
                        "Quit the quiz? Your current progress will be lost.",
                        "Confirm Quit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    timer.stop();
                    new DashboardFrame(QuizFrame.this.user).setVisible(true);
                    dispose();
                }
            }
        });

        setContentPane(root);
        Theme.apply(root);
        timer = new javax.swing.Timer(1000, e -> tick());
        show(0);
        updateTimerLabel();
        timer.start();
        setSize(860, 620);
        setLocationRelativeTo(null);
    }

    private void go(int i) {
        if (i < 0 || i >= questions.size()) return;
        show(i);
    }

    private void show(int i) {
        index = i;
        Question q = questions.get(i);
        loading = true;
        txtQuestion.setText("Question " + (i + 1) + " of " + questions.size() + ":\n" + q.getText());
        group.clearSelection();
        for (int k = 0; k < 4; k++) {
            opts[k].setText("<html><body style='width:640px; padding: 4px;'>" + (char) ('A' + k) + ". &nbsp; " + esc(q.getOptions()[k]) + "</body></html>");
            opts[k].setSelected(answers[i] == k);
        }
        loading = false;
        lblProgress.setText(String.format("Question %d of %d", (i + 1), questions.size()));
        btnPrev.setEnabled(i > 0);
        btnNext.setEnabled(i < questions.size() - 1);
        refreshNav();
    }

    /** Highlights question buttons: Blue = Current, Green = Answered, Gray = Unanswered */
    private void refreshNav() {
        for (int i = 0; i < navButtons.length; i++) {
            JButton b = navButtons[i];
            b.setOpaque(true);
            if (i == index) {
                b.setBackground(Theme.PRIMARY);
                b.setForeground(Color.WHITE);
            } else if (answers[i] >= 0) {
                b.setBackground(Theme.SUCCESS);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(Theme.dark ? new Color(0x33, 0x41, 0x55) : new Color(0xE2, 0xE8, 0xF0));
                b.setForeground(Theme.fg());
            }
        }
    }

    private void tick() {
        remaining--;
        updateTimerLabel();
        if (remaining <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Time is up! Your quiz will be submitted automatically.", "Time Up", JOptionPane.WARNING_MESSAGE);
            submit();
        }
    }

    private void updateTimerLabel() {
        lblTimer.setText(String.format("⏱️ %02d:%02d", remaining / 60, remaining % 60));
        lblTimer.setForeground(remaining <= 30 ? Theme.DANGER : Theme.PRIMARY);
    }

    private void confirmSubmit() {
        long left = Arrays.stream(answers).filter(a -> a < 0).count();
        String m = left > 0 ? "You have " + left + " unanswered question(s). Are you sure you want to submit?" : "Are you ready to submit your quiz?";
        if (JOptionPane.showConfirmDialog(this, m, "Submit Quiz", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            submit();
        }
    }

    private void submit() {
        if (submitted) return;
        submitted = true;
        timer.stop();
        QuizResult r = QuizResult.evaluate(questions, answers);
        try {
            new ResultDAO().save(user.getId(), category.getId(), r);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save result to database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        new ResultFrame(user, category, questions, answers, r).setVisible(true);
        dispose();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
