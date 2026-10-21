package src.menu.regular_user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.booking.BookingStorage;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.util.Input;
import src.util.Display;
import src.util.InformationTables;
import src.util.Validator;

public class viewFlightOperations {

    private final Scanner sc = Input.getScanner();

    public void viewAllFlights() {
        try {
            System.out.println();
            System.out.println("=== Available Flights ===");
            List<Flight> flights = FlightStorage.getInstance().loadFlights();
            if (flights.isEmpty()) {
                System.out.println("No flights are available yet.");
                return;
            }
            flightTable(flights);
        } catch (IOException e) {
            System.out.println("Could not load available flights: " + e.getMessage());
        }
    }

    public void searchByOriginAndDestination() {
        System.out.println();
        System.out.println("=== Search by Origin and Destination ===");
        try {
            List<Flight> flights = FlightStorage.getInstance().loadFlights();
            if (flights.isEmpty()) {
                System.out.println("No flights are available yet.");
                return;
            }
            String origin = askLocation("Origin");
            String destination = askLocation("Destination");
            List<Flight> matches = new ArrayList<>();
            for (Flight flight : flights) {
                if (Validator.normalizeLocation(flight.getOrigin()).equalsIgnoreCase(origin)
                        && Validator.normalizeLocation(flight.getDestination()).equalsIgnoreCase(destination)) {
                    matches.add(flight);
                }
            }
            System.out.println();
            System.out.println("=== Flights from " + origin + " to " + destination + " ===");
            flightTable(matches);
        } catch (IOException e) {
            System.out.println("Could not load available flights: " + e.getMessage());
        }
    }

    public void searchByServiceClassRemainingSeats() {
        System.out.println();
        System.out.println("=== Remaining Seats by Service Class ===");
        System.out.print("Enter flight number: ");
        String flightNumber = sc.nextLine().trim();

        try {
            Flight flight = FlightStorage.getInstance().findFlight(flightNumber);
            if (flight == null) {
                System.out.println("No matching flight found.");
                return;
            }

            BookingStorage bookingStorage = BookingStorage.getInstance();
            ServiceClass[] serviceClasses = ServiceClass.values();
            String[] headers = { "Service Class", "Remaining Seats" };
            String[][] rows = new String[serviceClasses.length][headers.length];
            for (int row = 0; row < serviceClasses.length; row++) {
                ServiceClass serviceClass = serviceClasses[row];
                rows[row] = new String[] {
                        serviceClass.toString(),
                        Integer.toString(bookingStorage.availableSeats(flight, serviceClass).size())
                };
            }

            System.out.println();
            System.out.println("=== Flight " + flight.getFlightNumber() + " | "
                    + flight.getOrigin() + " -> " + flight.getDestination() + " ===");
            Display.table(headers, rows);
        } catch (IOException e) {
            System.out.println("Could not load remaining seats: " + e.getMessage());
        }
    }

    private String askLocation(String label) {
        boolean isLocationValid = false;
        String location = "";
        while (!isLocationValid) {
            System.out.print(label + ": ");
            location = Validator.normalizeLocation(sc.nextLine());
            if (Validator.isValidLocation(location)) {
                isLocationValid = true;
            } else {
                System.out.println("Enter a valid " + label.toLowerCase() + " (e.g. Manila).");
            }
        }
        return location;
    }

    private void flightTable(List<Flight> flights) {
        if (flights.isEmpty()) {
            System.out.println("No matching flights found.");
            return;
        }
        InformationTables.flights(flights);
    }
}
