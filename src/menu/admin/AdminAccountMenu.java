package src.menu.admin;

import src.util.Input;

/**
 * Admin > Manage User Accounts
 * View, search, and delete registered passenger accounts.
 */
public class AdminAccountMenu {

    /** Runs this submenu until the administrator selects Back. */
    public void start() {
        boolean running = true;
        while (running) {
            running = handleChoice(Input.askChoice(getTitle(), getOptions()));
        }
    }

    private void notYetAvailable(String feature) {
        System.out.println(feature + " is not available yet.");
    }


    protected String getTitle() {
        return "Manage User Accounts";
    }

    protected String[] getOptions() {
        return new String[] {
                "View All Registered Users",
                "Search User Accounts",
                "Delete User Account",
                "Back"
        };
    }

    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1 -> viewAllUsers();
            case 2 -> searchUsers();
            case 3 -> deleteUser();
            case 4 -> {
                return false;
            }
        }
        return true;
    }

    private void viewAllUsers() {
        // TODO: AccountStorage.getInstance().loadUsers(), then print each user
        //       (never print passwords).
        notYetAvailable("View All Registered Users");
    }

    private void searchUsers() {
        // TODO: ask for a username and show the matching account.
        notYetAvailable("Search User Accounts");
    }

    private void deleteUser() {
        // TODO: ask for a username, ask "Are you sure? (Y/N)", then delete.
        //       Decide what happens to that user's bookings.
        //       AccountStorage will need a method that rewrites USER_ACCOUNTS.txt without that user.
        notYetAvailable("Delete User Account");
    }
}