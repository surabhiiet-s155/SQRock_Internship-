package com.quiz.ui;

import com.quiz.dao.UserDAO;
import com.quiz.model.User;
import com.quiz.util.Theme;
import javax.swing.*;
import java.awt.*;

/**
 * Modern, clean, attractive Login Screen.
 */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);

    public LoginFrame() {
        super("Online Quiz Application - Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(Theme.bg());
        background.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Centered Card Container
        JPanel card = Theme.card(32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(420, 600));

        // App Logo / Title Header
        JLabel iconBadge = new JLabel("🎓", SwingConstants.CENTER);
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = Theme.title("Online Quiz Portal");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = Theme.subtitle("Interactive Knowledge & Assessment System");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconBadge);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));

        // Input Fields
        JPanel form = new JPanel(new GridLayout(4, 1, 6, 8));
        form.setOpaque(false);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Theme.fg());
        txtUser.setPreferredSize(new Dimension(280, 36));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(Theme.fg());
        txtPass.setPreferredSize(new Dimension(280, 36));

        form.add(lblUser);
        form.add(txtUser);
        form.add(lblPass);
        form.add(txtPass);

        card.add(form);
        card.add(Box.createVerticalStrut(20));

        // Buttons
        JButton btnLogin = Theme.button("🚀 Sign In");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(320, 42));

        JButton btnReg = Theme.secondaryButton("✨ Create New Account");
        btnReg.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReg.setMaximumSize(new Dimension(320, 40));

        JCheckBox darkCheck = new JCheckBox("🌙 Dark Mode", Theme.dark);
        darkCheck.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnReg);
        card.add(Box.createVerticalStrut(12));
        card.add(darkCheck);
        card.add(Box.createVerticalStrut(16));

        // Hint Box for Quick Testing
        JPanel hintBox = new JPanel();
        hintBox.setLayout(new BoxLayout(hintBox, BoxLayout.Y_AXIS));
        hintBox.setOpaque(true);
        hintBox.setBackground(Theme.dark ? new Color(0x27, 0x35, 0x4A) : new Color(0xEE, 0xF2, 0xFF));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.PRIMARY, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        hintBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintTitle = new JLabel("💡 Quick Demo Credentials:");
        hintTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hintTitle.setForeground(Theme.PRIMARY);

        JLabel hint1 = new JLabel("Student: student / user123");
        hint1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint1.setForeground(Theme.fg());

        JLabel hint2 = new JLabel("Admin: admin / admin123");
        hint2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint2.setForeground(Theme.fg());

        hintBox.add(hintTitle);
        hintBox.add(Box.createVerticalStrut(2));
        hintBox.add(hint1);
        hintBox.add(hint2);
        card.add(hintBox);

        // Actions
        btnLogin.addActionListener(e -> doLogin());
        txtPass.addActionListener(e -> doLogin());
        btnReg.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        darkCheck.addActionListener(e -> {
            Theme.setDark(darkCheck.isSelected());
            SwingUtilities.updateComponentTreeUI(this);
            Theme.apply(getContentPane());
            repaint();
        });

        background.add(card);
        setContentPane(background);
        Theme.apply(background);
        pack();
        setSize(480, 620);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String u = txtUser.getText().trim(), p = new String(txtPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = new UserDAO().login(u, p);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new DashboardFrame(user).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Connection Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
