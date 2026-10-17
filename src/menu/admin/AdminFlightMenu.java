package src.menu.admin;

import src.menu.BaseMenu;

/**
 * Admin > Manage Flights
 * Add, update, change status, remove, search, and view flights.
 */
public class AdminFlightMenu extends BaseMenu {

    @Override
    protected String getTitle() {
        return "Manage Flights";
    }

    @Override
    protected String[] getOptions() {
        return new String[] {
                "Add Flight",
                "Update Flight (times, fares, seats)",
                "Update Flight Status",
                "Remove Flight",
                "Search Flights",
                "View Flights",
                "Back"
        };
    }

    @Override
    protected boolean handleChoice(int choice) {
        switch (choice) {
            case 1 -> addFlight();
            case 2 -> updateFlight();
            case 3 -> updateFlightStatus();
            case 4 -> removeFlight();
            case 5 -> searchFlights();
            case 6 -> viewFlights();
            case 7 -> {
                return false;
            }
        }
        return true;
    }

    private void addFlight() {
        // TODO: ask for each field, validating with Validator as you go:
        //   flight number  -> isValidFlightNumber, and must NOT already exist (flightNumberExists)
        //   gate number
        //   origin         -> isValidLocation
        //   destination    -> isValidLocation + isDifferentLocation(origin, destination)
        //   departure time -> parseDateTime + isFutureDateTime
        //   arrival time   -> parseDateTime + isArrivalAfterDeparture + isReasonableFlightDuration
        //   fare per service class, number of seats -> isIntInRange
        // then create the Flight and save it through the flight storage.
        notYetAvailable("Add Flight");
    }

    private void updateFlight() {
        // TODO: ask for flight number (must exist), then which field to change.
        int field = askChoice("What do you want to update?",
                "Departure and Arrival Time",
                "Fare per Service Class",
                "Number of Seats",
                "Back");

        switch (field) {
            case 1 -> notYetAvailable("Updating times");
            case 2 -> notYetAvailable("Updating fares");
            case 3 -> notYetAvailable("Updating seats");
            case 4 -> { } // back
        }
    }

    private void updateFlightStatus() {
        // TODO: ask for flight number first (must exist).
        int status = askChoice("Set Flight Status",
                "Scheduled",
                "Rescheduled",
                "Delayed",
                "Cancelled",
                "Back");

        switch (status) {
            case 1 -> notYetAvailable("Set to Scheduled");
            case 2, 3 -> {
                // TODO: Rescheduled and Delayed both need a new departure and arrival time.
                notYetAvailable("Set to Rescheduled/Delayed");
            }
            case 4 -> {
                // TODO: decide what happens to existing bookings on this flight.
                notYetAvailable("Set to Cancelled");
            }
            case 5 -> { } // back
        }
    }

    private void removeFlight() {
        // TODO: ask for flight number (must exist), ask "Are you sure? (Y/N)", then remove it.
        notYetAvailable("Remove Flight");
    }

    private void searchFlights() {
        int by = askChoice("Search Flights",
                "By Flight Number",
                "By Origin",
                "By Destination",
                "By Month and Year",
                "Back");

        switch (by) {
            case 1 -> notYetAvailable("Search by Flight Number");
            case 2 -> notYetAvailable("Search by Origin");
            case 3 -> notYetAvailable("Search by Destination");
            case 4 -> notYetAvailable("Search by Month and Year");
            case 5 -> { } // back
        }
    }

    private void viewFlights() {
        int view = askChoice("View Flights",
                "All Flights (A-Z)",
                "Filter by Date",
                "Filter by Status",
                "Back");

        switch (view) {
            case 1 -> notYetAvailable("View all flights");
            case 2 -> notYetAvailable("Filter by Date");
            case 3 -> notYetAvailable("Filter by Status");
            case 4 -> { } // back
        }
    }
}