/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.util.Hashtable;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Config {
	private static String filename = "windmon.cfg";
	private static Config config = null;
	
	private static Hashtable params;
	
	private Config()
	{
	}
		
	public static void loadConfig()
	{
		BufferedReader br;
		params = new Hashtable();
        String homeDir = System.getProperties().getProperty("user.home", "/");
        String path = homeDir + "/" + filename;
        EventLog.log(EventLog.SEV_DEBUG, "Config file is '" + path + "'");
		try
		{
			br = new BufferedReader(new FileReader(path));
		}
		catch (Exception e)
		{
            EventLog.log(EventLog.SEV_FATAL, "Unable to open config file " + path);
			e.printStackTrace();
			return;
		}
		
		try
		{
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
					params.put(ln.substring(0, j),
							   ln.substring(j+1, ln.length()));
				}
				
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println(params.toString());
	}
	

	public static int getParamAsInt(String param, int dflt)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
			return dflt;
		}
		else
		{
			EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
			return Integer.parseInt((String) ob);
		}
	}

	public static int getParamAsInt(String param)
	{
		return getParamAsInt(param, -1);
	}
	
	public static long getParamAsLong(String param, long dflt)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
			return dflt;
		}
		else
		{
			EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
			return Long.parseLong((String) ob);
		}
	}

	public static long getParamAsLong(String param)
	{
		return getParamAsLong(param, -1);
	}
	
	public static float getParamAsFloat(String param, float dflt)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
			return dflt;
		}
		else
		{
			EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
			return Float.parseFloat((String) ob);
		}
	}

	public static float getParamAsFloat(String param)
	{
		return getParamAsFloat(param, -1.0f);
	}
	
	public static double getParamAsDouble(String param, double dflt)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
			return dflt;
		}
		else
		{
			EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
			return Double.parseDouble((String) ob);
		}
	}

	public static double getParamAsDouble(String param)
	{
		return getParamAsDouble(param, -1.0);
	}
	
	public static String getParamAsString(String param, String dflt)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
			return dflt;
		}
		else
		{
			EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
			return ((String) ob);
		}
	}
	
	public static String getParamAsString(String param)
	{
		return getParamAsString(param, null);
	}

    public static boolean getParamAsBoolean(String param, boolean dflt)
    {
        Object ob = params.get(param);
        if ( ob == null )
        {
            EventLog.log(EventLog.SEV_WARN, param + " not found. Defaulted to " + dflt);
            return dflt;
        }
        else
        {
            EventLog.log(EventLog.SEV_INFO, param + " = " + ob);
            
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
        return getParamAsBoolean(param, false);
    }

}
