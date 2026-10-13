import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {

    private final SecureRandom random = new SecureRandom();

    public BankAccount createAccount(int userId) {

        String sql = """
            INSERT INTO accounts (user_id, account_number, balance)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Prevent multiple accounts for the same user in this version.
            String checkSql =
                    "SELECT account_id FROM accounts WHERE user_id = ?";

            try (PreparedStatement check =
                         connection.prepareStatement(checkSql)) {

                check.setInt(1, userId);

                try (ResultSet result = check.executeQuery()) {
                    if (result.next()) {
                        System.out.println(
                                "This user already has a bank account.");
                        return getAccountByUserId(userId);
                    }
                }
            }

            String accountNumber = generateAccountNumber();

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 sql,
                                 PreparedStatement.RETURN_GENERATED_KEYS)) {

                statement.setInt(1, userId);
                statement.setString(2, accountNumber);
                statement.setBigDecimal(3, BigDecimal.ZERO);

                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        BankAccount account = new BankAccount(
                                keys.getInt(1),
                                userId,
                                accountNumber,
                                BigDecimal.ZERO);

                        System.out.println("Bank account created successfully!");
                        System.out.println(
                                "Account number: " + account.getAccountNumber());
                        System.out.println(
                                "Initial balance: ₹" + account.getBalance());

                        return account;
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not create account: " + e.getMessage());
        }

        return null;
    }

    public BankAccount getAccountByUserId(int userId) {

        String sql = """
            SELECT account_id, user_id, account_number, balance
            FROM accounts
            WHERE user_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new BankAccount(
                            result.getInt("account_id"),
                            result.getInt("user_id"),
                            result.getString("account_number"),
                            result.getBigDecimal("balance"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve account: " + e.getMessage());
        }

        return null;
    }

    private String generateAccountNumber() {
        // A 12-digit account number.
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            number.append(random.nextInt(10));
        }

        return number.toString();
    }
}
