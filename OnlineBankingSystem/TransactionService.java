import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionService {

    public boolean deposit(int accountId, BigDecimal amount) {
        if (!isValidAmount(amount)) {
            System.out.println("Invalid deposit amount.");
            return false;
        }

        String updateSql =
                "UPDATE accounts SET balance = balance + ? "
                + "WHERE account_id = ?";

        String transactionSql =
                "INSERT INTO transactions "
                + "(account_id, transaction_type, amount) "
                + "VALUES (?, 'DEPOSIT', ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement update =
                             connection.prepareStatement(updateSql)) {
                    update.setBigDecimal(1, amount);
                    update.setInt(2, accountId);

                    if (update.executeUpdate() != 1) {
                        connection.rollback();
                        System.out.println("Account not found.");
                        return false;
                    }
                }

                try (PreparedStatement insert =
                             connection.prepareStatement(transactionSql)) {
                    insert.setInt(1, accountId);
                    insert.setBigDecimal(2, amount);
                    insert.executeUpdate();
                }

                connection.commit();
                System.out.println("Deposit successful!");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(int accountId, BigDecimal amount) {
        if (!isValidAmount(amount)) {
            System.out.println("Invalid withdrawal amount.");
            return false;
        }

        String updateSql =
                "UPDATE accounts "
                + "SET balance = balance - ? "
                + "WHERE account_id = ? AND balance >= ?";

        String transactionSql =
                "INSERT INTO transactions "
                + "(account_id, transaction_type, amount) "
                + "VALUES (?, 'WITHDRAWAL', ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement update =
                             connection.prepareStatement(updateSql)) {
                    update.setBigDecimal(1, amount);
                    update.setInt(2, accountId);
                    update.setBigDecimal(3, amount);

                    if (update.executeUpdate() != 1) {
                        connection.rollback();
                        System.out.println(
                                "Withdrawal failed: account not found "
                                + "or insufficient balance.");
                        return false;
                    }
                }

                try (PreparedStatement insert =
                             connection.prepareStatement(transactionSql)) {
                    insert.setInt(1, accountId);
                    insert.setBigDecimal(2, amount);
                    insert.executeUpdate();
                }

                connection.commit();
                System.out.println("Withdrawal successful!");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
            return false;
        }
    }

    public BigDecimal getBalance(int accountId) {
        String sql =
                "SELECT balance FROM accounts WHERE account_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBigDecimal("balance");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve balance: "
                    + e.getMessage());
        }

        return null;
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.scale() <= 2
                && amount.precision() <= 15;
    }
}
