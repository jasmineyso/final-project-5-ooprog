package src.menu.regular_user;

public class PassengerSeat {
    private final String seatNumber;
    private final String passengerName;

    public PassengerSeat(String seatNumber, String passengerName) {
        this(seatNumber, passengerName, false);
    }

    private PassengerSeat(String seatNumber, String passengerName, boolean legacy) {
        if (seatNumber == null || !seatNumber.trim().toUpperCase(java.util.Locale.ROOT).matches("[EPBF][1-9][0-9]*")) {
            throw new IllegalArgumentException("Invalid seat number.");
        }
        if (passengerName == null || passengerName.isBlank()
                || passengerName.matches("(?s).*[|:;\\r\\n\\t].*")
                || (!legacy && !isValidName(passengerName))) {
            throw new IllegalArgumentException("Enter a passenger name using letters, spaces, hyphens, apostrophes or periods (2-100 characters).");
        }
        this.seatNumber = seatNumber.trim().toUpperCase(java.util.Locale.ROOT);
        this.passengerName = passengerName.trim();
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 100
                && name.trim().matches("\\p{L}[\\p{L}\\p{M} .'’-]*")
                && name.codePoints().filter(Character::isLetter).count() >= 2;
    }

    // Old records used account usernames, which can include digits or underscores.
    public static PassengerSeat fromLegacy(String seat, String name) {
        return new PassengerSeat(seat, name, true);
    }

    public PassengerSeat withSeatNumber(String seat) {
        return new PassengerSeat(seat, passengerName, true);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PassengerSeat passenger)) return false;
        return seatNumber.equals(passenger.seatNumber) && passengerName.equals(passenger.passengerName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(seatNumber, passengerName);
    }
}
