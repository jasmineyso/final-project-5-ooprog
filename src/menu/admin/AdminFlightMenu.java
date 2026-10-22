package src.menu.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import src.booking.BookingStorage;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.flight.Flight;
import src.flight.FlightStatus;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.flight.filter.ByDate;
import src.flight.filter.ByDestination;
import src.flight.filter.ByMonthYear;
import src.flight.filter.ByOrigin;
import src.flight.filter.ByStatus;
import src.flight.filter.FlightFilter;
import src.util.Display;
import src.util.InformationTables;
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


    protected String getTitle() {
        return "Manage Flights";
    }

    protected String[] getOptions() {
        return new String[] {
                "Add Flight",
                "Update Flight Time",
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
        Display.title("Add Flight");
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
            String origin = Validator.normalizeCityCountry(askField("Origin (City, Country)",
                    Validator::isValidCityCountry,
                    "Enter the origin as City, Country (e.g. Manila, Philippines)."));
            String destination = Validator.normalizeCityCountry(askField("Destination (City, Country)",
                    value -> Validator.isValidCityCountry(value) && Validator.isDifferentLocation(origin, value),
                    "Enter the destination as City, Country (e.g. Tokyo, Japan), different from the origin."));
            LocalDateTime departure = askDeparture();
            LocalDateTime arrival = askArrival(departure);
            BigDecimal economyFare = askFare("Economy");
            BigDecimal premiumEconomyFare = askFare("Premium Economy");
            BigDecimal businessFare = askFare("Business");
            BigDecimal firstClassFare = askFare("First Class");

            storage.saveFlight(new Flight(flightNumber, gateNumber, origin, destination,
                    departure, arrival, FlightStatus.SCHEDULED,
                    economyFare, premiumEconomyFare, businessFare, firstClassFare,
                    ServiceClass.ECONOMY.seatsPerFlight(), ServiceClass.PREMIUM_ECONOMY.seatsPerFlight(),
                    ServiceClass.BUSINESS.seatsPerFlight(), ServiceClass.FIRST_CLASS.seatsPerFlight()));
            Display.success("Flight " + flightNumber + " has been added.");
        } catch (IOException e) {
            Display.error("Could not add flight: " + e.getMessage());
        }
    }

    private String askField(String prompt, Predicate<String> validation, String error) {
        boolean isFieldValid = false;
        String value = "";
        while (!isFieldValid) {
            Display.prompt(prompt);
            value = Input.getScanner().nextLine().trim();
            if (validation.test(value)) {
                isFieldValid = true;
            } else {
                Display.error(error);
            }
        }
        return value;
    }

    private BigDecimal askFare(String serviceClass) {
        String value = askField("Fare for " + serviceClass,
                input -> input.matches("[0-9]{1,10}(\\.[0-9]{1,2})?")
                        && new BigDecimal(input).signum() > 0,
                "Enter a positive fare with at most two decimal places (e.g. 1500.00).");
        return new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);
    }

    public void updateFlight() {
        Display.title("Update Flight");
        try {
            Flight flight = askExistingFlight();
            Display.divider();
            if (flight == null) {
                return;
            }
            if (flight.getStatus() == FlightStatus.CANCELLED) {
                Display.error("Flight " + flight.getFlightNumber() + " is cancelled and can no longer be updated.");
                return;
            }
            InformationTables.flightDetails(flight);
            Display.divider();

            Flight updated;
            LocalDateTime departure = askLaterDeparture(flight.getDepartureTime());
            LocalDateTime arrival = askArrival(departure);
            updated = flight.withTimes(departure, arrival).withStatus(FlightStatus.DELAYED);


            FlightStorage.getInstance().updateFlight(updated);
            Display.success("Flight " + flight.getFlightNumber() + " has been updated.");
            InformationTables.flightDetails(updated);
        } catch (IOException e) {
            Display.error("Could not update flight: " + e.getMessage());
        }
    }

    public void updateFlightStatus() {
        Display.title("Update Flight Status");
        try {
            Flight flight = askExistingFlight();
            if (flight == null) {
                return;
            }
            InformationTables.flightDetails(flight);

            FlightStatus[] statuses = FlightStatus.values();
            String[] options = new String[statuses.length + 1];
            for (int i = 0; i < statuses.length; i++) {
                options[i] = statuses[i].toString();
            }
            options[statuses.length] = "Back";

            int choice = Input.askChoice("Set Flight Status", options);
            if (choice == options.length) {
                return; // Back
            }
            FlightStatus newStatus = statuses[choice - 1];

            Flight updated = flight.withStatus(newStatus);
            switch (newStatus) {
                case DELAYED -> {
                    Display.message("Enter the new schedule for this flight.");
                    LocalDateTime departure = askLaterDeparture(flight.getDepartureTime());
                    LocalDateTime arrival = askArrival(departure);
                    updated = updated.withTimes(departure, arrival);
                }
                case CANCELLED -> {
                    Display.message("All active bookings on this flight will also be cancelled.");
                    Display.message("Paid bookings will be automatically refunded in full.");
                    if (!Input.askYesNo("Cancel flight " + flight.getFlightNumber() + "?")) {
                        Display.message("Status not changed.");
                        return;
                    }
                }
                case SCHEDULED -> { } // nothing extra to ask
                case DEPARTED -> {}
            }

            List<Booking> affectedBookings = newStatus == FlightStatus.CANCELLED
                    ? BookingStorage.getInstance().loadBookings().stream()
                            .filter(booking -> booking.flightNumber().equalsIgnoreCase(flight.getFlightNumber())
                                    && booking.isActive())
                            .toList()
                    : List.of();
            if (!FlightStorage.getInstance().updateFlight(updated)) {
                Display.error("Flight was not found. Status not changed.");
                return;
            }
            Display.success("Flight " + flight.getFlightNumber() + " is now " + newStatus + ".");
            InformationTables.flightDetails(updated);

            if (newStatus == FlightStatus.CANCELLED) {
                Display.message(affectedBookings.size() + " booking(s) on this flight were cancelled.");
                BigDecimal refundTotal = BigDecimal.ZERO;
                for (Booking booking : affectedBookings) {
                    if (booking.status() == BookingStatus.PAID) {
                        refundTotal = refundTotal.add(booking.totalPrice());
                        Display.message("Refunded: " + booking.bookingId() + " (" + booking.username()
                                + ") - " + InformationTables.money(booking.totalPrice()));
                    }
                }
                Display.message("Total refunded for these cancellations: " + InformationTables.money(refundTotal));
            }
        } catch (IOException e) {
            Display.error("Could not update flight status: " + e.getMessage());
        }
    }

    /**
     * Asks for a flight number once. Returns that flight, or null (after showing a message)
     * if there are no flights or the number doesn't exist.
     */
    private Flight askExistingFlight() throws IOException {
        if (FlightStorage.getInstance().loadFlights().isEmpty()) {
            Display.message("There are no flights yet.");
            return null;
        }
        Display.prompt("Enter flight number");
        String flightNumber = Input.getScanner().nextLine();
        Flight flight = FlightStorage.getInstance().findFlight(flightNumber);
        if (flight == null) {
            Display.error("Flight " + flightNumber.trim() + " was not found.");
        }
        return flight;
    }

    private LocalDateTime askDeparture() {
        return askDateTime("Departure", null,
                Validator::isFutureDateTime,
                "Departure must be in the future. Please enter it again.");
    }

    /** For delays: the new departure must be later than the flight's current departure. */
    private LocalDateTime askLaterDeparture(LocalDateTime currentDeparture) {
        return askDateTime("New Departure", null,
                newDeparture -> newDeparture.isAfter(currentDeparture),
                "The new departure must be later than " + Validator.formatDateTime(currentDeparture)
                        + ". Please enter it again.");
    }

    /** Pressing Enter at the arrival date uses the same day as the departure. */
    private LocalDateTime askArrival(LocalDateTime departure) {
        return askDateTime("Arrival", departure.toLocalDate(),
                arrival -> Validator.isReasonableFlightDuration(departure, arrival),
                "Arrival must be after departure and within 24 hours. Please enter it again.");
    }

    /**
     * Asks for a date, then a time, and combines them into one LocalDateTime.
     * If the combined value fails the check, both are asked again.
     *
     * @param defaultDate used when the date is left blank (null = the date is required)
     */
    private LocalDateTime askDateTime(String label, LocalDate defaultDate,
            Predicate<LocalDateTime> check, String error) {
        boolean isDateTimeValid = false;
        LocalDateTime dateTime = null;
        while (!isDateTimeValid) {
            String datePrompt = label + " Date (YYYY-MM-DD)";
            String dateText = askField(datePrompt,
                    value -> (defaultDate != null && value.isEmpty()) || Validator.isValidDate(value),
                    "Enter a real date in the format YYYY-MM-DD (e.g. 2026-11-05).");
            LocalDate date = dateText.isEmpty() ? defaultDate : Validator.parseDate(dateText);

            LocalTime time = Validator.parseTime(askField(label + " Time (HH:MM, 24-hour)",
                    Validator::isValidTime,
                    "Enter a real time in 24-hour format (e.g. 09:05 or 21:30)."));

            dateTime = LocalDateTime.of(date, time);
            if (check.test(dateTime)) {
                isDateTimeValid = true;
            } else {
                Display.error(error);
            }
        }
        return dateTime;
    }

    public void removeFlight() {
        Display.title("Remove Flight");
        try {
            if (FlightStorage.getInstance().loadFlights().isEmpty()) {
                Display.message("There are no flights to remove.");
                return;
            }

            Display.prompt("Enter flight number to remove");
            String flightNumber = Input.getScanner().nextLine();

            Flight flight = FlightStorage.getInstance().findFlight(flightNumber);
            if (flight == null) {
                Display.error("Flight " + flightNumber.trim() + " was not found.");
                return;
            }

            InformationTables.flightDetails(flight);
            if (!Input.askYesNo("Are you sure you want to remove this flight?")) {
                Display.message("Remove cancelled.");
                return;
            }

            FlightStorage.getInstance().removeFlight(flight.getFlightNumber());
            Display.success("Flight " + flight.getFlightNumber() + " has been removed.");
        } catch (IOException e) {
            Display.error("Could not remove flight: " + e.getMessage());
        }
    }

    public void searchFlights() {
        int by = Input.askChoice("Search Flights",
                "By Flight Number",
                "By Origin",
                "By Destination",
                "By Month and Year",
                "Back");

        switch (by) {
            case 1 -> searchByFlightNumber();
            case 2 -> searchByOrigin();
            case 3 -> searchByDestination();
            case 4 -> searchByMonthYear();
            case 5 -> { } // back
        }
    }

    private void searchByFlightNumber() {
        Display.prompt("Enter flight number");
        String flightNumber = Input.getScanner().nextLine();
        try {
            Flight flight = FlightStorage.getInstance().findFlight(flightNumber);
            if (flight == null) {
                Display.error("Flight " + flightNumber.trim() + " was not found.");
                return;
            }
            Display.title("Search Result");
            InformationTables.flightDetails(flight);
        } catch (IOException e) {
            Display.error("Could not load flights: " + e.getMessage());
        }
    }

    private void searchByOrigin() {
        String origin = askField("Enter origin (city, country, or City, Country)", this::isSearchablePlace,
                "Enter a place name (e.g. Manila, Philippines, or Manila, Philippines).");
        showSearchResults("Flights from " + origin, new ByOrigin(origin));
    }

    private void searchByDestination() {
        String destination = askField("Enter destination (city, country, or City, Country)", this::isSearchablePlace,
                "Enter a place name (e.g. Tokyo, Japan, or Tokyo, Japan).");
        showSearchResults("Flights to " + destination, new ByDestination(destination));
    }

    /** A search can be just a city or country ("Manila"), or a full "City, Country". */
    private boolean isSearchablePlace(String value) {
        return Validator.isValidLocation(value) || Validator.isValidCityCountry(value);
    }

    private void searchByMonthYear() {
        int month = Integer.parseInt(askField("Enter month (1-12)",
                value -> Validator.isIntInRange(value, 1, 12),
                "Month must be a number from 1 to 12."));
        int year = Integer.parseInt(askField("Enter year (e.g. 2026)",
                value -> Validator.isIntInRange(value, 2000, 2100),
                "Enter a year from 2000 to 2100."));

        YearMonth monthYear = YearMonth.of(year, month);
        String label = monthYear.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        showSearchResults("Flights in " + label, new ByMonthYear(monthYear));
    }

    /** Runs any filter and prints the matching flights, sorted by flight number. */
    private void showSearchResults(String title, FlightFilter filter) {
        try {
            List<Flight> results = FlightStorage.getInstance().search(filter);
            Display.title(title);
            if (results.isEmpty()) {
                Display.message("No flights found.");
                return;
            }
            results.sort(Comparator.comparing(Flight::getFlightNumber));
            InformationTables.flights(results);
        } catch (IOException e) {
            Display.error("Could not load flights: " + e.getMessage());
        }
    }

    public void viewFlights() {
        int view = Input.askChoice("View Flights",
                "All Flights (A-Z)",
                "Filter by Date",
                "Filter by Status",
                "Back");

        switch (view) {
            case 1 -> viewAllFlights();
            case 2 -> filterByDate();
            case 3 -> filterByStatus();
            case 4 -> { } // back
        }
    }

    private void filterByDate() {
        String input = askField("Enter date in " + Validator.DATE_FORMAT_HINT,
                Validator::isValidDate,
                "Enter a real date in the format YYYY-MM-DD.");
        LocalDate date = Validator.parseDate(input);
        String label = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
        showSearchResults("Flights on " + label, new ByDate(date));
    }

    private void filterByStatus() {
        FlightStatus[] statuses = FlightStatus.values();
        String[] options = new String[statuses.length + 1];
        for (int i = 0; i < statuses.length; i++) {
            options[i] = statuses[i].toString();
        }
        options[statuses.length] = "Back";

        int choice = Input.askChoice("Filter by Status", options);
        if (choice == options.length) {
            return; // Back
        }
        FlightStatus status = statuses[choice - 1];
        showSearchResults(status + " Flights", new ByStatus(status));
    }

    private void viewAllFlights() {
        try {
            List<Flight> flights = FlightStorage.getInstance().loadFlights();
            if (flights.isEmpty()) {
                Display.message("No flights have been added yet.");
                return;
            }
            flights.sort(Comparator.comparing(Flight::getFlightNumber));
            Display.title("All Flights");
            InformationTables.flights(flights);
        } catch (IOException e) {
            Display.error("Could not load flights: " + e.getMessage());
        }
    }
}
