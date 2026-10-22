package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.booking.exception.NotEnoughSeatsException;
import src.core.ErrorMessages;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.flight.ServiceClass;
import src.util.Input;
import src.util.Display;
import src.util.InformationTables;

/** Collects booking choices and saves only confirmed drafts. */
public class BookFlightOperation {
    private final BookingStorage bookingStorage = BookingStorage.getInstance();
    private final PaymentOperation paymentOperation = new PaymentOperation();

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
            System.out.print("Enter Flight Number (0 to cancel): ");
            String flightNumber = Input.readLine().trim();
            if (flightNumber.equals("0")) {
                Display.message("Booking cancelled. No seats were reserved.");
                return;
            }

            try {
                selectedFlight = FlightStorage.getInstance().findFlight(flightNumber);
                if (selectedFlight != null) {
                    if (!selectedFlight.isBookable()) {
                        Display.error(
                                "This flight is cancelled or has already departed. Please choose another flight.");
                        continue;
                    }
                    boolean hasSeats = false;
                    for (ServiceClass serviceClass : ServiceClass.values()) {
                        if (!bookingStorage.availableSeats(selectedFlight, serviceClass).isEmpty()) {
                            hasSeats = true;
                            break;
                        }
                    }
                    if (!hasSeats) {
                        Display.message("This flight is sold out. Please choose another flight.");
                        continue;
                    }
                    isValidFlight = true;
                } else {
                    System.out.println(ErrorMessages.FLIGHT_NOT_FOUND);
                }
            } catch (IOException e) {
                System.out.println("Could not load flights or seat availability: " + e.getMessage());
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
                System.out.println("No seats remain on this flight. Booking cancelled.");
                return;
            }
            System.out.print("Enter your service class choice (1-4, 0 to cancel): ");

            String classChoice = Input.readLine().trim();
            if (classChoice.equals("0")) {
                Display.message("Booking cancelled. No seats were reserved.");
                return;
            }
            selectedServiceClass = switch (classChoice) {
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
                        "No seats remain in this service class. Booking cancelled.");
                return;
            }
            System.out.printf("Enter Number of Seats(1-%d, 0 to cancel): ", Math.min(9, availableSeats));
            String numOfSeats = Input.readLine().trim();
            if (numOfSeats.equals("0")) {
                Display.message("Booking cancelled. No seats were reserved.");
                return;
            }

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
            int paymentChoice = Input.askChoice("Payment", "Pay now", "Pay later", "Cancel Booking");
            if (paymentChoice == 3) {
                Display.message("Booking cancelled. No seats were reserved.");
                return;
            }
            if (paymentChoice == 1) {
                BigDecimal amountPaid = paymentOperation.collectPayment(booking);
                if (amountPaid == null) {
                    System.out.println("Booking was not saved. No seats were reserved.");
                    return;
                }
                booking = booking.withStatus(BookingStatus.PAID);
                bookingStorage.saveBooking(booking);
                System.out.println("Booking saved: " + booking.bookingId());
                paymentOperation.displayPaymentReceipt(booking, amountPaid);
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
                String input = Input.readLine();
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

}
