package src.booking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.ServiceClass;

/**
 * Saves and loads bookings from BOOKINGS.txt (Singleton, like FlightStorage).
 * Also assigns seats automatically.
 */
public final class BookingStorage {
    private static BookingStorage instance;
    private final Path filePath;

    private BookingStorage() {
        // Support launching from either the project or its parent workspace.
        Path directory = Path.of("src", "files");
        Path workspaceDirectory = Path.of("final-project-5-ooprog", "src", "files");
        if (Files.isDirectory(workspaceDirectory)) {
            directory = workspaceDirectory;
        }
        filePath = directory.resolve("BOOKINGS.txt");
    }

    public static BookingStorage getInstance() {
        if (instance == null) {
            instance = new BookingStorage();
        }
        return instance;
    }

    // ===================== LOADING =====================

    public List<Booking> loadBookings() throws IOException {
        List<Booking> bookings = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return bookings;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            try {
                if (fields.length != 8) {
                    throw new IllegalArgumentException("Wrong number of fields");
                }
                List<String> seats = Arrays.asList(fields[5].split(","));
                bookings.add(new Booking(fields[0], fields[1], fields[2], fields[3],
                        ServiceClass.valueOf(fields[4]), seats,
                        new BigDecimal(fields[6]), BookingStatus.valueOf(fields[7])));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid booking data on line " + lineNumber + " of BOOKINGS.txt.", e);
            }
        }
        return bookings;
    }

    /** Returns the booking with this ID (ignores capital letters), or null. */
    public Booking findBooking(String bookingId) throws IOException {
        for (Booking booking : loadBookings()) {
            if (booking.bookingId().equalsIgnoreCase(bookingId.trim())) {
                return booking;
            }
        }
        return null;
    }

    /** Every booking made by one account. */
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

    /** Adds ONE booking to the end of the file. */
    public void saveBooking(Booking booking) throws IOException {
        String line = toLine(booking);
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    /** Replaces the WHOLE file with this list. */
    public void saveAllBookings(List<Booking> bookings) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Booking booking : bookings) {
            lines.add(toLine(booking));
        }
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Files.write(filePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Replaces the saved booking with the same ID. Returns false if it wasn't found. */
    public boolean updateBooking(Booking updated) throws IOException {
        List<Booking> bookings = loadBookings();
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).bookingId().equalsIgnoreCase(updated.bookingId())) {
                bookings.set(i, updated);
                saveAllBookings(bookings);
                return true;
            }
        }
        return false;
    }

    /**
     * Cancels every active (Pending or Paid) booking that matches the condition.
     * Returns how many bookings were cancelled. Their seats become free again.
     */
    public int cancelBookingsWhere(Predicate<Booking> condition) throws IOException {
        List<Booking> bookings = loadBookings();
        int cancelled = 0;
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            if (booking.status() != BookingStatus.CANCELLED && condition.test(booking)) {
                bookings.set(i, booking.withStatus(BookingStatus.CANCELLED));
                cancelled++;
            }
        }
        if (cancelled > 0) {
            saveAllBookings(bookings);
        }
        return cancelled;
    }

    /** Used when a flight is cancelled. */
    public int cancelBookingsForFlight(String flightNumber) throws IOException {
        return cancelBookingsWhere(booking -> booking.flightNumber().equalsIgnoreCase(flightNumber));
    }

    /** Used when a user account is deleted. */
    public int cancelBookingsOf(String username) throws IOException {
        return cancelBookingsWhere(booking -> booking.username().equalsIgnoreCase(username));
    }

    /**
     * Moves a booking to another service class: new seats are assigned automatically
     * and the price is recalculated. Saves and returns the updated booking.
     */
    public Booking changeServiceClass(Booking booking, Flight flight, ServiceClass newClass)
            throws IOException, NotEnoughSeatsException {
        int count = booking.seats().size();
        List<String> newSeats = assignSeats(flight, newClass, count);
        BigDecimal newTotal = flight.fareFor(newClass).multiply(BigDecimal.valueOf(count));
        Booking updated = booking.withServiceClass(newClass, newSeats, newTotal);
        updateBooking(updated);
        return updated;
    }

    /**
     * Turns one booking into one line of the file (8 fields):
     * id|username|passengerName|flightNumber|CLASS|seat,seat,...|total|STATUS
     */
    private String toLine(Booking booking) throws IOException {
        String[] fields = {
                booking.bookingId(), booking.username(), booking.passengerName(), booking.flightNumber(),
                booking.serviceClass().name(), String.join(",", booking.seats()),
                booking.totalPrice().toPlainString(), booking.status().name()
        };
        for (String field : fields) {
            if (field.contains("|") || field.contains("\n") || field.contains("\r")) {
                throw new IOException("Booking fields cannot contain | or line breaks.");
            }
        }
        return String.join("|", fields);
    }

    // ===================== IDS AND SEATS =====================

    /** The next free booking ID: BK0001, BK0002, ... */
    public String nextBookingId() throws IOException {
        int highest = 0;
        for (Booking booking : loadBookings()) {
            try {
                highest = Math.max(highest, Integer.parseInt(booking.bookingId().substring(2)));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // not in BK#### form; ignore it
            }
        }
        return String.format("BK%04d", highest + 1);
    }

    /** Seats in one class of one flight that are NOT used by any active (non-cancelled) booking. */
    public List<String> availableSeats(Flight flight, ServiceClass serviceClass) throws IOException {
        Set<String> taken = new HashSet<>();
        for (Booking booking : loadBookings()) {
            if (booking.status() != BookingStatus.CANCELLED
                    && booking.flightNumber().equalsIgnoreCase(flight.flightNumber())
                    && booking.serviceClass() == serviceClass) {
                taken.addAll(booking.seats());
            }
        }

        List<String> available = new ArrayList<>();
        for (int number = 1; number <= flight.seatCount(serviceClass); number++) {
            String seat = serviceClass.seatPrefix() + number;
            if (!taken.contains(seat)) {
                available.add(seat);
            }
        }
        return available;
    }

    /**
     * Picks the first 'count' free seats in a class, e.g. B3, B4.
     * Throws NotEnoughSeatsException if the class doesn't have that many left.
     */
    public List<String> assignSeats(Flight flight, ServiceClass serviceClass, int count)
            throws IOException, NotEnoughSeatsException {
        List<String> available = availableSeats(flight, serviceClass);
        if (available.size() < count) {
            throw new NotEnoughSeatsException("Only " + available.size() + " " + serviceClass
                    + " seat(s) left on flight " + flight.flightNumber() + ".");
        }
        return new ArrayList<>(available.subList(0, count));
    }
}