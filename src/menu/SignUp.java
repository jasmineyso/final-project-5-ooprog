package src.menu;

import java.io.IOException;
import java.util.Scanner;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Input;

public class SignUp {
    private final Scanner sc = Input.getScanner();

    public RegularUser signUp() {

        System.out.println("=== Sign Up ===");

        String username = "";
        boolean isUsernameValid = false;
        while (!isUsernameValid) {
            System.out.print("Create Username: ");
            username = sc.nextLine().trim();
            if (!username.matches("[A-Za-z0-9_.]{3,20}")) {
                System.out.println("Username must be 3 to 20 letters, digits, _ or . (no spaces).");
            } else if (isUsernameTaken(username)) {
                System.out.println("That username is already taken. Please choose another.");
            } else {
                isUsernameValid = true;
            }
        }

        String password = "";
        boolean isPasswordValid = false;
        while (!isPasswordValid) {
            System.out.print("Create Password: ");
            password = sc.nextLine();
            isPasswordValid = !password.trim().isEmpty();
            if (!isPasswordValid) {
                System.out.println(ErrorMessages.INVALID_PASSWORD);
            }
        }

        String email = "";
        boolean isEmailValid = false;
        while (!isEmailValid) {
            System.out.print("Enter Email: ");
            email = sc.nextLine().trim();
            isEmailValid = email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+");
            if (!isEmailValid) {
                System.out.println(ErrorMessages.INVALID_EMAIL);
            }
        }

        String phoneNumber = "";
        boolean isPhoneNumberValid = false;
        while (!isPhoneNumberValid) {
            System.out.print("Enter Phone Number: ");
            phoneNumber = sc.nextLine().trim();
            isPhoneNumberValid = phoneNumber.matches("\\+?[0-9]+");
            if (!isPhoneNumberValid) {
                System.out.println(ErrorMessages.INVALID_PHONE_NUMBER);
            }
        }

        String address = "";
        boolean isAddressValid = false;
        while (!isAddressValid) {
            System.out.print("Enter Address: ");
            address = sc.nextLine().trim();
            isAddressValid = !address.isEmpty();
            if (!isAddressValid) {
                System.out.println(ErrorMessages.INVALID_ADDRESS);
            }
        }

        return new RegularUser(username, password, email, phoneNumber, address);
    }

    /** True if another account already uses this username (ignores capital letters). */
    private boolean isUsernameTaken(String username) {
        try {
            return AccountStorage.getInstance().findUser(username) != null;
        } catch (IOException e) {
            System.out.println("Could not check existing accounts: " + e.getMessage());
            return false;
        }
    }
}
