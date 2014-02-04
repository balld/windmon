package windmon;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.jfree.chart.ChartUtilities;

public class WindDataLoggerFile extends TimerTask implements WindDataListener {
  private static final Logger logger = Logger.getLogger(WindDataLoggerFile.class.getName());

  private WindDataLoggerSet currentSet;
  private WindDataLoggerSet lastSet;

  private Timer timer;

  private long analysisInterval; // Hold data in memory (ms)
  private long recordInterval; // Record data at this interval (ms)

  long nextMidnight;

  private GregorianCalendar calendar = null;
  private SimpleDateFormat fnameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
  private SimpleDateFormat labelDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
  private SimpleDateFormat maxWindDateFormat = new SimpleDateFormat("HH:mm z");

  private WindDataRecord dayMax = null;

  private List<WindDataRecord> dataRecords;
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
  private String ftpRemoteNameDial;
  private String ftpRemoteNameSpeed;
  private String ftpRemoteNameAngle;
  private String ftpRemoteNameReport;

  private DecimalFormat df = new DecimalFormat("0.0");
  private DecimalFormat dfc = new DecimalFormat("000");

  // Report Generator
  ReportGenerator rg = new ReportGenerator();

  private FTPTaskQueue ftpQueue = null;




  public WindDataLoggerFile (WindDataPlotter plotter, Ticker ticker, boolean storeDataToFile, FTPTaskQueue ftp)
  {
    this.ftpQueue = ftp;
    readConfig();

    this.plotter = plotter;
    this.ticker = ticker;

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

    List<WindDataRecord> archData = null;
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
      dataRecords = new ArrayList<WindDataRecord>();
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
        /* Delete this record now as it is not within the analysis period */
        dataRecords.remove(i);
      }
      else
      {
        i++;
      }
    }
    WindDataRecord data[] = (WindDataRecord[]) dataRecords.toArray(new WindDataRecord[dataRecords.size()]);

    plotter.plotData( data );
  }

  public void readConfig()
  {
    recordInterval=Config.getParamAsLong("WindLogRecordIntervalSec", 300)*1000;
    analysisInterval=Config.getParamAsLong("WindLogHistorySec", 3600)*1000;
    imageWidth = Config.getParamAsInt("WindLogGraphImageWidth", 600);
    imageHeight = Config.getParamAsInt("WindLogGraphImageHeight", 400);
    dialWidth = Config.getParamAsInt("WindLogDialImageHeight", 400);
    webOutput = Config.getParamAsBoolean("GenerateWebFilesYN", true);
    initTickerText = Config.getParamAsString("InitialTickerText", "WindMonitor (c) David Ball 2006");
    templatePathname = Config.getParamAsString("ReportTemplate");

    if (ftpQueue != null) {
      ftpRemoteNameDial = Config.getParamAsString("FTPRemoteNameDial", "dial.png");
      ftpRemoteNameSpeed = Config.getParamAsString("FTPRemoteNameSpeed", "speed.png");
      ftpRemoteNameAngle = Config.getParamAsString("FTPRemoteNameAngle", "angle.png");
      ftpRemoteNameReport = Config.getParamAsString("FTPRemoteNameReport", "report.html");
    }

    if (webOutput || ftpQueue != null) {
      uploadDir = Config.getParamAsString("WindLogUploadDirectory", "/tmp/");
      Utils.createDirectoryIfNotExists(uploadDir);
    }

    // If timer exists, reset it
    if ( timer != null ) {
      assertTimer(true);
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

    //
    // Periodic plot update
    //
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

      logger.finest("Saved : " + rec);

      // Only store data records going back for the configured period.
      long minTime = lastSet.getEndPeriod() - analysisInterval;
      while ( dataRecords.size() > 0 && ((WindDataRecord)dataRecords.get(0)).getEndTime() < minTime)
      {
        Object ob = dataRecords.remove(0);
        logger.finest("Removed : " + ob);
      }

      WindDataRecord data[] = (WindDataRecord[]) dataRecords.toArray(new WindDataRecord[dataRecords.size()]);

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

      if ( webOutput || ftpQueue != null)
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
          logger.severe("Could not write image '" + dialfname + "'");
        }


        // Generate report from template
        rg.genReport( templatePathname, txtfname);



        if (ftpQueue != null) {
          //
          // GUI to upload files to FTP site.
          //

          // Build all the filenames needed
          String remSpeedfname = fnameDate + "_speed_tmp.png";
          String remAnglefname = fnameDate + "_angle_tmp.png";
          String remDialfname = fnameDate  + "_dialx_tmp.png";
          String remTxtfname = fnameDate   + "_infox_tmp.txt";

          ftpQueue.addTask(FTPTask.createSendTask(dialfname, remDialfname, true));
          ftpQueue.addTask(FTPTask.createSendTask(speedfname, remSpeedfname, true));
          ftpQueue.addTask(FTPTask.createSendTask(anglefname, remAnglefname, true));
          ftpQueue.addTask(FTPTask.createSendTask(txtfname, remTxtfname, false));

          ftpQueue.addTask(FTPTask.createRemoteDeleteTask(ftpRemoteNameDial));
          ftpQueue.addTask(FTPTask.createRemoteDeleteTask(ftpRemoteNameSpeed));
          ftpQueue.addTask(FTPTask.createRemoteDeleteTask(ftpRemoteNameAngle));
          ftpQueue.addTask(FTPTask.createRemoteDeleteTask(ftpRemoteNameReport));

          ftpQueue.addTask(FTPTask.createRemoteRenameTask(remDialfname, ftpRemoteNameDial));
          ftpQueue.addTask(FTPTask.createRemoteRenameTask(remSpeedfname, ftpRemoteNameSpeed));
          ftpQueue.addTask(FTPTask.createRemoteRenameTask(remAnglefname, ftpRemoteNameAngle));
          ftpQueue.addTask(FTPTask.createRemoteRenameTask(remTxtfname, ftpRemoteNameReport));

          // Don't need to keep web files if Web Output parameter is not set.
          if (!webOutput) {
            ftpQueue.addTask(FTPTask.createLocalDeleteTask(dialfname));
            ftpQueue.addTask(FTPTask.createLocalDeleteTask(speedfname));
            ftpQueue.addTask(FTPTask.createLocalDeleteTask(anglefname));
            ftpQueue.addTask(FTPTask.createLocalDeleteTask(txtfname));
          }
        }

        if (webOutput) {
          // And now set the trigger file to indicate files ready for
          // any external application to upload to a website or other
          // (we no longer need them).
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
            ftpQueue.addTask(FTPTask.createLocalRenameTask(tmpname, triggerfname));
          } catch (Exception e) {
            logger.severe(
                "Could not write file '" + triggerfname + "'");
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
    WindDataRecord data[] = (WindDataRecord[]) dataRecords.toArray(new WindDataRecord[dataRecords.size()]);

    plotter.plotData( data );
  }



  /**
   * It timer does not exist, create and start it. If timer does exist
   * reset it (only if reset == true)
   */
  private synchronized void assertTimer(boolean reset) {
    // Start timer on receipt of message
    if ( timer == null || reset)
    {
      long timeNow = System.currentTimeMillis();
      if (timer == null) {
        timer = new Timer();
      }
      timer.schedule(this, new Date(timeNow + recordInterval), recordInterval);
    }
  }
}

