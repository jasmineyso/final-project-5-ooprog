package src.flight.filter;

import src.flight.Flight;
import src.util.Validator;

public class ByOrigin implements FlightFilter {
    private final String origin;

    public ByOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public boolean matches(Flight flight) {
        return Validator.locationMatches(flight.getOrigin(), origin);
    }
}