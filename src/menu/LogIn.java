package src.menu;

import java.io.IOException;
import java.util.Scanner;

import src.account.AccountStorage;
import src.account.Admin;
import src.account.RegularUser;
import src.core.ErrorMessages;

public class LogIn {

    private final Scanner sc;

    public LogIn(Scanner sc) {
        this.sc = sc;
    }

    public void loginAdmin() {
        System.out.println();
        System.out.println("=== Admin Log In ===");
        System.out.print("Enter admin username: ");
        String username = sc.nextLine();
        System.out.print("Enter admin password: ");
        String password = sc.nextLine();

        try {
            for (Admin user : AccountStorage.getInstance().loadAdmins()) {
                if (user.getUsername().equals(username) && user.checkPassword(password)) {
                    System.out.println("Login successful! Welcome, " + user.getUsername() + "!");
                    UserInterface.getInstance().adminUI();
                    return;
                }
            }
            System.out.println(ErrorMessages.LogInErrors.INVALID_CREDENTIALS);
        } catch (IOException e) {
            System.out.println(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }

    public void loginRegularUser() {
        System.out.println();
        System.out.println("=== Log In ===");
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        try {
            for (RegularUser user : AccountStorage.getInstance().loadUsers()) {
                if (user.getUsername().equals(username) && user.checkPassword(password)) {
                    System.out.println("Login successful! Welcome, " + user.getUsername() + "!");
                    UserInterface.getInstance().userUI();
                    return;
                }
            }
            System.out.println(ErrorMessages.LogInErrors.INVALID_CREDENTIALS);
        } catch (IOException e) {
            System.out.println(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }
}
