package src.booking;

/** Where a booking stands. Every new booking starts as PENDING until it is paid. */
public enum BookingStatus {
    PENDING("Pending"),
    PAID("Paid / Booked"),
    CANCELLED("Cancelled"),
    CANCELLED_REFUNDED("Cancelled / Refunded"),
    COMPLETED("Completed");

    // compatibility name used by the attached version of this enum
    public static final BookingStatus CONFIRMED = PAID;

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
