package mctmods.immersivetechnology.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class ITLogger {
	public static Logger logger;

	public static void log(Level logLevel, String message, Object... params) { logger.log(logLevel, message, params); }

	public static void info(String message, Object... params) { log(Level.INFO, message, params); }

	public static void warn(String message, Object... params) { log(Level.WARN, message, params); }

	public static void error(String message, Object... params) { log(Level.ERROR, message, params); }
}
