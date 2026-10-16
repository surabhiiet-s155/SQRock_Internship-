import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionHistoryService {

    public void viewHistory(int accountId) {

        String query =
                "SELECT transaction_id, transaction_type, amount, "
                + "related_account_id, transaction_date "
                + "FROM transactions "
                + "WHERE account_id = ? "
                + "ORDER BY transaction_date DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\n========== TRANSACTION HISTORY ==========");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println("Transaction ID: "
                            + rs.getInt("transaction_id"));

                    System.out.println("Type: "
                            + rs.getString("transaction_type"));

                    System.out.println("Amount: ₹"
                            + rs.getBigDecimal("amount"));

                    System.out.println("Related Account ID: "
                            + rs.getObject("related_account_id"));

                    System.out.println("Date: "
                            + rs.getTimestamp("transaction_date"));

                    System.out.println("-----------------------------------------");
                }

                if (!found) {
                    System.out.println("No transactions found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not retrieve transaction history.");
            System.out.println(e.getMessage());
        }
    }
}
