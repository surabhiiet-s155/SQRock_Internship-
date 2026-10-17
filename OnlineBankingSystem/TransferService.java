import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferService {

    public boolean transferMoney(
            int senderAccountId,
            String receiverAccountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2 || amount.precision() > 15) {
            System.out.println("Invalid transfer amount.");
            return false;
        }

        if (receiverAccountNumber == null
                || !receiverAccountNumber.matches("\\d{12}")) {
            System.out.println("Invalid receiver account number.");
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int receiverAccountId;
                BigDecimal senderBalance;

                // Get and lock sender account
                String senderQuery =
                        "SELECT balance FROM accounts WHERE account_id = ? FOR UPDATE";

                try (PreparedStatement ps =
                             connection.prepareStatement(senderQuery)) {

                    ps.setInt(1, senderAccountId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Sender account not found.");
                        }

                        senderBalance = rs.getBigDecimal("balance");
                    }
                }

                // Find and lock receiver account
                String receiverQuery =
                        "SELECT account_id FROM accounts "
                        + "WHERE account_number = ? FOR UPDATE";

                try (PreparedStatement ps =
                             connection.prepareStatement(receiverQuery)) {

                    ps.setString(1, receiverAccountNumber);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Receiver account not found.");
                        }

                        receiverAccountId = rs.getInt("account_id");
                    }
                }

                if (senderAccountId == receiverAccountId) {
                    throw new SQLException("You cannot transfer money to your own account.");
                }

                if (senderBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance.");
                }

                // Deduct money from sender
                String deductQuery =
                        "UPDATE accounts SET balance = balance - ? "
                        + "WHERE account_id = ? AND balance >= ?";

                try (PreparedStatement ps =
                             connection.prepareStatement(deductQuery)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, senderAccountId);
                    ps.setBigDecimal(3, amount);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not deduct sender balance.");
                    }
                }

                // Add money to receiver
                String addQuery =
                        "UPDATE accounts SET balance = balance + ? "
                        + "WHERE account_id = ?";

                try (PreparedStatement ps =
                             connection.prepareStatement(addQuery)) {

                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, receiverAccountId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not update receiver balance.");
                    }
                }

                // Record sender transaction
                recordTransaction(
                        connection, senderAccountId,
                        "TRANSFER", amount, receiverAccountId);

                // Record receiver transaction
                recordTransaction(
                        connection, receiverAccountId,
                        "TRANSFER", amount, senderAccountId);

                connection.commit();

                System.out.println("Transfer successful!");
                System.out.println("Amount transferred: ₹" + amount);
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Transfer failed: " + e.getMessage());
                return false;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private void recordTransaction(
            Connection connection,
            int accountId,
            String type,
            BigDecimal amount,
            int relatedAccountId) throws SQLException {

        String query =
                "INSERT INTO transactions "
                + "(account_id, transaction_type, amount, related_account_id) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setInt(4, relatedAccountId);
            ps.executeUpdate();
        }
    }
}
