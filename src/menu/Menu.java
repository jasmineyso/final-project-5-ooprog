package src.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Input;
import src.util.Display;

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
            System.out.println(ErrorMessages.LOAD_FAILED + e.getMessage());
        }
    }

    /** First screen of the program: pick Admin or Regular User. */
    public void welcomePage() {
        try {
            runWelcomePage();
        } catch (Input.EndOfInputException e) {
            System.out.println("Input ended. Goodbye!");
        }
    }

    private void runWelcomePage() {
        Display.title("Welcome to the Airline Booking System!");

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
                    System.out.print("Are you sure you want to exit? (y/n): ");
                    String confirm = Input.readLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        Display.success("Thank you for using the ___ Airline. Goodbye!");
                        running = false;
                    } else {
                        Display.error("Exit cancelled.");
                    }
                }
            }
        }
    }

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
                case 3 -> {
                    System.out.print("Are you sure you want to go back? (y/n): ");
                    String confirm = Input.readLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        running = false;
                    }
                }
            }
        }
    }

    private void signUp() {
        RegularUser user = signup.signUp();
        try {
            AccountStorage.getInstance().saveUser(user);
            users.add(user);
            System.out.println("Account created and saved successfully! You can now log in.");
        } catch (IOException e) {
            System.out.println(ErrorMessages.SAVE_FAILED + e.getMessage());
        }
    }
}