package src.util;

import java.math.BigDecimal;
import java.util.List;

import src.account.RegularUser;
import src.booking.Booking;
import src.flight.Flight;

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
        String[] headers = { "Booking ID", "Passenger", "Account", "Flight Number", "Service Class", "Seats",
                "Fee", "Booking Status" };
        String[][] rows = new String[bookings.size()][];
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            rows[i] = new String[] { booking.bookingId(), booking.passengerName(), booking.username(),
                    booking.flightNumber(), booking.serviceClass().toString(), String.join(", ", booking.seats()),
                    money(booking.totalPrice()), booking.status().toString() };
        }
        Display.table(headers, rows);
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
