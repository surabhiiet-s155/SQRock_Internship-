package com.quiz.ui;

import com.quiz.dao.UserDAO;
import com.quiz.util.Theme;
import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame owner) {
        super(owner, "Register", true);
        JTextField name = new JTextField(18), user = new JTextField(18);
        JPasswordField p1 = new JPasswordField(18), p2 = new JPasswordField(18);
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;
        String[] labels = {"Full name", "Username", "Password", "Confirm password"};
        JComponent[] fields = {name, user, p1, p2};
        for (int i = 0; i < 4; i++) {
            g.gridy = i; g.gridx = 0; root.add(new JLabel(labels[i]), g);
            g.gridx = 1; root.add(fields[i], g);
        }
        JButton ok = Theme.button("Register");
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2; root.add(ok, g);

        ok.addActionListener(e -> {
            String n = name.getText().trim(), u = user.getText().trim();
            String a = new String(p1.getPassword()), b = new String(p2.getPassword());
            if (n.isEmpty() || u.isEmpty() || a.isEmpty()) { JOptionPane.showMessageDialog(this, "All fields are required."); return; }
            if (u.length() < 3) { JOptionPane.showMessageDialog(this, "Username must have at least 3 characters."); return; }
            if (a.length() < 4) { JOptionPane.showMessageDialog(this, "Password must have at least 4 characters."); return; }
            if (!a.equals(b)) { JOptionPane.showMessageDialog(this, "Passwords do not match."); return; }
            try {
                if (new UserDAO().register(u, a, n)) {
                    JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.");
                    dispose();
                } else JOptionPane.showMessageDialog(this, "That username is already taken.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        setContentPane(root); Theme.apply(root);
        pack(); setLocationRelativeTo(owner);
    }
}
