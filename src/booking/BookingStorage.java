
package src.booking;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.menu.regular_user.PassengerSeat;

public class BookingStorage {

    private static BookingStorage instance;
    private final Path filePath;

    private BookingStorage() {
        Path directory = Path.of("src", "files");
        Path workspaceDirectory = Path.of("final-project-5-ooprog", "src", "files");

        if (Files.isDirectory(workspaceDirectory)) {
            directory = workspaceDirectory;
        }

        filePath = directory.resolve("BOOKED_FLIGHTS.txt");
    }

    // Singleton - only one BookingStorage object
    public synchronized static BookingStorage getInstance() {
        if (instance == null) {
            instance = new BookingStorage();
        }
        return instance;
    }

    // ===================== LOADING =====================

    // Read all bookings from the text file
    public List<Booking> loadBookings() throws IOException {
        List<Booking> bookings = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return bookings;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath.toFile(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\\|", -1);

                try {
                    if (fields.length != 8 && fields.length != 7) {
                        throw new IllegalArgumentException("Invalid number of fields");
                    }

                    Booking booking;
                    if (fields.length == 8) {
                        booking = new Booking(fields[0], fields[1], fields[2], fields[3],
                                ServiceClass.valueOf(fields[4]), Arrays.asList(fields[5].split(",", -1)),
                                new BigDecimal(fields[6]), parseStatus(fields[7]));
                    } else {
                        List<PassengerSeat> passengers = new ArrayList<>();
                        for (String entry : fields[4].split(";", -1)) {
                            String[] pair = entry.split(":", -1);
                            if (pair.length != 2) {
                                throw new IllegalArgumentException("Invalid passenger-seat entry");
                            }
                            // Retain old usernames as names when a legacy booking has been saved again.
                            passengers.add(PassengerSeat.fromLegacy(pair[0], pair[1]));
                        }
                        booking = new Booking(fields[0], fields[1], fields[2],
                                ServiceClass.valueOf(fields[3]), passengers,
                                new BigDecimal(fields[5]), parseStatus(fields[6]));
                    }

                    bookings.add(booking);

                } catch (IllegalArgumentException e) {
                    throw new IOException(
                            "Invalid booking data on line " + lineNumber
                                    + ". Fix this record before saving; no bookings were changed.", e);
                }
            }
        }

        return bookings;
    }

    private BookingStatus parseStatus(String text) {
        // Older versions used CONFIRMED for bookings that have been paid.
        if ("CONFIRMED".equalsIgnoreCase(text)) {
            return BookingStatus.PAID;
        }
        return BookingStatus.valueOf(text);
    }

    // Find a booking using its booking ID
    public Booking findBooking(String bookingId) throws IOException {
        for (Booking booking : loadBookings()) {
            if (booking.bookingId().equalsIgnoreCase(bookingId.trim())) {
                return booking;
            }
        }

        return null;
    }

    // Get all bookings belonging to a user
    public List<Booking> bookingsOf(String username) throws IOException {
        List<Booking> results = new ArrayList<>();

        for (Booking booking : loadBookings()) {
            if (booking.username().equalsIgnoreCase(username)) {
                results.add(booking);
            }
        }

        return results;
    }

    // ===================== SAVING =====================

    // Add a new booking without deleting existing bookings
    public synchronized void saveBooking(Booking booking) throws IOException {
        List<Booking> bookings = loadBookings();
        for (Booking existing : bookings) {
            if (existing.bookingId().equalsIgnoreCase(booking.bookingId())) {
                throw new IOException("This booking ID already exists. Please start the booking again.");
            }
        }
        validateSeats(booking, bookings);
        bookings.add(booking);
        saveAllBookings(bookings);
    }

    // Replace all bookings in the text file
    public synchronized void saveAllBookings(List<Booking> bookings) throws IOException {
        List<String> lines = new ArrayList<>();
        java.util.Set<String> ids = new java.util.HashSet<>();
        java.util.Set<String> occupied = new java.util.HashSet<>();
        for (Booking booking : bookings) {
            if (!ids.add(booking.bookingId().toUpperCase(java.util.Locale.ROOT))) {
                throw new IOException("Duplicate booking ID: " + booking.bookingId());
            }
            if (booking.isActive()) {
                for (String seat : booking.seats()) {
                    String key = booking.flightNumber().toUpperCase(java.util.Locale.ROOT) + ":" + seat;
                    if (!occupied.add(key)) throw new IOException("Seat is already occupied: " + key);
                }
            }
            lines.add(toLine(booking));
        }
        // Prepare the entire replacement before touching the original booking file.
        Path target = filePath.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "bookings-", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            Path backup = target.resolveSibling("BOOKED_FLIGHTS.txt.legacy.bak");
            if (Files.exists(target) && !Files.exists(backup)) {
                for (String line : Files.readAllLines(target, StandardCharsets.UTF_8)) {
                    if (line.split("\\|", -1).length == 8) {
                        Files.copy(target, backup);
                        break;
                    }
                }
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void validateSeats(Booking booking, List<Booking> bookings) throws IOException {
        if (!booking.isActive()) return;
        Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
        if (flight == null || !flight.isBookable()) {
            throw new IOException("The flight is no longer available.");
        }
        for (String seat : booking.seats()) {
            int number;
            try {
                number = Integer.parseInt(seat.substring(1));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid seat number: " + seat, e);
            }
            if (number > flight.getSeatCount(booking.serviceClass())) {
                throw new IOException("Seat does not exist on this flight: " + seat);
            }
            for (Booking other : bookings) {
                if (other.isActive() && !other.bookingId().equalsIgnoreCase(booking.bookingId())
                        && other.flightNumber().equalsIgnoreCase(booking.flightNumber())
                        && other.seats().contains(seat)) {
                    throw new IOException("Seat " + seat + " is no longer available. Please start again.");
                }
            }
        }
    }

    // Update an existing booking
    public synchronized boolean updateBooking(Booking updated) throws IOException {
        if (updated == null) {
            throw new IllegalArgumentException("Updated booking cannot be null.");
        }
        List<Booking> bookings = loadBookings();

        for (int i = 0; i < bookings.size(); i++) {

            if (bookings.get(i).bookingId()
                    .equalsIgnoreCase(updated.bookingId())) {

                validateSeats(updated, bookings);
                bookings.set(i, updated);
                saveAllBookings(bookings);

                return true;
            }
        }

        return false;
    }

    /** Changes the seat for a booking containing exactly one seat. */
    public Booking updateBookingSeat(String bookingId, String newSeat) throws IOException {
        Booking current = findBooking(bookingId);
        requireActive(current);
        if (current.seats().size() != 1) {
            throw new IllegalStateException("Seat changes require a booking with exactly one seat.");
        }
        if (newSeat == null || newSeat.isBlank()) {
            throw new IllegalArgumentException("Seat cannot be blank.");
        }
        String normalizedSeat = newSeat.trim().toUpperCase();
        String expectedPrefix = current.serviceClass().seatPrefix();
        if (!normalizedSeat.matches(expectedPrefix + "[1-9][0-9]*")) {
            throw new IllegalArgumentException("Seat does not match the booking's service class.");
        }
        int seatNumber = Integer.parseInt(normalizedSeat.substring(expectedPrefix.length()));
        Flight flight = FlightStorage.getInstance().findFlight(current.flightNumber());
        if (flight == null || seatNumber > flight.getSeatCount(current.serviceClass())) {
            throw new IllegalArgumentException("Seat does not exist on this flight.");
        }
        if (!normalizedSeat.equalsIgnoreCase(current.seats().get(0))
                && !availableSeats(flight, current.serviceClass()).contains(normalizedSeat)) {
            throw new IllegalStateException("Seat " + normalizedSeat + " is already taken.");
        }

        Booking updated = current.withAssignedSeats(current.flightNumber(), current.serviceClass(),
                List.of(normalizedSeat), current.totalPrice());
        if (!updateBooking(updated)) throw new IOException("Booking was not found.");
        return updated;
    }

    /** Moves a booking to another flight, retaining its service class and seat count. */
    public Booking updateBookingFlight(String bookingId, String newFlightNumber)
            throws IOException, NotEnoughSeatsException {
        Booking current = findBooking(bookingId);
        requireActive(current);
        if (current.status() != BookingStatus.PENDING) {
            throw new IOException("Only Pending bookings can change flights.");
        }
        if (newFlightNumber == null || newFlightNumber.isBlank()) {
            throw new IllegalArgumentException("Flight number cannot be blank.");
        }
        Flight target = FlightStorage.getInstance().findFlight(newFlightNumber.trim());
        if (target == null) {
            throw new IllegalArgumentException("Flight not found: " + newFlightNumber);
        }
        if (!target.isBookable()) {
            throw new IllegalStateException("Cannot move a booking to a cancelled flight.");
        }
        if (target.getFlightNumber().equalsIgnoreCase(current.flightNumber())) {
            throw new IllegalArgumentException("Choose a different flight.");
        }
        List<String> newSeats = assignSeats(target, current.serviceClass(), current.seats().size());
        BigDecimal newTotal = target.getFareFor(current.serviceClass())
                .multiply(BigDecimal.valueOf(current.seats().size()));
        Booking updated = current.withAssignedSeats(target.getFlightNumber(), current.serviceClass(), newSeats, newTotal);
        if (!updateBooking(updated)) throw new IOException("Booking was not found.");
        return updated;
    }

    /** Cancels an active booking while retaining its record in the file. */
    public boolean cancelBooking(String bookingId) throws IOException {
        Booking current = findBooking(bookingId);
        if (current == null || !current.isActive()) {
            return false;
        }
        return updateBooking(current.withStatus(BookingStatus.CANCELLED));
    }

    /** Returns whether a flight has any booking that has not been cancelled. */
    public boolean hasActiveBookingsOnFlight(String flightNumber) throws IOException {
        if (flightNumber == null || flightNumber.isBlank()) {
            return false;
        }
        for (Booking booking : loadBookings()) {
            if (booking.flightNumber().equalsIgnoreCase(flightNumber.trim()) && booking.isActive()) {
                return true;
            }
        }
        return false;
    }

    private void requireActive(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found.");
        }
        if (!booking.isActive()) {
            throw new IllegalStateException("Cancelled or completed bookings cannot be modified.");
        }
    }

    // Cancel bookings that match a condition
    public int cancelBookingsWhere(Predicate<Booking> condition)
            throws IOException {

        List<Booking> bookings = loadBookings();
        int cancelled = 0;

        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);

            if (booking.isActive() && condition.test(booking)) {

                bookings.set(i, booking.withStatus(BookingStatus.CANCELLED));
                cancelled++;
            }
        }

        if (cancelled > 0) {
            saveAllBookings(bookings);
        }

        return cancelled;
    }

    // Cancel all bookings for a specific flight
    public int cancelBookingsForFlight(String flightNumber) throws IOException {
        return cancelBookingsWhere(
                booking -> booking.flightNumber().equalsIgnoreCase(flightNumber));
    }

    // Cancel all bookings belonging to a user
    public int cancelBookingsOf(String username) throws IOException {
        return cancelBookingsWhere(
                booking -> booking.username().equalsIgnoreCase(username));
    }

    // Change the service class of an existing booking
    public Booking changeServiceClass(
            Booking booking,
            Flight flight,
            ServiceClass newClass)
            throws IOException, NotEnoughSeatsException {

        requireActive(booking);
        if (booking.status() != BookingStatus.PENDING) {
            throw new IOException("Only Pending bookings can change service class.");
        }
        int count = booking.seats().size();

        List<String> newSeats = assignSeats(flight, newClass, count);

        BigDecimal newTotal = flight.getFareFor(newClass)
                .multiply(BigDecimal.valueOf(count));

        Booking updated = booking.withServiceClass(
                newClass, newSeats, newTotal);

        if (!updateBooking(updated)) throw new IOException("Booking was not found.");

        return updated;
    }

    // Convert a booking into one line of text
    private String toLine(Booking booking) throws IOException {
        List<String> entries = new ArrayList<>();
        for (PassengerSeat passenger : booking.passengers()) {
            entries.add(passenger.getSeatNumber() + ":" + passenger.getPassengerName());
        }
        String[] fields = {
                booking.bookingId(),
                booking.username(),
                booking.flightNumber(),
                booking.serviceClass().name(),
                String.join(";", entries),
                booking.totalPrice().toPlainString(),
                booking.status().name()
        };

        for (String field : fields) {
            if (field.contains("|")
                    || field.contains("\n")
                    || field.contains("\r")) {

                throw new IOException("Invalid booking field.");
            }
        }

        return String.join("|", fields);
    }

    // ===================== IDS AND SEATS =====================

    // Generate the next booking ID
    public String nextBookingId() throws IOException {
        int highest = 0;

        for (Booking booking : loadBookings()) {
            try {
                int number = Integer.parseInt(
                        booking.bookingId().substring(2));

                highest = Math.max(highest, number);

            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println("Invalid Booking ID: " + booking.bookingId());
            }
        }

        return String.format("BK%04d", highest + 1);
    }

    // Get all available seats for a flight and service class
    public List<String> availableSeats(
            Flight flight,
            ServiceClass serviceClass) throws IOException {

        List<String> taken = new ArrayList<>();

        for (Booking booking : loadBookings()) {

            if (booking.isActive()
                    && booking.flightNumber().equalsIgnoreCase(flight.getFlightNumber())
                    && booking.serviceClass() == serviceClass) {

                taken.addAll(booking.seats());
            }
        }

        List<String> available = new ArrayList<>();

        for (int number = 1;
                number <= flight.getSeatCount(serviceClass);
                number++) {

            String seat = serviceClass.seatPrefix() + number;

            if (!taken.contains(seat)) {
                available.add(seat);
            }
        }

        return available;
    }

    // Automatically assign available seats
    public List<String> assignSeats(
            Flight flight,
            ServiceClass serviceClass,
            int count)
            throws IOException, NotEnoughSeatsException {

        List<String> available = availableSeats(flight, serviceClass);

        if (available.size() < count) {
            throw new NotEnoughSeatsException(
                    "Only " + available.size() + " "
                    + serviceClass + " seat(s) left on flight "
                    + flight.getFlightNumber() + ".");
        }

        return new ArrayList<>(available.subList(0, count));
    }
}
