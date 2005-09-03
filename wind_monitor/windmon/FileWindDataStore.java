/*
 * Created on Sep 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileWindDataStore implements WindDataStore {
	// These settins affect the number of files created. Currently every hour.
	private static final String DATE_FMT = "yyyyMMddHH";
	private static final long   DATE_INT = 3600000;
	
	DateFormat fnameFormat = new SimpleDateFormat("'windlog_'"
			                                      + DATE_FMT + "'.dat'");
	String path = null;
	
	public FileWindDataStore()
	{
		path = Config.getParamAsString("WindLogDataDirectory");
	}
	/* (non-Javadoc)
	 * @see windmon.WindDataStore#storeWindDataRecord(windmon.WindDataRecord)
	 */
	public void storeWindDataRecord(WindDataRecord record) {
		String fn = fnameFormat.format(new Date(record.getEndTime()));
		
		try
		{
			PrintWriter pw = new PrintWriter(new FileWriter(path + fn, true));
			pw.println(record.toString());
			pw.close();
		}
		catch (Exception e)
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not open log file '" + fn + "'");
			e.printStackTrace();
			return;
		}
	}

	/* (non-Javadoc)
	 * @see windmon.WindDataStore#getWindDataRecords(long, long)
	 */
	public WindDataRecord[] getWindDataRecords(long start, long end) {
		// TODO Auto-generated method stub
		Vector records = new Vector();
		long curr = start;
		
		while (curr <= end)
		{
			String fn = fnameFormat.format(new Date(curr));
			long fileStart = fnameFormat.parse(fn).getTime();
			long fileEnd = fileStart + DATE_INT;
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(path + fn));
				String ln;
				while ( (ln = br.readLine()) != null)
				{
					WindDataRecord rec = WindDataRecord.parse(ln);
					if ( rec != null )
					{
						curr = rec.getEndTime();
						if ( curr > end )
						{
							break;
						}
						else
						{
							records.add(rec);
						}
					}
				}
				// file finished, set current time to end time of file.
			}
			catch (Exception e)
			{
				EventLog.log(EventLog.SEV_WARN, "Could not read log file '" + path + fn + "'");
			}
			curr = fileEnd;
		}
		return null;
	}
}
