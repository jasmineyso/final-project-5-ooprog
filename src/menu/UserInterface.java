package src.menu;

import src.account.RegularUser;
import src.core.ErrorMessages;
import src.menu.admin.AdminAccountMenu;
import src.menu.admin.AdminBookingMenu;
import src.menu.admin.AdminFlightMenu;
import src.menu.regular_user.RegularUserOperations;
import src.util.Display;
import src.util.Input;


public class UserInterface {
    private static UserInterface instance;
    private UserInterface() {}
    

    public synchronized static UserInterface getInstance() {
        if (instance == null) {
            instance = new UserInterface();
        }
        return instance;
    }

    public void adminUI() {
        try {
            runAdminUI();
        } catch (Input.EndOfInputException e) {
            Display.message("Input ended. Logging out...");
        }
    }

    private void runAdminUI() {
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
                    boolean isValid = false;

                    while(!isValid){
                        System.out.print("Are you sure you want to exit? (y/n): ");
                        String confirm = Input.readLine().trim();
                        
                        if (confirm.equalsIgnoreCase("y")) {
                            System.out.println("Logging out...");
                            isValid = true;
                            running = false;
                        } else if (confirm.equalsIgnoreCase("n")){
                            System.out.println("Log out cancelled.");
                            isValid = true;
                        } else {
                            System.out.println(ErrorMessages.INVALID_CHOICE);
                        }
                    }
                }
            }
        }
    }

    /** The passenger's main menu. 'user' is whoever logged in; bookings will use it. */
    public void userUI(RegularUser user) {
        try {
            runUserUI(user);
        } catch (Input.EndOfInputException e) {
            Display.message("Input ended. Logging out...");
        }
    }

    private void runUserUI(RegularUser user) {
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
                case 5 -> RegularUserOperations.getInstance().updateBookedFlight(user);
                case 6 -> RegularUserOperations.getInstance().cancelBookedFlight(user);
                case 7 -> {
                    boolean isValid = false;

                    while(!isValid){
                        System.out.print("Are you sure you want to exit? (y/n): ");
                        String confirm = Input.readLine().trim();
                        
                        if (confirm.equalsIgnoreCase("y")) {
                            System.out.println("Logging out...");
                            isValid = true;
                            running = false;
                        } else if (confirm.equalsIgnoreCase("n")){
                            System.out.println("Log out cancelled.");
                            isValid = true;
                        } else {
                            System.out.println(ErrorMessages.INVALID_CHOICE);
                        }
                    }
                }
            }
        }
    }
}
