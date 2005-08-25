/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.util.Hashtable;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Config {
	private static String filename = "config/windmon.cfg";
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
	
	private void loadConfig()
	{
		params = new Hashtable();
	}
}
