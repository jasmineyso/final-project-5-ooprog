package src.menu;

import java.io.IOException;

import src.account.AccountStorage;
import src.account.RegularUser;
import src.core.ErrorMessages;
import src.util.Input;
import src.util.Display;

public class SignUp {

    public RegularUser signUp() {

        Display.title("Sign Up");

        String username = "";
        boolean isUsernameValid = false;
        while (!isUsernameValid) {
            System.out.print("Create Username: ");
            username = Input.readLine().trim();
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
            password = Input.readLine();
            isPasswordValid = !password.trim().isEmpty();
            if (!isPasswordValid) {
                System.out.println(ErrorMessages.INVALID_PASSWORD);
            }
        }

        String email = "";
        boolean isEmailValid = false;
        while (!isEmailValid) {
            System.out.print("Enter Email: ");
            email = Input.readLine().trim();
            isEmailValid = email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+");
            if (!isEmailValid) {
                System.out.println(ErrorMessages.INVALID_EMAIL);
            }
        }

        String phoneNumber = "";
        boolean isPhoneNumberValid = false;
        while (!isPhoneNumberValid) {
            System.out.print("Enter Phone Number: ");
            phoneNumber = Input.readLine().trim();
            isPhoneNumberValid = phoneNumber.matches("\\+?[0-9]+");
            if (!isPhoneNumberValid) {
                System.out.println(ErrorMessages.INVALID_PHONE_NUMBER);
            }
        }

        String address = "";
        boolean isAddressValid = false;
        while (!isAddressValid) {
            System.out.print("Enter Address: ");
            address = Input.readLine().trim();
            isAddressValid = !address.isEmpty();
            if (!isAddressValid) {
                System.out.println(ErrorMessages.INVALID_ADDRESS);
            }
        }

        return new RegularUser(username, password, email, phoneNumber, address);
    }

    private boolean isUsernameTaken(String username) {
        try {
            return AccountStorage.getInstance().findUser(username) != null;
        } catch (IOException e) {
            System.out.println("Could not check existing accounts: " + e.getMessage());
            return false;
        }
    }
}
