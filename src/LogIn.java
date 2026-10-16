package src;
import java.io.IOException;
import java.util.Scanner;

public class LogIn {

    Scanner sc;

    public LogIn(Scanner sc) {
        this.sc = sc;
    }

    public void userType() {
        System.out.println("What type of user are you?:");
        System.out.println("1. Admin");
        System.out.println("2. Regular User");

        System.out.print("Enter your choice: ");
        String userChoice = sc.nextLine();

        switch (userChoice) {
            case "1" -> loginAdmin();
            case "2" ->loginRegularUser();
            default -> System.out.println(ErrorMessages.switcherror.INVALID_CHOICE);
        }
    }

    public void loginRegularUser() {
        System.out.println("=== Regular User Login ===");
        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        try {
            AccountStorage accountStorage = new AccountStorage();
            for (RegularUser user : accountStorage.loadUsers()) {
                if (user.getUsername().equals(username) && user.checkPassword(password)) {
                    System.out.println("Login successful! Welcome, " + user.getUsername() + "!");
                    return;
                }
            }
            System.out.println(ErrorMessages.LogInErrors.INVALID_CREDENTIALS);
        } catch (IOException e) {
            System.out.println(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }

    public void loginAdmin() {
        // Add your admin login logic here
        // Use the USER_ACCOUNTS list to validate the username and password
    }
}
