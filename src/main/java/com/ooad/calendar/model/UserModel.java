package com.ooad.calendar.model;

import com.ooad.calendar.config.Database;
import com.ooad.calendar.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserModel {
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, username, full_name FROM users ORDER BY full_name")) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("full_name")));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load users", ex);
        }
        return users;
    }

    public User create(String fullName) {
        String normalizedName = fullName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Full name must not be empty");
        }
        int id = nextId();
        String username = uniqueUsername(normalizedName);
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users(id, username, full_name) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setString(2, username);
            statement.setString(3, normalizedName);
            statement.executeUpdate();
            return new User(id, username, normalizedName);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot create user", ex);
        }
    }

    public void delete(int userId) {
        try (Connection connection = Database.getConnection()) {
            executeDelete(connection, "DELETE FROM group_members WHERE user_id = ?", userId);
            executeDelete(connection, "DELETE FROM appointments WHERE owner_id = ?", userId);
            executeDelete(connection, "DELETE FROM users WHERE id = ?", userId);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot delete user", ex);
        }
    }

    private void executeDelete(Connection connection, String sql, int userId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private int nextId() {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 FROM users");
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot generate user id", ex);
        }
    }

    private String uniqueUsername(String fullName) {
        String base = fullName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "")
                .trim();
        if (base.isEmpty()) {
            base = "user";
        }
        String username = base;
        int suffix = 2;
        while (usernameExists(username)) {
            username = base + suffix;
            suffix++;
        }
        return username;
    }

    private boolean usernameExists(String username) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot check username", ex);
        }
    }
}
