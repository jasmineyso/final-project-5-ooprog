package src.flight.filter;

import src.flight.Flight;
import src.util.Validator;

public class ByDestination implements FlightFilter {
    private final String destination;

    public ByDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean matches(Flight flight) {
        return Validator.locationMatches(flight.getDestination(), destination);
    }
}