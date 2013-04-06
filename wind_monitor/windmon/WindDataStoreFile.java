package windmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WindDataStoreFile implements WindDataStore {
	// These settings affect the number of files created. Currently every hour.
	private static final String DATE_FMT = "yyyyMMddHHz";
	private static final long   FILE_INTERVAL = 3600000; // 1 hour
	
	DateFormat fnameFormat = new SimpleDateFormat("'windlog_'"
			                                      + DATE_FMT + "'.dat'");
	String path = null;
	
	public WindDataStoreFile()
	{
		path = Config.getParamAsString("WindLogDataDirectory");
		File fpath = new File(path);
		if ( fpath.exists())
		{
			if ( !fpath.isDirectory() )
			{
				EventLog.log(EventLog.SEV_FATAL, "Log directory '" + path + "' exists but is not a directory");
			}
		}
		else
		{
			if ( fpath.mkdirs() != true )
			{
				EventLog.log(EventLog.SEV_FATAL, "Log directory '" + path + "' could not be created");
			}
			else
			{
				EventLog.log(EventLog.SEV_INFO, "Log directory '" + path + "' created");
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see windmon.WindDataStore#storeWindDataRecord(windmon.WindDataRecord)
	 */
	public void storeWindDataRecord(WindDataRecord record) {
		String fn = fnameFormat.format(new Date(record.getEndTime()));
		
		try
		{
			PrintWriter pw = new PrintWriter(new FileWriter(path + "/" + fn, true));
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

	public List<WindDataRecord> getWindDataRecords(long start, long end)
	{
		return getWindDataRecords(start, end, true);
	}

	/* (non-Javadoc)
	 * @see windmon.WindDataStore#getWindDataRecords(long, long)
	 */
	public List<WindDataRecord> getWindDataRecords(long start, long end, boolean includeNull) {
		List<WindDataRecord> records = new ArrayList<WindDataRecord>();
		long curr = start;
		
		while (curr <= end)
		{
				String fn = fnameFormat.format(new Date(curr));
				long fileStart = 0;
				try
				{
					fileStart = fnameFormat.parse(fn).getTime();
				}
				catch (Exception e)
				{
					EventLog.log(EventLog.SEV_ERROR, "Could not build filename");
					return null;
				}
				long fileEnd = fileStart + FILE_INTERVAL;
				BufferedReader br = null;
				try
				{
					br = new BufferedReader(new FileReader(path + "/" + fn));
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
							else if ( curr < start )
							{
								continue;
							}
							else if ( includeNull == false && rec.getNumReadings() == 0 )
							{
								continue;
							}
							else
							{
								records.add(rec);
							}
						}
					}
				}
			catch (Exception e)
			{
				EventLog.log(EventLog.SEV_WARN, "Could not read log file '" + path + "/" + fn + "'");
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}
			// file finished, set current time to end time of file.
			curr = fileEnd;
		}
		
		return records;
	}
}
