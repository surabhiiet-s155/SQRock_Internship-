package com.quiz.dao;

import com.quiz.db.DBConnection;
import com.quiz.model.Question;
import java.sql.*;
import java.util.*;

public class QuestionDAO {
    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question(rs.getInt("id"), rs.getInt("category_id"), rs.getString("question_text"),
            rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"),
            rs.getString("option_d"), rs.getString("correct_option").charAt(0));
        try { q.setCategoryName(rs.getString("cat_name")); } catch (SQLException ignored) {}
        return q;
    }

    /** Random question generation: ORDER BY RAND() LIMIT n. */
    public List<Question> randomByCategory(int categoryId, int limit) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM questions WHERE category_id=? ORDER BY RAND() LIMIT ?");
            ps.setInt(1, categoryId); ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Question> findAll() throws SQLException {
        List<Question> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             ResultSet rs = c.createStatement().executeQuery(
                 "SELECT q.*, c.name AS cat_name FROM questions q JOIN categories c ON q.category_id=c.id ORDER BY c.name, q.id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void add(Question q) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO questions(category_id,question_text,option_a,option_b,option_c,option_d,correct_option) VALUES(?,?,?,?,?,?,?)");
            fill(ps, q); ps.executeUpdate();
        }
    }

    public void update(Question q) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "UPDATE questions SET category_id=?,question_text=?,option_a=?,option_b=?,option_c=?,option_d=?,correct_option=? WHERE id=?");
            fill(ps, q); ps.setInt(8, q.getId()); ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM questions WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private void fill(PreparedStatement ps, Question q) throws SQLException {
        ps.setInt(1, q.getCategoryId()); ps.setString(2, q.getText());
        for (int i = 0; i < 4; i++) ps.setString(3 + i, q.getOptions()[i]);
        ps.setString(7, String.valueOf(q.getCorrect()));
    }
}
