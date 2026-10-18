package src.menu.admin;

import src.util.Input;

/**
 * Admin > Manage Passenger Bookings
 * View, filter, update, and cancel passengers' bookings.
 */
public class AdminBookingMenu {

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
        return "Manage Passenger Bookings";
    }

    protected String[] getOptions() {
        return new String[] {
                "View All Bookings",
                "Search a Passenger's Booking",
                "Filter by Flight",
                "Filter by Status",
                "Update/Cancel a Passenger's Booking",
                "Back"
        };
    }

    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1 -> viewAllBookings();
            case 2 -> searchByPassenger();
            case 3 -> filterByFlight();
            case 4 -> filterByStatus();
            case 5 -> updateOrCancelBooking();
            case 6 -> {
                return false;
            }
        }
        return true;
    }

    public void viewAllBookings() {
        notYetAvailable("View All Bookings");
    }

    private void searchByPassenger() {
        // TODO: ask for the passenger's name.
        notYetAvailable("Search a Passenger's Booking");
    }

    private void filterByFlight() {
        // TODO: ask for flight number (must exist).
        notYetAvailable("Filter by Flight");
    }

    private void filterByStatus() {
        int status = Input.askChoice("Filter by Status",
                "Pending",
                "Paid",
                "Cancelled",
                "Back");

        switch (status) {
            case 1 -> notYetAvailable("Pending bookings");
            case 2 -> notYetAvailable("Paid bookings");
            case 3 -> notYetAvailable("Cancelled bookings");
            case 4 -> { } // back
        }
    }

    private void updateOrCancelBooking() {
        // TODO: find the booking first, then:
        int action = Input.askChoice("Update/Cancel Booking",
                "Update Booking",
                "Cancel Booking",
                "Back");

        switch (action) {
            case 1 -> notYetAvailable("Update Booking");
            case 2 -> notYetAvailable("Cancel Booking");
            case 3 -> { } // back
        }
    }
}