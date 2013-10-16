package windmon;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventLog {
  private static final Logger logger = Logger.getLogger(EventLog.class.getName());

  /**
   * @param s
   */
  public static void setLogLevel (String s)
  {
    Level l = Level.parse(s);
    if (l == null) {
      logger.severe("Uknown log level '" + s + "'. Defaulting to INFO");
      l = Level.INFO;
    }
    setLogLevel(l);
  }

  /**
   * @param l
   */
  public static void setLogLevel (Level l)
  {
    Logger.getLogger("").setLevel(l);
  }
  
  /**
   * @return
   */
  public static Level getLogLevel() {
    return Logger.getLogger("").getLevel();
  }


  public static void setAppLogDirectory(String s)
  {
    String appLogDirectory = null;
    File path = new File(s);
    if ( path.exists())
    {
      if (path.isDirectory())
      {
        appLogDirectory = s;
      }
      else
      {
        appLogDirectory = null;
        logger.severe("AppLogDirectory '" + s + "' exists but is not a directory");
        System.exit(1);
      }
    }
    else
    {
      if ( path.mkdirs() == true )
      {
        logger.info("AppLogDirectory '" + s + "' created");
        appLogDirectory = s;
      }
      else {
        appLogDirectory = null;
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
      FileHandler handler = new FileHandler(appLogDirectory + "/windmon.log", 1024000, 5, true);
      handler.setFormatter(new LogFormatter());
      topLevelLogger.addHandler(handler);
      topLevelLogger.setLevel(Level.INFO);
      // topLevelLogger.setUseParentHandlers(false);
      logger.info("Logging to '" + appLogDirectory + "'");
    } catch (Exception e) {
      logger.warning("Failed to set up logging to log directory: " + e.getMessage());
    }
  }
}
