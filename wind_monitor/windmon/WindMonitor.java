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

        wv = new WindDisplay();
        wv.setBorder(border);
        jp.add(wv, BorderLayout.CENTER);


//        Graph graph = new TestGraph();
//        getContentPane().add(graph, BorderLayout.SOUTH);
        JFreeChartPlotter plotter = new JFreeChartPlotter();
        plotter.setBorder(border);


        jp.add(plotter, BorderLayout.EAST);

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
        	EventLog.log(EventLog.SEV_FATAL, "Unrecognised connection type '" +
        			                          connectionType + "'");
        }
        NMEAController nmea = NMEAController.getCreateInstance(link);
        nmea.addWindDataListener(wv);
        WindDataLogger logger = new WindDataLogger(plotter);
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