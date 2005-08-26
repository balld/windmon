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
	private static String filename = "config/windmon.txt";
	private static Config config = null;
	
	private Hashtable params;
	
	private Config()
	{
	}
	
	public static Config getCreateConfig()
	{
		if ( config == null)
		{
			config = new Config();
			config.loadConfig();
		}
		
		return config;
	}
	
	public static Config getConfig()
	{
		return config;
	}
	
	public void loadConfig()
	{
		BufferedReader br;
		params = new Hashtable();
		try
		{
			br = new BufferedReader(new FileReader(filename));
		}
		catch (Exception e)
		{
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
					ln = ln.substring(0, i-1).trim();
				}
				else
				{
					ln = ln.trim();
				}
				int j = ln.indexOf('=');
				if ( j > 0 ) // Need at least one character before '='
				{
					params.put(ln.substring(0, j-1),
							   ln.substring(j+1, ln.length()-1));
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
	
	public int getParamAsInt(String param)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			return -1;
		}
		else
		{
			return Integer.parseInt((String) ob);
		}
	}

	public float getParamAsFloat(String param)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			return -1.0f;
		}
		else
		{
			return Float.parseFloat((String) ob);
		}
	}

	public double getParamAsDouble(String param)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			return -1.0;
		}
		else
		{
			return Double.parseDouble((String) ob);
		}
	}
	
	public String getParamAsString(String param)
	{
		Object ob = params.get(param);
		if ( ob == null )
		{
			return null;
		}
		else
		{
			return ((String) ob);
		}
	}
}
