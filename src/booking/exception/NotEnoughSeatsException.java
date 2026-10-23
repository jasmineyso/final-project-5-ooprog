package src.booking.exception;

// thrown when a service class doesn't have enough free seats for a booking
public class NotEnoughSeatsException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotEnoughSeatsException(String message) {
        super(message);
    }
}