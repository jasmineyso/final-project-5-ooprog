package src.menu.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.FlightStatus;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.util.Display;
import src.util.InformationTables;
import src.util.Input;

/**
 * Admin > Manage Passenger Bookings
 * View, search, filter, update, and cancel passengers' bookings.
 */
public class AdminBookingMenu {

    /** Runs this submenu until the administrator selects Back. */
    public void start() {
        boolean running = true;
        while (running) {
            running = handleChoice(Input.askChoice(getTitle(), getOptions()));
        }
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

    // ===================== VIEWING =====================

    public void viewAllBookings() {
        showBookings("All Bookings", booking -> true);
    }

    /** Matches any part of the passenger's name, ignoring capitals: "dela" finds "Juan Dela Cruz". */
    private void searchByPassenger() {
        Display.prompt("Enter passenger name (or part of it)");
        String search = Input.getScanner().nextLine().trim().toLowerCase();
        if (search.isEmpty()) {
            Display.error("Please type at least one character.");
            return;
        }
        showBookings("Bookings for \"" + search + "\"",
                booking -> booking.passengerName().toLowerCase().contains(search));
    }

    private void filterByFlight() {
        Display.prompt("Enter flight number");
        String flightNumber = Input.getScanner().nextLine().trim().replace(" ", "");
        showBookings("Bookings on flight " + flightNumber.toUpperCase(),
                booking -> booking.flightNumber().equalsIgnoreCase(flightNumber));
    }

    private void filterByStatus() {
        BookingStatus[] statuses = BookingStatus.values();
        String[] options = new String[statuses.length + 1];
        for (int i = 0; i < statuses.length; i++) {
            options[i] = statuses[i].toString();
        }
        options[statuses.length] = "Back";

        int choice = Input.askChoice("Filter by Status", options);
        if (choice == options.length) {
            return; // Back
        }
        BookingStatus status = statuses[choice - 1];
        showBookings(status + " Bookings", booking -> booking.status() == status);
    }

    /** Prints every booking that matches the condition, sorted by booking ID. */
    private void showBookings(String title, Predicate<Booking> condition) {
        try {
            List<Booking> matches = new ArrayList<>();
            for (Booking booking : BookingStorage.getInstance().loadBookings()) {
                if (condition.test(booking)) {
                    matches.add(booking);
                }
            }
            Display.title(title);
            if (matches.isEmpty()) {
                Display.message("No bookings found.");
                return;
            }
            matches.sort(Comparator.comparing(Booking::bookingId));
            InformationTables.bookings(matches);
            Display.message(matches.size() + " booking(s)");
        } catch (IOException e) {
            Display.error("Could not load bookings: " + e.getMessage());
        }
    }

    // ===================== UPDATE / CANCEL =====================

    private void updateOrCancelBooking() {
        Display.prompt("Enter booking ID (e.g. BK0001)");
        String bookingId = Input.getScanner().nextLine().trim();
        try {
            Booking booking = BookingStorage.getInstance().findBooking(bookingId);
            if (booking == null) {
                Display.error("Booking " + bookingId + " was not found.");
                return;
            }
            InformationTables.bookings(List.of(booking));
            if (booking.status() == BookingStatus.CANCELLED) {
                Display.error("This booking is already cancelled.");
                return;
            }

            int action = Input.askChoice("Update/Cancel Booking",
                    "Change Service Class",
                    "Cancel Booking",
                    "Back");
            switch (action) {
                case 1 -> changeServiceClass(booking);
                case 2 -> cancelBooking(booking);
                default -> { } // Back
            }
        } catch (IOException e) {
            Display.error("Could not update booking: " + e.getMessage());
        }
    }

    /** Only Pending bookings on flights that aren't cancelled can change class. */
    private void changeServiceClass(Booking booking) throws IOException {
        if (booking.status() != BookingStatus.PENDING) {
            Display.error("Only Pending bookings can be changed. Paid bookings must be cancelled instead.");
            return;
        }
        Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
        if (flight == null || flight.getStatus() == FlightStatus.CANCELLED) {
            Display.error("Flight " + booking.flightNumber() + " is no longer available.");
            return;
        }

        ServiceClass[] classes = ServiceClass.values();
        String[] options = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            int left = BookingStorage.getInstance().availableSeats(flight, classes[i]).size();
            options[i] = classes[i] + " - PHP " + flight.getFareFor(classes[i]).toPlainString()
                    + " (" + left + " seats left)";
        }
        ServiceClass newClass = classes[Input.askChoice("Choose the New Service Class", options) - 1];
        if (newClass == booking.serviceClass()) {
            Display.error("The booking is already in " + newClass + ".");
            return;
        }

        try {
            Booking updated = BookingStorage.getInstance().changeServiceClass(booking, flight, newClass);
            Display.success("Booking " + updated.bookingId() + " moved to " + newClass + ".");
            InformationTables.bookings(List.of(updated));
        } catch (NotEnoughSeatsException e) {
            Display.error(e.getMessage());
        }
    }

    private void cancelBooking(Booking booking) throws IOException {
        if (!Input.askYesNo("Cancel booking " + booking.bookingId() + "?")) {
            Display.message("Booking not changed.");
            return;
        }
        BookingStorage.getInstance().updateBooking(booking.withStatus(BookingStatus.CANCELLED));
        Display.success("Booking " + booking.bookingId() + " has been cancelled. Its seats are free again.");
        if (booking.status() == BookingStatus.PAID) {
            Display.message("This booking was paid. Refund PHP " + booking.totalPrice().toPlainString()
                    + " to the passenger.");
        }
    }
}