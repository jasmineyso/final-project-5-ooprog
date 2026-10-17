package src.menu;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Input;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    private final Scanner sc = Input.getScanner();
    LogIn login;
    SignUp signup;
    private ArrayList<RegularUser> users = new ArrayList<>();

    public Menu() {
        this.login = new LogIn();
        this.signup = new SignUp();
        try {
            users = AccountStorage.getInstance().loadUsers();
        } catch (IOException e) {
            System.out.println(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }
    
    public void Menus() {
        boolean exit = false;

        while (!exit) {

        System.out.println("Welcome to the ___ Airline!");
        System.out.println("1. Log in");
        System.out.println("2. Sign up");
        System.out.println("3. View available flights");
        System.out.println("4. Exit");

        System.out.print("Enter your choice: ");
        String userChoice = sc.nextLine();

            switch (userChoice) {
                case "1" -> {
                    login.userType();
                }
                case "2" -> {
                    RegularUser user = signup.signUp();
                    try {
                        AccountStorage.getInstance().saveUser(user);
                        users.add(user);
                        System.out.println("Account created and saved successfully!");
                    } catch (IOException e) {
                        System.out.println(ErrorMessages.AccountErrors.SAVE_FAILED + e.getMessage());
                    }
                }
                case "3" -> {
                    // Add logic to view available flights
                }
                case "4" -> {
                    System.out.println("Thank you for using the ___ Airline. Goodbye!");
                    exit = true;
                }
                default -> System.out.println(ErrorMessages.SwitchError.INVALID_CHOICE);
            }
        }
    }
}
