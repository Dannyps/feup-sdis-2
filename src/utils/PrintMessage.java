package utils;

/**
 * PrintMesssage
 */
public class PrintMessage {

    public static boolean printMessages = false;
    public static boolean printWarnings = false;

    // public static void p(String action, Message m) {
    // PrintMessage.p(action, m.toString(), ConsoleColours.BLUE_BOLD,
    // ConsoleColours.BLUE);
    // }

    /**
     * print info
     * 
     * @param action
     * @param m
     */
    public static void i(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.BLUE_BOLD, ConsoleColours.BLUE);
    }

    /**
     * print error
     * 
     * @param action
     * @param m
     */
    public static void e(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED);
    }

    /**
     * print success
     * 
     * @param action
     * @param m
     */
    public static void s(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.GREEN_BOLD_BRIGHT, ConsoleColours.GREEN);
    }

    /**
     * print warning
     * 
     * @param action
     * @param m
     */
    public static void w(String action, String m) {
        if (printWarnings)
            PrintMessage.p(action, m, ConsoleColours.YELLOW_BOLD_BRIGHT, ConsoleColours.YELLOW);
    }

    public static void p(String action, String m, String accentColor, String msgColor) {
        if (printMessages) {
            // if is Windows, don't use colours
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                System.out.println("[" + action + "] " + m);
            } else {
                System.out.println(accentColor + "[" + action + "]" + msgColor + " " + m + ConsoleColours.RESET);
            }
        }
    }

	public static void rmi(String string) {
        PrintMessage.p("RMI", string, ConsoleColours.BLACK_BACKGROUND_BRIGHT+ConsoleColours.WHITE_BOLD_BRIGHT, ConsoleColours.BLACK_BACKGROUND+ConsoleColours.WHITE_BRIGHT);
	}
}