package windmon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventLog {
  private static final Logger logger = Logger.getLogger(EventLog.class.getName());
  public static final int SEV_DEBUG = 1;
  public static final int SEV_INFO  = 2;
  public static final int SEV_WARN  = 3;
  public static final int SEV_ERROR = 4;
  public static final int SEV_FATAL = 5;
  public static final int SEV_MIN =   SEV_DEBUG;
  public static final int SEV_MAX =   SEV_FATAL;

  public static final String sevStrings[] = { "XXXXX",
    "DEBUG",
    "INFO",
    "WARN",
    "ERROR",
  "FATAL" };

  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
  private static SimpleDateFormat fmtFileName = new SimpleDateFormat("yyyyMMdd");

  private static String AppLogDirectory = null;

  private static int logLevel = SEV_DEBUG;

  public static void log_old(int sev, String msg)
  {
    if ( sev >= logLevel)
    {
      StringBuffer bf = new StringBuffer();
      fmt.format(new Date(System.currentTimeMillis()), bf, new FieldPosition(0));

      PrintStream ps = null;
      if ( AppLogDirectory != null )
      {
        String fname =  AppLogDirectory + "/"
            + "windmon_" 
            + fmtFileName.format(new Date(System.currentTimeMillis())) 
            + ".log";
        try {
          ps = new PrintStream(new FileOutputStream(new File(fname), true));
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          ps = System.err;
          AppLogDirectory = null;
        }
      }
      else
      {
        ps = System.err;
      }

      ps.println(bf + " " + sevToString(sev) + " " + msg );

      if (sev == SEV_FATAL )
      {
        Exception e = new Exception();
        e.printStackTrace(ps);
        System.exit(1);
      }

      if ( ps != System.err )
      {
        ps.flush();
        ps.close();
      }
    }
  }

  private static String sevToString (int sev)
  {
    if ( sev < SEV_MIN || sev > SEV_MAX )
    {
      return sevStrings[0];
    }
    else
    {
      return sevStrings[sev];
    }
  }

  private static int stringToSev (String s)
  {
    int i = SEV_MAX;

    while ( i>= SEV_MIN && sevStrings[i].compareToIgnoreCase(s) != 0)
    {
      i--;
    }
    // Must have found match - or reached index of 0
    return i;
  }

  public static void setLogLevel(int level)
  {
    logLevel = level;
  }

  public static void setLogLevelAsString(String s)
  {
    logLevel = stringToSev(s);
  }

  public static void setAppLogDirectory(String s)
  {
    File path = new File(s);
    if ( path.exists())
    {
      if (path.isDirectory())
      {
        AppLogDirectory = s;
      }
      else
      {
        AppLogDirectory = null;
        logger.severe("AppLogDirectory '" + s + "' exists but is not a directory");
        System.exit(1);
      }
    }
    else
    {
      if ( path.mkdirs() == true )
      {
        logger.info("AppLogDirectory '" + s + "' created");
        AppLogDirectory = s;
      }
      else {
        AppLogDirectory = null;
        logger.severe("AppLogDirectory '" + s + "' could not be created");
        System.exit(1);
      }				
    }
    try {
      Logger topLevelLogger = Logger.getLogger("");
      //
      // Set formatter on default handler(s)
      //
      for (Handler h: topLevelLogger.getHandlers()) {
        h.setFormatter(new LogFormatter());
      }
      
      //
      // Create file handler
      //
      FileHandler handler = new FileHandler(AppLogDirectory + "/windmon.log", 1024000, 5, true);
      handler.setFormatter(new LogFormatter());
      topLevelLogger.addHandler(handler);
      topLevelLogger.setLevel(Level.INFO);
      // topLevelLogger.setUseParentHandlers(false);
      logger.info("Logging to '" + AppLogDirectory + "'");
    } catch (Exception e) {
      logger.warning("Failed to set up logging to log directory: " + e.getMessage());
    }

  }

  public static int getLogLevel()
  {
    return logLevel;
  }
}
