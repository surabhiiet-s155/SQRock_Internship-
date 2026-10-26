package com.quiz.ui;

import com.quiz.dao.CategoryDAO;
import com.quiz.dao.QuestionDAO;
import com.quiz.model.Category;
import com.quiz.model.Question;
import com.quiz.util.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Admin module: add / edit / delete questions and manage categories. */
public class AdminFrame extends JFrame {
    private final QuestionDAO qdao = new QuestionDAO();
    private final CategoryDAO cdao = new CategoryDAO();
    private List<Question> questions;
    private List<Category> categories;

    private final DefaultTableModel qModel = new DefaultTableModel(
        new String[]{"ID", "Category", "Question", "A", "B", "C", "D", "Correct"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable qTable = new JTable(qModel);
    private final DefaultListModel<Category> cModel = new DefaultListModel<>();
    private final JList<Category> cList = new JList<>(cModel);

    public AdminFrame() {
        super("Admin Panel");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Questions", questionPanel());
        tabs.addTab("Categories", categoryPanel());
        setContentPane(tabs);
        reload();
        Theme.apply(tabs);
        setSize(950, 500); setLocationRelativeTo(null);
    }

    private JPanel questionPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(qTable), BorderLayout.CENTER);
        qTable.getColumnModel().getColumn(0).setMaxWidth(45);
        qTable.getColumnModel().getColumn(7).setMaxWidth(60);
        JButton add = Theme.button("Add Question"), edit = new JButton("Edit Selected"), del = new JButton("Delete Selected");
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        b.add(add); b.add(edit); b.add(del);
        p.add(b, BorderLayout.SOUTH);
        add.addActionListener(e -> openDialog(null));
        edit.addActionListener(e -> {
            int r = qTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a question first."); return; }
            openDialog(questions.get(r));
        });
        del.addActionListener(e -> {
            int r = qTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a question first."); return; }
            if (JOptionPane.showConfirmDialog(this, "Delete this question?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                run(() -> qdao.delete(questions.get(r).getId()));
        });
        return p;
    }

    private JPanel categoryPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(cList), BorderLayout.CENTER);
        JButton add = Theme.button("Add Category"), ren = new JButton("Rename"), del = new JButton("Delete");
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        b.add(add); b.add(ren); b.add(del);
        p.add(b, BorderLayout.SOUTH);
        add.addActionListener(e -> {
            String n = JOptionPane.showInputDialog(this, "Category name:");
            if (n != null && !n.trim().isEmpty()) run(() -> cdao.add(n.trim()));
        });
        ren.addActionListener(e -> {
            Category c = cList.getSelectedValue();
            if (c == null) { JOptionPane.showMessageDialog(this, "Select a category first."); return; }
            String n = JOptionPane.showInputDialog(this, "New name:", c.getName());
            if (n != null && !n.trim().isEmpty()) run(() -> cdao.rename(c.getId(), n.trim()));
        });
        del.addActionListener(e -> {
            Category c = cList.getSelectedValue();
            if (c == null) { JOptionPane.showMessageDialog(this, "Select a category first."); return; }
            if (JOptionPane.showConfirmDialog(this, "Delete '" + c.getName() + "' and ALL its questions/results?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) run(() -> cdao.delete(c.getId()));
        });
        return p;
    }

    private void openDialog(Question existing) {
        if (categories.isEmpty()) { JOptionPane.showMessageDialog(this, "Create a category first."); return; }
        JComboBox<Category> cat = new JComboBox<>(categories.toArray(new Category[0]));
        JTextArea text = new JTextArea(3, 30); text.setLineWrap(true); text.setWrapStyleWord(true);
        JTextField[] o = new JTextField[4];
        for (int i = 0; i < 4; i++) o[i] = new JTextField(30);
        JComboBox<String> correct = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        if (existing != null) {
            for (int i = 0; i < categories.size(); i++) if (categories.get(i).getId() == existing.getCategoryId()) cat.setSelectedIndex(i);
            text.setText(existing.getText());
            for (int i = 0; i < 4; i++) o[i].setText(existing.getOptions()[i]);
            correct.setSelectedIndex(existing.getCorrectIndex());
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Category")); form.add(cat);
        form.add(new JLabel("Question")); form.add(new JScrollPane(text));
        for (int i = 0; i < 4; i++) { form.add(new JLabel("Option " + (char) ('A' + i))); form.add(o[i]); }
        form.add(new JLabel("Correct answer")); form.add(correct);
        Theme.apply(form);
        if (JOptionPane.showConfirmDialog(this, form, existing == null ? "Add Question" : "Edit Question",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        if (text.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Question text is required."); return; }
        for (JTextField f : o) if (f.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "All 4 options are required."); return; }
        Question q = new Question(existing == null ? 0 : existing.getId(), ((Category) cat.getSelectedItem()).getId(),
            text.getText().trim(), o[0].getText().trim(), o[1].getText().trim(), o[2].getText().trim(), o[3].getText().trim(),
            ((String) correct.getSelectedItem()).charAt(0));
        run(() -> { if (existing == null) qdao.add(q); else qdao.update(q); });
    }

    private interface Action { void go() throws Exception; }
    private void run(Action a) {
        try { a.go(); reload(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void reload() {
        try {
            questions = qdao.findAll(); categories = cdao.findAll();
            qModel.setRowCount(0);
            for (Question q : questions) {
                String[] op = q.getOptions();
                qModel.addRow(new Object[]{q.getId(), q.getCategoryName(), q.getText(), op[0], op[1], op[2], op[3], q.getCorrect()});
            }
            cModel.clear(); for (Category c : categories) cModel.addElement(c);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage()); }
    }
}
