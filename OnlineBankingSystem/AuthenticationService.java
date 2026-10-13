import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthenticationService {

    public boolean register(String username, String password) {

        if (username == null || !username.matches("[a-zA-Z0-9_]{3,30}")) {
            System.out.println(
                    "Username must contain 3-30 letters, numbers, or underscores.");
            return false;
        }

        if (!isValidPassword(password)) {
            System.out.println(
                    "Password must be at least 8 characters and contain uppercase, lowercase, and a digit.");
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            String passwordHash = hashPassword(password);

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, passwordHash);

                statement.executeUpdate();

                System.out.println("Registration successful!");
                return true;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Username already exists.");
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }

            return false;
        }
    }

    public User login(String username, String password) {

        String sql = "SELECT user_id, username, password_hash FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    String storedHash = result.getString("password_hash");

                    if (verifyPassword(password, storedHash)) {
                        System.out.println("Login successful!");

                        return new User(
                                result.getInt("user_id"),
                                result.getString("username"));
                    }
                }

                System.out.println("Invalid username or password.");
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean digit = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) uppercase = true;
            if (Character.isLowerCase(ch)) lowercase = true;
            if (Character.isDigit(ch)) digit = true;
        }

        return uppercase && lowercase && digit;
    }

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);

            byte[] hash = digest.digest(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password.", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);

            byte[] actualHash = digest.digest(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }
    }
}
