package src.flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import src.util.Validator;

/**
 * One flight. It is a record, so its values never change after it is created.
 * To "change" a flight (e.g. its status), make a new Flight and save that instead.
 *
 * fares and seats hold one value per service class, for example:
 *   fares: Economy = 1500.00, Premium Economy = 2500.00, Business = 5000.00, First Class = 9000.00
 *   seats: Economy = 100,     Premium Economy = 25,      Business = 15,      First Class = 10
 * The seat numbers come from ServiceClass (150 seats per flight).
 */
public record Flight(String flightNumber, String gateNumber, String origin, String destination,
        LocalDateTime departureTime, LocalDateTime arrivalTime, FlightStatus status,
        Map<ServiceClass, BigDecimal> fares, Map<ServiceClass, Integer> seats) {

    /** Runs on every new Flight: copies the maps so they can't be changed from outside. */
    public Flight {
        fares = copyOf(fares, BigDecimal.ZERO);
        seats = copyOf(seats, 0);
    }

    /** Copies a map, filling in any missing service class with a default value. */
    private static <V> Map<ServiceClass, V> copyOf(Map<ServiceClass, V> source, V missingValue) {
        Map<ServiceClass, V> copy = new EnumMap<>(ServiceClass.class);
        for (ServiceClass serviceClass : ServiceClass.values()) {
            copy.put(serviceClass, source.getOrDefault(serviceClass, missingValue));
        }
        return Collections.unmodifiableMap(copy);
    }

    /** The fare for one service class. */
    public BigDecimal fareFor(ServiceClass serviceClass) {
        return fares.get(serviceClass);
    }

    /** How many seats one service class has. */
    public int seatCount(ServiceClass serviceClass) {
        return seats.get(serviceClass);
    }

    /** All seats on the plane, every class added together. */
    public int totalSeats() {
        int total = 0;
        for (int count : seats.values()) {
            total += count;
        }
        return total;
    }

    /** A copy of this flight with a different status. */
    public Flight withStatus(FlightStatus newStatus) {
        return new Flight(flightNumber, gateNumber, origin, destination,
                departureTime, arrivalTime, newStatus, fares, seats);
    }

    /** A copy of this flight with new departure and arrival times. */
    public Flight withTimes(LocalDateTime newDeparture, LocalDateTime newArrival) {
        return new Flight(flightNumber, gateNumber, origin, destination,
                newDeparture, newArrival, status, fares, seats);
    }

    /** A copy of this flight with new fares. */
    public Flight withFares(Map<ServiceClass, BigDecimal> newFares) {
        return new Flight(flightNumber, gateNumber, origin, destination,
                departureTime, arrivalTime, status, newFares, seats);
}

    // Kept so code written for the old Flight still compiles.
    public BigDecimal economyFare() {
        return fareFor(ServiceClass.ECONOMY);
    }

    public BigDecimal businessFare() {
        return fareFor(ServiceClass.BUSINESS);
    }

    public BigDecimal firstClassFare() {
        return fareFor(ServiceClass.FIRST_CLASS);
    }

    public int numberOfSeats() {
        return totalSeats();
    }

    @Override
    public String toString() {
        return flightNumber + " | " + origin + " -> " + destination
                + " | Gate " + gateNumber
                + " | Departs " + Validator.formatDateTime(departureTime)
                + " | Arrives " + Validator.formatDateTime(arrivalTime)
                + " | " + status;
    }
}