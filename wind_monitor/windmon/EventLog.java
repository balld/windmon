/*
 * Created on Aug 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.FieldPosition;
/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventLog {
	
	public static final int SEV_DEBUG = 1;
	public static final int SEV_INFO  = 2;
	public static final int SEV_WARN  = 3;
	public static final int SEV_ERROR = 4;
	public static final int SEV_FATAL = 5;

	private static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	
	private static int logLevel = SEV_DEBUG;
	
	public static void log(int sev, String msg)
	{
		if ( sev >= logLevel)
		{
			StringBuffer bf = new StringBuffer();
			fmt.format(new Date(System.currentTimeMillis()), bf, new FieldPosition(0));
			System.err.println(bf + " " + sevToString(sev) + " " + msg );
            
            if (sev == SEV_FATAL )
            {
                Exception e = new Exception();
                e.printStackTrace();
                System.exit(1);
            }
		}
	}
	
	private static String sevToString (int sev)
	{
		switch (sev)
		{
		    case SEV_DEBUG: return "DEBUG"; // break;
		    case SEV_INFO: return  "INFO "; // break;
		    case SEV_WARN: return  "WARN "; // break;
		    case SEV_ERROR: return "ERROR"; // break;
		    case SEV_FATAL: return "FATAL"; // break;
		    default:        return "     "; // break;
		}
	}
	
	public static void setLogLevel(int level)
	{
		logLevel = level;
	}
	
	public static int getLogLevel()
	{
		return logLevel;
	}
}
