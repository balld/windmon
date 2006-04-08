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
public class WindMonitor extends JWindow
{
    private static int WIDTH = 850, HEIGHT = 600;
    private static WindDisplay wv = null;
    private static WindDial wdl = null;
    private static WindDigits2 wdt = null;
    
    public WindMonitor()
    {
    	// If using JFrame then set title.
//        super("Wind Monitor");
        getAccessibleContext().setAccessibleDescription(
                                              "Wind Monitoring Application");

        Config.loadConfig();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
            }
            public void windowIconified(WindowEvent e) { 
            }
        });
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(0,0);
        setSize(d.width, d.height);
//        setSize(WIDTH, HEIGHT);

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout(5,5));
        jp.setBorder(new EmptyBorder(5,5,5,5));
        jp.setBackground(Color.black);

        // If using JFrame, set an Icon image
        // Use package class as image observer, else this won't work!
//        setIconImage(Utils.getImage(jp, "MSCLogo.gif"));

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(5,5));
        getContentPane().add(jp, BorderLayout.CENTER);
        
        Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128));
        Insets insets = new Insets(5,5,5,5);


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
        jp.add(jp1, BorderLayout.NORTH);
        dc.start();
        tick.start();

        
//      wv = new WindDisplay();
//      wv.setBorder(border);
//      jp.add(wv, BorderLayout.CENTER);

        
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
        jp.add(jp2, BorderLayout.CENTER);

        wdt = new WindDigits2();
        jp.add(wdt, BorderLayout.SOUTH);

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
//        nmea.addWindDataListener(wv);
        nmea.addWindDataListener(wdl);
        nmea.addWindDataListener(wdt);
        WindDataLogger logger = new WindDataLogger(plotter, tick);
        nmea.addWindDataListener(logger);
        
        setBackground(Color.pink);
       	setVisible(true);
        validate();
        this.requestFocus();
        
        // JFrame : Set extended state
//        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        repaint();
        nmea.start();
    }
    
    public static void main(String args[])
    {
    	WindMonitor wm = new WindMonitor();
    }
}