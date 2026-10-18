package src.menu;

import java.util.Scanner;

import src.core.ErrorMessages;
import src.menu.admin.AdminAccountMenu;
import src.menu.admin.AdminBookingMenu;
import src.menu.admin.AdminFlightMenu;
import src.menu.regular_user.RegularUserOperations;
import src.util.Input;

public class UserInterface {
    // Add your user interface logic here
    // After the user logs in or signs up, it will lead you here.
    // If the user is an admin, it will lead you to the AdminUI(can be changed)
    // class.
    // If the user is a regular user, it will lead you to the UserUI(can be changed)
    // class.
    private static UserInterface instance;
    private final Scanner sc = Input.getScanner();

    private UserInterface() {
    }

    public static UserInterface getInstance() {
        if (instance == null) {
            instance = new UserInterface();
        }
        return instance;
    }



    public void adminUI() {
        AdminFlightMenu flights = new AdminFlightMenu();
        AdminBookingMenu bookings = new AdminBookingMenu();
        AdminAccountMenu accounts = new AdminAccountMenu();
        boolean isTrue = true;
        while (isTrue) {
            System.out.println("=== Admin Menu ===");
            System.out.println("1. Add Flight");
            System.out.println("2. Update Flight");
            System.out.println("3. Update Flight Status");
            System.out.println("4. Remove Flight");
            System.out.println("5. Search Flight");
            System.out.println("6. View Flights");
            System.out.println("7. View Passenger Bookings");
            System.out.println("8. Manage Passenger Bookings");
            System.out.println("9. Manage User Accounts");
            System.out.println("10. Log Out");

            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1" -> {
                    flights.addFlight();
                }
                case "2" -> {
                    flights.updateFlight();
                }
                case "3" -> {
                    flights.updateFlightStatus();
                }
                case "4" -> {
                    flights.removeFlight();
                }
                case "5" -> {
                    flights.searchFlights();
                }
                case "6" -> {
                    flights.viewFlights();
                }
                case "7" -> {
                    bookings.viewAllBookings();
                }
                case "8" -> {
                    bookings.start();
                }
                case "9" -> {
                    accounts.start();
                }
                case "10" -> {
                    System.out.println("Logging out...");
                    isTrue = false;
                }
                default -> System.out.println(ErrorMessages.SwitchError.INVALID_CHOICE);
            }
        }
    }

    public void userUI() {

        boolean isTrue = true;
        while (isTrue) {
            System.out.println("=== User Menu ===");
            System.out.println("1. View Flights");
            System.out.println("2. View Booked Flights");
            System.out.println("3. Book a Flight");
            System.out.println("4. Pay for Booked Flight");
            System.out.println("5. Update Booked Flight");
            System.out.println("6. Cancel Booked Flight");
            System.out.println("7. Log Out");

            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1" -> RegularUserOperations.getInstance().viewFlights();
                case "2" -> {

                }
                case "3" -> {

                }
                case "4" -> {

                }
                case "5" -> {

                }
                case "6" -> {

                }
                case "7" -> {
                    System.out.println("Logging out...");
                    isTrue = false;
                }
                default -> System.out.println(ErrorMessages.SwitchError.INVALID_CHOICE);
            }
        }

    }
}
