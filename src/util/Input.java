package src.util;

import java.util.Scanner;

import src.core.ErrorMessages;

public final class Input {
    private static Scanner instance;

    private Input() {}

    public static Scanner getScanner() {
        if (instance == null) {
            instance = new Scanner(System.in);
        }
        return instance;
    }

    public static int askChoice(String title, String... options) {
        Scanner sc = getScanner();
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
            System.out.println(ErrorMessages.INVALID_CHOICE);
        }
    }

    /** Asks a yes/no question until the user types Y or N. Returns true for Y. */
    public static boolean askYesNo(String question) {
        Scanner sc = getScanner();
        while (true) {
            Display.prompt(question + " (Y/N)");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                return true;
            }
            if (input.equalsIgnoreCase("N")) {
                return false;
            }
            Display.error("Please enter Y or N.");
        }
    }
}