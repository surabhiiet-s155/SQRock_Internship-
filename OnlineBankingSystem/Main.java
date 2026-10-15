import java.math.BigDecimal;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final AuthenticationService authService =
            new AuthenticationService();

    private static final AccountService accountService =
            new AccountService();

    private static final TransactionService transactionService =
            new TransactionService();

    private static final TransferService transferService =
            new TransferService();

    private static final TransactionHistoryService historyService =
            new TransactionHistoryService();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===== ONLINE BANKING SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    registerUser();
                    break;

                case 2:
                    loginUser();
                    break;

                case 3:
                    System.out.println("Thank you for using our bank!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void registerUser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Create password: ");
        String password = scanner.nextLine();

        boolean registered = authService.register(username, password);

        if (registered) {
            User user = authService.login(username, password);

            if (user != null) {
                BankAccount account =
                        accountService.createAccount(user.getUserId());

                if (account != null) {
                    System.out.println("Registration successful!");
                    System.out.println(
                            "Your account number: "
                            + account.getAccountNumber());
                    System.out.println(
                            "Your account ID: " + account.getAccountId());
                } else {
                    System.out.println(
                            "Account creation failed. Please try again.");
                }
            }
        }
    }

    private static void loginUser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = authService.login(username, password);

        if (user != null) {
            BankAccount account =
                    accountService.getAccountByUserId(user.getUserId());

            if (account == null) {
                System.out.println("No bank account found for this user.");
                return;
            }

            System.out.println("Login successful!");
            System.out.println("Welcome, " + user.getUsername() + "!");

            showBankingMenu(account);
        }
    }

    private static void showBankingMenu(BankAccount account) {

        while (true) {
            System.out.println("\n===== BANKING MENU =====");
            System.out.println("1. View Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. Transaction History");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    viewBalance(account);
                    break;

                case 2:
                    depositMoney(account);
                    break;

                case 3:
                    withdrawMoney(account);
                    break;

                case 4:
                    transferMoney(account);
                    break;

                case 5:
                    historyService.viewHistory(account.getAccountId());
                    break;

                case 6:
                    System.out.println("Logged out successfully!");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void viewBalance(BankAccount account) {

        BigDecimal balance =
                transactionService.getBalance(account.getAccountId());

        if (balance != null) {
            System.out.printf("Current Balance: ₹%s%n", balance);
        } else {
            System.out.println("Unable to retrieve balance.");
        }
    }

    private static void depositMoney(BankAccount account) {

        System.out.print("Enter deposit amount: ₹");
        BigDecimal amount = readAmount();

        if (amount != null
                && transactionService.deposit(
                        account.getAccountId(), amount)) {
            System.out.println("Deposit successful!");
        } else {
            System.out.println("Deposit failed. Check the amount.");
        }
    }

    private static void withdrawMoney(BankAccount account) {

        System.out.print("Enter withdrawal amount: ₹");
        BigDecimal amount = readAmount();

        if (amount != null
                && transactionService.withdraw(
                        account.getAccountId(), amount)) {
            System.out.println("Withdrawal successful!");
        } else {
            System.out.println(
                    "Withdrawal failed. Check the amount and balance.");
        }
    }

    private static void transferMoney(BankAccount account) {

        System.out.print("Enter receiver's 12-digit account number: ");
        String receiverAccountNumber = scanner.nextLine().trim();

        System.out.print("Enter transfer amount: ₹");
        BigDecimal amount = readAmount();

        if (amount != null) {
            transferService.transferMoney(
                    account.getAccountId(),
                    receiverAccountNumber,
                    amount);
        }
    }

    private static BigDecimal readAmount() {

        try {
            return new BigDecimal(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
            return null;
        }
    }

    private static int readInt() {

        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }
}
