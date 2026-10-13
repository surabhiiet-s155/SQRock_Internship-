import java.util.Scanner;

public class AccountTest {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        AuthenticationService auth = new AuthenticationService();
        AccountService accountService = new AccountService();

        System.out.println("===== BANK ACCOUNT TEST =====");
        System.out.println("1. Login and create account");
        System.out.println("2. Login and view account");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("3")) {
            System.out.println("Test completed.");
            scanner.close();
            return;
        }

        if (!choice.equals("1") && !choice.equals("2")) {
            System.out.println("Invalid choice.");
            scanner.close();
            return;
        }

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = auth.login(username, password);

        if (user == null) {
            System.out.println("Please log in with a registered account.");
            scanner.close();
            return;
        }

        if (choice.equals("1")) {
            accountService.createAccount(user.getUserId());
        } else {
            BankAccount account =
                    accountService.getAccountByUserId(user.getUserId());

            if (account != null) {
                System.out.println("\n===== ACCOUNT DETAILS =====");
                System.out.println("Account number: "
                        + account.getAccountNumber());
                System.out.println("Balance: ₹" + account.getBalance());
            } else {
                System.out.println(
                        "No bank account found. Create one first.");
            }
        }

        scanner.close();
    }
}
