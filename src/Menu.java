package src;
import java.util.Scanner;

public class Menu {

    Scanner sc;
    LogIn login;


    public Menu(Scanner sc) {
        this.sc = sc;
        this.login = new LogIn(sc);
    }
    
    public void Menus() {
        System.out.println("Welcome to the ___ Airline!");
        System.out.println("1. Log in");
        System.out.println("2. Sign up");
        System.out.println("3. View available flights");
        System.out.println("4. Exit");

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your choice: ");
        String userChoice = sc.nextLine();

        switch (userChoice) {
            case "1" -> {
                login.userType();
            }
            case "2" -> {
                SignUp signup = new SignUp();
            }
            case "3" -> {
                // Add logic to view available flights
            }
            case "4" -> {
                System.out.println("Thank you for using the ___ Airline. Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice. Please try again.");
        }
        sc.close();
    }
}
