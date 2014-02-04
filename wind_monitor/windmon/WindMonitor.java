package windmon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
  private static WindDial windDial = null;
  private static WindDigits2 windDigits = null;

  private JWindow appWindow = null;
  private JFrame  appFrame = null;
  private Dimension screenSize;

  private JMenuItem screenModeMenuItem;

  private JPopupMenu popup;

  public WindMonitor()
  {
    super();

    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // TODO - Remove Me
    // Apply 4:3 aspect ration - for testing
//    screenSize = Utils.applyRatio(screenSize,  4,  3);
    
    
    setLayout(new BorderLayout(5,5));
    setBorder(new EmptyBorder(5,5,5,5));
    setBackground(Color.black);


    Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
        new Color(100, 100, 255),
        new Color(50, 50, 128));



    windDial = new WindDial();
    windDigits = new WindDigits2(WindDigits2.deriveLayout(screenSize.width, screenSize.height));

    JPanel windDialBorderPanel = new JPanel();
    windDialBorderPanel.setLayout(new BorderLayout());
    windDialBorderPanel.setBorder(border);
    windDialBorderPanel.add(windDial, BorderLayout.CENTER);
    windDialBorderPanel.add(windDigits, BorderLayout.SOUTH);


    WindDataPlotterJFreeChart plotter = new WindDataPlotterJFreeChart();
    plotter.setBorder(border);

    //        Create digital clock and image
    LogoPanel logo = new LogoPanel();
    DigitalClock clock = new DigitalClock();
    clock.setBackground(Color.BLACK);
    clock.setForeground(Color.WHITE);
    clock.start();

    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BorderLayout());
    northPanel.setBackground(Color.white);
    northPanel.add(logo, BorderLayout.WEST);
    northPanel.add(clock, BorderLayout.CENTER);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BorderLayout());
    this.add(centerPanel, BorderLayout.CENTER);

//    centerPanel.add(northPanel, BorderLayout.NORTH);
    centerPanel.add(plotter, BorderLayout.CENTER);

    /*
     * Ticker is optional.
     */
    Ticker tick = null;
    if ( Config.getParamAsBoolean("ShowTickerYN",false) == true)
    {
      tick = new Ticker();
      tick.setForeground(Color.WHITE);
      tick.setBackground(Color.BLACK);
      tick.setBorder(border);
      centerPanel.add(tick, BorderLayout.SOUTH);
      tick.start();
      TickerFileWatcher tfw = new TickerFileWatcher(tick);
      tfw.start();
    }        
    
    
    this.add(windDialBorderPanel, BorderLayout.WEST);


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
    
    WindMonitorLogic logic = new WindMonitorLogic();
    logic.init(plotter, tick);
    NMEAController nmea = logic.getNmea();

    nmea.addWindDataListener(windDial);
    nmea.addWindDataListener(windDigits);

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
    logic.start();
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
    if ( appFrame != null )
    {
      appFrame.getContentPane().remove(this);
      appFrame.setVisible(false);
      appFrame.dispose();
      appFrame = null;
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
    appWindow = new JWindow();

    appWindow.setLocation(0,0);
    appWindow.setSize(screenSize.width, screenSize.height);

    appWindow.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { 
        // Ignore
      }
      public void windowIconified(WindowEvent e) { 
        // Ignore
      }
    });
    appWindow.getAccessibleContext().setAccessibleDescription(
        "Wind Monitoring Application");
    appWindow.getContentPane().removeAll();
    appWindow.getContentPane().setLayout(new BorderLayout(5,5));
    appWindow.getContentPane().add(this, BorderLayout.CENTER);

    appWindow.setBackground(Color.pink);
    appWindow.setVisible(true);
    appWindow.validate();
    appWindow.requestFocus();
  }

  private void applyFrameScreenMode()
  {
    //
    // Destroy the JWindow if it exists
    //
    if ( appWindow != null )
    {
      appWindow.getContentPane().remove(this);
      appWindow.setVisible(false);
      appWindow.dispose();
      appWindow = null;
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


    appFrame = new JFrame ("Wind Monitor");

    appFrame.setLocation(0,0);
    appFrame.setSize(screenSize.width, screenSize.height);
    appFrame.setIconImage(Utils.getImage(this, "MSCLogo.gif"));

    appFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
      public void windowDeiconified(WindowEvent e) { 
        // Ignore
      }
      public void windowIconified(WindowEvent e) { 
        // Ignore
      }
    });
    appFrame.getAccessibleContext().setAccessibleDescription(
        "Wind Monitoring Application");
    appFrame.getContentPane().removeAll();
    appFrame.getContentPane().setLayout(new BorderLayout(5,5));
    appFrame.getContentPane().add(this, BorderLayout.CENTER);

    appFrame.setBackground(Color.pink);
    appFrame.setVisible(true);
    appFrame.validate();
    appFrame.requestFocus();
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
    LogUtils.initLog();
    Config.loadConfig();
    LogUtils.setAppLogDirectory(Config.getParamAsString("AppLogDirectory"));
    LogUtils.setLogLevel(Config.getParamAsString("AppLogLevel", "FINE"));

    String initScreenMode = Config.getParamAsString("ScreenMode", "Normal");
    if ( initScreenMode.compareToIgnoreCase("Headless") == 0 ) {
      WindMonitorLogic logic = new WindMonitorLogic();
      logic.init(null,  null);
      logic.start();
    } else {
      @SuppressWarnings("unused")
      WindMonitor wm = new WindMonitor();
    }
  }

  
}