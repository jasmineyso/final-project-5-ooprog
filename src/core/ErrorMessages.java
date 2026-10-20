package src.core;

// USE THIS CLASS TO STORE ALL ERROR MESSAGES IN ONE PLACE.
// THEN CALL THE ERROR MESSAGES FROM THIS CLASS INSTEAD OF WRITING THEM IN THE CODE.

public class ErrorMessages {
    public static final String SAVE_FAILED = "Account could not be saved: ";
    public static final String LOAD_FAILED = "Saved accounts could not be loaded: ";

    public static final String INVALID_CREDENTIALS = "Invalid username or password. Please try again.";

    public static final String INVALID_USERNAME = "Username cannot be blank. Please try again.";
    public static final String INVALID_PASSWORD = "Password cannot be blank. Please try again.";
    public static final String INVALID_EMAIL = "Enter a valid email address (name@domain.tld).";
    public static final String INVALID_PHONE_NUMBER = "Phone number must contain digits, with an optional leading +.";
    public static final String INVALID_ADDRESS = "Address cannot be blank. Please try again.";
    
    public static final String INVALID_CHOICE = "Invalid choice. Please try again.";

    public static final String FLIGHT_EMPTY = "There are no flights available.";
    public static final String FLIGHT_NOT_FOUND = "Flight number does not exist. Please try again.";
    public static final String INVALID_NUMBER_OF_SEATS = "Enter a whole number from 1 to 9. Please try again.";
    public static final String BOOKING_SAVE_FAILED = "Could not save booking: ";
    public static final String BOOKING_NOT_FOUND = "Booking not found in your account. Please try again.";
    public static final String BOOKING_NOT_PENDING = "Only Pending bookings can be paid. Please choose another booking.";
    public static final String INVALID_PAYMENT_AMOUNT = "Enter a valid cash amount with up to two decimal places.";
    public static final String INSUFFICIENT_PAYMENT = "The cash amount must cover the full booking fee. Please try again.";
    public static final String PAYMENT_FAILED = "Could not complete payment: ";
}
