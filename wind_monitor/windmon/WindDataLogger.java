/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.util.Timer;
import java.util.TimerTask;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartUtilities;

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
    
    private WindDataStore store;
    private WindDial dial = new WindDial();

    // Config parameters
    private String imageDir;
    private int imageWidth;
    private int imageHeight;
    
	
	public WindDataLogger (WindDataPlotter plotter)
	{
		readConfig();
		this.plotter = plotter;

		store = new FileWindDataStore();
		long now = System.currentTimeMillis();
		Vector archData = store.getWindDataRecords(now - analysisInterval, now, false);
		if ( archData != null )
		{
			dataRecords = archData;
		}
		else
		{
			dataRecords = new Vector();
		}
	}

	public void readConfig()
	{
		recordInterval=Config.getParamAsLong("WindLogRecordIntervalSec", 10)*1000;
		analysisInterval=Config.getParamAsLong("WindLogHistorySec", 3600)*1000;
		imageDir = Config.getParamAsString("WindLogDataDirectory", "/tmp/");
		imageWidth = Config.getParamAsInt("WindLogGraphImageWidth", 600);
		imageHeight = Config.getParamAsInt("WindLogGraphImageHeight", 400);

		// If timer exists, reset it
		if ( timer != null )
		{
			timer.cancel();
			timer = new Timer();
			timer.schedule(this, new Date(0), recordInterval);
		}
		
		// Reset the dial size
		dial.setSize(new Dimension(imageWidth, imageHeight));
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
		if ( currentSet != null )
		{
			synchronized(currentSet)
			{
				currentSet.setEndPeriod(System.currentTimeMillis());
				lastSet = (WindDataLoggerSet) currentSet.clone();
				currentSet.reset(lastSet.getEndPeriod()+1);
			}
			
			// Add the new record to memory
			WindDataRecord rec = lastSet.generateWindDataRecord();

			// Only add data to display data if at least one reading was received.
			if ( rec.getNumReadings() > 0 )
			{
				dataRecords.add(rec);
			}
			store.storeWindDataRecord(rec);
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
			plotter.writeSpeedPlotPNG(imageDir + "speed.png", imageWidth, imageHeight);
			plotter.writeAnglePlotPNG(imageDir + "angle.png", imageWidth, imageHeight);
			
			dial.setWindAngle(rec.getAveAngle());
			dial.setSpeed(rec.getAveSpeed());
			dial.setWindSpeedHigh(rec.getMaxSpeed());
			dial.setWindSpeedLow(rec.getMinSpeed());
			BufferedImage bdimg = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D bdg = bdimg.createGraphics();
			dial.justPaint(bdg);
			String fname = imageDir + "dial.png";
			File tmpDialFile = new File(fname + ".tmp");
			try
			{
				FileOutputStream os = new FileOutputStream(tmpDialFile, false);
				ChartUtilities.writeBufferedImageAsPNG(os, bdimg);
				os.close();
				File dialFile = new File(fname);
				tmpDialFile.renameTo(dialFile);
				
			}
			catch (Exception e)
			{
				EventLog.log(EventLog.SEV_ERROR, "Could not write image '" + fname + "'");
			}
		}
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
