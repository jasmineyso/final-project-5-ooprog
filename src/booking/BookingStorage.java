
package src.booking;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.ServiceClass;

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
                    if (fields.length != 8) {
                        throw new IllegalArgumentException("Invalid number of fields");
                    }

                    List<String> seats = Arrays.asList(fields[5].split(","));

                    Booking booking = new Booking(
                            fields[0],
                            fields[1],
                            fields[2],
                            fields[3],
                            ServiceClass.valueOf(fields[4]),
                            seats,
                            new BigDecimal(fields[6]),
                            BookingStatus.valueOf(fields[7])
                    );

                    bookings.add(booking);

                } catch (IllegalArgumentException e) {
                    throw new IOException(
                            "Invalid booking data on line " + lineNumber, e);
                }
            }
        }

        return bookings;
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
    public void saveBooking(Booking booking) throws IOException {
        Files.createDirectories(filePath.toAbsolutePath().getParent());

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath.toFile(), StandardCharsets.UTF_8, true))) {

            writer.write(toLine(booking));
            writer.newLine();
        }
    }

    // Replace all bookings in the text file
    public void saveAllBookings(List<Booking> bookings) throws IOException {
        Files.createDirectories(filePath.toAbsolutePath().getParent());

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath.toFile(), StandardCharsets.UTF_8))) {

            for (Booking booking : bookings) {
                writer.write(toLine(booking));
                writer.newLine();
            }
        }
    }

    // Update an existing booking
    public boolean updateBooking(Booking updated) throws IOException {
        List<Booking> bookings = loadBookings();

        for (int i = 0; i < bookings.size(); i++) {

            if (bookings.get(i).bookingId()
                    .equalsIgnoreCase(updated.bookingId())) {

                bookings.set(i, updated);
                saveAllBookings(bookings);

                return true;
            }
        }

        return false;
    }

    // Cancel bookings that match a condition
    public int cancelBookingsWhere(Predicate<Booking> condition)
            throws IOException {

        List<Booking> bookings = loadBookings();
        int cancelled = 0;

        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);

            if (booking.status() != BookingStatus.CANCELLED
                    && condition.test(booking)) {

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

        int count = booking.seats().size();

        List<String> newSeats = assignSeats(flight, newClass, count);

        BigDecimal newTotal = flight.getFareFor(newClass)
                .multiply(BigDecimal.valueOf(count));

        Booking updated = booking.withServiceClass(
                newClass, newSeats, newTotal);

        updateBooking(updated);

        return updated;
    }

    // Convert a booking into one line of text
    private String toLine(Booking booking) throws IOException {

        String[] fields = {
                booking.bookingId(),
                booking.username(),
                booking.passengerName(),
                booking.flightNumber(),
                booking.serviceClass().name(),
                String.join(",", booking.seats()),
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

            if (booking.status() != BookingStatus.CANCELLED
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
