import java.util.HashMap;
import java.util.Map;

public class BankingService {
    private final Map<String, User> users = new HashMap<>();

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }

        if (!isValidPassword(password)) {
            System.out.println(
                    "Password must be at least 8 characters "
                    + "and contain uppercase, lowercase, "
                    + "and a digit.");
            return false;
        }

        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return false;
        }

        users.put(username, new User(username, password));

        System.out.println("Registration successful!");
        System.out.println("Welcome, " + username + "!");
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            }

            if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            }

            if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit;
    }

    public User login(String username, String password) {
        User user = users.get(username);

        if (user != null && user.validatePassword(password)) {
            System.out.println("Login successful!");
            System.out.println("Welcome back, " + username + "!");
            return user;
        }

        System.out.println("Invalid username or password.");
        return null;
    }

    public User findUser(String username) {
        return users.get(username);
    }
}
