package src;

import java.util.Scanner;

public class SignUp {
    Scanner sc;

    public SignUp(Scanner sc) {
        this.sc = sc;
    }

    public RegularUser signUp() {

        System.out.println("=== Sign Up ===");

        String fullName = "";
        boolean isFullNameValid = false;
        while (!isFullNameValid) {
            System.out.print("Enter Full Name: ");
            fullName = sc.nextLine().trim();
            isFullNameValid = !fullName.isEmpty();
            if (!isFullNameValid) {
                System.out.println(ErrorMessages.SignUpErrors.INVALID_USERNAME);
            }
        }

        String password = "";
        boolean isPasswordValid = false;
        while (!isPasswordValid) {
            System.out.print("Create Password: ");
            password = sc.nextLine();
            isPasswordValid = !password.trim().isEmpty();
            if (!isPasswordValid) {
                System.out.println(ErrorMessages.SignUpErrors.INVALID_PASSWORD);
            }
        }

        String email = "";
        boolean isEmailValid = false;
        while (!isEmailValid) {
            System.out.print("Enter Email: ");
            email = sc.nextLine().trim();
            isEmailValid = email.matches("[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+");
            if (!isEmailValid) {
                System.out.println(ErrorMessages.SignUpErrors.INVALID_EMAIL);
            }
        }

        String phoneNumber = "";
        boolean isPhoneNumberValid = false;
        while (!isPhoneNumberValid) {
            System.out.print("Enter Phone Number: ");
            phoneNumber = sc.nextLine().trim();
            isPhoneNumberValid = phoneNumber.matches("\\+?[0-9]+");
            if (!isPhoneNumberValid) {
                System.out.println(ErrorMessages.SignUpErrors.INVALID_PHONE_NUMBER);
            }
        }

        String address = "";
        boolean isAddressValid = false;
        while (!isAddressValid) {
            System.out.print("Enter Address: ");
            address = sc.nextLine().trim();
            isAddressValid = !address.isEmpty();
            if (!isAddressValid) {
                System.out.println(ErrorMessages.SignUpErrors.INVALID_ADDRESS);
            }
        }

        return new RegularUser(fullName, password, email, phoneNumber, address);
    }
}
