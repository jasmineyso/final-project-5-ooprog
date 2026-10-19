package src.flight.filter;

import src.flight.Flight;

/**
 * Strategy interface: each search rule is its own class.
 * A filter answers one question: "Does this flight match?"
 */
public interface FlightFilter {
    boolean matches(Flight flight);
}