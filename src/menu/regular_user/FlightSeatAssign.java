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
import src.flight.ServiceClass;

// assigns available seats and prepares a booking for the payment choice
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

    public List<String> assignSeats() throws IOException, NotEnoughSeatsException {
        if (!flight.isBookable()) {
            throw new IOException("This flight is cancelled or has already departed.");
        }

        return BookingStorage.getInstance().assignSeats(flight, serviceClass, numberOfSeats);
    }

    public Booking prepareBookingFor(RegularUser user, List<PassengerSeat> passengers) throws IOException {
        if (passengers.size() != numberOfSeats) {
            throw new IllegalArgumentException("Enter exactly one passenger name per assigned seat.");
        }
        BigDecimal totalPrice = flight.getFareFor(serviceClass).multiply(BigDecimal.valueOf(numberOfSeats));
        return new Booking(BookingStorage.getInstance().nextBookingId(), user.getUsername(),
                flight.getFlightNumber(), serviceClass, passengers, totalPrice, BookingStatus.PENDING);
    }
}
