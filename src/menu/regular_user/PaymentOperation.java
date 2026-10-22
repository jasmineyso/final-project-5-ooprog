package src.menu.regular_user;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.account.RegularUser;
import src.booking.Booking;
import src.booking.BookingStatus;
import src.booking.BookingStorage;
import src.core.ErrorMessages;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.util.Input;
import src.util.Display;
import src.util.InformationTables;

/** Lists payable bookings, collects payment, and displays receipts. */
public class PaymentOperation {
    public void payBooking(RegularUser user) {
        System.out.println("=== Booking Payment ===");
        BookingStorage storage = BookingStorage.getInstance();
        try {
            List<Booking> bookings = storage.bookingsOf(user.getUsername());
            List<Booking> pendingBookings = new ArrayList<>();
            for (Booking booking : bookings) {
                if (booking.status() == BookingStatus.PENDING) {
                    Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
                    if (flight != null && flight.isBookable()) {
                        pendingBookings.add(booking);
                    }
                }
            }
            if (pendingBookings.isEmpty()) {
                System.out.println("You have no unpaid bookings available for payment.");
                return;
            }

            InformationTables.bookings(pendingBookings);

            Booking selectedBooking = null;
            boolean isBookingValid = false;
            while (!isBookingValid) {
                System.out.print("Enter Booking ID to pay (0 to cancel): ");
                String bookingId = Input.readLine().trim();
                if (bookingId.equals("0")) {
                    return;
                }

                selectedBooking = storage.findBooking(bookingId);

                if (selectedBooking == null || !selectedBooking.username().equalsIgnoreCase(user.getUsername())) {
                    System.out.println(ErrorMessages.BOOKING_NOT_FOUND);
                } else if (selectedBooking.status() != BookingStatus.PENDING) {
                    System.out.println(ErrorMessages.BOOKING_NOT_PENDING);
                } else {
                    Flight flight = FlightStorage.getInstance().findFlight(selectedBooking.flightNumber());
                    if (flight == null || !flight.isBookable()) {
                        Display.error("This flight is unavailable, cancelled or has already departed. Choose another booking.");
                        continue;
                    }
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
    BigDecimal collectPayment(Booking booking) throws IOException {
        Flight flight = FlightStorage.getInstance().findFlight(booking.flightNumber());
        if (flight == null || !flight.isBookable()) {
            throw new IOException("This flight is unavailable, cancelled or has already departed.");
        }
        System.out.printf("Amount due: PHP %,.2f%n", booking.totalPrice());
        while (true) {
            System.out.print("Enter cash amount in PHP (0 to cancel): ");
            String paymentInput = Input.readLine().trim();
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

    void displayPaymentReceipt(Booking booking, BigDecimal amountPaid) {
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
