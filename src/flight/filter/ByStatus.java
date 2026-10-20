package src.flight.filter;

import src.flight.Flight;
import src.flight.FlightStatus;

public class ByStatus implements FlightFilter {
    private final FlightStatus status;

    public ByStatus(FlightStatus status) {
        this.status = status;
    }

    @Override
    public boolean matches(Flight flight) {
        return flight.status() == status;
    }
}