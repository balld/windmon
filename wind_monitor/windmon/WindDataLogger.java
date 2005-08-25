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
import java.util.Date;

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

	public WindDataLogger (long interval)
	{
		currentSet = new WindDataLoggerSet();
		currentSet.reset(System.currentTimeMillis());
		timer = new Timer();
		timer.schedule(this, new Date(0), interval);
//		timer.schedule(this, 0, interval);
	}
	/* (non-Javadoc)
	 * @see windmon.WindDataListener#windDataEventReceived(windmon.WindDataEvent)
	 */
	public void windDataEventReceived(WindDataEvent e) {
		if ( currentSet != null)
		{
			synchronized(currentSet)
			{
				currentSet.logData(e.getWindSpeed(),
						           e.getWindAngle());
			}
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
		WindDataRecord rec = lastSet.generateWindDataRecord();
		rec.write(new PrintWriter(System.out));
		System.out.println(rec.toString());
	}
}

