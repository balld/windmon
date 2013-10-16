/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * @author David
 *
 * Load configuration files and access individual parameters.
 */
public class Config {
  private static final Logger logger = Logger.getLogger(Config.class.getName());
  private static final String filename = "windmon.cfg";
  private static final String homeDirVar="%HOME%";
  private static final String homeDir = System.getProperties().getProperty("user.home", "/");
  private static Hashtable<String,String> params;

  private Config()
  {
  }

  public static void loadConfig()
  {
    BufferedReader br = null;
    params = new Hashtable<String,String>();
    String configPaths[] = {
        homeDir + "/" + filename,
        "config/" + filename
    };
    
    for (String path: configPaths) {
      try {
        br = new BufferedReader(new FileReader(path));
      } catch (FileNotFoundException e1) {
        logger.info("No configuration file at '" + path + "'");
      }
      if (br != null) {
        logger.info("Found configuration file '" + path + "'");
        break;
      }
    }
    
    if (br == null) {
      logger.severe("Could not load configuration. Terminating.");
      System.exit(1);
    }
    
    
    try {
      String ln;
      while ( (ln = br.readLine()) != null)
      {
        // Remove any text after '#'
        int i = ln.indexOf('#');
        if ( i >= 0 )
        {
          ln = ln.substring(0, i).trim();
        }
        else
        {
          ln = ln.trim();
        }
        int j = ln.indexOf('=');
        if ( j > 0 ) // Need at least one character before '='
        {
          String key = ln.substring(0, j);
          String value = doSubs(ln.substring(j+1, ln.length()));
          params.put(key,value);
        }
      }
      br.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    // System.out.println(params.toString());
  }

  public static String doSubs(String s)
  {
    String res = null;
    res = s.replace(homeDirVar, homeDir);
    return res;
  }

  public static int getParamAsInt(String param, int dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Integer.parseInt((String) ob);
    }
  }

  public static int getParamAsInt(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return 0;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Integer.parseInt((String) ob);
    }
  }

  public static long getParamAsLong(String param, long dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Long.parseLong((String) ob);
    }
  }

  public static long getParamAsLong(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return 0;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Long.parseLong((String) ob);
    }
  }

  public static float getParamAsFloat(String param, float dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Float.parseFloat((String) ob);
    }
  }

  public static float getParamAsFloat(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return 0.0f;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Float.parseFloat((String) ob);
    }
  }

  public static double getParamAsDouble(String param, double dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Double.parseDouble((String) ob);
    }
  }

  public static double getParamAsDouble(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return 0.0;
    }
    else
    {
      logger.info(param + " = " + ob);
      return Double.parseDouble((String) ob);
    }
  }

  public static String getParamAsString(String param, String dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);
      return ((String) ob);
    }
  }

  public static String getParamAsString(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return null;
    }
    else
    {
      logger.info(param + " = " + ob);
      return (String) ob;
    }
  }

  public static boolean getParamAsBoolean(String param, boolean dflt)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.warning("Configuration parameter '" + param + "' not found. Defaulted to " + dflt);
      return dflt;
    }
    else
    {
      logger.info(param + " = " + ob);

      String str = (String) ob;
      if ( str.equalsIgnoreCase("Y"))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
  }

  public static boolean getParamAsBoolean(String param)
  {
    Object ob = params.get(param);
    if ( ob == null )
    {
      logger.severe("Configuration parameter '" + param + "' not found. Mandatory.");
      System.exit(1);
      return false;
    }
    else
    {
      logger.info(param + " = " + ob);

      String str = (String) ob;
      if ( str.equalsIgnoreCase("Y"))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
  }
}
