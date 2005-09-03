/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.util.Timer;
import java.util.TimerTask;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.Vector;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDataLogger extends TimerTask implements WindDataListener {

	private WindDataLoggerSet currentSet;
	private WindDataLoggerSet lastSet;
	private Timer timer;
	
	private long analysisInterval; // Hold data in memory (ms)
    private long recordInterval; // Record data at this interval (ms)
    
    private Vector dataRecords;
    private WindDataPlotter plotter;
	
	public WindDataLogger (WindDataPlotter plotter)
	{
		dataRecords = new Vector();
		this.plotter = plotter;
		recordInterval=Config.getParamAsLong("WindLogRecordIntervalSec", 10)*1000;
		analysisInterval=Config.getParamAsLong("WindLogHistorySec", 3600)*1000;
	}

	/* (non-Javadoc)
	 * @see windmon.WindDataListener#windDataEventReceived(windmon.WindDataEvent)
	 */
	public void windDataEventReceived(WindDataEvent e) {
		if ( currentSet == null)
		{
			currentSet = new WindDataLoggerSet();
			currentSet.reset(System.currentTimeMillis());
		}

		// Start timer on receipt of message
		if ( timer == null )
		{
			timer = new Timer();
			timer.schedule(this, new Date(0), recordInterval);
		}

		synchronized(currentSet)
		{
			currentSet.logData(e.getWindSpeed(),
					e.getWindAngle());
		}
	}
	
	public void run()
	{
		synchronized(currentSet)
		{
			currentSet.setEndPeriod(System.currentTimeMillis());
			lastSet = (WindDataLoggerSet) currentSet.clone();
			currentSet.reset(lastSet.getEndPeriod()+1);
		}

		// Add the new record to memory
		WindDataRecord rec = lastSet.generateWindDataRecord();
		dataRecords.add(rec);
		EventLog.log(EventLog.SEV_DEBUG, "Saved : " + rec);
		
		// Only store data records going back for the configured period.
		long minTime = lastSet.getEndPeriod() - analysisInterval;
		while ( dataRecords.size() > 0 && ((WindDataRecord)dataRecords.get(0)).getEndTime() < minTime)
		{
			Object ob = dataRecords.remove(0);
			EventLog.log(EventLog.SEV_DEBUG, "Removed : " + ob);
		}

		WindDataRecord data[] = (WindDataRecord[]) Array.newInstance(WindDataRecord.class,
				                                                     dataRecords.size());
		dataRecords.copyInto(data);
		plotter.plotData( data );
	}
	/**
	 * @return Returns the plotter.
	 */
	public WindDataPlotter getPlotter() {
		return plotter;
	}
	/**
	 * @param plotter The plotter to set.
	 */
	public void setPlotter(WindDataPlotter plotter) {
		this.plotter = plotter;
	}
}
