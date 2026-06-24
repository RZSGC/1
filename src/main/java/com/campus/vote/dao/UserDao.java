package com.campus.vote.dao;

import com.campus.vote.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    public CreateUserResult createUser(String username, String password, String displayName, String role) throws Exception {
        String sql = "INSERT INTO users(username, password_hash, display_name, role) VALUES(?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeUsername(username));
            ps.setString(2, hashPassword(password));
            ps.setString(3, displayName);
            ps.setString(4, normalizeRole(role));
            ps.executeUpdate();
            return CreateUserResult.SUCCESS;
        } catch (SQLException e) {
            if (isConstraintViolation(e)) {
                return CreateUserResult.DUPLICATE_USERNAME;
            }
            throw e;
        }
    }

    public User findByCredentials(String username, String password) throws Exception {
        String sql = "SELECT id, username, display_name, role FROM users WHERE username = ? AND password_hash = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeUsername(username));
            ps.setString(2, hashPassword(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setDisplayName(rs.getString("display_name"));
                user.setRole(rs.getString("role"));
                return user;
            }
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private String normalizeRole(String role) {
        return "ADMIN".equals(role) ? "ADMIN" : "USER";
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private boolean isConstraintViolation(SQLException e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("constraint");
    }

    public enum CreateUserResult {
        SUCCESS,
        DUPLICATE_USERNAME
    }
}
