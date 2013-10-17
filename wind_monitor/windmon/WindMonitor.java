package windmon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * Wind Monitor Application
 *
 * @version @(#)WeatherView.java	0.1 26/01/2005
 * @author David Ball
 */
public class WindMonitor extends JPanel implements ActionListener
{
  Logger logger = Logger.getLogger(WindMonitor.class.getName());
  private static final long serialVersionUID = 0;

  private final String actionCommandQuit = "Quit";
  private final String actionCommandNMEAOptions = "NMEA Options";
  private final String actionCommandWindowScreenMode = "Full Screen Mode";
  private final String actionCommandFrameScreenMode  = "Normal Mode";

  //    private static WindDisplay wv = null;
  private static WindDial wdl = null;
  private static WindDigits2 wdt = null;

  private JWindow w = null;
  private JFrame  f = null;

  private JMenuItem screenModeMenuItem;

  private FTPTaskQueue ftpQueue = null;

  private JPopupMenu popup;

  public WindMonitor()
  {
    super();
    LogUtils.initLog();
    Config.loadConfig();
    LogUtils.setAppLogDirectory(Config.getParamAsString("AppLogDirectory"));
    LogUtils.setLogLevel(Config.getParamAsString("AppLogLevel", "FINE"));

    setLayout(new BorderLayout(5,5));
    setBorder(new EmptyBorder(5,5,5,5));
    setBackground(Color.black);


    Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
        new Color(100, 100, 255),
        new Color(50, 50, 128));


    //        Create digital clock and image
    LogoPanel bn = new LogoPanel();
    DigitalClock dc = new DigitalClock();
    dc.setBackground(Color.BLACK);
    dc.setForeground(Color.WHITE);

    JPanel jp1 = new JPanel();
    jp1.setLayout(new BorderLayout());
    jp1.setBackground(Color.white);
    jp1.add(bn, BorderLayout.WEST);
    jp1.add(dc, BorderLayout.CENTER);

    /*
     * Ticker is optional.
     */
    Ticker tick = null;
    if ( Config.getParamAsBoolean("ShowTickerYN",false) == true)
    {
      tick = new Ticker();
      tick.setForeground(Color.WHITE);
      tick.setBackground(Color.BLACK);
      jp1.add(tick, BorderLayout.SOUTH);
      dc.start();
      tick.start();

      TickerFileWatcher tfw = new TickerFileWatcher(tick);
      tfw.start();
    }        

    this.add(jp1, BorderLayout.NORTH);

    JPanel jp2 = new JPanel();
    jp2.setLayout(new GridLayout(1,2));

    wdl = new WindDial();
    JPanel wdlp = new JPanel();
    wdlp.setLayout(new BorderLayout());
    wdlp.setBorder(border);
    wdlp.add(wdl, BorderLayout.CENTER);
    jp2.add(wdlp);

    WindDataPlotterJFreeChart plotter = new WindDataPlotterJFreeChart();
    plotter.setBorder(border);
    jp2.add(plotter);
    this.add(jp2, BorderLayout.CENTER);

    wdt = new WindDigits2();
    this.add(wdt, BorderLayout.SOUTH);

    //
    //Create the popup menu.
    //
    JMenuItem menuItem;
    popup = new JPopupMenu();
    menuItem = new JMenuItem(actionCommandQuit);
    menuItem.addActionListener(this);
    popup.add(menuItem);
    menuItem = new JMenuItem(actionCommandNMEAOptions);
    menuItem.addActionListener(this);
    popup.add(menuItem);
    // NOTE : We add one more menu item later to switch between screen modes.


    //Add listener to components that can bring up popup menus.
    MouseListener popupListener = new PopupListener();
    this.addMouseListener(popupListener);


    //
    // Set cursor to something different - just because
    //
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

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
    NMEAController nmea = NMEAController.getCreateInstance(link, ss);
    nmea.addWindDataListener(wdl);
    nmea.addWindDataListener(wdt);


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

    /*
     * Original code logged wind data direct from the NMEA link.
     * To aid stability, logging of data can be moved to external process,
     * the Java app can just pull the data from a database.
     */
    String logMode = Config.getParamAsString("LogMode", "live");
    if ( logMode.compareToIgnoreCase("DB") == 0 )
    {
      @SuppressWarnings("unused")
      WindDataLoggerMySql dataLogger = new WindDataLoggerMySql(plotter, tick);
      /* Gets data from DB, so we don't register this logger as
       * a WindDataListener */
    }
    else if ( logMode.compareToIgnoreCase("file") == 0 )
    {
      WindDataLoggerFile dataLogger = new WindDataLoggerFile(plotter, tick, true, ftpQueue);
      nmea.addWindDataListener(dataLogger);
    }
    else /* ( logMode == "live") */
    {
      if ( logMode.compareToIgnoreCase("live") != 0 )
      {
        logger.info("Unrecognised LogMode '" +
            logMode + "'. Using 'live'");
      }
      WindDataLoggerFile dataLogger = new WindDataLoggerFile(plotter, tick, false, ftpQueue);
      nmea.addWindDataListener(dataLogger);
    }

    String initScreenMode = Config.getParamAsString("ScreenMode", "Normal");
    if ( initScreenMode.compareToIgnoreCase("FullScreen") == 0 )
    {
      applyWindowScreenMode();
    }
    else
    {
      applyFrameScreenMode();
    }

    // JFrame : Set extended state
    // setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
    repaint();
    nmea.start();
  }

  // Handle actions from popup menu items
  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();
    logger.info("Menu action: " + cmd);
    if ( cmd.equalsIgnoreCase(actionCommandQuit) )
    {
      System.exit(0);
    }
    else if ( cmd.equalsIgnoreCase(actionCommandNMEAOptions))
    {
      JDialog dialog = new JDialog();
      dialog.setTitle("NMEA Options");
      NMEAController nmea = NMEAController.getInstance();
      if ( nmea == null )
      {
        return;
      }

      OptionsPanel op = new OptionsPanel(nmea);
      dialog.getContentPane().add(op);
      dialog.setSize(new Dimension(650,300));
      dialog.validate();
      dialog.setVisible(true);
    }
    else if (cmd.equalsIgnoreCase(actionCommandWindowScreenMode))
    {
      applyWindowScreenMode();
    }
    else if (cmd.equalsIgnoreCase(actionCommandFrameScreenMode))
    {
      applyFrameScreenMode();
    }
  }

  private void applyWindowScreenMode()
  {
    //
    // Destroy the JFrame if it exists
    //
    if ( f != null )
    {
      f.getContentPane().remove(this);
      f.setVisible(false);
      f.dispose();
      f = null;
    }

    //
    // Create or update the popup menu item so user can change mode back to
    // frame mode
    //
    if ( screenModeMenuItem == null )
    {
      screenModeMenuItem = new JMenuItem(actionCommandFrameScreenMode);
      screenModeMenuItem.addActionListener(this);
      popup.add(screenModeMenuItem);
    }
    else
    {
      screenModeMenuItem.setActionCommand(actionCommandFrameScreenMode);
      screenModeMenuItem.setText(actionCommandFrameScreenMode);
    }

    /* Full screen window (no borders) */
    w = new JWindow();

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    w.setLocation(0,0);
    w.setSize(d.width, d.height);

    w.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { 
        // Ignore
      }
      public void windowIconified(WindowEvent e) { 
        // Ignore
      }
    });
    w.getAccessibleContext().setAccessibleDescription(
        "Wind Monitoring Application");
    w.getContentPane().removeAll();
    w.getContentPane().setLayout(new BorderLayout(5,5));
    w.getContentPane().add(this, BorderLayout.CENTER);

    w.setBackground(Color.pink);
    w.setVisible(true);
    w.validate();
    w.requestFocus();
  }

  private void applyFrameScreenMode()
  {
    //
    // Destroy the JWindow if it exists
    //
    if ( w != null )
    {
      w.getContentPane().remove(this);
      w.setVisible(false);
      w.dispose();
      w = null;
    }

    //
    // Create or update the popup menu item so user can change mode back to
    // frame mode
    //
    if ( screenModeMenuItem == null )
    {
      screenModeMenuItem = new JMenuItem(actionCommandWindowScreenMode);
      screenModeMenuItem.addActionListener(this);
      popup.add(screenModeMenuItem);
    }
    else
    {
      screenModeMenuItem.setActionCommand(actionCommandWindowScreenMode);
      screenModeMenuItem.setText(actionCommandWindowScreenMode);
    }


    f = new JFrame ("Wind Monitor");

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation(0,0);
    f.setSize(d.width, d.height);
    f.setIconImage(Utils.getImage(this, "MSCLogo.gif"));

    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { 
        // Ignore
      }
      public void windowIconified(WindowEvent e) { 
        // Ignore
      }
    });
    f.getAccessibleContext().setAccessibleDescription(
        "Wind Monitoring Application");
    f.getContentPane().removeAll();
    f.getContentPane().setLayout(new BorderLayout(5,5));
    f.getContentPane().add(this, BorderLayout.CENTER);

    f.setBackground(Color.pink);
    f.setVisible(true);
    f.validate();
    f.requestFocus();
  }



  //
  // Sub-class to make popup menu appear when appropriate
  //
  class PopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popup.show(e.getComponent(),
            e.getX(), e.getY());
      }
    }
  }
  
  
  /**
   * **************** MAIN *********************
   * 
   * @param args
   */
  public static void main(String args[])
  {
    @SuppressWarnings("unused")
    WindMonitor wm = new WindMonitor();
  }

  
}