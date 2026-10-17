package src.menu.admin;

import src.menu.BaseMenu;

/**
 * Admin > Manage User Accounts
 * View, search, and delete registered passenger accounts.
 */
public class AdminAccountMenu extends BaseMenu {

    @Override
    protected String getTitle() {
        return "Manage User Accounts";
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
                "View All Registered Users",
                "Search User Accounts",
                "Delete User Account",
                "Back"
        };
    }

    @Override
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