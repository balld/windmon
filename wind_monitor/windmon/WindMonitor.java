/*
 * @(#)WindMonitor.java	0.1 26/01/2005
 * 
 * Copyright (c) 2005 David Ball All Rights Reserved.
 * 
 */

/*
 * @(#)WeatherMonitor.java	0.1 26/01/2005
 */


package windmon;

import java.awt.*;
import java.awt.event.*;
// import java.awt.font.TextLayout;
// import java.awt.font.FontRenderContext;
// import java.io.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 * Wind Monitor Application
 *
 * @version @(#)WeatherView.java	0.1 26/01/2005
 * @author David Ball
 */
public class WindMonitor extends JPanel
{
    private static WindDisplay wv = null;
    private static WindDial wdl = null;
    private static WindDigits2 wdt = null;
    
    private JWindow w = null;
    private JFrame  f = null;
    
    public WindMonitor()
    {
    	super();

    	Config.loadConfig();
        
        setLayout(new BorderLayout(5,5));
        setBorder(new EmptyBorder(5,5,5,5));
        setBackground(Color.black);
    	
    	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        String screenMode = Config.getParamAsString("ScreenMode", "Window");
        if ( screenMode == "Window")
        {
        	/* Full screen */
        	w = new JWindow();

        	w.setLocation(0,0);
            w.setSize(d.width, d.height);
        	
            w.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
                public void windowDeiconified(WindowEvent e) { 
                }
                public void windowIconified(WindowEvent e) { 
                }
            });
            w.getAccessibleContext().setAccessibleDescription(
            "Wind Monitoring Application");
            w.getContentPane().removeAll();
            w.getContentPane().setLayout(new BorderLayout(5,5));
            w.getContentPane().add(this, BorderLayout.CENTER);
        }
        else
        {
        	/* Maximised Window */
        	f = new JFrame ("Wind Monitor");

        	f.setLocation(0,0);
            f.setSize(d.width, d.height);
            f.setIconImage(Utils.getImage(this, "MSCLogo.gif"));
        	
        	f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
                public void windowDeiconified(WindowEvent e) { 
                }
                public void windowIconified(WindowEvent e) { 
                }
            });
            f.getAccessibleContext().setAccessibleDescription(
            "Wind Monitoring Application");
            f.getContentPane().removeAll();
            f.getContentPane().setLayout(new BorderLayout(5,5));
            f.getContentPane().add(this, BorderLayout.CENTER);
        }
        

        Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128));


//        Create digital clock and image
        Banner2 bn = new Banner2();
        DigitalClock dc = new DigitalClock();
        dc.setBackground(Color.BLACK);
        dc.setForeground(Color.WHITE);
        Ticker tick = new Ticker();
        tick.setForeground(Color.WHITE);
        tick.setBackground(Color.BLACK);
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BorderLayout());
        jp1.setBackground(Color.white);
        jp1.add(bn, BorderLayout.WEST);
        jp1.add(dc, BorderLayout.CENTER);
        jp1.add(tick, BorderLayout.SOUTH);
        this.add(jp1, BorderLayout.NORTH);
        dc.start();
        tick.start();

        TickerFileWatcher tfw = new TickerFileWatcher(tick);
        tfw.start();
        
        JPanel jp2 = new JPanel();
        jp2.setLayout(new GridLayout(1,2));
        
        wdl = new WindDial();
        JPanel wdlp = new JPanel();
        wdlp.setLayout(new BorderLayout());
        wdlp.setBorder(border);
        wdlp.add(wdl, BorderLayout.CENTER);
        jp2.add(wdlp);
        
        JFreeChartPlotter plotter = new JFreeChartPlotter();
        plotter.setBorder(border);
        jp2.add(plotter);
        this.add(jp2, BorderLayout.CENTER);

        wdt = new WindDigits2();
        this.add(wdt, BorderLayout.SOUTH);

        // JFrame : Create menu bar
//        JMenuBar mbar = new JMenuBar();
//        JMenu mfile = new JMenu("File");
//        JMenu mhelp = new JMenu("Help");
//        Action exit = new ButtonExit("Exit",
//        		                     new ImageIcon("images/icon_exit.gif"), this);
//        Action options = new ButtonOptions("Options",
//                                     new ImageIcon("images/icon_options.gif"), this);
//        
//        Action about = new ButtonAbout("About Wind Monitor",
//        		                       new ImageIcon("images/icon_help.gif"), this);
//
//        mfile.add(options);
//        mfile.addSeparator();
//        mfile.add(exit);
//        
//        mhelp.add(about);
//        
//        mbar.add(mfile);
//        mbar.add(mhelp);
//        getContentPane().add(mbar, BorderLayout.NORTH);

      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        String connectionType = Config.getParamAsString("ConnectionType",
        		                                        "serial");
        NMEALink link = null;
        if ( connectionType.compareToIgnoreCase("socket") == 0 )
        {
        	link = new NMEALinkSocket(
        			Config.getParamAsString("NMEADefaultServerHost", "localhost"),
					Config.getParamAsInt("NMEADefaultServerPort", 2468));
        }
        else if ( connectionType.compareToIgnoreCase("serial") == 0 )
        {
        	link = new NMEALinkSerial();
        }
        else
        {
        	EventLog.log(EventLog.SEV_INFO, "Unrecognised connection type '" +
        			                          connectionType + "'. Using stub");
            link = new NMEALinkStub();
        }
        NMEAController nmea = NMEAController.getCreateInstance(link);
        nmea.addWindDataListener(wdl);
        nmea.addWindDataListener(wdt);

        /*
         * Original code logged wind data direct from the NMEA link.
         * To aid stability, logging of data can be moved to external process,
         * the Java app can just pull the data from a database.
         */
        String logMode = Config.getParamAsString("LogMode", "NMEA");
        if ( logMode == "NMEA")
        {
        	WindDataLoggerNMEA logger = new WindDataLoggerNMEA(plotter, tick);
        	nmea.addWindDataListener(logger);
        }
        else /* ( logMode == "DB" ) */
        {
        	/* TODO */
        	WindDataLoggerMySql logger = new WindDataLoggerMySql(plotter, tick);
        	/* Gets data from DB, so we don't register this logger as
        	 * a WindDataListener */
        }
        
        if ( w != null )
        {
        	w.setBackground(Color.pink);
        	w.setVisible(true);
        	w.validate();
        	w.requestFocus();
        }
        else
        {
        	f.setBackground(Color.pink);
        	f.setVisible(true);
        	f.validate();
        	f.requestFocus();
        }        	

        // JFrame : Set extended state
        // setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        repaint();
        nmea.start();
    }
    
    public static void main(String args[])
    {
    	WindMonitor wm = new WindMonitor();
    }
}