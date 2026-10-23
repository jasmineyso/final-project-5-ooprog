package src.menu.regular_user;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.flight.Flight;
import src.flight.FlightStatus;
import src.flight.FlightStorage;
import src.util.Display;
import src.util.InformationTables;
import src.util.Input;
import src.util.Validator;

public class ViewBookedFlightOperation {
    public void viewBookedFlights(RegularUser user) {
        try {
            List<Booking> bookings = BookingStorage.getInstance().bookingsOf(user.getUsername());
            if (bookings.isEmpty()) {
                System.out.println("You have no bookings yet.");
                return;
            }

            int choice = Input.askChoice("View Booked Flights",
                    "View Upcoming Trips", "View Cancelled Trips", "View Past / Departed Trips",
                    "View All Booked Flights", "View Expired Bookings", "Back");
            if (choice == 6) {
                return;
            }

            Map<String, Flight> flights = new LinkedHashMap<>();
            try {
                for (Flight flight : FlightStorage.getInstance().loadFlights()) {
                    flights.put(Validator.normalizeFlightNumber(flight.getFlightNumber()), flight);
                }
            } catch (IOException e) {
                System.out.println("Flight details could not be loaded: " + e.getMessage());
            }

            Map<String, List<Booking>> groups = new LinkedHashMap<>();
            groups.put("Upcoming Trips", new ArrayList<>());
            groups.put("Expired Bookings", new ArrayList<>());
            groups.put("Cancelled Trips", new ArrayList<>());
            groups.put("Past / Departed Trips", new ArrayList<>());
            groups.put("Flight Details Unavailable", new ArrayList<>());
            LocalDateTime now = LocalDateTime.now();
            for (Booking booking : bookings) {
                Flight flight = flights.get(Validator.normalizeFlightNumber(booking.flightNumber()));
                String group;
                if (booking.status() == BookingStatus.EXPIRED) {
                    group = "Expired Bookings";
                } else if (booking.isCancelled()
                        || (flight != null && flight.getStatus() == FlightStatus.CANCELLED)) {
                    group = "Cancelled Trips";
                } else if (booking.status() == BookingStatus.COMPLETED) {
                    group = "Past / Departed Trips";
                } else if (flight == null) {
                    group = "Flight Details Unavailable";
                } else if (flight.getStatus() != FlightStatus.DEPARTED && flight.getDepartureTime().isAfter(now)) {
                    group = "Upcoming Trips";
                } else {
                    group = "Past / Departed Trips";
                }
                groups.get(group).add(booking);
            }

            String selectedGroup = switch (choice) {
                case 1 -> "Upcoming Trips";
                case 2 -> "Cancelled Trips";
                case 3 -> "Past / Departed Trips";
                case 5 -> "Expired Bookings";
                default -> null;
            };
            for (Map.Entry<String, List<Booking>> group : groups.entrySet()) {
                if (selectedGroup != null && !selectedGroup.equals(group.getKey())) {
                    continue;
                }
                if (group.getValue().isEmpty() && selectedGroup == null) {
                    continue;
                }
                System.out.println("\n=== " + group.getKey() + " ===");
                if (group.getValue().isEmpty()) {
                    System.out.println("You have no bookings in this category.");
                }
                for (Booking booking : group.getValue()) {
                    displayBooking(booking, flights.get(Validator.normalizeFlightNumber(booking.flightNumber())));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load your bookings: " + e.getMessage());
        }
    }

    private void displayBooking(Booking booking, Flight flight) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Booking ID", booking.bookingId() });
        rows.add(new String[] { "Account", booking.username() });
        rows.add(new String[] { "Flight Number", booking.flightNumber() });
        if (flight == null) {
            System.out.println("Flight details unavailable; the flight may have been removed.");
        } else {
            if (booking.isActive() && flight.getScheduleUpdatedAt() != null) {
                Display.message("SCHEDULE UPDATED - Flight " + flight.getFlightNumber());
                Display.message("Schedule changed on: " + Validator.formatDateTime(flight.getScheduleUpdatedAt()));
                Display.message("New departure: " + Validator.formatDateTime(flight.getDepartureTime()));
                Display.message("New arrival: " + Validator.formatDateTime(flight.getArrivalTime()));
            }
            rows.add(new String[] { "Origin", flight.getOrigin() });
            rows.add(new String[] { "Destination", flight.getDestination() });
            rows.add(new String[] { "Departure", Validator.formatDateTime(flight.getDepartureTime()) });
            rows.add(new String[] { "Arrival", Validator.formatDateTime(flight.getArrivalTime()) });
            rows.add(new String[] { "Gate", flight.getGateNumber() });
        }
        rows.add(new String[] { "Flight Status", flight == null ? "Unavailable" : flight.getStatus().toString() });
        rows.add(new String[] { "Service Class", booking.serviceClass().toString() });
        rows.add(new String[] { "Seats", String.join(", ", booking.seats()) });
        rows.add(new String[] { "Fee", InformationTables.money(booking.totalPrice()) });
        rows.add(new String[] { "Booking Status", booking.status().toString() });
        Display.table(new String[] { "Detail", "Information" }, rows.toArray(new String[0][]));
        InformationTables.passengers(booking);
    }
}
