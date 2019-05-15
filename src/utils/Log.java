package utils;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

public class Log {

	public static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	static {
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);
		try {
			FileHandler h = new FileHandler("backup_%g_log_%u.log", true);
			h.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(h);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*** */
	public static void log(String message) {
		LOGGER.info(message);
		// Log Levels (ascending order):
		// FINEST - FINER - FINE - CONFIG - INFO - WARNING - SEVERE (highest)
	}

}
