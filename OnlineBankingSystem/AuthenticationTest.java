import java.util.Scanner;

public class AuthenticationTest {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        AuthenticationService auth = new AuthenticationService();

        while (true) {
            System.out.println("\n===== AUTHENTICATION TEST =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    System.out.print("Enter username: ");
                    String newUsername = scanner.nextLine().trim();

                    System.out.print("Enter password: ");
                    String newPassword = scanner.nextLine();

                    auth.register(newUsername, newPassword);
                    break;

                case "2":
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine().trim();

                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    User user = auth.login(username, password);

                    if (user != null) {
                        System.out.println(
                                "Welcome, " + user.getUsername() + "!");
                    }
                    break;

                case "3":
                    System.out.println("Test completed.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
