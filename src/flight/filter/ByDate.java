package src.flight.filter;

import java.time.LocalDate;

import src.flight.Flight;

public class ByDate implements FlightFilter {
    private final LocalDate date;

    public ByDate(LocalDate date) {
        this.date = date;   
    }

    @Override
    public boolean matches(Flight flight) {
        return flight.getDepartureTime().toLocalDate().equals(date);
    }
}