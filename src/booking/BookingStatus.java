package src.booking;

/** Where a booking stands. Every new booking starts as PENDING until it is paid. */
public enum BookingStatus {
    PENDING("Pending"),
    PAID("Paid / Booked"),
    CANCELLED("Cancelled");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
