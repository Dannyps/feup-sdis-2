package utils;

import java.io.PrintStream;

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
     * @brief Print info
     * 
     * @param action
     * @param m
     */
    public static void i(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.BLUE_BOLD, ConsoleColours.BLUE);
    }

    /**
     * @brief Print error
     * 
     * @param action
     * @param m
     */
    public static void e(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.RED_BOLD_BRIGHT, ConsoleColours.RED, System.err);
    }

    /**
     * @brief Print success
     * 
     * @param action
     * @param m
     */
    public static void s(String action, String m) {
        PrintMessage.p(action, m, ConsoleColours.GREEN_BOLD_BRIGHT, ConsoleColours.GREEN);
    }

    /**
     * @brief print warning
     * 
     * @param action
     * @param m
     */
    public static void w(String action, String m) {
        if (printWarnings)
            PrintMessage.p(action, m, ConsoleColours.YELLOW_BOLD_BRIGHT, ConsoleColours.YELLOW);
    }

    public static void p(String action, String m, String accentColor, String msgColor){
        p(action, m, accentColor, msgColor, System.out);
    }
    
    public static void p(String action, String m, String accentColor, String msgColor, PrintStream out) {
        if (printMessages) {
            // if is Windows, don't use colours
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                out.println("[" + action + "] " + m);
            } else {
                out.println(accentColor + "[" + action + "]" + msgColor + " " + m + ConsoleColours.RESET);
            }
        }
    }

	public static void rmi(String string) {
        PrintMessage.p("RMI", string, ConsoleColours.BLACK_BACKGROUND_BRIGHT+ConsoleColours.WHITE_BOLD_BRIGHT, ConsoleColours.BLACK_BACKGROUND+ConsoleColours.WHITE_BRIGHT);
	}

	public static void d(String action, String m) {
        PrintMessage.p("DEBUG: "+action, m, ConsoleColours.RED_BACKGROUND+ConsoleColours.GREEN_BRIGHT, ConsoleColours.BLACK_BACKGROUND+ConsoleColours.WHITE_BRIGHT);
	}
}