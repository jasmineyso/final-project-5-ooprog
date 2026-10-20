package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

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

public class RegularUserOperations {

    private static RegularUserOperations instance;
    private final Scanner sc = Input.getScanner();

    private RegularUserOperations() {
    }

    public static RegularUserOperations getInstance() {
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
                default -> System.out.println(ErrorMessages.INVALID_CHOICE);
            }
        }
    }

    public void viewBookedFlights(RegularUser user) {
        System.out.println("BOOKINGS");
        try {
            List<Booking> bookings = BookingStorage.getInstance().bookingsOf(user.getUsername());
            if (bookings.isEmpty()) {
                System.out.println("You have no bookings yet.");
                return;
            }
            int bookingNumber = 1;
            for (Booking booking : bookings) {
                System.out.println();
                System.out.println("#" + bookingNumber++);
                System.out.printf("%-13s: %s%n", "BOOKID", booking.bookingId());
                System.out.printf("%-13s: %s%n", "NAME", booking.passengerName());
                System.out.printf("%-13s: %s%n", "FLIGHT", booking.flightNumber());
                System.out.printf("%-13s: %s%n", "SERVICE CLASS", booking.serviceClass());
                System.out.printf("%-13s: %s%n", "SEATS", String.join(", ", booking.seats()));
                System.out.printf("%-13s: PHP %,.2f%n", "FEE", booking.totalPrice());
                System.out.printf("%-13s: %s%n", "STATUS", booking.status());
            }
        } catch (IOException e) {
            System.out.println("Could not load your bookings: " + e.getMessage());
        }
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
            System.out.println("=== Service Classes for " + selectedFlight.flightNumber() + " ===");
            System.out.printf("1. Economy - %,.2f%n", selectedFlight.fareFor(ServiceClass.ECONOMY));
            System.out.printf("2. Premium Economy - %,.2f%n", selectedFlight.fareFor(ServiceClass.PREMIUM_ECONOMY));
            System.out.printf("3. Business Class - %,.2f%n", selectedFlight.fareFor(ServiceClass.BUSINESS));
            System.out.printf("4. First Class - %,.2f%n", selectedFlight.fareFor(ServiceClass.FIRST_CLASS));
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
                isServiceClassValid = true;
            }
        }

        System.out.printf("Selected: %s | %s | Fare: %,.2f%n",
                selectedFlight.flightNumber(), selectedServiceClass,
                selectedFlight.fareFor(selectedServiceClass));

        boolean isNumberOfSeatsValid = false;
        int numberOfSeats = 0;

        while (!isNumberOfSeatsValid) {
            try {
                int availableSeats = BookingStorage.getInstance()
                        .availableSeats(selectedFlight, selectedServiceClass).size();
                System.out.printf("Available seats in %s on flight %s: %d%n",
                        selectedServiceClass, selectedFlight.flightNumber(), availableSeats);
            } catch (IOException e) {
                System.out.println("Could not load seat availability: " + e.getMessage());
                return;
            }
            System.out.print("Enter Number of Seats(1-9): ");
            String numOfSeats = sc.nextLine().trim();

            if (numOfSeats.matches("[1-9]")) {
                numberOfSeats = Integer.parseInt(numOfSeats);
                isNumberOfSeatsValid = true;
            } else {
                System.out.println(ErrorMessages.INVALID_NUMBER_OF_SEATS);
            }
        }

        
        try {
            // Auto assigns the seat
            FlightSeatAssign seatAssignment = new FlightSeatAssign(
                    selectedFlight, selectedServiceClass, numberOfSeats);
            Booking booking = seatAssignment.bookFor(user);
            System.out.println("Booking saved: " + booking.bookingId());
            System.out.println("Assigned seats: " + String.join(", ", booking.seats()));
            System.out.printf("Total price: %,.2f | Status: %s%n",
                    booking.totalPrice(), booking.status());

            //Payment for the booking (PAID OR PENDING)
            int paymentChoice = Input.askChoice("Payment", "Pay now", "Pay later");
            if (paymentChoice == 1) {
                processPayment(user, booking);
            } else {
                System.out.println("Booking remains Pending. You can pay later using Pay for Booked Flight.");
            }
        } catch (NotEnoughSeatsException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(ErrorMessages.BOOKING_SAVE_FAILED + e.getMessage());
        }
    }

    public void payBooking(RegularUser user) {
        System.out.println("=== Booking Payment ===");
        BookingStorage storage = BookingStorage.getInstance();
        try {
            List<Booking> bookings = storage.bookingsOf(user.getUsername());
            boolean hasPendingBooking = false;
            for (Booking booking : bookings) {
                if (booking.status() == BookingStatus.PENDING) {
                    System.out.printf("%s | Flight %s | %s | PHP %,.2f%n",
                            booking.bookingId(), booking.flightNumber(),
                            booking.serviceClass(), booking.totalPrice());
                    hasPendingBooking = true;
                }
            }
            if (!hasPendingBooking) {
                System.out.println("You have no unpaid bookings.");
                return;
            }

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

    /** Shared payment flow for a newly created booking or a Pending booking selected later. */
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

            System.out.printf("Amount due: PHP %,.2f%n", selectedBooking.totalPrice());
            BigDecimal amountPaid = BigDecimal.ZERO;
            boolean isPaymentValid = false;
            while (!isPaymentValid) {
                System.out.print("Enter cash amount in PHP (0 to cancel): ");
                String paymentInput = sc.nextLine().trim();
                if (!paymentInput.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
                    System.out.println(ErrorMessages.INVALID_PAYMENT_AMOUNT);
                    continue;
                }
                amountPaid = new BigDecimal(paymentInput);
                if (amountPaid.signum() == 0) {
                    System.out.println("Payment cancelled. Booking remains Pending.");
                    return;
                }
                if (amountPaid.compareTo(selectedBooking.totalPrice()) < 0) {
                    System.out.println(ErrorMessages.INSUFFICIENT_PAYMENT);
                    continue;
                }
                isPaymentValid = true;
            }

            if (!Input.askYesNo("Confirm payment for " + selectedBooking.bookingId() + "?")) {
                System.out.println("Payment cancelled. Booking remains Pending.");
                return;
            }
            if (!storage.updateBooking(selectedBooking.withStatus(BookingStatus.PAID))) {
                System.out.println(ErrorMessages.BOOKING_NOT_FOUND);
                return;
            }

            System.out.println("Payment successful.");
            System.out.printf("%-13s: %s%n", "BOOKID", selectedBooking.bookingId());
            System.out.printf("%-13s: PHP %,.2f%n", "FEE", selectedBooking.totalPrice());
            System.out.printf("%-13s: PHP %,.2f%n", "CASH", amountPaid);
            System.out.printf("%-13s: PHP %,.2f%n", "CHANGE",
                    amountPaid.subtract(selectedBooking.totalPrice()));
            System.out.printf("%-13s: %s%n", "STATUS", BookingStatus.PAID);
        } catch (IOException e) {
            System.out.println(ErrorMessages.PAYMENT_FAILED + e.getMessage());
        }
    }

}
