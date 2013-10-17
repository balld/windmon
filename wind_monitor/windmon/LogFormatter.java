/**
 * 
 */
package windmon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author David
 *
 */
public class LogFormatter extends Formatter {
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd yyyy HH:mm:ss");
  
	/**
	 * 
	 */
	public LogFormatter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord rec) {
	  return String.format("%s %-7s %9d %-30s %s\n", calcDate(rec.getMillis()), rec.getLevel().toString(), Integer.valueOf(rec.getThreadID()), rec.getLoggerName(), formatMessage(rec));
	}

	private String calcDate(long millisecs) {
		Date resultdate = new Date(millisecs);
		synchronized(dateFormat) {
		  return dateFormat.format(resultdate);
		}
	}


}
