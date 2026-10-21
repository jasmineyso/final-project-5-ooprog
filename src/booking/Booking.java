package src.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import src.flight.ServiceClass;

/**
 * One booking: one or more seats in one service class on one flight.
 * Like Flight, it is a record, so "changing" a booking means making a new one with with...().
 *
 * username      = the account that made the booking (who can see, pay, and cancel it)
 * passengerName = the name typed in when booking (can be someone else, e.g. a family member)
 */
public record Booking(String bookingId, String username, String passengerName, String flightNumber,
        ServiceClass serviceClass, List<String> seats, BigDecimal totalPrice, BookingStatus status) {

    /** Runs on every new Booking: makes the seat list read-only. */
    public Booking {
        Objects.requireNonNull(bookingId, "bookingId");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(passengerName, "passengerName");
        Objects.requireNonNull(flightNumber, "flightNumber");
        Objects.requireNonNull(serviceClass, "serviceClass");
        Objects.requireNonNull(seats, "seats");
        Objects.requireNonNull(totalPrice, "totalPrice");
        Objects.requireNonNull(status, "status");
        seats = List.copyOf(seats);
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("A booking must contain at least one seat.");
        }
    }

    /** A copy of this booking with a different status. */
    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(bookingId, username, passengerName, flightNumber,
                serviceClass, seats, totalPrice, newStatus);
    }

    /** A copy of this booking moved to another class, with its new seats and price. */
    public Booking withServiceClass(ServiceClass newClass, List<String> newSeats, BigDecimal newTotal) {
        return new Booking(bookingId, username, passengerName, flightNumber,
                newClass, newSeats, newTotal, status);
    }

    /** True while this booking can still reserve seats or be modified. */
    public boolean isActive() {
        return status != BookingStatus.CANCELLED && status != BookingStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return bookingId + " | " + passengerName + " | Flight " + flightNumber
                + " | " + serviceClass + " | Seats " + String.join(", ", seats)
                + " | PHP " + totalPrice.toPlainString() + " | " + status;
    }
}
