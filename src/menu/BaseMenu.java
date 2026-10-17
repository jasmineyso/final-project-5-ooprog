package src.menu;

import java.util.Scanner;

import src.core.ErrorMessages;
import src.util.Input;
import src.util.Validator;

/**
 * Every menu in the system extends this class.
 *
 * It handles the part every menu repeats: print the title and options,
 * read a choice, reject invalid input, and loop until the user leaves.
 * A subclass only says WHAT its options are and WHAT each one does.
 */
public abstract class BaseMenu {

    protected final Scanner sc = Input.getScanner();

    /** Runs the menu until handleChoice() returns false. */
    public void start() {
        boolean running = true;
        while (running) {
            int choice = askChoice(getTitle(), getOptions());
            running = handleChoice(choice);
        }
    }

    /** The heading shown above the options, e.g. "Admin Menu". */
    protected abstract String getTitle();

    /** The options in order. Option 1 is the first item in the array. */
    protected abstract String[] getOptions();

    /**
     * Runs the option the user picked.
     * Return true to show this menu again, false to leave it (Back / Log Out).
     */
    protected abstract boolean handleChoice(int choice);

    /**
     * Prints a numbered list and keeps asking until the user enters a valid number.
     * Returns the chosen number (1 to options.length).
     */
    protected int askChoice(String title, String... options) {
        while (true) {
            System.out.println();
            System.out.println("=== " + title + " ===");
            for (int i = 0; i < options.length; i++) {
                System.out.println("[" + (i + 1) + "] " + options[i]);
            }
            System.out.print("Enter your choice: ");
            String input = sc.nextLine();

            if (Validator.isIntInRange(input, 1, options.length)) {
                return Integer.parseInt(input.trim());
            }
            System.out.println(ErrorMessages.SwitchError.INVALID_CHOICE);
        }
    }

    /** Placeholder for options whose logic hasn't been written yet. */
    protected void notYetAvailable(String feature) {
        System.out.println(feature + " is not available yet.");
    }
}