package com.quiz.dao;

import com.quiz.db.DBConnection;
import com.quiz.model.QuizResult;
import java.sql.*;
import java.util.*;

public class ResultDAO {
    public void save(int userId, int categoryId, QuizResult r) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO results(user_id,category_id,total,correct,wrong,unanswered,score,percentage) VALUES(?,?,?,?,?,?,?,?)");
            ps.setInt(1, userId); ps.setInt(2, categoryId); ps.setInt(3, r.total);
            ps.setInt(4, r.correct); ps.setInt(5, r.wrong); ps.setInt(6, r.unanswered);
            ps.setDouble(7, r.score); ps.setDouble(8, r.percentage);
            ps.executeUpdate();
        }
    }

    /** Quiz history for one user. */
    public List<Object[]> history(int userId) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT r.taken_at, c.name, r.total, r.correct, r.wrong, r.score, r.percentage " +
                "FROM results r JOIN categories c ON r.category_id=c.id WHERE r.user_id=? ORDER BY r.taken_at DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rows.add(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getInt(3),
                rs.getInt(4), rs.getInt(5), String.format("%.2f", rs.getDouble(6)), String.format("%.1f%%", rs.getDouble(7))});
        }
        return rows;
    }

    /** Leaderboard (top 10 attempts). categoryId <= 0 means all categories. */
    public List<Object[]> leaderboard(int categoryId) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT u.full_name, c.name, r.score, r.percentage, r.taken_at FROM results r " +
            "JOIN users u ON r.user_id=u.id JOIN categories c ON r.category_id=c.id " +
            (categoryId > 0 ? "WHERE r.category_id=? " : "") +
            "ORDER BY r.percentage DESC, r.taken_at ASC LIMIT 10";
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(sql);
            if (categoryId > 0) ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) rows.add(new Object[]{rank++, rs.getString(1), rs.getString(2),
                String.format("%.2f", rs.getDouble(3)), String.format("%.1f%%", rs.getDouble(4)), rs.getTimestamp(5)});
        }
        return rows;
    }
}
