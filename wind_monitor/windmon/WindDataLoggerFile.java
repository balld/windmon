/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.Vector;

import org.jfree.chart.ChartUtilities;
import org.jibble.simpleftp.SimpleFTP;
// import org.jibble.simpleftp.simpleftp.SimpleFTP;

/**
 * @author David
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WindDataLoggerFile extends TimerTask implements WindDataListener {

	private WindDataLoggerSet currentSet;
	private WindDataLoggerSet lastSet;
	private Timer timer;
	
	private long analysisInterval; // Hold data in memory (ms)
    private long recordInterval; // Record data at this interval (ms)
    
	long nextMidnight;
	
	private boolean storeDataToFile = true;
    
    private GregorianCalendar calendar = null;
    private SimpleDateFormat fnameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat labelDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
    private SimpleDateFormat maxWindDateFormat = new SimpleDateFormat("HH:mm z");
    
    private WindDataRecord dayMax = null;
    
    private Vector dataRecords;
    private WindDataPlotter plotter;
    private Ticker ticker;
    
    private WindDataStore store;
    private WindDial dial = new WindDial(WindDial.COL_SCHEME_BLUE);

    // Config parameters
    private String uploadDir;
    private int imageWidth;
    private int imageHeight;
    private int dialWidth;
    private boolean webOutput = true;
    private String initTickerText;
    private String templatePathname;
    private boolean ftpUpload = false;
    private String ftpHost;
    private String ftpUser;
    private String ftpPassword;
    private String ftpRemoteDirectory;
    private String ftpRemoteNameDial;
    private String ftpRemoteNameSpeed;
    private String ftpRemoteNameAngle;
    private String ftpRemoteNameReport;


    DecimalFormat df = new DecimalFormat("0.0");
    DecimalFormat dfc = new DecimalFormat("000");
    
    // Report Generator
    ReportGenerator rg = new ReportGenerator();
    
	
	public WindDataLoggerFile (WindDataPlotter plotter, Ticker ticker, boolean storeDataToFile)
	{
		readConfig();
		this.plotter = plotter;
        this.ticker = ticker;
        this.storeDataToFile = storeDataToFile;
        
        if ( ticker != null )
        {
        	ticker.setText(this, initTickerText);
        }
        
        if ( storeDataToFile )
        {
        	store = new WindDataStoreFile();
        }
        else
        {
        	store = null;
        }
		
		/* Create new calendar with current time */
		calendar = new GregorianCalendar();

		/* Get current millisecond time */
		long now = calendar.getTimeInMillis();
		
		/* Set calendar to last midnight (start of today) */
		calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		calendar.set(GregorianCalendar.MINUTE, 0);
		calendar.set(GregorianCalendar.SECOND, 0);
		calendar.set(GregorianCalendar.MILLISECOND, 0);
		long lastMidnight = calendar.getTimeInMillis();
		
		/* Advance calendar to end of today (start of tomorrow) */
		/* GregorianCalendar.add() automatically handles rollover to next year */
		calendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
		nextMidnight = calendar.getTimeInMillis();
		
		/* Period of data required for display is determined by analysisInterval */
		long analysisStart = now - analysisInterval;
		
		/* Fetch data back to the earlier of last midnight or analysis start */
		long dataFetchStart = Math.min(lastMidnight, analysisStart);

		Vector archData = null;
		if ( store != null )
		{
			archData = store.getWindDataRecords(dataFetchStart, now, false);
		}
		
		if ( archData != null )
		{
			dataRecords = archData;
		}
		else
		{
			dataRecords = new Vector();
		}
		
		int i = 0;
        while ( i < dataRecords.size() )
		{
			WindDataRecord rec = (WindDataRecord) dataRecords.get(i);
			if ( dayMax == null || rec.getMaxSpeed() > dayMax.getMaxSpeed() )
			{
				dayMax = rec;
			}
			if ( rec.getEndTime() < analysisStart )
			{
				/*
				 * Delete this record now as it is not within the analysis
				 * period
				 */
				dataRecords.remove(i);
			}
            else
            {
                i++;
            }
		}
        WindDataRecord data[] = (WindDataRecord[]) Array.newInstance(WindDataRecord.class,
                dataRecords.size());
        dataRecords.copyInto(data);

        plotter.plotData( data );
	}

	public void readConfig()
	{
		recordInterval=Config.getParamAsLong("WindLogRecordIntervalSec", 10)*1000;
		analysisInterval=Config.getParamAsLong("WindLogHistorySec", 3600)*1000;
		imageWidth = Config.getParamAsInt("WindLogGraphImageWidth", 600);
		imageHeight = Config.getParamAsInt("WindLogGraphImageHeight", 400);
        dialWidth = Config.getParamAsInt("WindLogDialImageHeight", 400);
        webOutput = Config.getParamAsBoolean("GenerateWebFilesYN", true);
        initTickerText = Config.getParamAsString("InitialTickerText", "WindMonitor (c) David Ball 2006");
        templatePathname = Config.getParamAsString("ReportTemplate");
        
        ftpUpload = Config.getParamAsBoolean("FTPUploadToWebYN", false);
        if (ftpUpload) {
        	// FTP fields are mandatory if FTP upload is enabled.
        	ftpHost = Config.getParamAsString("FTPHost");
        	ftpUser = Config.getParamAsString("FTPUser");
        	ftpPassword = Config.getParamAsString("FTPPassword");
        	ftpRemoteDirectory = Config.getParamAsString("FTPRemoteDirectory", ".");
            ftpRemoteNameDial = Config.getParamAsString("FTPRemoteNameDial", "dial.png");
            ftpRemoteNameSpeed = Config.getParamAsString("FTPRemoteNameSpeed", "speed.png");
            ftpRemoteNameAngle = Config.getParamAsString("FTPRemoteNameAngle", "angle.png");
            ftpRemoteNameReport = Config.getParamAsString("FTPRemoteNameReport", "report.html");
        }
        
        if (webOutput || ftpUpload) {
    		uploadDir = Config.getParamAsString("WindLogUploadDirectory", "/tmp/");
        	Utils.createDirectoryIfNotExists(uploadDir);
        }

		if ( webOutput == true )
		{
			File path = new File(uploadDir);
			if ( path.exists())
			{
				if ( !path.isDirectory() )
				{
					EventLog.log(EventLog.SEV_FATAL, "Upload directory '" + uploadDir + "' exists but is not a directory");
				}
			}
			else
			{
				if ( path.mkdirs() != true )
				{
					EventLog.log(EventLog.SEV_FATAL, "Upload directory '" + uploadDir + "' could not be created");
				}
				else
				{
					EventLog.log(EventLog.SEV_INFO, "Upload directory '" + uploadDir + "' created");
				}
			}
		}
        
        
		// If timer exists, reset it
		if ( timer != null )
		{
			timer.cancel();
			timer = new Timer();
            timer.schedule(this,
                           new Date(System.currentTimeMillis() + recordInterval),
                           recordInterval);
		}
		
		// Reset the dial size
		dial.setSize(new Dimension(dialWidth, dialWidth));
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see windmon.WindDataListener#windDataEventReceived(windmon.WindDataEvent)
	 */
	public void windDataEventReceived(WindDataEvent e) {
	    // Ignore negative readings. They indicate no signal
        if ( e.getWindAngle() < 0 || e.getWindSpeed() < 0)
        {
            return;
        }
        
        if ( currentSet == null)
		{
			currentSet = new WindDataLoggerSet();
			currentSet.reset(System.currentTimeMillis());
		}

		// Start timer on receipt of message
		if ( timer == null )
		{
			timer = new Timer();
            timer.schedule(this,
                    new Date(System.currentTimeMillis() + recordInterval),
                    recordInterval);
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
            // Minimum synchronised block on currentSet. We copy data,
            // reset and then release it so that we can continue to log
            // incoming readings whilst we processing data.
			synchronized(currentSet)
			{
				currentSet.setEndPeriod(System.currentTimeMillis());
				lastSet = (WindDataLoggerSet) currentSet.clone();
				currentSet.reset(lastSet.getEndPeriod()+1);
			}
			
			// Generate wind data summary record from recorded data
			WindDataRecord rec = lastSet.generateWindDataRecord();
			
			// Have we rolled over into tomorrow?
			if ( rec.getEndTime() > nextMidnight )
			{
				dayMax = rec;
				// Find next midnight. While loop just in case we've fallen more
				// than
				// one day behind.
				while ( nextMidnight < rec.getEndTime() )
				{
					calendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
					nextMidnight = calendar.getTimeInMillis();
				}
			}
			// Else check for new max today
			else if ( dayMax == null || rec.getMaxSpeed() > dayMax.getMaxSpeed())
			{
				dayMax = rec;
			}

			// Only add data to display data if at least one reading was
			// received.
			if ( rec.getNumReadings() > 0 )
			{
				dataRecords.add(rec);
			}

			// Store data to file if using file store.
			if ( store != null )
			{
				store.storeWindDataRecord(rec);
			}
			
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

			long actualRecordIntervalMilli = rec.getEndTime() - rec.getStartTime();
			long actualRecordInterval = Math.round(((double)actualRecordIntervalMilli)/1000.0);
			long actualRecordIntervalMins =  Math.round(((double)actualRecordInterval) / 60.0);
			
            // Format various date/times for output
            String fnameDate = fnameDateFormat.format(new Date(rec.getEndTime()));
            String maxWindDate = maxWindDateFormat.format(new Date(dayMax.getEndTime()));
			String labelDate = labelDateFormat.format(new Date(rec.getEndTime()));

            // Set ticker text
            if ( ticker != null )
            {
    			String actualRecordIntervalStr = null;
    			if ( actualRecordInterval < 59 )
    				actualRecordIntervalStr = actualRecordInterval + " second measurement";
    			else
    				actualRecordIntervalStr = actualRecordIntervalMins + " minute measurement";

    			ticker.setText(this, 
    					labelDate + " (" + actualRecordIntervalStr + ")   " +
    					"Mean Direction : " + dfc.format(rec.getAveAngle()) + " (" + Utils.angleToCompass(rec.getAveAngle()) + ")  " +
    					"Average Speed : " + df.format(rec.getAveSpeed()) + " knots (F" + Utils.speedToBeaufort(rec.getAveSpeed()) + ")  " +
    					"Gust : " + df.format(rec.getMaxSpeed()) + " knots (F" + Utils.speedToBeaufort(rec.getMaxSpeed()) + ")  " +
    					"Today's peak windspeed " + df.format(dayMax.getMaxSpeed()) + " knots (F" + Utils.speedToBeaufort(dayMax.getMaxSpeed()) + ") recorded at " + maxWindDate);
            }
			
            if ( webOutput || ftpUpload)
            {
                // Draw dial image to bdg
                dial.setWindAngle(rec.getAveAngle());
                dial.setSpeed(rec.getAveSpeed());
                dial.setWindSpeedHigh(rec.getMaxSpeed());
                dial.setWindSpeedLow(rec.getMinSpeed());
                BufferedImage bdimg = new BufferedImage(dialWidth, dialWidth, BufferedImage.TYPE_INT_RGB);
                Graphics2D bdg = bdimg.createGraphics();
                dial.justPaint(bdg);

                // Set report values
                rg.setValue( ReportGenerator.REPORT_DTM, labelDate); 
                rg.setValue( ReportGenerator.INTERVAL_SEC, "" + actualRecordInterval );
                rg.setValue( ReportGenerator.INTERVAL_MIN, "" + actualRecordIntervalMins );
                rg.setValue( ReportGenerator.DIR_DEG, dfc.format(rec.getAveAngle()));
                rg.setValue( ReportGenerator.DIR_COMP, Utils.angleToCompass(rec.getAveAngle()));
                rg.setValue( ReportGenerator.AVE_SPEED_KTS, df.format(rec.getAveSpeed()));
                rg.setValue( ReportGenerator.AVE_SPEED_BFT, Utils.speedToBeaufort(rec.getAveSpeed()));
                rg.setValue( ReportGenerator.MIN_SPEED_KTS, df.format(rec.getMinSpeed()));
                rg.setValue( ReportGenerator.MIN_SPEED_BFT, Utils.speedToBeaufort(rec.getMinSpeed()));
                rg.setValue( ReportGenerator.MAX_SPEED_KTS, df.format(rec.getMaxSpeed()));
                rg.setValue( ReportGenerator.MAX_SPEED_BFT, Utils.speedToBeaufort(rec.getMaxSpeed()));
                rg.setValue( ReportGenerator.DAY_PEAK_KTS, df.format(dayMax.getMaxSpeed()));
                rg.setValue( ReportGenerator.DAY_PEAK_BFT, Utils.speedToBeaufort(dayMax.getMaxSpeed()));
                rg.setValue( ReportGenerator.DAY_PEAK_TM, maxWindDate);

                // Build all the filenames needed
                String speedfname = uploadDir + "/" + fnameDate + "_speed.png";
                String anglefname = uploadDir + "/" + fnameDate + "_angle.png";
                String dialfname = uploadDir + "/" + fnameDate  + "_dialx.png";
                String txtfname = uploadDir + "/" + fnameDate   + "_infox.txt";
                String triggerfname = uploadDir + "/" + fnameDate  + "_trigr.rdy";

                // Graphs are easy thanks to JFreeChart
                plotter.writeSpeedPlotPNG(speedfname, imageWidth, imageHeight);
                plotter.writeAnglePlotPNG(anglefname, imageWidth, imageHeight);

                File dialFile = new File(dialfname);
                try
                {
                    FileOutputStream os = new FileOutputStream(dialFile, false);
                    ChartUtilities.writeBufferedImageAsPNG(os, bdimg);
                    os.close();
                }
                catch (Exception e)
                {
                    EventLog.log(EventLog.SEV_ERROR, "Could not write image '" + dialfname + "'");
                }
                

                // Generate report from template
                rg.genReport( templatePathname, txtfname);
                
                
                // And now set the trigger file to indicate files ready for
				// upload
                try
				{
                	String tmpname = triggerfname + ".tmp";
                	PrintWriter pw = new PrintWriter(
                			new FileWriter(triggerfname, false));
                	pw.println(speedfname);
                	pw.println(anglefname);
                	pw.println(dialfname);
                	pw.println(txtfname);
                	pw.close();
                	
                	File tmpFile = new File(tmpname);
                	File triggerFile = new File(triggerfname);
                	// This rename is atomic action which indicates all files
					// are ready
                	tmpFile.renameTo(triggerFile);
				}
                catch (Exception e)
				{
                	EventLog.log(EventLog.SEV_ERROR,
                			"Could not write file '" + triggerfname + "'");
				}
                
                if (ftpUpload) {
                	//
                	// GUI to upload files to FTP site.
                	//
                	
                	// Build all the filenames needed
                	String remSpeedfname = fnameDate + "_speed_tmp.png";
                	String remAnglefname = fnameDate + "_angle_tmp.png";
                	String remDialfname = fnameDate  + "_dialx_tmp.png";
                	String remTxtfname = fnameDate   + "_infox_tmp.txt";
                	
                	// Open FTP Connection
                	SimpleFTP ftp = ftpConnect();
                	
                	if (ftp != null) {
                		ftpSendFile(ftp, dialfname, remDialfname);
                		ftpSendFile(ftp, speedfname, remSpeedfname);
                		ftpSendFile(ftp, anglefname, remAnglefname);
                		ftpSendFile(ftp, txtfname, remTxtfname);

                		ftpDeleteFile(ftp, ftpRemoteNameDial);
                		ftpDeleteFile(ftp, ftpRemoteNameSpeed);
                		ftpDeleteFile(ftp, ftpRemoteNameAngle);
                		ftpDeleteFile(ftp, ftpRemoteNameReport);

                		ftpRenameFile(ftp, remDialfname, ftpRemoteNameDial);
                		ftpRenameFile(ftp, remSpeedfname, ftpRemoteNameSpeed);
                		ftpRenameFile(ftp, remAnglefname, ftpRemoteNameAngle);
                		ftpRenameFile(ftp, remTxtfname, ftpRemoteNameReport);
                		
                		ftpDisconnect(ftp);
                	}
                		
            		// Don't need to keep web files if Web Output parameter is not set.
                	if (!webOutput) {
                		localDeleteFile(dialfname);
                		localDeleteFile(speedfname);
                		localDeleteFile(anglefname);
                		localDeleteFile(txtfname);
                		localDeleteFile(triggerfname);
                	}
                }
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
	 * @param plotter
	 *            The plotter to set.
	 */
	public void setPlotter(WindDataPlotter plotter) {
		this.plotter = plotter;
        WindDataRecord data[] = (WindDataRecord[]) Array.newInstance(WindDataRecord.class,
                dataRecords.size());
        dataRecords.copyInto(data);

        plotter.plotData( data );
	}
	
	
	private SimpleFTP ftpConnect() {
    	SimpleFTP ftp = new SimpleFTP();
    	boolean connected = false;
    	try {
    		ftp.connect(ftpHost, 21, ftpUser, ftpPassword);
    		connected = true;
    		
    		if(!ftp.cwd(ftpRemoteDirectory)) {
    			throw new IOException("Could not change to remote directory '" + ftpRemoteDirectory + "'.");
    		}

    		if(!ftp.bin()) {
    			throw new IOException("Could not change to binary mode.");
    		}

    		EventLog.log(EventLog.SEV_INFO, "Opened FTP connection to '" + 
    				ftpHost + "' directory '" + ftpRemoteDirectory + "' as user '" + ftpUser +"'.");
    	} catch (Exception e) {
    		EventLog.log(EventLog.SEV_ERROR, "Could not open FTP connection to '" + 
    				ftpHost + "' as user '" + ftpUser +"': " + e.getMessage());
    		if (connected) {
    			try {
    				ftp.disconnect();
    				connected = false;
    			} catch (Exception e2) {
    			}
    		}
    	}
    	
    	if (connected) {
    		return ftp;
    	} else {
    		return null;
    	}
	}
	
	private static void ftpDisconnect(SimpleFTP ftp) {
		try {
			ftp.disconnect();
		}
		catch (Exception e) {
        	EventLog.log(EventLog.SEV_WARN, "Failed FTP disconnect: " + e.getMessage()); 
		}
	}
	
	private static void ftpSendFile(SimpleFTP ftp, String localName, String remoteName) {
		boolean success = false;
		try {
			success = ftp.stor(new FileInputStream(new File(localName)), remoteName);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		
        if (!success) {
        	EventLog.log(EventLog.SEV_WARN, "Failed FTP transfer local file '" + 
        			localName + "' to remote file '" + remoteName + "'");
        } else {
        	EventLog.log(EventLog.SEV_INFO, "Successful FTP transfer local file '" + 
        			localName + "' to remote file '" + remoteName + "'");
        }
	}
	
	private static void ftpDeleteFile(SimpleFTP ftp, String remoteName) {
		try {
			ftp.delete(remoteName);
        	EventLog.log(EventLog.SEV_INFO,
					"Successful FTP delete remote file '" + remoteName + "'");
		} catch (Exception e) {
        	EventLog.log(EventLog.SEV_WARN, "Failed FTP delete remote file '" + 
        			remoteName + "': " + e.getMessage());
		}
	}

	private static void ftpRenameFile(SimpleFTP ftp, String oldName, String newName) {
		try {
			ftp.rename(oldName, newName);
        	EventLog.log(EventLog.SEV_INFO, "Successful FTP rename remote file from '" + 
        			oldName + "' to '" + newName + "'");
		} catch (Exception e) {
        	EventLog.log(EventLog.SEV_WARN, "Failed FTP rename remote file from '" + 
        			oldName + "' to '" + newName + "': " + e.getMessage());
		}
	}

	private static void localDeleteFile(String fname)
	{
		try {
			File f = new File(fname);
			if (!f.delete()) {
				throw new IOException("Delete Failed.");
			}
        	EventLog.log(EventLog.SEV_INFO, "Deleted local file '" + 
        			fname + "'.");
		} catch (Exception e) {
        	EventLog.log(EventLog.SEV_WARN, "Failed delete local file '" + 
        			fname + "': " + e.getMessage());
		}
	}
}

