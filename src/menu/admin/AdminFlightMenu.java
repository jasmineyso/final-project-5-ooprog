package src.menu.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import src.flight.Flight;
import src.flight.FlightStorage;
import src.util.Input;
import src.util.Validator;

/**
 * Admin > Manage Flights
 * Add, update, change status, remove, search, and view flights.
 */
public class AdminFlightMenu {

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
        return "Manage Flights";
    }

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

    public void addFlight() {
        System.out.println();
        System.out.println("=== Add Flight ===");
        FlightStorage storage = FlightStorage.getInstance();
        try {
            List<String> existingNumbers = storage.loadFlightNumbers();
            String flightNumber = Validator.normalizeFlightNumber(askField("Flight Number (e.g. PR102)",
                    value -> Validator.isValidFlightNumber(value)
                            && !Validator.flightNumberExists(value, existingNumbers),
                    "Enter a valid flight number that does not already exist."));
            String gateNumber = askField("Gate Number (e.g. 12 or A12)",
                    value -> value.matches("[A-Za-z0-9]{1,10}"),
                    "Gate number must contain 1 to 10 letters or digits.").toUpperCase(Locale.ROOT);
            String origin = Validator.normalizeLocation(askField("Origin", Validator::isValidLocation,
                    "Enter a valid origin (e.g. Manila)."));
            String destination = Validator.normalizeLocation(askField("Destination",
                    value -> Validator.isValidLocation(value) && Validator.isDifferentLocation(origin, value),
                    "Enter a valid destination different from the origin."));
            LocalDateTime departure = Validator.parseDateTime(askField(
                    "Departure Time (" + Validator.DATE_TIME_FORMAT_HINT + ")",
                    value -> Validator.isFutureDateTime(Validator.parseDateTime(value)),
                    "Enter a valid future departure time."));
            LocalDateTime arrival = Validator.parseDateTime(askField(
                    "Arrival Time (" + Validator.DATE_TIME_FORMAT_HINT + ")",
                    value -> Validator.isReasonableFlightDuration(departure, Validator.parseDateTime(value)),
                    "Arrival must be after departure and within 24 hours."));
            BigDecimal economyFare = askFare("Economy");
            BigDecimal businessFare = askFare("Business");
            BigDecimal firstClassFare = askFare("First Class");
            int seats = Integer.parseInt(askField("Number of Seats (total)",
                    value -> Validator.isIntInRange(value, 1, Integer.MAX_VALUE),
                    "Enter a positive whole number of seats."));

            storage.saveFlight(new Flight(flightNumber, gateNumber, origin, destination,
                    departure, arrival, economyFare, businessFare, firstClassFare, seats));
            System.out.println("Flight " + flightNumber + " added and saved to AVAILABLE_FLIGHTS.txt.");
        } catch (IOException e) {
            System.out.println("Could not add flight: " + e.getMessage());
        }
    }

    private String askField(String prompt, Predicate<String> validation, String error) {
        while (true) {
            System.out.print(prompt + ": ");
            String value = Input.getScanner().nextLine().trim();
            if (validation.test(value)) {
                return value;
            }
            System.out.println(error);
        }
    }

    private BigDecimal askFare(String serviceClass) {
        String value = askField("Fare for " + serviceClass,
                input -> input.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")
                        && new BigDecimal(input).signum() > 0,
                "Enter a positive fare with at most two decimal places (e.g. 1500.00).");
        return new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);
    }

    public void updateFlight() {
        // TODO: ask for flight number (must exist), then which field to change.
        int field = Input.askChoice("What do you want to update?",
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

    public void updateFlightStatus() {
        // TODO: ask for flight number first (must exist).
        int status = Input.askChoice("Set Flight Status",
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

    public void removeFlight() {
        // TODO: ask for flight number (must exist), ask "Are you sure? (Y/N)", then remove it.
        notYetAvailable("Remove Flight");
    }

    public void searchFlights() {
        int by = Input.askChoice("Search Flights",
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

    public void viewFlights() {
        int view = Input.askChoice("View Flights",
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
