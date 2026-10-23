package src.flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import src.util.Validator;

/**
 * stores one flight, including a fare and seat capacity for each service class
 * fields cannot be reassigned, the with... methods return updated copies
 */
public class Flight {
    private final String flightNumber;
    private final String gateNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final FlightStatus status;
    private final BigDecimal economyFare;
    private final BigDecimal premiumEconomyFare;
    private final BigDecimal businessFare;
    private final BigDecimal firstClassFare;
    private final int economySeats;
    private final int premiumEconomySeats;
    private final int businessSeats;
    private final int firstClassSeats;

    public Flight(String flightNumber, String gateNumber, String origin, String destination,
            LocalDateTime departureTime, LocalDateTime arrivalTime, FlightStatus status,
            BigDecimal economyFare, BigDecimal premiumEconomyFare, BigDecimal businessFare, BigDecimal firstClassFare,
            int economySeats, int premiumEconomySeats, int businessSeats, int firstClassSeats) {
        this.flightNumber = flightNumber;
        this.gateNumber = gateNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = status;
        this.economyFare = economyFare;
        this.premiumEconomyFare = premiumEconomyFare;
        this.businessFare = businessFare;
        this.firstClassFare = firstClassFare;
        this.economySeats = economySeats;
        this.premiumEconomySeats = premiumEconomySeats;
        this.businessSeats = businessSeats;
        this.firstClassSeats = firstClassSeats;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getGateNumber() {
        return gateNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public FlightStatus getStatus() {
        return status;
    }

    // scheduled and delayed flights accept bookings only before departure
    public boolean isBookable() {
        return (status == FlightStatus.SCHEDULED || status == FlightStatus.DELAYED)
                && departureTime.isAfter(LocalDateTime.now());
    }

    public BigDecimal getEconomyFare() {
        return economyFare;
    }

    public BigDecimal getPremiumEconomyFare() {
        return premiumEconomyFare;
    }

    public BigDecimal getBusinessFare() {
        return businessFare;
    }

    public BigDecimal getFirstClassFare() {
        return firstClassFare;
    }

    /** Returns the fare per passenger for the selected class. */
    public BigDecimal getFareFor(ServiceClass serviceClass) {
        return switch (serviceClass) {
            case ECONOMY -> economyFare;
            case PREMIUM_ECONOMY -> premiumEconomyFare;
            case BUSINESS -> businessFare;
            case FIRST_CLASS -> firstClassFare;
        };
    }

    /** Returns the seat capacity of the selected class. */
    public int getSeatCount(ServiceClass serviceClass) {
        return switch (serviceClass) {
            case ECONOMY -> economySeats;
            case PREMIUM_ECONOMY -> premiumEconomySeats;
            case BUSINESS -> businessSeats;
            case FIRST_CLASS -> firstClassSeats;
        };
    }

    public int getTotalSeats() {
        return economySeats + premiumEconomySeats + businessSeats + firstClassSeats;
    }

    public int getNumberOfSeats() {
        return getTotalSeats();
    }

    public Flight withStatus(FlightStatus newStatus) {
        return new Flight(flightNumber, gateNumber, origin, destination,
                departureTime, arrivalTime, newStatus,
                economyFare, premiumEconomyFare, businessFare, firstClassFare,
                economySeats, premiumEconomySeats, businessSeats, firstClassSeats);
    }

    public Flight withTimes(LocalDateTime newDeparture, LocalDateTime newArrival) {
        return new Flight(flightNumber, gateNumber, origin, destination,
                newDeparture, newArrival, status,
                economyFare, premiumEconomyFare, businessFare, firstClassFare,
                economySeats, premiumEconomySeats, businessSeats, firstClassSeats);
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
