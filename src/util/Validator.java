package src.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;

/**
 * All input validation rules in one place.
 *
 * These methods only CHECK input. They never read from the Scanner and never print,
 * so the menus decide which error message (from ErrorMessages) to show.
 */
public final class Validator {

    private Validator() {
    }

    // ===================== WHOLE NUMBERS =====================

    // Either "0" by itself, or a number that does not start with 0.
    // No signs, spaces, letters, decimals, or 'e' allowed anywhere.
    private static final String WHOLE_NUMBER = "0|[1-9][0-9]*";

    /**
     * Accepts: "0", "7", "10", "250"
     * Rejects: "007", "010", "10e", "10 e", "1e1", "-5", "+5", "3.5", "", "abc",
     *          and numbers too large to fit in an int.
     * Spaces before or after the number are ignored (" 10 " is accepted).
     */
    public static boolean isValidInt(String input) {
        if (input == null) {
            return false;
        }
        String value = input.trim();
        if (!value.matches(WHOLE_NUMBER)) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false; // too big for int, e.g. "99999999999"
        }
    }

    /** Same rules as isValidInt, plus the number must be between min and max (inclusive). */
    public static boolean isIntInRange(String input, int min, int max) {
        if (!isValidInt(input)) {
            return false;
        }
        int value = Integer.parseInt(input.trim());
        return value >= min && value <= max;
    }

    // ===================== ORIGIN / DESTINATION =====================

    // Words made of letters (including ñ, é, etc.), separated by a space, hyphen,
    // period, or apostrophe. Examples: "Manila", "Cagayan de Oro", "Las Piñas",
    // "Ho Chi Minh", "St. John's", "MNL"
    private static final String LOCATION = "\\p{L}+(?:[ .'-]{1,2}\\p{L}+)*\\.?";
    private static final int LOCATION_MIN_LENGTH = 2;
    private static final int LOCATION_MAX_LENGTH = 50;

    /**
     * Accepts: "Manila", "Cebu", "Cagayan de Oro", "Las Piñas", "St. John's"
     * Rejects: "", "M", "Manila1", "123", "Cebu!!", "  "
     */
    public static boolean isValidLocation(String input) {
        if (input == null) {
            return false;
        }
        String value = normalizeLocation(input);
        return value.length() >= LOCATION_MIN_LENGTH
                && value.length() <= LOCATION_MAX_LENGTH
                && value.matches(LOCATION);
    }

    /** True if origin and destination are different places (ignores case and extra spaces). */
    public static boolean isDifferentLocation(String origin, String destination) {
        return !normalizeLocation(origin).equalsIgnoreCase(normalizeLocation(destination));
    }

    /** Trims and collapses repeated spaces: "  Cagayan   de Oro " -> "Cagayan de Oro". */
    public static String normalizeLocation(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    // ===================== DEPARTURE / ARRIVAL TIME =====================

    /** Show this in prompts so users know the expected format. */
    public static final String DATE_TIME_FORMAT_HINT = "YYYY-MM-DD HH:MM, 24-hour (e.g. 2026-11-05 14:30)";

    // STRICT means impossible dates like 2026-02-30 or 25:00 are rejected
    // instead of being silently "corrected". ("uuuu" is required for STRICT mode.)
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    // Longest allowed flight. Catches typos like arriving a year later.
    private static final long MAX_FLIGHT_HOURS = 24;

    /**
     * Converts text like "2026-11-05 14:30" into a LocalDateTime.
     * Returns null if the text is not a real date and time in that exact format.
     * Rejects: "2026-02-30 10:00", "2026-11-05 25:00", "2026-11-5 9:30", "11/05/2026 2:30 PM"
     */
    public static LocalDateTime parseDateTime(String input) {
        if (input == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(input.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isValidDateTime(String input) {
        return parseDateTime(input) != null;
    }

    /** Departure must not be in the past. */
    public static boolean isFutureDateTime(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /** Arrival must be strictly after departure. */
    public static boolean isArrivalAfterDeparture(LocalDateTime departure, LocalDateTime arrival) {
        return departure != null && arrival != null && arrival.isAfter(departure);
    }

    /** Flight must not last longer than MAX_FLIGHT_HOURS. */
    public static boolean isReasonableFlightDuration(LocalDateTime departure, LocalDateTime arrival) {
        if (!isArrivalAfterDeparture(departure, arrival)) {
            return false;
        }
        return Duration.between(departure, arrival).compareTo(Duration.ofHours(MAX_FLIGHT_HOURS)) <= 0;
    }

    /** Turns a LocalDateTime back into the same text format, for display or saving to files. */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMAT);
    }

    // ===================== DATE ONLY =====================

    /** Show this in prompts so users know the expected format. */
    public static final String DATE_FORMAT_HINT = "YYYY-MM-DD (e.g. 2026-11-05)";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Converts text like "2026-11-05" into a LocalDate.
     * Returns null if it is not a real date in that exact format.
     * Rejects: "2026-02-30", "2026-11-5", "11/05/2026"
     */
    public static LocalDate parseDate(String input) {
        if (input == null) {
            return null;
        }
        try {
            return LocalDate.parse(input.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isValidDate(String input) {
        return parseDate(input) != null;
    }

    // ===================== TIME ONLY =====================

    // 24-hour clock. "H" allows one or two digits, so both 9:05 and 09:05 work.
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Converts text like "14:30" or "9:05" into a LocalTime.
     * Returns null if it is not a real 24-hour time.
     * Rejects: "24:00", "14:60", "2:30 PM", "1430"
     */
    public static LocalTime parseTime(String input) {
        if (input == null) {
            return null;
        }
        try {
            return LocalTime.parse(input.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isValidTime(String input) {
        return parseTime(input) != null;
    }

    // ===================== FLIGHT NUMBER =====================

    // 2-character airline code + 1 to 4 digits, with no leading zero in the number part.
    // The code can be two letters (PR) or a letter and a digit (5J, Z2).
    // Examples: PR102, 5J560, Z2123, AB1
    private static final String FLIGHT_NUMBER = "(?:[A-Z]{2}|[A-Z][0-9]|[0-9][A-Z])[1-9][0-9]{0,3}";

    /** Uppercases and removes spaces: " pr 102 " -> "PR102". Save flight numbers in this form. */
    public static String normalizeFlightNumber(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replace(" ", "").toUpperCase();
    }

    /**
     * Accepts: "PR102", "pr102", "PR 102", "5J560"
     * Rejects: "PR0102", "PR", "102", "PR12345", "P-102", "55102"
     */
    public static boolean isValidFlightNumber(String input) {
        return normalizeFlightNumber(input).matches(FLIGHT_NUMBER);
    }

    /**
     * True if the flight number is already in the list (ignores case and spaces).
     *
     * Add Flight:              it must NOT exist yet  -> if (flightNumberExists(...)) show error
     * Update / Remove / Book:  it MUST exist          -> if (!flightNumberExists(...)) show error
     *
     * Pass the flight numbers from your flight storage, e.g. a List<String>.
     */
    public static boolean flightNumberExists(String flightNumber, Collection<String> existingFlightNumbers) {
        if (existingFlightNumbers == null) {
            return false;
        }
        String target = normalizeFlightNumber(flightNumber);
        for (String existing : existingFlightNumbers) {
            if (normalizeFlightNumber(existing).equals(target)) {
                return true;
            }
        }
        return false;
    }
}