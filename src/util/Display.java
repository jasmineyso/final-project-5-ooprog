package src.util;

public final class Display {

    private Display() {
    }

    /** Prints a bordered table, sizing each column to its longest header or value. */
    public static void table(String[] headers, String[][] rows) {
        if (headers.length == 0) {
            throw new IllegalArgumentException("A table needs at least one column.");
        }
        String[][] cells = new String[rows.length + 1][headers.length];
        int[] widths = new int[headers.length];
        for (int row = 0; row < cells.length; row++) {
            String[] values = row == 0 ? headers : rows[row - 1];
            if (values.length != headers.length) {
                throw new IllegalArgumentException("Every table row must match the header column count.");
            }
            for (int column = 0; column < headers.length; column++) {
                // Keep a stored line break or tab from disrupting the table borders.
                String value = values[column] == null ? "" : values[column].replaceAll("[\\r\\n\\t]", " ");
                cells[row][column] = value;
                widths[column] = Math.max(widths[column], value.length());
            }
        }
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            border.append("-".repeat(width + 2)).append('+');
        }
        System.out.println(border);
        for (int row = 0; row < cells.length; row++) {
            StringBuilder line = new StringBuilder("|");
            for (int column = 0; column < headers.length; column++) {
                String value = cells[row][column];
                line.append(' ').append(value)
                        .append(" ".repeat(widths[column] - value.length())).append(" |");
            }
            System.out.println(line);
            if (row == 0) {
                System.out.println(border);
            }
        }
        if (rows.length > 0) {
            System.out.println(border);
        }
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
