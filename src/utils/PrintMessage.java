package utils;

/**
 * PrintMesssage
 */
public class PrintMessage {

    public static boolean printMessages = false;

    // public static void p(String action, Message m) {
    // PrintMessage.p(action, m.toString(), ConsoleColours.BLUE_BOLD,
    // ConsoleColours.BLUE);
    // }

    /**
     * print info
     * @param action
     * @param m
     */
    public static void i(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.BLUE_BOLD, ConsoleColours.BLUE);
    }

    /**
     * print error
     * @param action
     * @param m
     */
    public static void e(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
    }

    public static void p(String action, String m, String accentColor, String msgColor) {
        if (printMessages) {
            // if is Windows, don't use colours
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                System.out.println("[" + action + "] " + m);
            } else {
                System.out.println(accentColor + "[" + action + "] " + msgColor + m + ConsoleColours.RESET);
            }
        }
    }
}