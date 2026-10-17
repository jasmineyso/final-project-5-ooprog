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
            Display.title(title);
            Display.options(options);
            Display.prompt("Enter your choice");
            String input = sc.nextLine();

            if (Validator.isIntInRange(input, 1, options.length)) {
                return Integer.parseInt(input.trim());
            }
            Display.error(ErrorMessages.SwitchError.INVALID_CHOICE);
        }
    }
}