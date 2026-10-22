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

    /** Signals closed input so a menu can exit without saving an unfinished action. */
    public static final class EndOfInputException extends RuntimeException {
        private EndOfInputException() {
            super("Input has ended.");
        }
    }

    public static String readLine() {
        Scanner scanner = getScanner();
        if (!scanner.hasNextLine()) {
            throw new EndOfInputException();
        }
        return scanner.nextLine();
    }

    public static int askChoice(String title, String... options) {
        boolean isChoiceValid = false;
        int choice = 0;
        while (!isChoiceValid) {
            System.out.println();
            System.out.println("=== " + title + " ===");
            for (int i = 0; i < options.length; i++) {
                System.out.println("[" + (i + 1) + "] " + options[i]);
            }
            System.out.print("Enter your choice: ");
            String input = readLine();

            if (Validator.isIntInRange(input, 1, options.length)) {
                choice = Integer.parseInt(input.trim());
                isChoiceValid = true;
            } else {
                System.out.println(ErrorMessages.INVALID_CHOICE);
            }
        }
        return choice;
    }

    /** Asks a yes/no question until the user types Y or N. Returns true for Y. */
    public static boolean askYesNo(String question) {
        boolean isAnswerValid = false;
        boolean answer = false;
        while (!isAnswerValid) {
            Display.prompt(question + " (Y/N)");
            String input = readLine().trim();

            if (input.equalsIgnoreCase("Y")) {
                answer = true;
                isAnswerValid = true;
            } else if (input.equalsIgnoreCase("N")) {
                isAnswerValid = true;
            } else {
                Display.error("Please enter Y or N.");
            }
        }
        return answer;
    }
}
