package src.menu.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.booking.BookingStorage;
import src.util.Display;
import src.util.InformationTables;
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
        try {
            showUsers("All Registered Users", AccountStorage.getInstance().loadUsers());
        } catch (IOException e) {
            Display.error("Could not load accounts: " + e.getMessage());
        }
    }

    /** Shows every user whose username CONTAINS the search text, e.g. "jua" finds "juan123". */
    private void searchUsers() {
        Display.prompt("Enter username (or part of it)");
        String search = Input.readLine().trim();
        if (search.isEmpty()) {
            Display.error("Please type at least one character.");
            return;
        }
        try {
            List<RegularUser> matches = new ArrayList<>();
            for (RegularUser user : AccountStorage.getInstance().loadUsers()) {
                if (user.getUsername().toLowerCase().contains(search.toLowerCase())) {
                    matches.add(user);
                }
            }
            showUsers("Users matching \"" + search + "\"", matches);
        } catch (IOException e) {
            Display.error("Could not load accounts: " + e.getMessage());
        }
    }

    private void deleteUser() {
        Display.prompt("Enter the exact username to delete");
        String username = Input.readLine().trim();
        try {
            RegularUser user = AccountStorage.getInstance().findUser(username);
            if (user == null) {
                Display.error("User \"" + username + "\" was not found.");
                return;
            }

            InformationTables.users(List.of(user));
            Display.message("Any active bookings of this user will be cancelled.");
            if (!Input.askYesNo("Are you sure you want to delete this account?")) {
                Display.message("Delete cancelled.");
                return;
            }

            AccountStorage.getInstance().deleteUser(user.getUsername());
            int cancelled = BookingStorage.getInstance().cancelBookingsOf(user.getUsername());
            Display.success("Account \"" + user.getUsername() + "\" has been deleted.");
            Display.message(cancelled + " active booking(s) of this user were cancelled.");
        } catch (IOException e) {
            Display.error("Could not delete account: " + e.getMessage());
        }
    }

    /** Prints a list of users sorted by username, or a message if the list is empty. */
    private void showUsers(String title, List<RegularUser> users) {
        Display.title(title);
        if (users.isEmpty()) {
            Display.message("No users found.");
            return;
        }
        users.sort(Comparator.comparing(user -> user.getUsername().toLowerCase()));
        InformationTables.users(users);
        Display.message(users.size() + " user(s)");
    }

}
