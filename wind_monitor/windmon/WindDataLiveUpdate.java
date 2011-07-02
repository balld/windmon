/*
 * Created on 25-Aug-2005
 */
package windmon;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @author David
 * 
 */
public class WindDataLiveUpdate extends TimerTask implements WindDataListener {

	private WindDataLoggerSet currentSet;
	private WindDataLoggerSet lastSet;
	
	private Vector<WindDataRecord> dataRecords = null;
	
	private Timer timer;
	
    // Config parameters
    private String uploadDir;
    private String ftpRemoteNameLiveUpdate;
    private long ftpLiveUpdateInterval = 0;
    private long ftpLiveUpdateRecordsPerFile = 0;

    private SimpleDateFormat fnameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private DecimalFormat df = new DecimalFormat("0.0");
    private DecimalFormat dfc = new DecimalFormat("000");
    
    private FTPTaskQueue ftpQueue = null;
    
    
    
	
	public WindDataLiveUpdate (FTPTaskQueue ftp)
	{
		readConfig();
		this.ftpQueue = ftp;
		this.dataRecords = new Vector<WindDataRecord>();
	}

	public void readConfig()
	{
    	ftpRemoteNameLiveUpdate = Config.getParamAsString("FTPRemoteNameLiveUpdate", "live.txt");
    	ftpLiveUpdateInterval = Config.getParamAsLong("FTPLiveUpdateIntervalSec", 10)*1000;
    	ftpLiveUpdateRecordsPerFile = Config.getParamAsLong("FTPLiveUpdateRecordsPerFile", 12);
		uploadDir = Config.getParamAsString("WindLogUploadDirectory", "/tmp/");
    	Utils.createDirectoryIfNotExists(uploadDir);
        
		// If timer exists, reset it
		if ( timer != null ) {
			assertTimer(true);
		}
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
        
        if ( currentSet == null) {
			currentSet = new WindDataLoggerSet();
			currentSet.reset(System.currentTimeMillis());
		}

		synchronized(currentSet)
		{
			currentSet.logData(e.getWindSpeed(), e.getWindAngle());
		}

		// Start timer on receipt of message
        assertTimer(false);
	}
	
	public void run()
	{
		long timeNow = System.currentTimeMillis();

		if ( currentSet != null )
		{
            // Minimum synchronised block on currentSet. We copy data,
            // reset and then release it so that we can continue to log
            // incoming readings whilst processing data.
			synchronized(currentSet) {
				currentSet.setEndPeriod(timeNow);
				lastSet = (WindDataLoggerSet) currentSet.clone();
				currentSet.reset(lastSet.getEndPeriod()+1);
			}
			
			// Generate wind data summary record from recorded data
			WindDataRecord rec = lastSet.generateWindDataRecord();

			if ( rec.getNumReadings() > 0 ) {
				// Add records to the set of records.
				dataRecords.add(rec);
            	EventLog.log(EventLog.SEV_DEBUG,
            			     "Adding live update record number " + dataRecords.size() + ". Contains " + rec.getNumReadings() + " readings.");
			}
			
			// If we he defined number of readings. Create and upload the file.
			if (dataRecords.size() >= ftpLiveUpdateRecordsPerFile) {
				String fnameDate = fnameDateFormat.format(new Date(timeNow));
                String localFname = uploadDir + "/" + fnameDate + "_live.csv";
                String remoteFnameTmp = fnameDate + "_live.tmp";

                try	{
                	PrintWriter pw = new PrintWriter(
                			new FileWriter(localFname, false));
                	// Write header
                	pw.println("WEATHER," + 
                			   dataRecords.elementAt(0).getStartTime() + "," +
                			   ftpLiveUpdateInterval);
                	
                	Enumeration<WindDataRecord> e = dataRecords.elements();
                	WindDataRecord r = null;
                	while (e.hasMoreElements()) {
                		r = e.nextElement();
                    	pw.println(df.format(r.getMaxSpeed()) + "," + 
                 			       dfc.format(r.getAveAngle()));
                	}
                	pw.close();
				} catch (Exception e) {
                	EventLog.log(EventLog.SEV_ERROR,
                			"Could not write file '" + localFname + "': " + e.getMessage());
				}

				ftpQueue.addTask(FTPTask.createSendTask(localFname, remoteFnameTmp, false));
            	ftpQueue.addTask(FTPTask.createRemoteDeleteTask(ftpRemoteNameLiveUpdate));
            	ftpQueue.addTask(FTPTask.createRemoteRenameTask(remoteFnameTmp, ftpRemoteNameLiveUpdate));
            	ftpQueue.addTask(FTPTask.createLocalDeleteTask(localFname));
            	
            	dataRecords.removeAllElements();
			}
		}
	}
	
	/**
	 * It timer does not exist, create and start it. If timer does exist
	 * reset it (only if reset == true)
	 */
	private synchronized void assertTimer(boolean reset) {
		// Start timer on receipt of message
		if ( timer == null || reset)
		{
			if (timer == null) {
				timer = new Timer();
			}
			long timeNow = System.currentTimeMillis();
			timer.schedule(this, new Date(timeNow + ftpLiveUpdateInterval), ftpLiveUpdateInterval);
		}
	}
}

