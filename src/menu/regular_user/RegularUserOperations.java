package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import src.account.Access;
import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.booking.exception.NotEnoughSeatsException;
import src.core.ErrorMessages;
import src.flight.Flight;
import src.flight.FlightStatus;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.util.Input;
import src.util.Display;
import src.util.InformationTables;
import src.util.Validator;

public class RegularUserOperations {

    private static RegularUserOperations instance;
    private final Scanner sc = Input.getScanner();

    private final BookingStorage bookingStorage = BookingStorage.getInstance();

    private RegularUserOperations() {
    }

    public synchronized static RegularUserOperations getInstance() {
        if (instance == null) {
            instance = new RegularUserOperations();
        }
        return instance;
    }

    public void viewFlights() {
        boolean isValid = false;

        try {
            if (FlightStorage.getInstance().loadFlights().isEmpty()) {
                System.out.println("No flights are available yet.");
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not load available flights: " + e.getMessage());
            return;
        }

        viewFlightOperations viewFlights = new viewFlightOperations();

        while (!isValid) {
            System.out.println("1. View All Flights");
            System.out.println("2. Search by Origin and Destination");
            System.out.println("3. Search Service Class Remaining Seat");

            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1" -> {
                    viewFlights.viewAllFlights();
                    return;
                }
                case "2" -> {
                    viewFlights.searchByOriginAndDestination();
                    return;
                }
                case "3" -> {
                    viewFlights.searchByServiceClassRemainingSeats();
                    return;
                }
                default -> System.out.println(ErrorMessages.INVALID_CHOICE);
            }
        }
    }

    public void viewBookedFlights(RegularUser user) {
        try {
            List<Booking> bookings = BookingStorage.getInstance().bookingsOf(user.getUsername());
            if (bookings.isEmpty()) {
                System.out.println("You have no bookings yet.");
                return;
            }

            System.out.println("BOOKINGS");
            Map<String, Flight> flights = new LinkedHashMap<>();
            try {
                for (Flight flight : FlightStorage.getInstance().loadFlights()) {
                    flights.put(Validator.normalizeFlightNumber(flight.getFlightNumber()), flight);
                }
            } catch (IOException e) {
                System.out.println("Flight details could not be loaded: " + e.getMessage());
            }
            Map<String, List<Booking>> groups = new LinkedHashMap<>();
            groups.put("Upcoming Trips", new ArrayList<>());
            groups.put("Past / Departed Trips", new ArrayList<>());
            groups.put("Cancelled Trips", new ArrayList<>());
            groups.put("Flight Details Unavailable", new ArrayList<>());
            LocalDateTime now = LocalDateTime.now();
            for (Booking booking : bookings) {
                Flight flight = flights.get(Validator.normalizeFlightNumber(booking.flightNumber()));
                String group;
                if (booking.status() == BookingStatus.CANCELLED
                        || (flight != null && flight.getStatus() == FlightStatus.CANCELLED)) {
                    group = "Cancelled Trips";
                } else if (flight == null) {
                    group = "Flight Details Unavailable";
                } else if (flight.getDepartureTime().isAfter(now)) {
                    group = "Upcoming Trips";
                } else {
                    group = "Past / Departed Trips";
                }
                groups.get(group).add(booking);
            }
            for (Map.Entry<String, List<Booking>> group : groups.entrySet()) {
                if (group.getValue().isEmpty()) {
                    continue;
                }
                System.out.println("\n=== " + group.getKey() + " ===");
                for (Booking booking : group.getValue()) {
                    displayBooking(booking, flights.get(Validator.normalizeFlightNumber(booking.flightNumber())));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load your bookings: " + e.getMessage());
        }
    }

    private void displayBooking(Booking booking, Flight flight) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Booking ID", booking.bookingId() });
        rows.add(new String[] { "Account", booking.username() });
        rows.add(new String[] { "Flight Number", booking.flightNumber() });
        if (flight == null) {
            System.out.println("Flight details unavailable; the flight may have been removed.");
        } else {
            rows.add(new String[] { "Origin", flight.getOrigin() });
            rows.add(new String[] { "Destination", flight.getDestination() });
            rows.add(new String[] { "Departure", Validator.formatDateTime(flight.getDepartureTime()) });
            rows.add(new String[] { "Arrival", Validator.formatDateTime(flight.getArrivalTime()) });
            rows.add(new String[] { "Gate", flight.getGateNumber() });
        }
        rows.add(new String[] { "Flight Status", flight == null ? "Unavailable" : flight.getStatus().toString() });
        rows.add(new String[] { "Service Class", booking.serviceClass().toString() });
        rows.add(new String[] { "Seats", String.join(", ", booking.seats()) });
        rows.add(new String[] { "Fee", InformationTables.money(booking.totalPrice()) });
        rows.add(new String[] { "Booking Status", booking.status().toString() });
        Display.table(new String[] { "Detail", "Information" }, rows.toArray(new String[0][]));
        InformationTables.passengers(booking);
    }

    public void bookFlight(RegularUser user) {

        System.out.println("=== Book Flight ===");
        boolean isValidFlight = false;

        Flight selectedFlight = null;

        try {
            if (FlightStorage.getInstance().loadFlights().isEmpty()) {
                System.out.println(ErrorMessages.FLIGHT_EMPTY);
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not load flights: " + e.getMessage());
            return;
        }

        while (!isValidFlight) {
            System.out.print("Enter Flight Number: ");
            String flightNumber = sc.nextLine().trim();

            try {
                selectedFlight = FlightStorage.getInstance().findFlight(flightNumber);
                if (selectedFlight != null) {
                    isValidFlight = true;
                } else {
                    System.out.println(ErrorMessages.FLIGHT_NOT_FOUND);
                }
            } catch (IOException e) {
                System.out.println("Could not load flights: " + e.getMessage());
                return;
            }
        }

        boolean isServiceClassValid = false;
        ServiceClass selectedServiceClass = null;

        while (!isServiceClassValid) {
            System.out.println("=== Service Classes for " + selectedFlight.getFlightNumber() + " ===");
            boolean hasAvailableSeats = false;
            try {
                List<String[]> serviceRows = new ArrayList<>();
                for (ServiceClass serviceClass : ServiceClass.values()) {
                    int remaining = bookingStorage.availableSeats(selectedFlight, serviceClass).size();
                    hasAvailableSeats |= remaining > 0;
                    serviceRows.add(new String[] { Integer.toString(serviceClass.ordinal() + 1),
                            serviceClass.toString(), InformationTables.money(selectedFlight.getFareFor(serviceClass)),
                            Integer.toString(remaining), Integer.toString(selectedFlight.getSeatCount(serviceClass)),
                            remaining == 0 ? "Sold Out" : "Available" });
                }
                Display.table(
                        new String[] { "Choice", "Service Class", "Fare", "Remaining Seats", "Capacity",
                                "Availability" },
                        serviceRows.toArray(new String[0][]));
            } catch (IOException e) {
                System.out.println("Could not load seat availability: " + e.getMessage());
                return;
            }
            if (!hasAvailableSeats) {
                System.out.println("This flight is sold out. Please choose another flight.");
                return;
            }
            System.out.print("Enter your service class choice (1-4): ");

            selectedServiceClass = switch (sc.nextLine().trim()) {
                case "1" -> ServiceClass.ECONOMY;
                case "2" -> ServiceClass.PREMIUM_ECONOMY;
                case "3" -> ServiceClass.BUSINESS;
                case "4" -> ServiceClass.FIRST_CLASS;
                default -> null;
            };

            if (selectedServiceClass == null) {
                System.out.println(ErrorMessages.INVALID_CHOICE);
            } else {
                try {
                    boolean isSoldOut = bookingStorage.availableSeats(selectedFlight, selectedServiceClass).isEmpty();
                    if (isSoldOut) {
                        System.out.println("This service class is sold out. Please choose another service class.");
                    } else {
                        isServiceClassValid = true;
                    }
                } catch (IOException e) {
                    System.out.println("Could not load seat availability: " + e.getMessage());
                    return;
                }
            }
        }

        Display.table(new String[] { "Flight Number", "Service Class", "Fare" }, new String[][] {
                { selectedFlight.getFlightNumber(), selectedServiceClass.toString(),
                        InformationTables.money(selectedFlight.getFareFor(selectedServiceClass)) }
        });

        boolean isNumberOfSeatsValid = false;
        int numberOfSeats = 0;

        while (!isNumberOfSeatsValid) {
            int availableSeats;
            try {
                availableSeats = bookingStorage.availableSeats(selectedFlight, selectedServiceClass).size();
            } catch (IOException e) {
                System.out.println("Could not load seat availability: " + e.getMessage());
                return;
            }

            if (availableSeats == 0) {
                System.out.println(
                        "This service class is sold out. Please start again and choose another service class.");
                return;
            }
            System.out.printf("Enter Number of Seats(1-%d): ", Math.min(9, availableSeats));
            String numOfSeats = sc.nextLine().trim();

            if (!numOfSeats.matches("[1-9]")) {
                System.out.println(ErrorMessages.INVALID_NUMBER_OF_SEATS);
                continue;
            }

            numberOfSeats = Integer.parseInt(numOfSeats);
            if (numberOfSeats > availableSeats) {
                System.out.printf("Only %d seat(s) remain in %s. Please enter a smaller number.%n",
                        availableSeats, selectedServiceClass);
                continue;
            }

            isNumberOfSeatsValid = true;

        }

        try {
            // Auto assigns the seat
            FlightSeatAssign seatAssignment = new FlightSeatAssign(selectedFlight, selectedServiceClass, numberOfSeats);
            List<String> seats = seatAssignment.assignSeats();
            List<PassengerSeat> passengers = collectPassengerDetails(selectedFlight, selectedServiceClass, seats, user);
            if (passengers == null) {
                Display.message("Booking cancelled. No seats were reserved.");
                return;
            }
            Booking booking = seatAssignment.prepareBookingFor(user, passengers);
            InformationTables.passengers(booking);
            System.out.println("Assigned seats: " + String.join(", ", booking.seats()));
            System.out.printf("Total price: PHP %,.2f%n", booking.totalPrice());

            // Save only after the payment choice and, for Pay now, successful payment.
            int paymentChoice = Input.askChoice("Payment", "Pay now", "Pay later");
            if (paymentChoice == 1) {
                BigDecimal amountPaid = collectPayment(booking);
                if (amountPaid == null) {
                    System.out.println("Booking was not saved. No seats were reserved.");
                    return;
                }
                booking = booking.withStatus(BookingStatus.PAID);
                bookingStorage.saveBooking(booking);
                System.out.println("Booking saved: " + booking.bookingId());
                displayPaymentReceipt(booking, amountPaid);
            } else {
                bookingStorage.saveBooking(booking);
                System.out.println("Booking saved: " + booking.bookingId() + " | Status: " + booking.status());
                System.out.println("You can pay later using Pay for Booked Flight.");
            }
        } catch (NotEnoughSeatsException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(ErrorMessages.BOOKING_SAVE_FAILED + e.getMessage());
        }
    }

    private List<PassengerSeat> collectPassengerDetails(Flight flight, ServiceClass serviceClass,
            List<String> seats, RegularUser user) {
        Display.title("Passenger Details");
        Display.message("Flight: " + flight.getFlightNumber() + " | Service Class: " + serviceClass
                + " | Number of Seats: " + seats.size());
        Display.message("Enter 0 to cancel the booking.");
        List<PassengerSeat> passengers = new ArrayList<>();
        for (String seat : seats) {
            String defaultName = passengers.isEmpty() && PassengerSeat.isValidName(user.getUsername())
                    ? user.getUsername()
                    : null;
            boolean isPassengerNameValid = false;
            while (!isPassengerNameValid) {
                Display.prompt("Seat " + seat + " - Passenger Name"
                        + (defaultName == null ? "" : " [Enter for " + defaultName + "]"));
                String input = sc.nextLine();
                String name = input.trim();
                if (name.equals("0"))
                    return null;
                if (input.isEmpty() && defaultName != null)
                    name = defaultName;
                try {
                    passengers.add(new PassengerSeat(seat, name));
                    isPassengerNameValid = true;
                } catch (IllegalArgumentException e) {
                    Display.error(e.getMessage());
                }
            }
        }
        return passengers;
    }

    /** Lets a passenger change the service class of their own pending booking. */
    public void updateBookedFlight(RegularUser user) {
        Display.title("Update Booked Flight");
        try {
            Booking booking = selectOwnBooking(user);
            if (booking == null) {
                return;
            }
            if (booking.status() != BookingStatus.PENDING) {
                Display.error("Only Pending bookings can be changed. Paid bookings must be cancelled instead.");
                return;
            }
            Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
            if (flight == null || flight.getStatus() == FlightStatus.CANCELLED) {
                Display.error("This flight is no longer available.");
                return;
            }

            ServiceClass[] classes = ServiceClass.values();
            String[] options = new String[classes.length + 1];
            for (int i = 0; i < classes.length; i++) {
                int remaining = bookingStorage.availableSeats(flight, classes[i]).size();
                options[i] = classes[i] + " - " + InformationTables.money(flight.getFareFor(classes[i]))
                        + " (" + remaining + " seats left)";
            }
            options[classes.length] = "Back";
            int choice = Input.askChoice("Choose the New Service Class", options);
            if (choice == options.length) {
                return;
            }
            ServiceClass newClass = classes[choice - 1];
            if (newClass == booking.serviceClass()) {
                Display.error("The booking is already in " + newClass + ".");
                return;
            }
            if (bookingStorage.availableSeats(flight, newClass).size() < booking.seats().size()) {
                Display.error("There are not enough seats in " + newClass + " for this booking.");
                return;
            }
            BigDecimal newTotal = flight.getFareFor(newClass)
                    .multiply(BigDecimal.valueOf(booking.seats().size()));
            Display.message("New total: " + InformationTables.money(newTotal));
            if (!Input.askYesNo("Change service class to " + newClass + "?")) {
                Display.message("Booking not changed.");
                return;
            }
            Booking updated = bookingStorage.changeServiceClass(booking, flight, newClass);
            Display.success("Booking " + updated.bookingId() + " has been updated.");
            InformationTables.bookings(List.of(updated));
        } catch (IOException | NotEnoughSeatsException e) {
            Display.error("Could not update booking: " + e.getMessage());
        }
    }

    public void cancelBookedFlight(RegularUser user) {
        Display.title("Cancel Booked Flight");
        try {
            Booking booking = selectOwnBooking(user);
            if (booking == null) {
                return;
            }
            if (!Input.askYesNo("Cancel booking " + booking.bookingId() + "?")) {
                Display.message("Booking not changed.");
                return;
            }
            if (!bookingStorage.cancelBooking(booking.bookingId())) {
                Display.error("Booking was not found or is no longer active.");
                return;
            }
            Display.success("Booking " + booking.bookingId() + " has been cancelled. Its seats are free again.");
            if (booking.status() == BookingStatus.PAID) {
                Display.message("Contact an administrator about a refund of "
                        + InformationTables.money(booking.totalPrice()) + ".");
            }
        } catch (IOException e) {
            Display.error("Could not cancel booking: " + e.getMessage());
        }
    }

    /**
     * Shows only the logged-in passenger's bookings and checks ownership before
     * use.
     */
    private Booking selectOwnBooking(RegularUser user) throws IOException {
        List<Booking> bookings = bookingStorage.bookingsOf(user.getUsername());
        if (bookings.isEmpty()) {
            Display.message("You have no bookings yet.");
            return null;
        }
        InformationTables.bookings(bookings);
        while (true) {
            Display.prompt("Enter Booking ID (0 to go back)");
            String bookingId = sc.nextLine().trim();
            if (bookingId.equals("0")) {
                return null;
            }
            Booking booking = bookingStorage.findBooking(bookingId);
            if (booking == null || !booking.username().equalsIgnoreCase(user.getUsername())) {
                Display.error("Booking not found in your account.");
                continue;
            }
            Access.requireOwnerOrAdmin(user, booking.username());
            if (!booking.isActive()) {
                Display.error("Cancelled or completed bookings cannot be modified.");
                continue;
            }
            return booking;
        }
    }

    public void payBooking(RegularUser user) {
        System.out.println("=== Booking Payment ===");
        BookingStorage storage = BookingStorage.getInstance();
        try {
            List<Booking> bookings = storage.bookingsOf(user.getUsername());
            List<Booking> pendingBookings = new ArrayList<>();
            for (Booking booking : bookings) {
                if (booking.status() == BookingStatus.PENDING) {
                    pendingBookings.add(booking);
                }
            }
            if (pendingBookings.isEmpty()) {
                System.out.println("You have no unpaid bookings.");
                return;
            }

            InformationTables.bookings(pendingBookings);

            Booking selectedBooking = null;
            boolean isBookingValid = false;
            while (!isBookingValid) {
                System.out.print("Enter Booking ID to pay (0 to cancel): ");
                String bookingId = sc.nextLine().trim();
                if (bookingId.equals("0")) {
                    return;
                }

                selectedBooking = storage.findBooking(bookingId);

                if (selectedBooking == null || !selectedBooking.username().equalsIgnoreCase(user.getUsername())) {
                    System.out.println(ErrorMessages.BOOKING_NOT_FOUND);
                } else if (selectedBooking.status() != BookingStatus.PENDING) {
                    System.out.println(ErrorMessages.BOOKING_NOT_PENDING);
                } else {
                    isBookingValid = true;
                }
            }

            processPayment(user, selectedBooking);
        } catch (IOException e) {
            System.out.println(ErrorMessages.PAYMENT_FAILED + e.getMessage());
        }
    }

    /** Pays an existing Pending booking selected using Pay for Booked Flight. */
    private void processPayment(RegularUser user, Booking booking) {
        BookingStorage storage = BookingStorage.getInstance();
        try {
            Booking selectedBooking = storage.findBooking(booking.bookingId());
            if (selectedBooking == null
                    || !selectedBooking.username().equalsIgnoreCase(user.getUsername())) {
                System.out.println(ErrorMessages.BOOKING_NOT_FOUND);
                return;
            }
            if (selectedBooking.status() != BookingStatus.PENDING) {
                System.out.println(ErrorMessages.BOOKING_NOT_PENDING);
                return;
            }

            BigDecimal amountPaid = collectPayment(selectedBooking);
            if (amountPaid == null) {
                System.out.println("Payment cancelled. Booking remains Pending.");
                return;
            }
            if (!storage.updateBooking(selectedBooking.withStatus(BookingStatus.PAID))) {
                System.out.println(ErrorMessages.BOOKING_NOT_FOUND);
                return;
            }

            displayPaymentReceipt(selectedBooking.withStatus(BookingStatus.PAID), amountPaid);
        } catch (IOException e) {
            System.out.println(ErrorMessages.PAYMENT_FAILED + e.getMessage());
        }
    }

    /**
     * Collects and confirms payment without saving. A null result means payment was
     * cancelled.
     */
    private BigDecimal collectPayment(Booking booking) {
        System.out.printf("Amount due: PHP %,.2f%n", booking.totalPrice());
        while (true) {
            System.out.print("Enter cash amount in PHP (0 to cancel): ");
            String paymentInput = sc.nextLine().trim();
            if (!paymentInput.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
                System.out.println(ErrorMessages.INVALID_PAYMENT_AMOUNT);
                continue;
            }
            BigDecimal amountPaid = new BigDecimal(paymentInput);
            if (amountPaid.signum() == 0) {
                return null;
            }
            if (amountPaid.compareTo(booking.totalPrice()) < 0) {
                System.out.println(ErrorMessages.INSUFFICIENT_PAYMENT);
                continue;
            }
            if (!Input.askYesNo("Confirm payment for " + booking.bookingId() + "?")) {
                return null;
            }
            return amountPaid;
        }
    }

    private void displayPaymentReceipt(Booking booking, BigDecimal amountPaid) {
        System.out.println("Payment successful.");
        Display.table(new String[] { "Detail", "Information" }, new String[][] {
                { "Booking ID", booking.bookingId() },
                { "Fee", InformationTables.money(booking.totalPrice()) },
                { "Cash", InformationTables.money(amountPaid) },
                { "Change", InformationTables.money(amountPaid.subtract(booking.totalPrice())) },
                { "Status", booking.status().toString() }
        });
        InformationTables.passengers(booking);
    }

}
