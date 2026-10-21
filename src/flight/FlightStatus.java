package src.flight;

/** Where a flight stands. Every new flight starts as SCHEDULED. */
public enum FlightStatus {
    SCHEDULED("Scheduled"),
    DELAYED("Delayed"),
    CANCELLED("Cancelled");

    /** Compatibility name used by the attached version of this enum. */
    public static final FlightStatus ON_TIME = SCHEDULED;

    private final String label;

    FlightStatus(String label) {
        this.label = label;
    }

    /** The readable name, e.g. "Delayed" instead of DELAYED. */
    @Override
    public String toString() {
        return label;
    }
}
