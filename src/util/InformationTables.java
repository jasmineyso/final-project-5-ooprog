package src.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import src.account.RegularUser;
import src.booking.Booking;
import src.flight.Flight;
import src.menu.regular_user.PassengerSeat;

/** Shared columns and formatting for information shown by both user roles. */
public class InformationTables {
    private InformationTables() {
    }

    public static String money(BigDecimal amount) {
        return String.format(java.util.Locale.US, "PHP %,.2f", amount);
    }

    public static void flights(List<Flight> flights) {
        String[] headers = { "Flight Number", "Gate", "Origin", "Destination", "Departure", "Arrival",
                "Status", "Economy Fare", "Premium Economy Fare", "Business Fare", "First Class Fare", "Seats" };
        String[][] rows = new String[flights.size()][];
        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            rows[i] = new String[] { flight.getFlightNumber(), flight.getGateNumber(), flight.getOrigin(),
                    flight.getDestination(), Validator.formatDateTime(flight.getDepartureTime()),
                    Validator.formatDateTime(flight.getArrivalTime()), flight.getStatus().toString(),
                    money(flight.getEconomyFare()), money(flight.getPremiumEconomyFare()),
                    money(flight.getBusinessFare()), money(flight.getFirstClassFare()),
                    Integer.toString(flight.getTotalSeats()) };
        }
        Display.table(headers, rows);
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
