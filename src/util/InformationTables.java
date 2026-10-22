package src.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import src.account.RegularUser;
import src.booking.Booking;
import src.flight.Flight;
import src.flight.ServiceClass;
import src.menu.regular_user.PassengerSeat;

/** Shared columns and formatting for information shown by both user roles. */
public class InformationTables {
    private InformationTables() {
    }

    public static String money(BigDecimal amount) {
        return Display.money(amount);
    }

    /** Short table for lists of flights. */
    public static void flights(List<Flight> flights) {
        String[] headers = { "Flight", "From", "To", "Departure", "Arrival", "Status" };
        String[][] rows = new String[flights.size()][];
        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            rows[i] = new String[] { flight.getFlightNumber(), flight.getOrigin(), flight.getDestination(),
                    Validator.formatDateTime(flight.getDepartureTime()),
                    Validator.formatDateTime(flight.getArrivalTime()), flight.getStatus().toString() };
        }
        Display.table(headers, rows);
    }

    /** Everything about one flight, including the fare for each class. */
    public static void flightDetails(Flight flight) {
        Display.detail("Flight", flight.getFlightNumber() + "  (Gate " + flight.getGateNumber() + ")");
        Display.detail("Route", flight.getOrigin() + " -> " + flight.getDestination());
        Display.detail("Departure", Validator.formatDateTime(flight.getDepartureTime()));
        Display.detail("Arrival", Validator.formatDateTime(flight.getArrivalTime()));
        Display.detail("Status", flight.getStatus().toString());
        for (ServiceClass serviceClass : ServiceClass.values()) {
            Display.detail(serviceClass.toString(), money(flight.getFareFor(serviceClass)));
        }
    }

    public static void bookings(List<Booking> bookings) {
        String[] headers = { "Booking ID", "Passenger", "Account", "Flight Number", "Service Class", "Seat",
                "Fee", "Booking Status" };
        List<String[]> rows = new ArrayList<>();
        for (Booking booking : bookings) {
            boolean first = true;
            for (PassengerSeat passenger : booking.passengers()) {
                rows.add(new String[] { booking.bookingId(), passenger.getPassengerName(), booking.username(),
                        booking.flightNumber(), booking.serviceClass().toString(), passenger.getSeatNumber(),
                        first ? money(booking.totalPrice()) : "", booking.status().toString() });
                first = false;
            }
        }
        Display.table(headers, rows.toArray(new String[0][]));
    }

    public static void passengers(Booking booking) {
        String[][] rows = new String[booking.passengers().size()][];
        for (int i = 0; i < rows.length; i++) {
            PassengerSeat passenger = booking.passengers().get(i);
            rows[i] = new String[] { passenger.getSeatNumber(), passenger.getPassengerName() };
        }
        Display.table(new String[] { "Seat", "Passenger Name" }, rows);
    }

    public static void users(List<RegularUser> users) {
        String[][] rows = new String[users.size()][];
        for (int i = 0; i < users.size(); i++) {
            RegularUser user = users.get(i);
            rows[i] = new String[] { user.getUsername(), user.getEmail(), user.getPhoneNumber(), user.getAddress() };
        }
        Display.table(new String[] { "Username", "Email", "Phone Number", "Address" }, rows);
    }

    
}
