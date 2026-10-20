package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.booking.exception.NotEnoughSeatsException;
import src.flight.Flight;
import src.flight.FlightStatus;
import src.flight.ServiceClass;

/** Assigns available seats and saves the booking to BOOKED_FLIGHTS.txt. */
public class FlightSeatAssign {
    private final Flight flight;
    private final ServiceClass serviceClass;
    private final int numberOfSeats;

    public FlightSeatAssign(Flight flight, ServiceClass serviceClass, int numberOfSeats) {
        this.flight = flight;
        this.serviceClass = serviceClass;
        if (numberOfSeats < 1 || numberOfSeats > 9) {
            throw new IllegalArgumentException("Number of seats must be between 1 and 9.");
        }
        this.numberOfSeats = numberOfSeats;
    }

    public Booking bookFor(RegularUser user) throws IOException, NotEnoughSeatsException {
        if (flight.status() == FlightStatus.CANCELLED) {
            throw new IOException("This flight is cancelled.");
        }

        BookingStorage storage = BookingStorage.getInstance();
        synchronized (storage) {
            // Existing Pending and Paid bookings occupy seats; cancelled bookings do not.
            List<String> seats = storage.assignSeats(flight, serviceClass, numberOfSeats);
            BigDecimal totalPrice = flight.fareFor(serviceClass)
                    .multiply(BigDecimal.valueOf(numberOfSeats));
            // Accounts have no separate passenger name, so use the account username.
            Booking booking = new Booking(storage.nextBookingId(), user.getUsername(),
                    user.getUsername(), flight.flightNumber(), serviceClass, seats,
                    totalPrice, BookingStatus.PENDING);
            storage.saveBooking(booking);
            return booking;
        }
    }
}
