package windmon;

import java.util.logging.Logger;

public class WindMonitorLogic {
  Logger logger = Logger.getLogger(WindMonitorLogic.class.getName());
  
  private FTPTaskQueue ftpQueue = null;
  NMEAController nmea = null;

  public WindMonitorLogic() {
    // Empty
  }
  
  public void init(WindDataPlotter plotter, Ticker ticker) {
    
    //
    // Set up the wind data connection
    //
    String connectionType = Config.getParamAsString("ConnectionType",
        "serial");
    NMEALink link = null;
    if ( connectionType.compareToIgnoreCase("socket") == 0 )
    {
      link = new NMEALinkSocket(
          Config.getParamAsString("SocketConnectionHost", "localhost"),
          Config.getParamAsInt("SocketConnectionPort", 2468));
    }
    else if ( connectionType.compareToIgnoreCase("serial") == 0 )
    {
      link = new NMEALinkSerial();
    }
    else if ( connectionType.compareToIgnoreCase("dummy") == 0 )
    {
      link = new NMEALinkStub();
    }
    else
    {
      logger.info("Unrecognised connection type '" +
          connectionType + "'. Using stub");
      link = new NMEALinkStub();
    }

    NMEASocketServer ss = null;
    if (Config.getParamAsBoolean("SocketServerEnabledYN", false)) {
      ss = new NMEASocketServer(Config.getParamAsInt("SocketServerPort", 2468));
    }
    nmea = NMEAController.getCreateInstance(link, ss);


    boolean ftpUpload = Config.getParamAsBoolean("FTPUploadToWebYN", false);
    boolean ftpLiveUpdate = Config.getParamAsBoolean("FTPLiveUpdateYN", false);
    String ftpHost = null;
    String ftpUser = null;
    String ftpPassword = null;
    String ftpRemoteDirectory = null;

    if (ftpUpload || ftpLiveUpdate) {
      // FTP fields are mandatory if FTP upload is enabled.
      ftpHost = Config.getParamAsString("FTPHost");
      ftpUser = Config.getParamAsString("FTPUser");
      ftpPassword = Config.getParamAsString("FTPPassword");
      ftpRemoteDirectory = Config.getParamAsString("FTPRemoteDirectory", ".");
      this.ftpQueue = new FTPTaskQueue(ftpHost, ftpUser, ftpPassword, ftpRemoteDirectory);
    }

    if (ftpLiveUpdate) {
      WindDataLiveUpdate lu = new WindDataLiveUpdate(ftpQueue);
      nmea.addWindDataListener(lu);
    }
    
    // Only create data logger and store data if we have a plotter to render it
    // TODO - Seperate plotting from data logging so we can still log without plotting!

    if (plotter != null) {
      /*
       * Original code logged wind data direct from the NMEA link.
       * To aid stability, logging of data can be moved to external process,
       * the Java app can just pull the data from a database.
       */
      String logMode = Config.getParamAsString("LogMode", "live");
      if ( logMode.compareToIgnoreCase("DB") == 0 )
      {
        @SuppressWarnings("unused")
        WindDataLoggerMySql dataLogger = new WindDataLoggerMySql(plotter, ticker);
        /* Gets data from DB, so we don't register this logger as
         * a WindDataListener */
      }
      else if ( logMode.compareToIgnoreCase("file") == 0 )
      {
        WindDataLoggerFile dataLogger = new WindDataLoggerFile(plotter, ticker, true, ftpQueue);
        nmea.addWindDataListener(dataLogger);
      }
      else /* ( logMode == "live") */
      {
        if ( logMode.compareToIgnoreCase("live") != 0 )
        {
          logger.info("Unrecognised LogMode '" +
              logMode + "'. Using 'live'");
        }
        WindDataLoggerFile dataLogger = new WindDataLoggerFile(plotter, ticker, false, ftpQueue);
        nmea.addWindDataListener(dataLogger);
      }
    }
  }
  
  /**
   * 
   */
  public void start() {
    nmea.start();    
  }

  /**
   * @return the ftpQueue
   */
  public FTPTaskQueue getFtpQueue() {
    return ftpQueue;
  }

  /**
   * @return the nmea
   */
  public NMEAController getNmea() {
    return nmea;
  }
  
  

}
