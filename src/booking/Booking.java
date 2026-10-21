package src.booking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import src.flight.ServiceClass;
import src.menu.regular_user.PassengerSeat;

/** A booking owns one immutable passenger entry per seat. Updates return copies. */
public final class Booking {
    private final String bookingId;
    private final String username;
    private final String flightNumber;
    private final ServiceClass serviceClass;
    private final List<PassengerSeat> passengers;
    private final BigDecimal totalPrice;
    private final BookingStatus status;

    public Booking(String bookingId, String username, String flightNumber, ServiceClass serviceClass,
            List<PassengerSeat> passengers, BigDecimal totalPrice, BookingStatus status) {
        this.bookingId = Objects.requireNonNull(bookingId);
        this.username = Objects.requireNonNull(username);
        this.flightNumber = Objects.requireNonNull(flightNumber);
        this.serviceClass = Objects.requireNonNull(serviceClass);
        this.passengers = List.copyOf(passengers);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.status = Objects.requireNonNull(status);
        if (bookingId.isBlank() || username.isBlank() || flightNumber.isBlank()
                || passengers.isEmpty() || totalPrice.signum() < 0) {
            throw new IllegalArgumentException("Invalid booking details.");
        }
        HashSet<String> seats = new HashSet<>();
        for (PassengerSeat passenger : passengers) {
            if (!passenger.getSeatNumber().startsWith(serviceClass.seatPrefix())
                    || !seats.add(passenger.getSeatNumber())) {
                throw new IllegalArgumentException("Seats must be unique and match the service class.");
            }
        }
    }

    /** Reads the original single-name booking format without losing its stored name. */
    public Booking(String id, String username, String name, String flight, ServiceClass cabin,
            List<String> seats, BigDecimal total, BookingStatus status) {
        this(id, username, flight, cabin, legacyPassengers(seats, name), total, status);
    }

    private static List<PassengerSeat> legacyPassengers(List<String> seats, String name) {
        List<PassengerSeat> result = new ArrayList<>();
        for (String seat : seats) {
            result.add(PassengerSeat.fromLegacy(seat, name));
        }
        return result;
    }

    public String bookingId() { return bookingId; }
    public String username() { return username; }
    public String flightNumber() { return flightNumber; }
    public ServiceClass serviceClass() { return serviceClass; }
    public List<PassengerSeat> passengers() { return passengers; }
    public List<PassengerSeat> getPassengers() { return passengers; }
    public BigDecimal totalPrice() { return totalPrice; }
    public BookingStatus status() { return status; }

    public List<String> seats() {
        List<String> result = new ArrayList<>();
        for (PassengerSeat passenger : passengers) {
            result.add(passenger.getSeatNumber());
        }
        return List.copyOf(result);
    }

    public Booking withStatus(BookingStatus updated) {
        return new Booking(bookingId, username, flightNumber, serviceClass, passengers, totalPrice, updated);
    }

    public Booking withAssignedSeats(String flight, ServiceClass cabin, List<String> seats, BigDecimal total) {
        if (seats.size() != passengers.size()) {
            throw new IllegalArgumentException("Passenger count cannot change.");
        }
        List<PassengerSeat> updated = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            updated.add(passengers.get(i).withSeatNumber(seats.get(i)));
        }
        return new Booking(bookingId, username, flight, cabin, updated, total, status);
    }

    public Booking withServiceClass(ServiceClass cabin, List<String> seats, BigDecimal total) {
        return withAssignedSeats(flightNumber, cabin, seats, total);
    }

    public boolean isActive() {
        return status != BookingStatus.CANCELLED && status != BookingStatus.COMPLETED;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Booking booking)) return false;
        return bookingId.equals(booking.bookingId) && username.equals(booking.username)
                && flightNumber.equals(booking.flightNumber) && serviceClass == booking.serviceClass
                && passengers.equals(booking.passengers) && totalPrice.equals(booking.totalPrice)
                && status == booking.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, username, flightNumber, serviceClass, passengers, totalPrice, status);
    }

    @Override
    public String toString() {
        List<String> details = new ArrayList<>();
        for (PassengerSeat passenger : passengers) {
            details.add(passenger.getSeatNumber() + ": " + passenger.getPassengerName());
        }
        return bookingId + " | " + username + " | Flight " + flightNumber + " | " + serviceClass
                + " | Passengers " + String.join(", ", details) + " | PHP " + totalPrice + " | " + status;
    }
}
