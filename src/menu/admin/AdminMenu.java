package src.menu.admin;

import src.account.Admin;
import src.util.Input;

/**
 * The admin's main menu, shown right after a successful admin login.
 * It only routes to the three sub-menus; each sub-menu holds the actual features.
 */
public class AdminMenu {

    private final Admin admin;

    public AdminMenu(Admin admin) {
        this.admin = admin;
    }

    public void start() {
        System.out.println();
        System.out.println("Welcome, " + admin.getUsername() + "!");
        boolean running = true;
        while (running) {
            running = handleChoice(Input.askChoice(getTitle(), getOptions()));
        }
    }

    protected String getTitle() {
        return "Admin Menu";
    }

    protected String[] getOptions() {
        return new String[] {
                "Manage Flights",
                "Manage Passenger Bookings",
                "Manage User Accounts",
                "Log Out"
        };
    }

    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1 -> new AdminFlightMenu().start();
            case 2 -> new AdminBookingMenu().start();
            case 3 -> new AdminAccountMenu().start();
            case 4 -> {
                System.out.println("Logging out...");
                return false;
            }
        }
        return true;
    }
}
