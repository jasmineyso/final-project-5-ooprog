package src.util;

public final class Display {

    private Display() {
    }

    /** Blank line, then a heading:  === Admin Menu === */
    public static void title(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    /** Numbered list:  [1] Add Flight  [2] Update Flight ... */
    public static void options(String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.println("[" + (i + 1) + "] " + options[i]);
        }
    }

    /** Text before the user types, on the same line:  Enter your choice: */
    public static void prompt(String text) {
        System.out.print(text + ": ");
    }

    /** Normal message. */
    public static void message(String text) {
        System.out.println(text);
    }

    /** Something went wrong (invalid input, not found, failed to save...). */
    public static void error(String text) {
        System.out.println(text);
    }

    /** Something worked (account created, flight added...). */
    public static void success(String text) {
        System.out.println(text);
    }

    /** A line to separate sections. */
    public static void divider() {
        System.out.println("----------------------------------------");
    }
}