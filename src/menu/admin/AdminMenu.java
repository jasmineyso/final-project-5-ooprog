package src.menu.admin;

import src.account.Admin;
import src.menu.BaseMenu;

/**
 * The admin's main menu, shown right after a successful admin login.
 * It only routes to the three sub-menus; each sub-menu holds the actual features.
 */
public class AdminMenu extends BaseMenu {

    private final Admin admin;

    public AdminMenu(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void start() {
        System.out.println();
        System.out.println("Welcome, " + admin.getUsername() + "!");
        super.start();
    }

    @Override
    protected String getTitle() {
        return "Admin Menu";
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
                "Manage Flights",
                "Manage Passenger Bookings",
                "Manage User Accounts",
                "Log Out"
        };
    }

    @Override
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