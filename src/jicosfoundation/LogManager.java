/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 *  Manages the standard Jicos logger.
 *
 *      Created on:  July 18, 2004
 *      Created by:  pippin
 *
 * @author   Andy Pippin
 */

/*
 Comments:

 File creation is rather complicated.  If you specify a filename,
 it will be checked to see if it is fully qualified.  If it is, it
 will be used unchanged.  If not, the directory will be prepended.

 */

package jicosfoundation;

import java.util.logging.*;

public class LogManager {
	//-- Constants -----------------------------------------------------------//

	public static final Level  DEFAULT_Level = Level.WARNING;

	
	private static final Level ALL = Level.ALL;

	public static final Level SEVERE = Level.SEVERE;

	public static final Level WARNING = Level.WARNING;

	public static final Level INFO = Level.INFO;

	public static final Level CONFIG = Level.CONFIG;

	public static final Level FINE = Level.FINE;

	public static final Level FINER = Level.FINER;

	public static final Level FINEST = Level.FINEST;

	private static final Level OFF = Level.OFF;

	// Special levels.
	public static final Level DEBUG = new myLevel( "DEBUG", Level.FINE.intValue() );
	public static final Level ERROR = new myLevel( "ERROR", Level.WARNING.intValue() );
	public static final Level FAILED = new myLevel( "FAILED", ((Level.INFO.intValue() + Level.FINE.intValue())/2)+1 );
	public static final Level HANDLED = new myLevel( "HANDLED", (Level.INFO.intValue() + Level.FINE.intValue())/2 );
	
	
	public static final String PROPERTY_Base = "jicos.log";
	public static final String PROPERTY_Filename = "filename";
	public static final String PROPERTY_Directory = "directory";
	public static final String PROPERTY_Level = "level";
	public static final String PROPERTY_MaxSize = "maxsize";
	public static final String PROPERTY_Files = "files";
	public static final String PROPERTY_Handler = "handler";
	public static final String PROPERTY_NumRecords = "records";
	public static final String PROPERTY_LogHost = "loghost";
	public static final String PROPERTY_LogPort = "logport";
	public static final String PROPERTY_OutputStream = "output";

	// Property values : Logger names.
	private static final String LOGGER_Base = "edu.ucsb.cs.jicos";
	
	public static final String LOGGER_Default = "default";

	public static final String LOGGER_Applications = "applications";

	public static final String LOGGER_Examples = "examples";

	public static final String LOGGER_Foundation = "foundation";

	public static final String LOGGER_Services = "services";

	public static final String LOGGER_Utilities = "utilities";
	
	

	private final static java.text.DateFormat dateFormatter = new java.text.SimpleDateFormat(
			"yyyy.MM.dd-HH:mm:ss");

	private final static String CRLF = System.getProperty("line.separator");

	

	//-- Variables -----------------------------------------------------------//

	private static java.util.Map loggerMap = null;

	//-- Methods -------------------------------------------------------------//

	/**
	 * Helper method - get the default logger.
	 * 
	 * @return  Default logger.
	 */
	public static Logger getLogger() {
		return (localGetLogger(null));
	}

	/**
	 * Helper class - get the appropriate logger depending on ths class of
	 * the <CODE>object</CODE>.
	 * <BR><BR>
	 * <TABLE BORDER=0>
	 *   <TR><TH>null</TH>  <TD>default logger.</TD></TR>
	 *   <TR><TH>String</TH><TD>treat it as the logger name.</TD></TR>
	 *   <TR><TH>Class</TH> <TD>use the name of the class.</TD></TR>
	 *   <TR><TH>???</TH>   <TD>use the name of the class of the object.</TD></TR>
	 * </TABLE>
	 * 
	 * @param object
	 * @return
	 */
	public static Logger getLogger( Object object ) {
		Logger logger = null;
		
		// If null, get default.
		if( null == object ) {
			logger = localGetLogger( (String)null );

		// If a string, use that at the logger name.
		} else if( object instanceof String ) {
			logger = localGetLogger( (String)object );
			
		// If a class, then use the class name as the logger.
		} else if( object instanceof Class ) {
			String className = ((Class)object).getName();
			int lastDot = className.lastIndexOf( '.' );
			String loggerName = className.substring( 0, lastDot );
			loggerName = loggerName.replaceFirst( "edu.ucsb.cs.jicos.", "" );
			//
			logger = localGetLogger( className.substring( 0, lastDot ) );
			
		// Otherwise, use the class name of the object as the logger.
		} else {
			String className = object.getClass().getName();
			int lastDot = className.lastIndexOf( '.' );
			String loggerName = className.substring( 0, lastDot );
			loggerName = loggerName.replaceFirst( "edu.ucsb.cs.jicos.", "" );
			//
			logger = localGetLogger( className.substring( 0, lastDot ) );
			
		}
		
		return( logger );
	}
	
	
	/**
	 * Get a particular logger.
	 * 
	 * @param loggerName The "unqualified" (no jicos.log) logger name.
	 * @return The specified logger.
	 */
	private static Logger localGetLogger(String loggerName) {
		Logger logger = null;
		String fqLoggerName = loggerName;

		// Allow null (the default).
		if( null == loggerName ) {
			fqLoggerName = LogManager.LOGGER_Default;
		}
		
		// Prepend the fully qualified name.
		if( !fqLoggerName.startsWith( LogManager.LOGGER_Base ) ) {
			fqLoggerName = LogManager.LOGGER_Base + '.' + fqLoggerName;
		}

		// Create the map, if necessary.
		if (null == LogManager.loggerMap) {
			LogManager.loggerMap = new java.util.HashMap();
			
			// Add the parent logger.  This currently consumes all log messages.
			//
			Logger parentLogger = Logger.getLogger( LogManager.LOGGER_Base );
			parentLogger.setUseParentHandlers( false );
		}

		// Get the logger.  Create it if doesn't exist yet.
		logger = (Logger)loggerMap.get( fqLoggerName );
		if( null == logger ) {
			logger = createLogger( fqLoggerName );
			LogManager.loggerMap.put( fqLoggerName, logger );
		}

		return (logger);
	}

	/**
	 * Helper method - display loggers to System.out.
	 */
	public static void showLoggers() {
		showLoggers(System.out);
	}

	/**
	 * Display all currently created loggers.
	 * 
	 * @param output Where to send the output.
	 */
	public static void showLoggers(java.io.PrintStream output) {
		if (null != loggerMap) {
			final int width1 = 50;
			final int width2 = 64;
			final int width3 = 80;
			
			String outputLine;
			Handler handler;
			
			System.out.println();
			outputLine = " --Logger----------------------------------------- --Level---- --Handler------------------";
			output.println( outputLine );
			
			java.util.Set entrySet = loggerMap.entrySet();
			java.util.Iterator entries = entrySet.iterator();
			while (entries.hasNext()) {
				Object mapEntry = entries.next();
				Object loggerName = ((java.util.Map.Entry) mapEntry).getKey();
				outputLine = "  " + loggerName + "                                      ";
				outputLine = outputLine.substring(0, width1);

				Object loggerObj = ((java.util.Map.Entry) mapEntry).getValue();
				Logger logger = (Logger) loggerObj;
				outputLine += "  " + logger.getLevel().getName() + "              ";
				outputLine = outputLine.substring(0, width2);

				Handler[] handlerArray = logger.getHandlers();
				if (0 != handlerArray.length) {
					handler = handlerArray[0];
					outputLine += handler.getClass().getName().replaceAll(
							"java.util.logging.", "");
					for (int h = 1; h < handlerArray.length; ++h) {
						handler = handlerArray[0];
						outputLine += ", "
								+ handler.getClass().getName().replaceAll(
										"java.util.logging.", "");
					}
				}

				output.println(outputLine);
			}
			
			System.out.println();
		} else {
			System.out.println( "  There are no loggers currently available." );
		}

		return;
	}

	//
	//-- Helper Methods ------------------------------------------------------
	
	/**
	 * Helper method - log a message of level INFO to the default logger. 
	 * 
	 * @param message  The message to log.
	 */
	public static void log(String message) {
		Logger logger = localGetLogger(null);
		logger.log(Level.INFO, message);
	}
	
	/**
	 * Helper method - Log the message of the exception to the default logger
	 * at level WARNING.
	 * 
	 * @param exception  The exception to be logged.
	 */
	public static void log(Throwable exception) {
		if( null != exception ) {
			Logger logger = localGetLogger(null);
			logger.log(Level.WARNING, exception.getMessage() );
		}
	}

	/**
	 * Helper method - log a message of the specified level to the default 
	 * logger.
	 *
	 * @param level  The log level.
	 * @param message  The message to log.
	 */
	public static void log(Level level, String message) {
		Logger logger = localGetLogger(null);
		logger.log(level, message);
	}
	
	/**
	 * Helper method - log a message of the specified level to the default 
	 * logger.
	 *
	 * @param object  The object for which this log message belongs. 
	 * @param level  The log level.
	 * @param message  The message to log.
	 */
	/*
	public static void log(Object object, Level level, String message) {
		Logger logger = getLogger(object);
		logger.log(level, message);
	}
	*/
	
	//
	//-- Private Methods -----------------------------------------------------

	// This is only called to create new loggers.
	//
	private static Logger createLogger(String loggerName) {
		Logger newLogger = null;
		
		// Bail if they are stupid.
		if (null == loggerName) {
			throw new NullPointerException( "Need to specify the logger name." );
		}

		// Get the logger, handler, formatter, and level.
		newLogger = Logger.getLogger( loggerName );
		Handler handler = getHandler( loggerName );
		java.util.logging.Formatter formatter = new LogManager.Formatter();
		Level level = getValue_Level( loggerName );
		
		// Put everything together and create the logger.
		handler.setLevel( level );
		handler.setFormatter( formatter );
		newLogger.setLevel( level );
		newLogger.addHandler( handler );

		// Give it back.
		return( newLogger );
	}

	//
	//========================================================================
	//== Create the handler ==================================================
	//========================================================================
	
	private static Handler getHandler( String loggerName ) {
		Handler handler = null;
		
		// Get the type of handler.
		String handlerName = getProperty( loggerName, PROPERTY_Handler, "Console" );
		handlerName = handlerName.toLowerCase();
		
		if( handlerName.startsWith( "file" ) ) {
			handler = createFileHandler( loggerName );
		} else if( handlerName.startsWith( "memory" ) ) {
			handler = createMemoryHandler( loggerName );
		} else if( handlerName.startsWith( "socket" ) ) {
			handler = createSocketHandler( loggerName );
		} else if( handlerName.startsWith( "stream" ) ) {
			handler = createStreamHandler( loggerName );
		} else {
			handler = createConsoleHandler( loggerName );
		}
		
		// Set the formatter.
		handler.setFormatter( new LogManager.Formatter() );
		
		return( handler );
	}

	private static Handler createFileHandler( String loggerName ) {
		Handler handler = null;
		
		String filename = getValue_Filename( loggerName );
		int maxSize = getValue_MaxSize( loggerName );
		int numFiles = getValue_Files( loggerName );
		boolean append = true;

		try {
			handler = new FileHandler( filename, maxSize, numFiles, append );
			
		} catch( Exception anyException ) { // Ack!!  Pfft!!
			handler = new ConsoleHandler();
		}
		
		return( handler );
	}
	
	private static Handler createMemoryHandler( String loggerName ) {
		Handler handler = null;
		
		String num = getProperty(loggerName, PROPERTY_NumRecords, "1000");
		int numRecords = Integer.parseInt( num );
		
		handler = new MemoryHandler(new ConsoleHandler(), numRecords,
				LogManager.WARNING);
		
		return( handler );
	}
	
	private static Handler createSocketHandler( String loggerName ) {
		Handler handler = null;

		String host = getProperty( loggerName, PROPERTY_LogHost, "localhost" );
		String port = getProperty( loggerName, PROPERTY_LogPort, null );
		
		try {
			if( (null != host) && (null != port) ) {
				handler = new SocketHandler( host, Integer.parseInt( port ) );
			} else {
				handler = new SocketHandler(); // try the default.
			}
		} catch( Exception anyException ) {
			handler = new ConsoleHandler();
		}
		
		return( handler );
	}
	
	private static Handler createStreamHandler( String loggerName ) {
		Handler handler = null;
		
		String streamName = getProperty( loggerName, PROPERTY_OutputStream, "System.err" );
		
		if( "System.out".equals( streamName )) {
			handler = new StreamHandler( System.out, null );
		} else if( "System.err".equals( streamName )) {
			handler = new StreamHandler( System.err, null );
		} 
		
		return( handler );
	}
	
	private static Handler createConsoleHandler( String loggerName ) {
		Handler handler = new ConsoleHandler();
		return( handler );
	}
	
	//
	//========================================================================
	//== Get a property name =================================================
	//========================================================================
	
	// Filename of log.
	private static String getValue_Filename( String loggerName ) {
		return( getProperty( loggerName, PROPERTY_Filename, "logger"+'-'+loggerName ) );
	}
	
	// Directory of log file.
	private static String getValue_Directory( String loggerName ) {
		return( getProperty( loggerName, PROPERTY_Directory, "log" ) );
	}
	
	// Level of logging.
	private static Level getValue_Level( String loggerName ) {
		String level = getProperty( loggerName, PROPERTY_Level, DEFAULT_Level.toString() );
		return( getLevel( level ) );
	}

	// Maximum file size (in bytes).
	private static int getValue_MaxSize( String loggerName ) {
		String maxSize = getProperty( loggerName, PROPERTY_MaxSize, "5000000" ); // 5MB
		return( Integer.parseInt( maxSize ) );
	}

	// Number of rotating files.
	private static int getValue_Files( String loggerName ) {
		String numFiles = getProperty( loggerName, PROPERTY_Files, "5" );
		return( Integer.parseInt( numFiles ) );
	}
	
	
	/*
	 * Get the value of the property for a particular logger.
	 * 
	 * Example:
	 *     getProperty( "default", "filename", "logger-default" );
	 *
	 * If the PROPERTY_Base is "jicos.log", then the property:
	 *     jicos.log.default.filename
	 * is checked first, then the property:
	 *     jicos.log.filename
	 * is checked.  If neither exist then  logger-default is returned.
	 *
	 * This method will toss an exception if the loggerName is null!
	 * 
	 * @param loggerName The "unqualified" name of the logger.
	 * @param property The property to look for.
	 * @param defaultValue The default value.
	 * @return Ther property value.
	 */
	private static String getProperty( String loggerName, String property, String defaultValue ) {
		String propValue = null;
		Object value;
		
		// If loggerName or property is null, then it's the programmers own
		// damn fault this crashes.
		
		String specific = LogManager.PROPERTY_Base + '.' + loggerName + '.' + property;
		specific = specific.replaceFirst( "jicos.log."+ LOGGER_Base, "jicos.log" );
		String generic = LogManager.PROPERTY_Base + '.' + property;

		// Try for the logger-specific value first.
		if( null != (property = System.getProperty( specific )) ) {
			propValue = (String)property;
			
		// Try for the logger-general value next.
		} else if( null != (property = System.getProperty( generic )) ) {
			propValue = (String)property;
			
		} else {
			propValue = defaultValue;
			
		}
		
		return( propValue );
	}
	
	//
	//========================================================================
	
	private static Level getLevel(String levelName) {
		Level level;

		if (null == levelName)
			level = DEFAULT_Level;
		else if (levelName.equalsIgnoreCase("all"))
			level = Level.ALL;
		else if (levelName.equalsIgnoreCase("severe"))
			level = Level.SEVERE;
		else if (levelName.equalsIgnoreCase("warning"))
			level = Level.WARNING;
		else if (levelName.equalsIgnoreCase("error"))
			level = (Level)LogManager.ERROR;
		else if (levelName.equalsIgnoreCase("info"))
			level = Level.INFO;
		else if (levelName.equalsIgnoreCase("failed"))
			level = (Level)LogManager.FAILED;
		else if (levelName.equalsIgnoreCase("handled"))
			level = (Level)LogManager.HANDLED;
		else if (levelName.equalsIgnoreCase("config"))
			level = Level.CONFIG;
		else if (levelName.equalsIgnoreCase("debug"))
			level = (Level)LogManager.DEBUG;
		else if (levelName.equalsIgnoreCase("fine"))
			level = Level.FINE;
		else if (levelName.equalsIgnoreCase("finer"))
			level = Level.FINER;
		else if (levelName.equalsIgnoreCase("finest"))
			level = Level.FINEST;
		else if (levelName.equalsIgnoreCase("off"))
			level = Level.OFF;
		else
			level = DEFAULT_Level;

		return (level);
	}

	private static boolean isFullyQualified(String filename) {
		boolean isFullyQualified = false;

		if (null == filename)
			return (false);

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.startsWith("windows")) {
			if ((2 < filename.length())
					&& Character.isLetter(filename.charAt(0))
					&& (':' == filename.charAt(1))
					&& (('/' == filename.charAt(2) || ('\\' == filename
							.charAt(2))))) {
				isFullyQualified = true;
			}
			if ((0 < filename.length()) && ('\\' == filename.charAt(0))) {
				isFullyQualified = true;
			}
		}

		else if ((0 < filename.length()) && ('/' == filename.charAt(0))) {
			isFullyQualified = true;
		}

		return (isFullyQualified);

	}

	//-- Inner Classes -------------------------------------------------------//

	public static class Formatter extends java.util.logging.Formatter {
		public String format(LogRecord logRecord) {
			StringBuffer buffer = new StringBuffer(160);

			buffer.append(dateFormatter.format(new java.util.Date(logRecord
					.getMillis())));
			buffer.append("  ");
			buffer.append(logRecord.getLevel().getName());
			buffer.append("  ");
			String className = logRecord.getSourceClassName();
			buffer.append(className.replaceAll("edu.ucsb.cs.jicos", "*"));
			
			String methodName = logRecord.getSourceMethodName();
			if( "<init>".equals( methodName ) ) {
				methodName = "";
			} else {
				methodName = "." + methodName;
			}
			buffer.append( methodName );
			
			buffer.append("(): ");
			buffer.append(logRecord.getMessage());
			buffer.append(CRLF);

			return (new String(buffer));
		}
	}

	public static class myLevel extends java.util.logging.Level {
		myLevel() {
			super( null, 0 );
		}
		myLevel( String name, int value ) {
			super( name, value );
		}
		myLevel( String name, int value, String resourceBundleName ) {
			super( name, value, resourceBundleName );
		}
		myLevel( java.util.logging.Level level ) {
			super( level.getName(), level.intValue(), level.getResourceBundleName() );
		}
	}
}

//== End of LogManager.java ====================================================
