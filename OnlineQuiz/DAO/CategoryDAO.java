package com.quiz.dao;

import com.quiz.db.DBConnection;
import com.quiz.model.Category;
import java.sql.*;
import java.util.*;

public class CategoryDAO {
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             ResultSet rs = c.createStatement().executeQuery("SELECT id,name FROM categories ORDER BY name")) {
            while (rs.next()) list.add(new Category(rs.getInt(1), rs.getString(2)));
        }
        return list;
    }

    public void add(String name) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO categories(name) VALUES(?)");
            ps.setString(1, name); ps.executeUpdate();
        }
    }

    public void rename(int id, String name) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement("UPDATE categories SET name=? WHERE id=?");
            ps.setString(1, name); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM categories WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}
