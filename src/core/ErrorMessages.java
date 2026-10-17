package src.core;

// USER THIS CLASS TO STORE ALL ERROR MESSAGES IN ONE PLACE.
// THEN CALL THE ERROR MESSAGES FROM THIS CLASS INSTEAD OF WRITING THEM IN THE CODE.

public class ErrorMessages {
    public static class AccountErrors {
        public static final String SAVE_FAILED = "Account could not be saved: ";
        public static final String LOAD_FAILED = "Saved accounts could not be loaded: ";
    }

    public static class LogInErrors {
        public static final String INVALID_CREDENTIALS = "Invalid username or password. Please try again.";
    }

    public static class SignUpErrors {
        public static final String INVALID_USERNAME = "Username cannot be blank. Please try again.";
        public static final String INVALID_PASSWORD = "Password cannot be blank. Please try again.";
        public static final String INVALID_EMAIL = "Enter a valid email address (name@domain.tld).";
        public static final String INVALID_PHONE_NUMBER = "Phone number must contain digits, with an optional leading +.";
        public static final String INVALID_ADDRESS = "Address cannot be blank. Please try again.";
    }

    public static class SwitchError {
        public static final String INVALID_CHOICE = "Invalid choice. Please try again.";
    }
}
