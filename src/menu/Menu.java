package src.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Display;
import src.util.Input;

public class Menu {

    private final Scanner sc = Input.getScanner();
    private final LogIn login;
    private final SignUp signup;
    private ArrayList<RegularUser> users = new ArrayList<>();

    public Menu() {
        this.login = new LogIn(sc);
        this.signup = new SignUp();
        try {
            users = AccountStorage.getInstance().loadUsers();
        } catch (IOException e) {
            Display.error(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }

    /** First screen of the program: pick Admin or Regular User. */
    public void welcomePage() {
        Display.message("Welcome to the ___ Airline!");

        boolean running = true;
        while (running) {
            int choice = Input.askChoice("What type of user are you?",
                    "Admin",
                    "Regular User",
                    "Exit");

            switch (choice) {
                case 1 -> login.loginAdmin();
                case 2 -> regularUserPage();
                case 3 -> {
                    Display.message("Thank you for using the ___ Airline. Goodbye!");
                    running = false;
                }
            }
        }
    }

    /** Regular User screen: sign up, log in, or go back to the first screen. */
    private void regularUserPage() {
        boolean running = true;
        while (running) {
            int choice = Input.askChoice("Regular User",
                    "Sign Up",
                    "Log In",
                    "Back");

            switch (choice) {
                case 1 -> signUp();
                case 2 -> login.loginRegularUser();
                case 3 -> running = false;
            }
        }
    }

    private void signUp() {
        RegularUser user = signup.signUp();
        try {
            AccountStorage.getInstance().saveUser(user);
            users.add(user);
            Display.success("Account created and saved successfully! You can now log in.");
        } catch (IOException e) {
            Display.error(ErrorMessages.AccountErrors.SAVE_FAILED + e.getMessage());
        }
    }
}