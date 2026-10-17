package src.menu;

import java.util.Scanner;

import src.util.Display;

public class LogIn {

    private final Scanner sc;

    public LogIn(Scanner sc) {
        this.sc = sc;
    }

    public void loginAdmin() {
        Display.title("Admin Log In");
        Display.prompt("Enter admin username");
        String username = sc.nextLine();
        Display.prompt("Enter admin password");
        String password = sc.nextLine();

        // TODO: check the username and password against ADMIN_ACCOUNTS.
        // If correct:  new AdminMenu(admin).start();
        // If wrong:    Display.error(ErrorMessages.LogInErrors.INVALID_CREDENTIALS);
        // Either way, the program returns to the "What type of user are you?" screen afterwards.
    }

    public void loginRegularUser() {
        Display.title("Log In");
        Display.prompt("Enter username");
        String username = sc.nextLine();
        Display.prompt("Enter password");
        String password = sc.nextLine();

        // TODO: check the username and password against USER_ACCOUNTS.
        // If correct:  open the passenger menu.
        // If wrong:    Display.error(ErrorMessages.LogInErrors.INVALID_CREDENTIALS);
        // Either way, the program returns to the Sign Up / Log In screen afterwards.
    }
}