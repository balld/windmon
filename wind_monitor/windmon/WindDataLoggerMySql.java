package windmon;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.jfree.chart.ChartUtilities;

public class WindDataLoggerMySql extends TimerTask {
	
	private static final Logger logger = Logger.getLogger(WindDataLoggerMySql.class.getName());

	private Timer timer;

	private long analysisInterval; // Hold data in memory (ms)
	private long recordInterval; // Data is recorded in MySQL by external process at this interval (ms)
	private long dbPollInterval; // This logger checks the MySql database at this interval for new data.

	long lastUpdateDTM = -1;

	private static SimpleDateFormat fnameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static SimpleDateFormat labelDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
	private static SimpleDateFormat maxWindDateFormat = new SimpleDateFormat("HH:mm z");
	private static SimpleDateFormat mySqlDTMFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat mySqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private WindDataRecord dayMax = null;

	private List<WindDataRecord> dataRecords;
	private WindDataPlotter plotter;
	private Ticker ticker;

	private WindDial dial = new WindDial(WindDial.COL_SCHEME_BLUE);

	// Config parameters
	private String uploadDir;
	private int imageWidth;
	private int imageHeight;
	private int dialWidth;
	private boolean webOutput = true;
	private String initTickerText;
	private String templatePathname;
	private String dbUser;
	private String db;
	private String dbHost;
	private String dbPassword;

	//
	// MySql/JDBC Variables
	//

	//  The JDBC Connector Class.
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private Connection con = null;


	DecimalFormat df = new DecimalFormat("0.0");
	DecimalFormat dfc = new DecimalFormat("000");

	// Report Generator
	ReportGenerator rg = new ReportGenerator();


	public WindDataLoggerMySql (WindDataPlotter plotter, Ticker ticker)
	{
		this.plotter = plotter;
		this.ticker = ticker;
		
		if ( ticker != null )
		{
			ticker.setText(this, initTickerText);
		}

		// Class.forName(xxx) loads the jdbc classes and
		// creates a drivermanager class factory
		try
		{
			Class.forName(dbClassName);
		}
		catch (ClassNotFoundException e)
		{
			logger.severe("Could not load database driver class '" + dbClassName + "'");
			System.exit(1);
		}

		// Read config - also connects to database and starts update timer
		readConfig();


	}

	public void readConfig()
	{
		recordInterval=Config.getParamAsLong("WindLogRecordIntervalSec", 10)*1000;

		// Poll DB for new data at configured interval. Default is 1/10th of the 
		// interval at which new data should be recorded in the database by the
		// external process.
		dbPollInterval=Config.getParamAsLong("DBPollIntervalSec", recordInterval/(10*1000))*1000;

		analysisInterval=Config.getParamAsLong("WindLogHistorySec", 3600)*1000;
		uploadDir = Config.getParamAsString("WindLogUploadDirectory");
		imageWidth = Config.getParamAsInt("WindLogGraphImageWidth", 600);
		imageHeight = Config.getParamAsInt("WindLogGraphImageHeight", 400);
		dialWidth = Config.getParamAsInt("WindLogDialImageHeight", 400);
		webOutput = Config.getParamAsBoolean("GenerateWebFilesYN", true);
		initTickerText = Config.getParamAsString("InitialTickerText", "WindMonitor (c) David Ball 2006");
		templatePathname = Config.getParamAsString("ReportTemplate");

		dbUser     = Config.getParamAsString("DBUser", "windmon");
		db         = Config.getParamAsString("DB", "windmon");
		dbHost     = Config.getParamAsString("DBHost", "localhost");
		dbPassword = Config.getParamAsString("DBPassword");

		if ( webOutput == true )
		{
			File path = new File(uploadDir);
			if ( path.exists())
			{
				if ( !path.isDirectory() )
				{
					logger.severe("Upload directory '" + uploadDir + "' exists but is not a directory");
					System.exit(1);
				}
			}
			else
			{
				if ( path.mkdirs() != true )
				{
					logger.severe("Upload directory '" + uploadDir + "' could not be created");
					System.exit(1);
				}
				else
				{
					logger.info("Upload directory '" + uploadDir + "' created");
				}
			}
		}

		// Reset the dial size
		dial.setSize(new Dimension(dialWidth, dialWidth));

		// Stop and/or restart the timer
		startTimer();

		// Connect/reconnect DB
		connectDB();

	}

	private void startTimer()
	{
		// If timer exists, reset it
		if ( timer != null )
		{
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(this,
				new Date(System.currentTimeMillis() + dbPollInterval),
				dbPollInterval);
	}

	private void connectDB()
	{
		// Reset database connection
		if ( con != null )
		{
			try
			{
				con.close();
			}
			catch ( SQLException e) {
			  // Ignore
			}
		}
		String connectionString =	"jdbc:mysql://" + dbHost + "/" + db;

		Properties p = new Properties();
		p.put("user",dbUser);
		p.put("password",dbPassword);

		// Now try to connect
		try
		{
			con = DriverManager.getConnection(connectionString,p);
		}
		catch (SQLException e)
		{
			logger.severe("Unable to connect database '" + connectionString  + " " + e);
			con = null;
		}
	}

	long selectDBUpdateDTM()
	{
		long dtm = -1;

		try
		{
			Statement s = con.createStatement();
			s.executeQuery ("SELECT update_dtm FROM update_log where id = 'wind_data_record'");
			ResultSet rs = s.getResultSet ();
			if ( rs.next())
			{
				dtm = mySqlDTMFormat.parse(rs.getString(1)).getTime();
			}
			else
			{
				logger.severe("No data selecting update date/time from database.");
				return -1;
			}
			rs.close ();
			s.close ();
		} catch ( SQLException e)
		{
			logger.severe("Exception selecting update date/time from database. " + e);
			return -1;
		}
		catch ( ParseException e)
		{
			logger.severe("Exception parsing update date/time from database. " + e);
			return -1;
		}

		return ( dtm );
	}

	static WindDataRecord windDataRecordFromResultSet(ResultSet rs)
	{
		WindDataRecord rec = new WindDataRecord();
		try
		{
//			String startTime = mySqlDTMFormat.format(rs.getTime("start_dtm").getTime());
//			String endTime = mySqlDTMFormat.format(rs.getTime("end_dtm").getTime());
//			String startTime = rs.getString("start_dtm");
//			String endTime = rs.getString("end_dtm");
			
			rec.setStartTime(rs.getTimestamp("start_dtm").getTime());
			rec.setEndTime(rs.getTimestamp("end_dtm").getTime());
			rec.setNumReadings(rs.getInt("reading_count"));
			rec.setMinSpeed(rs.getFloat("min_speed"));
			rec.setAveSpeed(rs.getFloat("ave_speed"));
			rec.setMaxSpeed(rs.getFloat("max_speed"));
			rec.setAveAngle(rs.getFloat("Ave_angle"));
		} catch ( SQLException e)
		{
			logger.severe("Exception building wind data record from database result set. " + e);
			return null;
		}
		return rec;
	}

	
	WindDataRecord selectDayMax(long t)
	{
		String lastMidnight = mySqlDateFormat.format(Long.valueOf(t)) + " 00:00:00";
		WindDataRecord rec = null;

		try
		{
			Statement s = con.createStatement();
			s.setMaxRows(1);
			s.executeQuery ("SELECT * FROM wind_data_record " +
					        "where start_dtm >= '" + lastMidnight + "' " +
					        "order by max_speed desc");
			ResultSet rs = s.getResultSet ();
			if ( rs.next())
			{
				rec = windDataRecordFromResultSet(rs);
			}
			else
			{
				logger.severe("No data selecting day max from database.");
				return null;
			}
			rs.close ();
			s.close ();
		} catch ( SQLException e)
		{
			logger.severe("Exception selecting day max from database. " + e);
			return null;
		}

		return rec;
	}

	WindDataRecord[] selectWindData(long from)
	{
		List<WindDataRecord> dataVec = new ArrayList<WindDataRecord>();
		
		String fromDTM = mySqlDTMFormat.format(new Date(from));

		try
		{
			Statement s = con.createStatement();
			s.setMaxRows(0);
			s.executeQuery ("SELECT * FROM wind_data_record " +
					        "where start_dtm >= '" + fromDTM + "' " +
					        "order by start_dtm asc");
			ResultSet rs = s.getResultSet ();
			while ( rs.next())
			{
				dataVec.add(windDataRecordFromResultSet(rs));
			}
			rs.close ();
			s.close ();
		} catch ( SQLException e)
		{
			logger.severe("Exception selecting wind plot data. " + e);
			return null;
		}

		WindDataRecord data[] = (WindDataRecord[]) dataVec.toArray(new WindDataRecord[dataVec.size()]);
		
		return data;
	}
	
	public void run()
	{
		// If connection is null try to connect, else skip
		if ( con == null )
		{
			connectDB();
			if ( con == null )
			{
				logger.severe("Database not connected. Can not plot data");
				return;
			}
		}
		

		// Get the update date/time from the DB and see if anything has changed
		// since wind data was last plotted
		long dbUpdateDTM = selectDBUpdateDTM();
		if ( dbUpdateDTM <= lastUpdateDTM )
		{
			// No change, so return
			return;
		}

		// Work out start of time period
		long currentTime = System.currentTimeMillis();
		long minTime = currentTime - analysisInterval;

		/* Populate WindDataRecord data[] with data from DB */
		WindDataRecord data[] = selectWindData(minTime);
		if ( data.length == 0 )
		{
			// No data, so exit
			logger.severe("No data fromdatabase in range to plot.");
			return;
		}

		/* Copy latest (by datetime) record into WindataRecord rec */
		WindDataRecord rec = data[data.length-1];  

		long actualRecordIntervalMilli = rec.getEndTime() - rec.getStartTime();
		long actualRecordInterval = Math.round(((double)actualRecordIntervalMilli)/1000.0);
		long actualRecordIntervalMins =  Math.round(((double)actualRecordInterval) / 60.0);
		
		// Get the day maximum
		if ( (dayMax = selectDayMax(rec.getStartTime())) == null )
		{
			// No max record
			logger.severe("Failed to get max wind data. Skipping plot.");
			return;
		}

		lastUpdateDTM = dbUpdateDTM;

		/* Plot data on the screen */
		plotter.plotData( data );

		// Format various date/times for output
		String fnameDate = fnameDateFormat.format(new Date(rec.getEndTime()));
		String labelDate = labelDateFormat.format(new Date(rec.getEndTime()));
		String maxWindDate = maxWindDateFormat.format(new Date(dayMax.getEndTime()));

		/* If web upload files are to be produced ... */
		if ( webOutput )
		{
			//
			// Now write data to files for upload to database
			//

			// Build all the filenames needed
			String speedfname = uploadDir + "/" + fnameDate + "_speed.png";
			String anglefname = uploadDir + "/" + fnameDate + "_angle.png";
			String dialfname = uploadDir + "/" + fnameDate  + "_dialx.png";
			String txtfname = uploadDir + "/" + fnameDate   + "_infox.txt";
			String triggerfname = uploadDir + "/" + fnameDate  + "_trigr.rdy";

			// Render speed and angle charts to image files (PNG) for web upload
			plotter.writeSpeedPlotPNG(uploadDir + "/" + fnameDate + "_speed.png",
					imageWidth, imageHeight);
			plotter.writeAnglePlotPNG(uploadDir + "/" + fnameDate + "_angle.png",
					imageWidth, imageHeight);

			// Render wind speed dial for web upload
			dial.setWindAngle(    rec.getAveAngle());
			dial.setSpeed(        rec.getAveSpeed());
			dial.setWindSpeedHigh(rec.getMaxSpeed());
			dial.setWindSpeedLow( rec.getMinSpeed());
			BufferedImage bdimg = new BufferedImage(dialWidth, dialWidth, BufferedImage.TYPE_INT_RGB);
			Graphics2D bdg = bdimg.createGraphics();
			dial.justPaint(bdg);

			/* Write dial image to PNG image file for web upload */
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

			// Set report values - these are substituted into the upload report template
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

			// Generate report for upload using the template file and values set above
			rg.genReport( templatePathname, txtfname);


			// And now set the trigger file to indicate that files ready for web upload
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
				// This rename is atomic action which indicates all files are ready
				tmpFile.renameTo(triggerFile);
			}
			catch (Exception e)
			{
				logger.severe(
						"Could not write file '" + triggerfname + "'");
			}
		}

		// Set ticker text on the graphical display
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
		WindDataRecord data[] = (WindDataRecord[]) dataRecords.toArray(new WindDataRecord[dataRecords.size()]);

		plotter.plotData( data );
	}
}
