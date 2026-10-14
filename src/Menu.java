package src;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    Scanner sc;
    LogIn login;
    SignUp signup;
    private ArrayList<RegularUser> users = new ArrayList<>();
    private AccountStorage accountStorage = new AccountStorage();

    public Menu(Scanner sc) {
        this.sc = sc;
        this.login = new LogIn(sc);
        this.signup = new SignUp(sc);
        try {
            users = accountStorage.loadUsers();
        } catch (IOException e) {
            System.out.println(ErrorMessages.AccountErrors.LOAD_FAILED + e.getMessage());
        }
    }
    
    public void Menus() {
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
                    accountStorage.saveUser(user);
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
                System.exit(0);
            }
            default -> System.out.println(ErrorMessages.switcherror.INVALID_CHOICE);
        }
        sc.close();
    }
}
