package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import src.account.Access;
import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.util.Input;
import src.util.Display;
import src.util.InformationTables;

public class RegularUserOperations {

    private static RegularUserOperations instance;

    private final BookingStorage bookingStorage = BookingStorage.getInstance();

    private RegularUserOperations() {
    }

    public synchronized static RegularUserOperations getInstance() {
        if (instance == null) {
            instance = new RegularUserOperations();
        }
        return instance;
    }

    public void viewFlights() {
        runOperation(this::doViewFlights);
    }

    private void doViewFlights() {
        System.out.println();
        try {
            if (FlightStorage.getInstance().loadFlights().isEmpty()) {
                System.out.println("No flights are available yet.");
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not load available flights: " + e.getMessage());
            return;
        }

        ViewFlightOperations viewFlights = new ViewFlightOperations();

        boolean running = true;
        while (running) {
            int choice = Input.askChoice("View Flights",
                    "View All Flights",
                    "Search by Origin and Destination",
                    "Search Service Class Remaining Seats",
                    "Back");

            switch (choice) {
                case 1 -> viewFlights.viewAllFlights();
                case 2 -> viewFlights.searchByOriginAndDestination();
                case 3 -> viewFlights.searchByServiceClassRemainingSeats();
                case 4 -> running = false;
            }
        }
    }

    public void viewBookedFlights(RegularUser user) {
        runOperation(() -> new ViewBookedFlightOperation().viewBookedFlights(user));
    }

    public void bookFlight(RegularUser user) {
        runOperation(() -> new BookFlightOperation().bookFlight(user));
    }

    /** Lets a passenger change the service class of their own pending booking. */
    public void updateBookedFlight(RegularUser user) {
        runOperation(() -> doUpdateBookedFlight(user));
    }

    private void doUpdateBookedFlight(RegularUser user) {
        Display.subTitle("Update Booked Flight");
        try {
            Booking booking = selectOwnBooking(user);
            if (booking == null) {
                return;
            }
            if (booking.status() != BookingStatus.PENDING) {
                Display.error("Only Pending bookings can be changed. Paid bookings must be cancelled instead.");
                return;
            }
            Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
            if (flight == null || !flight.isBookable()) {
                Display.error("This flight is unavailable, cancelled or has already departed.");
                return;
            }

            ServiceClass[] classes = ServiceClass.values();
            String[] options = new String[classes.length + 1];
            for (int i = 0; i < classes.length; i++) {
                int remaining = bookingStorage.availableSeats(flight, classes[i]).size();
                options[i] = classes[i] + " - " + InformationTables.money(flight.getFareFor(classes[i]))
                        + " (" + remaining + " seats left)";
            }
            options[classes.length] = "Back";
            int choice = Input.askChoice("Choose the New Service Class", options);
            if (choice == options.length) {
                return;
            }
            ServiceClass newClass = classes[choice - 1];
            if (newClass == booking.serviceClass()) {
                Display.error("The booking is already in " + newClass + ".");
                return;
            }
            if (bookingStorage.availableSeats(flight, newClass).size() < booking.seats().size()) {
                Display.error("There are not enough seats in " + newClass + " for this booking.");
                return;
            }
            BigDecimal newTotal = flight.getFareFor(newClass)
                    .multiply(BigDecimal.valueOf(booking.seats().size()));
            Display.message("New total: " + InformationTables.money(newTotal));
            if (!Input.askYesNo("Change service class to " + newClass + "?")) {
                Display.message("Booking not changed.");
                return;
            }
            Booking updated = bookingStorage.changeServiceClass(booking, flight, newClass);
            Display.success("Booking " + updated.bookingId() + " has been updated.");
            InformationTables.bookings(List.of(updated));
        } catch (IOException | NotEnoughSeatsException e) {
            Display.error("Could not update booking: " + e.getMessage());
        }
    }

    public void cancelBookedFlight(RegularUser user) {
        runOperation(() -> doCancelBookedFlight(user));
    }

    private void doCancelBookedFlight(RegularUser user) {
        Display.title("Cancel Booked Flight");
        try {
            Booking booking = selectOwnBooking(user);
            if (booking == null) {
                return;
            }
            if (!Input.askYesNo("Cancel booking " + booking.bookingId() + "?")) {
                Display.message("Booking not changed.");
                return;
            }
            if (!bookingStorage.cancelBooking(booking.bookingId())) {
                Display.error("Booking was not found or is no longer active.");
                return;
            }
            Display.success("Booking " + booking.bookingId() + " has been cancelled. Its seats are free again.");
            if (booking.status() == BookingStatus.PAID) {
                Display.message("Your payment of " + InformationTables.money(booking.totalPrice())
                        + " has been automatically refunded.");
            }
        } catch (IOException e) {
            Display.error("Could not cancel booking: " + e.getMessage());
        }
    }

    /**
     * Shows only the logged-in passenger's bookings and checks ownership before
     * use.
     */
    private Booking selectOwnBooking(RegularUser user) throws IOException {
        List<Booking> bookings = bookingStorage.bookingsOf(user.getUsername());
        if (bookings.isEmpty()) {
            Display.message("You have no bookings yet.");
            return null;
        }
        InformationTables.bookings(bookings);
        while (true) {
            Display.prompt("Enter Booking ID (0 to go back)");
            String bookingId = Input.readLine().trim();
            if (bookingId.equals("0")) {
                return null;
            }
            Booking booking = bookingStorage.findBooking(bookingId);
            if (booking == null || !booking.username().equalsIgnoreCase(user.getUsername())) {
                Display.error("Booking not found in your account.");
                continue;
            }
            Access.requireOwnerOrAdmin(user, booking.username());
            if (!booking.isActive()) {
                Display.error("Cancelled or completed bookings cannot be modified.");
                continue;
            }
            return booking;
        }
    }

    public void payBooking(RegularUser user) {
        runOperation(() -> new PaymentOperation().payBooking(user));
    }

    private void runOperation(Runnable operation) {
        try {
            operation.run();
        } catch (Input.EndOfInputException e) {
            Display.message("Input ended. Returning to the menu.");
        }
    }
}
