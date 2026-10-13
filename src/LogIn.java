package src;
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

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your choice: ");
        String userChoice = sc.nextLine();

        switch (userChoice) {
            case "1" -> loginAdmin();
            case "2" ->loginRegularUser();
            default -> System.out.println(ErrorMessages.switcherror.INVALID_CHOICE);
        }
    }

    public void loginRegularUser() {
        System.out.print("Enter your username: ");
        String username = sc.nextLine();
        System.out.print("Enter your password: ");
        String password = sc.nextLine();

        // Add your login logic here
    }

    public void loginAdmin() {
        System.out.println("==== ADMIN LOGIN ====");
        System.out.print("Enter your admin username: ");
        String adminUsername = sc.nextLine();
        System.out.print("Enter your admin password: ");
        String adminPassword = sc.nextLine();

        // Add your admin login logic here
    }
}
