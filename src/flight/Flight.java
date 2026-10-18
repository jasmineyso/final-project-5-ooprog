package src.flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The details entered by an administrator for an available flight. */
public record Flight(
        String flightNumber,
        String gateNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal economyFare,
        BigDecimal businessFare,
        BigDecimal firstClassFare,
        int numberOfSeats) {
}
