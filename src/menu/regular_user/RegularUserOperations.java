package src.menu.regular_user;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.core.ErrorMessages;
import src.flight.Flight;
import src.flight.FlightStorage;
import src.util.Input;
import src.util.Validator;

public class RegularUserOperations {
    
    private static RegularUserOperations instance;
    private final Scanner sc = Input.getScanner();

    private RegularUserOperations(){}

    public static RegularUserOperations getInstance(){
        if (instance == null){
            instance = new RegularUserOperations();
        }
        return instance;
    }

    public void viewFlights(){
        boolean isValid = false;

        viewFlightOperations viewFlights = new viewFlightOperations();

        while (!isValid){
            System.out.println("1. View All Flights");
            System.out.println("2. Search by Origin and Destination");
            
            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1" -> {
                    viewFlights.viewAllFlights(); return;
                }
                case "2" -> {
                    viewFlights.searchByOriginAndDestination(); return;
                }
                default -> System.out.println(ErrorMessages.SwitchError.INVALID_CHOICE);
            }
        }
    }
}
