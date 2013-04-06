package windmon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventLog {
	
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
	
	public static void log(int sev, String msg)
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
				EventLog.log(EventLog.SEV_FATAL,
						     "AppLogDirectory '" + s + "' exists but is not a directory");
			}
		}
		else
		{
			if ( path.mkdirs() == true )
			{
				EventLog.log(EventLog.SEV_INFO,
					     "AppLogDirectory '" + s + "' created");
				AppLogDirectory = s;
			}
			else {
				AppLogDirectory = null;
				EventLog.log(EventLog.SEV_FATAL,
					     "AppLogDirectory '" + s + "' could not be created");
			}				
		}
	}
	
	public static int getLogLevel()
	{
		return logLevel;
	}
}
