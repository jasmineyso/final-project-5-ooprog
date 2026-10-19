package src.menu;

import java.io.IOException;
import java.util.Scanner;

import src.account.AccountStorage;
import src.account.Admin;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Display;

public class LogIn {

    private final Scanner sc;

    public LogIn(Scanner sc) {
        this.sc = sc;
    }

    public void loginAdmin() {
        Display.title("Admin Log In");
        Display.prompt("Enter admin username");
        String username = sc.nextLine().trim();
        Display.prompt("Enter admin password");
        String password = sc.nextLine();

        Admin admin = findAdmin(username, password);
        if (admin == null) {
            Display.error("Invalid username or password. Please try again.");
            return;
        }
        Display.message("Welcome, " + admin.getUsername() + "!");
        UserInterface.getInstance().adminUI();
    }

    /** Returns the admin with this username and password, or null if there is none. */
    private Admin findAdmin(String username, String password) {
        try {
            for (Admin admin : AccountStorage.getInstance().loadAdmins()) {
                if (admin.getUsername().equals(username) && admin.checkPassword(password)) {
                    return admin;
                }
            }
        } catch (IOException e) {
            Display.error("Admin accounts could not be loaded: " + e.getMessage());
        }
        return null;
    }

    public void loginRegularUser() {
        Display.title("Log In");
        Display.prompt("Enter username");
        String username = sc.nextLine().trim();
        Display.prompt("Enter password");
        String password = sc.nextLine();

        try {
            RegularUser user = AccountStorage.getInstance().findUser(username);
            if (user == null || !user.checkPassword(password)) {
                Display.error(ErrorMessages.INVALID_CREDENTIALS);
                return;
            }
            Display.message("Login successful! Welcome, " + user.getUsername() + "!");
            UserInterface.getInstance().userUI(user);
        } catch (IOException e) {
            Display.error(ErrorMessages.LOAD_FAILED + e.getMessage());
        }
    }
}
