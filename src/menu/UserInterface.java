package src.menu;

import java.util.Scanner;

import src.account.RegularUser;
import src.core.ErrorMessages;
import src.menu.admin.AdminAccountMenu;
import src.menu.admin.AdminBookingMenu;
import src.menu.admin.AdminFlightMenu;
import src.menu.regular_user.RegularUserOperations;
import src.util.Display;
import src.util.Input;

public class UserInterface {
<<<<<<< HEAD

=======
    // Add your user interface logic here
    // After the user logs in or signs up, it will lead you here.
    // If the user is an admin, it will lead you to the AdminUI(can be changed)
    // class.
    // If the user is a regular user, it will lead you to the UserUI(can be changed)
    // class.
>>>>>>> a93f2d912a7f82202bf7dc1d4266f061eb7a7641
    private static UserInterface instance;
    private final Scanner sc = Input.getScanner();

    private UserInterface() {}

    public static UserInterface getInstance() {
        if (instance == null) {
            instance = new UserInterface();
        }
        return instance;
    }

    public void adminUI() {
        boolean running = true;
        while (running) {
            int choice = Input.askChoice("Admin Menu",
                    "Manage Flights",
                    "Manage Passenger Bookings",
                    "Manage User Accounts",
                    "Log Out");

            switch (choice) {
                case 1 -> new AdminFlightMenu().start();
                case 2 -> new AdminBookingMenu().start();
                case 3 -> new AdminAccountMenu().start();
                case 4 -> {
<<<<<<< HEAD
                    System.out.print("Are you sure you want to log out? (y/n): ");
                    String confirm = sc.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        Display.message("Logging out...");
                        running = false;
                    } else {
                        Display.message("Log out cancelled.");
                    }
=======
                    Display.message("Logging out...");
                    running = false;
>>>>>>> a93f2d912a7f82202bf7dc1d4266f061eb7a7641
                }
            }
        }
    }

    /** The passenger's main menu. 'user' is whoever logged in; bookings will use it. */
    public void userUI(RegularUser user) {
        boolean running = true;
        while (running) {
            int choice = Input.askChoice("User Menu",
                    "View Flights",
                    "View Booked Flights",
                    "Book a Flight",
                    "Pay for Booked Flight",
                    "Update Booked Flight",
                    "Cancel Booked Flight",
                    "Log Out");

            switch (choice) {
                case 1 -> RegularUserOperations.getInstance().viewFlights();
                case 2 -> RegularUserOperations.getInstance().viewBookedFlights(user);
                case 3 -> RegularUserOperations.getInstance().bookFlight(user);
                case 4 -> RegularUserOperations.getInstance().payBooking(user);
                case 5, 6 -> Display.message("Not available yet.");
                case 7 -> {
<<<<<<< HEAD
                    System.out.print("Are you sure you want to log out? (y/n): ");
                    String confirm = sc.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        Display.message("Logging out...");
                        running = false;
                    } else {
                        Display.message("Log out cancelled.");
                    }
=======
                    Display.message("Logging out...");
                    running = false;
>>>>>>> a93f2d912a7f82202bf7dc1d4266f061eb7a7641
                }
            }
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> a93f2d912a7f82202bf7dc1d4266f061eb7a7641
