package src.flight.filter;

import java.time.YearMonth;

import src.flight.Flight;

public class ByMonthYear implements FlightFilter {
    private final YearMonth monthYear;

    public ByMonthYear(YearMonth monthYear) {
        this.monthYear = monthYear;
    }

    @Override
    public boolean matches(Flight flight) {
        return YearMonth.from(flight.getDepartureTime()).equals(monthYear);
    }
}