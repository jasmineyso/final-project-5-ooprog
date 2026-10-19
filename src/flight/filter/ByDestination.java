package src.flight.filter;

import src.flight.Flight;
import src.util.Validator;

public class ByDestination implements FlightFilter {
    private final String destination;

    public ByDestination(String destination) {
        this.destination = Validator.normalizeLocation(destination);
    }

    @Override
    public boolean matches(Flight flight) {
        return flight.destination().equalsIgnoreCase(destination);
    }
}