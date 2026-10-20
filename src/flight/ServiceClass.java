package src.flight;

/**
 * The four cabins a passenger can book.
 * Every flight has 150 seats, always divided the same way.
 * To change the split, change the numbers here and nowhere else.
 */
public enum ServiceClass {
    ECONOMY("Economy", 100, "E"),
    PREMIUM_ECONOMY("Premium Economy", 25, "P"),
    BUSINESS("Business", 15, "B"),
    FIRST_CLASS("First Class", 10, "F");

    private final String label;
    private final int seatsPerFlight;
    private final String seatPrefix;

    ServiceClass(String label, int seatsPerFlight, String seatPrefix) {
        this.label = label;
        this.seatsPerFlight = seatsPerFlight;
        this.seatPrefix = seatPrefix;
    }

    /** The letter in front of this class's seat labels: E1, P3, B12, F10... */
    public String seatPrefix() {
        return seatPrefix;
    }

    /** How many seats this class has on every flight. */
    public int seatsPerFlight() {
        return seatsPerFlight;
    }

    /** Seats on the whole plane: all classes added together (150). */
    public static int totalSeatsPerFlight() {
        int total = 0;
        for (ServiceClass serviceClass : values()) {
            total += serviceClass.seatsPerFlight;
        }
        return total;
    }

    /** The readable name, e.g. "Premium Economy" instead of PREMIUM_ECONOMY. */
    @Override
    public String toString() {
        return label;
    }
}