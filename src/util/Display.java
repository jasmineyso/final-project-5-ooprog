package src.util;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything that gets printed to the screen goes through here,
 * so the look of the whole program can be changed in one place.
 */
public final class Display {

    private static final int WIDTH = 60;

    private Display() {
    }

    /** Boxed, centered heading. */
    public static void title(String title) {
        System.out.println();
        System.out.println("=".repeat(WIDTH));
        int pad = (WIDTH - title.length()) / 2;
        System.out.println(" ".repeat(Math.max(0, pad)) + title.toUpperCase());
        System.out.println("=".repeat(WIDTH));
    }

    public static void subTitle(String title) {
        System.out.println("-".repeat(WIDTH));
        int pad = (WIDTH - title.length()) / 2;
        System.out.println(" ".repeat(Math.max(0, pad)) + title.toUpperCase());
        System.out.println("-".repeat(WIDTH));
    }

    /** Indented numbered options with a line under them. */
    public static void options(String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, options[i]);
        }
        System.out.println("-".repeat(WIDTH));
    }

    /** Text before the user types, on the same line:  Enter your choice: */
    public static void prompt(String text) {
        System.out.print(text + ": ");
    }

    /** Normal message. */
    public static void message(String text) {
        System.out.println(text);
    }

    /** A line to separate sections. */
    public static void divider() {
        System.out.println("-".repeat(WIDTH));
    }

    /** Prints rows in aligned columns. Each column is as wide as its longest value. */
    public static void table(String[] headers, List<String[]> rows) {
        System.out.println();
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        StringBuilder format = new StringBuilder();
        int total = 0;
        for (int width : widths) {
            format.append("%-").append(width).append("s  ");
            total += width + 2;
        }
        System.out.println(String.format(format.toString(), (Object[]) headers).stripTrailing());
        System.out.println("-".repeat(total - 2));
        for (String[] row : rows) {
            System.out.println(String.format(format.toString(), (Object[]) row).stripTrailing());
        }
    }

    public static void table(String[] headers, String[][] rows) {
        table(headers, java.util.Arrays.asList(rows));
    }

    /** One aligned "Label : value" line. */
    public static void detail(String label, String value) {
        System.out.printf("  %-16s: %s%n", label, value);
    }

    public static String money(BigDecimal amount) {
        return String.format(java.util.Locale.US, "PHP %,.2f", amount);
    }

    public static void error(String text) {
        System.out.println("[ERROR] " + text);
    }

    public static void success(String text) {
        System.out.println("[OK] " + text);
    }
}