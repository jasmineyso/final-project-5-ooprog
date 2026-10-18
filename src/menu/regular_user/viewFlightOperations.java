package src.menu.regular_user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import src.flight.Flight;
import src.flight.FlightStorage;
import src.util.Input;
import src.util.Validator;
import java.util.Scanner;

public class viewFlightOperations {

    private final Scanner sc = Input.getScanner();

    public void viewAllFlights(){
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

    public void searchByOriginAndDestination(){
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
                if (Validator.normalizeLocation(flight.origin()).equalsIgnoreCase(origin)
                        && Validator.normalizeLocation(flight.destination()).equalsIgnoreCase(destination)) {
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

    private String askLocation(String label) {
        while (true) {
            System.out.print(label + ": ");
            String location = Validator.normalizeLocation(sc.nextLine());
            if (Validator.isValidLocation(location)) {
                return location;
            }
            System.out.println("Enter a valid " + label.toLowerCase() + " (e.g. Manila).");
        }
    }

    /** Each column fits its heading and the longest value in the displayed flights. */
    private void flightTable(List<Flight> flights) {
        if (flights.isEmpty()) {
            System.out.println("No matching flights found.");
            return;
        }
        String[] headers = { "Flight Number", "Gate", "Origin", "Destination", "Departure", "Arrival",
                "Economy Fare", "Business Fare", "First Class Fare", "Seats" };
        String[][] rows = new String[flights.size()][headers.length];
        int[] widths = new int[headers.length];
        for (int column = 0; column < headers.length; column++) {
            widths[column] = headers[column].length();
        }
        for (int row = 0; row < flights.size(); row++) {
            Flight flight = flights.get(row);
            rows[row] = new String[] {
                    flight.flightNumber(), flight.gateNumber(), flight.origin(), flight.destination(),
                    Validator.formatDateTime(flight.departureTime()),
                    Validator.formatDateTime(flight.arrivalTime()),
                    flight.economyFare().toPlainString(), flight.businessFare().toPlainString(),
                    flight.firstClassFare().toPlainString(), Integer.toString(flight.numberOfSeats())
            };
            for (int column = 0; column < headers.length; column++) {
                widths[column] = Math.max(widths[column], rows[row][column].length());
            }
        }
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            border.append("-".repeat(width + 2)).append('+');
        }
        System.out.println(border.toString());
        tableRow(headers, widths);
        System.out.println(border.toString());
        for (String[] row : rows) {
            tableRow(row, widths);
        }
        System.out.println(border.toString());
    }

    private void tableRow(String[] values, int[] widths) {
        StringBuilder row = new StringBuilder("|");
        for (int column = 0; column < values.length; column++) {
            row.append(' ').append(values[column])
                    .append(" ".repeat(widths[column] - values[column].length()))
                    .append(" |");
        }
        System.out.println(row.toString());
    }
}
