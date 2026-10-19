package src.flight.filter;

import src.flight.Flight;
import src.util.Validator;

public class ByOrigin implements FlightFilter {
    private final String origin;

    public ByOrigin(String origin) {
        this.origin = Validator.normalizeLocation(origin);
    }

    @Override
    public boolean matches(Flight flight) {
        return flight.origin().equalsIgnoreCase(origin);
    }
}