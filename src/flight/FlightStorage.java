package src.flight;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import src.util.Validator;

public final class FlightStorage {
    private static final FlightStorage INSTANCE = new FlightStorage();
    private final Path filePath;

    private FlightStorage() {
        // Support launching from either the project or its parent workspace.
        Path directory = Path.of("src", "files");
        Path workspaceDirectory = Path.of("final-project-5-ooprog", "src", "files");
        if (Files.isDirectory(workspaceDirectory)) {
            directory = workspaceDirectory;
        }
        filePath = directory.resolve("AVAILABLE_FLIGHTS.txt");
    }

    public static FlightStorage getInstance() {
        return INSTANCE;
    }

    public List<Flight> loadFlights() throws IOException {
        List<Flight> flights = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return flights;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            try {
                if (fields.length != 10 || !Validator.isValidDateTime(fields[4])
                        || !Validator.isValidDateTime(fields[5])) {
                    throw new IllegalArgumentException("Invalid flight record");
                }
                flights.add(new Flight(fields[0], fields[1], fields[2], fields[3],
                        Validator.parseDateTime(fields[4]), Validator.parseDateTime(fields[5]),
                        new BigDecimal(fields[6]), new BigDecimal(fields[7]),
                        new BigDecimal(fields[8]), Integer.parseInt(fields[9])));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid flight data on line " + lineNumber + " of AVAILABLE_FLIGHTS.txt.", e);
            }
        }
        return flights;
    }

    public List<String> loadFlightNumbers() throws IOException {
        List<String> numbers = new ArrayList<>();
        if (Files.exists(filePath)) {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    numbers.add(line.split("\\|", -1)[0]);
                }
            }
        }
        return numbers;
    }

    public void saveFlight(Flight flight) throws IOException {
        if (Validator.flightNumberExists(flight.flightNumber(), loadFlightNumbers())) {
            throw new IOException("Flight number already exists.");
        }
        // One record per line, in this order:
        // number|gate|origin|destination|departure|arrival|economy|business|first|seats
        String[] fields = {
                flight.flightNumber(), flight.gateNumber(), flight.origin(), flight.destination(),
                Validator.formatDateTime(flight.departureTime()),
                Validator.formatDateTime(flight.arrivalTime()),
                flight.economyFare().toPlainString(), flight.businessFare().toPlainString(),
                flight.firstClassFare().toPlainString(), Integer.toString(flight.numberOfSeats())
        };
        for (String field : fields) {
            if (field.contains("|") || field.contains("\n") || field.contains("\r")) {
                throw new IOException("Flight fields cannot contain | or line breaks.");
            }
        }
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(String.join("|", fields));
            writer.newLine();
        }
    }
}
