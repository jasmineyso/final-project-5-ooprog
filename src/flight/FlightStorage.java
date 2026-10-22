package src.flight;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import src.booking.BookingStorage;
import src.flight.filter.FlightFilter;
import src.util.Validator;

public final class FlightStorage {
    private static FlightStorage instance;
    private final Path filePath;

    private FlightStorage() {
        // Support launching from either the project or its parent workspace.
        Path directory = Path.of("src", "files");
        Path workspaceDirectory = Path.of("final-project-5-ooprog", "src", "files");
        if (Files.isDirectory(workspaceDirectory)) {
            directory = workspaceDirectory;
        }
        filePath = directory.resolve("AVAILABLE_FLIGHTS.txt");
    }

    public static FlightStorage getInstance() {
        if (instance == null) {
            instance = new FlightStorage();
        }
        return instance;
    }

    public List<Flight> loadFlights() throws IOException {
        List<Flight> flights = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return flights;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            try {
                if (fields.length == 15) {
                    flights.add(fromLine(fields));
                } else if (fields.length == 10) {
                    flights.add(fromOldLine(fields));
                } else {
                    throw new IllegalArgumentException("Wrong number of fields");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid flight data on line " + lineNumber + " of AVAILABLE_FLIGHTS.txt.", e);
            }
        }
        return flights;
    }

    /**
     * Reads one line in the current format (15 fields):
     * number|gate|origin|destination|departure|arrival|status|
     * economyFare|premiumFare|businessFare|firstFare|economySeats|premiumSeats|businessSeats|firstSeats
     */
    private Flight fromLine(String[] fields) {
        return new Flight(fields[0], fields[1], fields[2], fields[3],
                parseDateTime(fields[4]), parseDateTime(fields[5]),
                parseStatus(fields[6]),
                new BigDecimal(fields[7]), new BigDecimal(fields[8]),
                new BigDecimal(fields[9]), new BigDecimal(fields[10]),
                Integer.parseInt(fields[11]), Integer.parseInt(fields[12]),
                Integer.parseInt(fields[13]), Integer.parseInt(fields[14]));
    }

    /**
     * Reads one line saved before status and Premium Economy existed (10 fields):
     * number|gate|origin|destination|departure|arrival|economyFare|businessFare|firstFare|seats
     * Such flights load as Scheduled with the standard 150-seat split, and Premium Economy
     * copies the Economy fare. They are saved in the new format the next time the file is rewritten.
     */
    private Flight fromOldLine(String[] fields) {
        BigDecimal economyFare = new BigDecimal(fields[6]);
        return new Flight(fields[0], fields[1], fields[2], fields[3],
                parseDateTime(fields[4]), parseDateTime(fields[5]),
                FlightStatus.SCHEDULED,
                economyFare, economyFare, new BigDecimal(fields[7]), new BigDecimal(fields[8]),
                ServiceClass.ECONOMY.seatsPerFlight(), ServiceClass.PREMIUM_ECONOMY.seatsPerFlight(),
                ServiceClass.BUSINESS.seatsPerFlight(), ServiceClass.FIRST_CLASS.seatsPerFlight());
    }

    private LocalDateTime parseDateTime(String text) {
        LocalDateTime dateTime = Validator.parseDateTime(text);
        if (dateTime == null) {
            throw new IllegalArgumentException("Invalid date and time: " + text);
        }
        return dateTime;
    }

    private FlightStatus parseStatus(String text) {
        // Older versions called a scheduled flight ON_TIME.
        if ("ON_TIME".equalsIgnoreCase(text)) {
            return FlightStatus.SCHEDULED;
        }
        return FlightStatus.valueOf(text);
    }

    public List<String> loadFlightNumbers() throws IOException {
        List<String> numbers = new ArrayList<>();
        
        if (Files.exists(filePath)) {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    numbers.add(line.split("\\|", -1)[0]);
                }
            }
        }
        return numbers;
    }

    /** Returns the flight with this number, or null if there is none. */
    public Flight findFlight(String flightNumber) throws IOException {
        String target = Validator.normalizeFlightNumber(flightNumber);
        for (Flight flight : loadFlights()) {
            if (Validator.normalizeFlightNumber(flight.getFlightNumber()).equals(target)) {
                return flight;
            }
        }
        return null;
    }

    /** Returns every flight that matches the filter (Strategy pattern). */
    public List<Flight> search(FlightFilter filter) throws IOException {
        List<Flight> results = new ArrayList<>();
        for (Flight flight : loadFlights()) {
            if (filter.matches(flight)) {
                results.add(flight);
            }
        }
        return results;
    }

    /** Adds ONE flight to the end of the file. */
    public void saveFlight(Flight flight) throws IOException {
        if (Validator.flightNumberExists(flight.getFlightNumber(), loadFlightNumbers())) {
            throw new IOException("Flight number already exists.");
        }
        String line = toLine(flight);
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    /** Replaces the WHOLE file with this list. Used by remove, update, and update status. */
    public void saveAllFlights(List<Flight> flights) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Flight flight : flights) {
            lines.add(toLine(flight));
        }
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Files.write(filePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Replaces the saved flight that has the same flight number with this one.
     * Returns false if no flight has that number.
     */
    public boolean updateFlight(Flight updated) throws IOException {
        if (updated == null) {
            throw new IllegalArgumentException("Updated flight cannot be null.");
        }
        String target = Validator.normalizeFlightNumber(updated.getFlightNumber());
        List<Flight> flights = loadFlights();
        for (int i = 0; i < flights.size(); i++) {
            if (Validator.normalizeFlightNumber(flights.get(i).getFlightNumber()).equals(target)) {
                BookingStorage bookings = BookingStorage.getInstance();
                if (updated.getStatus() == FlightStatus.CANCELLED) {
                    // Validate booking data before changing the flight file.
                    bookings.loadBookings();
                }
                Flight original = flights.get(i);
                flights.set(i, updated);
                saveAllFlights(flights);
                if (updated.getStatus() == FlightStatus.CANCELLED) {
                    try {
                        bookings.cancelBookingsForFlight(updated.getFlightNumber());
                    } catch (IOException e) {
                        flights.set(i, original);
                        try {
                            saveAllFlights(flights);
                        } catch (IOException rollbackError) {
                            e.addSuppressed(rollbackError);
                        }
                        throw e;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Updates a flight's departure and arrival times. */
    public boolean updateFlightSchedule(String flightNumber, LocalDateTime newDeparture,
            LocalDateTime newArrival) throws IOException {
        if (newDeparture == null || newArrival == null || !newArrival.isAfter(newDeparture)) {
            throw new IllegalArgumentException("Arrival must be after departure.");
        }
        Flight current = findFlight(flightNumber);
        if (current == null) {
            return false;
        }
        return updateFlight(current.withTimes(newDeparture, newArrival));
    }

    /** Updates the fare for one service class. */
    public boolean updateFlightFare(String flightNumber, ServiceClass serviceClass,
            BigDecimal newFare) throws IOException {
        if (serviceClass == null || newFare == null || newFare.signum() < 0) {
            throw new IllegalArgumentException("Service class is required and fare cannot be negative.");
        }
        Flight current = findFlight(flightNumber);
        if (current == null) {
            return false;
        }
        BigDecimal economy = current.getEconomyFare();
        BigDecimal premium = current.getPremiumEconomyFare();
        BigDecimal business = current.getBusinessFare();
        BigDecimal first = current.getFirstClassFare();
        switch (serviceClass) {
            case ECONOMY -> economy = newFare;
            case PREMIUM_ECONOMY -> premium = newFare;
            case BUSINESS -> business = newFare;
            case FIRST_CLASS -> first = newFare;
        }
        return updateFlight(current.withFares(economy, premium, business, first));
    }

    /** Updates a flight's status. */
    public boolean updateFlightStatus(String flightNumber, FlightStatus newStatus) throws IOException {
        if (newStatus == null) {
            throw new IllegalArgumentException("Flight status cannot be null.");
        }
        Flight current = findFlight(flightNumber);
        if (current == null) {
            return false;
        }
        return updateFlight(current.withStatus(newStatus));
    }

    /** Deletes a flight only when it has no active bookings. */
    public boolean deleteFlight(String flightNumber, BookingStorage bookings) throws IOException {
        if (bookings == null) {
            throw new IllegalArgumentException("Booking storage cannot be null.");
        }
        Flight current = findFlight(flightNumber);
        if (current == null) {
            return false;
        }
        if (bookings.hasActiveBookingsOnFlight(current.getFlightNumber())) {
            throw new IllegalStateException("Cannot delete a flight with active bookings.");
        }
        return removeFlight(current.getFlightNumber());
    }

    /** Removes the flight with this number. Returns false if it wasn't found. */
    public boolean removeFlight(String flightNumber) throws IOException {
        String target = Validator.normalizeFlightNumber(flightNumber);
        List<Flight> flights = loadFlights();
        boolean removed = flights.removeIf(
                flight -> Validator.normalizeFlightNumber(flight.getFlightNumber()).equals(target));
        if (removed) {
            saveAllFlights(flights);
        }
        return removed;
    }

    /**
     * Turns one flight into one line of the file (15 fields, same order as fromLine).
     * If you add a field to Flight later, this is the ONLY place saving needs to change.
     */
    private String toLine(Flight flight) throws IOException {
        List<String> fields = new ArrayList<>(List.of(
                flight.getFlightNumber(), flight.getGateNumber(), flight.getOrigin(), flight.getDestination(),
                Validator.formatDateTime(flight.getDepartureTime()),
                Validator.formatDateTime(flight.getArrivalTime()),
                flight.getStatus().name()));
        for (ServiceClass serviceClass : ServiceClass.values()) {
            fields.add(flight.getFareFor(serviceClass).toPlainString());
        }
        for (ServiceClass serviceClass : ServiceClass.values()) {
            fields.add(Integer.toString(flight.getSeatCount(serviceClass)));
        }
        for (String field : fields) {
            if (field.contains("|") || field.contains("\n") || field.contains("\r")) {
                throw new IOException("Flight fields cannot contain | or line breaks.");
            }
        }
        return String.join("|", fields);
    }
}
