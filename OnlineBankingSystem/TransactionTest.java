import java.math.BigDecimal;
import java.util.Scanner;

public class TransactionTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        AuthenticationService auth = new AuthenticationService();
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService();

        System.out.println("===== TRANSACTION TEST =====");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = auth.login(username, password);

        if (user == null) {
            System.out.println("Login failed. Test stopped.");
            scanner.close();
            return;
        }

        BankAccount account =
                accountService.getAccountByUserId(user.getUserId());

        if (account == null) {
            System.out.println("No bank account found.");
            scanner.close();
            return;
        }

        while (true) {
            System.out.println("\n===== TRANSACTION MENU =====");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    BigDecimal balance =
                            transactionService.getBalance(
                                    account.getAccountId());

                    if (balance != null) {
                        System.out.println("Current balance: ₹" + balance);
                    }
                    break;

                case "2":
                    System.out.print("Enter deposit amount: ₹");
                    processTransaction(
                            scanner, transactionService,
                            account.getAccountId(), true);
                    break;

                case "3":
                    System.out.print("Enter withdrawal amount: ₹");
                    processTransaction(
                            scanner, transactionService,
                            account.getAccountId(), false);
                    break;

                case "4":
                    System.out.println("Transaction test completed.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void processTransaction(
            Scanner scanner,
            TransactionService service,
            int accountId,
            boolean isDeposit) {

        String input = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(input);

            if (isDeposit) {
                service.deposit(accountId, amount);
            } else {
                service.withdraw(accountId, amount);
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        }
    }
}
