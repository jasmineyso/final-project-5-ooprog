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
        // Add your login logic here
        // Use the USER_ACCOUNTS list to validate the username and password
    }

    public void loginAdmin() {
        // Add your admin login logic here
        // Use the USER_ACCOUNTS list to validate the username and password
    }
}
