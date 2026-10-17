package src;

import java.util.Scanner;

public class UserInterface {
    // Add your user interface logic here
    // After the user logs in or signs up, it will lead you here.
    // If the user is an admin, it will lead you to the AdminUI(can be changed)
    // class.
    // If the user is a regular user, it will lead you to the UserUI(can be changed)
    // class.
    private static UserInterface instance;
    private final Scanner sc = Input.getScanner();

    public static UserInterface getInstance() {
        if (instance == null) {
            instance = new UserInterface();
        }
        return instance;
    }

    private UserInterface() {
    }

    public void adminUI() {
        boolean isTrue = true;
        while (isTrue) {
            System.out.println("=== Admin Menu ===");
            System.out.println("1. ");
            System.out.println("2. ");
            System.out.println("3. ");
            System.out.println("4. ");

            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1" -> {
                    // Add logic to view all users
                }
                case "2" -> {
                    // Add logic to add a new user
                }
                case "3" -> {
                    // Add logic to remove a user
                }
                case "4" -> {
                    System.out.println("Logging out...");
                    isTrue = false;
                }
                default -> System.out.println(ErrorMessages.switcherror.INVALID_CHOICE);
            }

        }
    }

    public void userUI() {
        // Add your regular user interface logic here
    }
}