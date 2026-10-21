package com.quiz.dao;

import com.quiz.db.DBConnection;
import com.quiz.model.User;
import com.quiz.util.PasswordUtil;
import java.sql.*;

public class UserDAO {
    /** @return true if registered, false if username already exists. */
    public boolean register(String username, String password, String fullName) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement chk = c.prepareStatement("SELECT id FROM users WHERE username=?");
            chk.setString(1, username);
            if (chk.executeQuery().next()) return false;
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users (username,password_hash,full_name,role) VALUES (?,?,?,'USER')");
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.executeUpdate();
            return true;
        }
    }

    /** @return the User, or null when credentials are wrong. */
    public User login(String username, String password) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,full_name,role FROM users WHERE username=? AND password_hash=?");
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            return null;
        }
    }
}
