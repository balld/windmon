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
public class WindMonitor extends JFrame
{
    private static int WIDTH = 850, HEIGHT = 600;
    private static WindDisplay wv = null;
    
    public WindMonitor()
    {
    	super("Wind Monitor");
        getAccessibleContext().setAccessibleDescription(
                                              "Wind Monitoring Application");

        Config cfg = Config.getCreateConfig();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
                if (wv != null) { wv.start(); }
            }
            public void windowIconified(WindowEvent e) { 
                if (wv != null) { wv.stop(); }
            }
        });
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(0,0);
        setSize(d.width, d.height);
//        setSize(WIDTH, HEIGHT);
        wv = new WindDisplay();


        // Use package class as image observer, else this won't work!
        setIconImage(Utils.getImage(wv, "MSCLogo.gif"));

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(wv, BorderLayout.CENTER);
        
//        Graph graph = new TestGraph();
//        getContentPane().add(graph, BorderLayout.SOUTH);
        
        JMenuBar mbar = new JMenuBar();
        JMenu mfile = new JMenu("File");
        JMenu mhelp = new JMenu("Help");
        Action exit = new ButtonExit("Exit",
        		                     new ImageIcon("images/icon_exit.gif"), this);
        Action options = new ButtonOptions("Options",
                                     new ImageIcon("images/icon_options.gif"), this);
        
        Action about = new ButtonAbout("About Wind Monitor",
        		                       new ImageIcon("images/icon_help.gif"), this);

        mfile.add(options);
        mfile.addSeparator();
        mfile.add(exit);
        
        mhelp.add(about);
        
        mbar.add(mfile);
        mbar.add(mhelp);
        getContentPane().add(mbar, BorderLayout.NORTH);

        // setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        NMEALink link = new SocketNMEALink("msc001", 2689);
        NMEAController nmea = NMEAController.getCreateInstance(link);
        nmea.addWindDataListener(wv);
        WindDataLogger logger = new WindDataLogger(10000);
        nmea.addWindDataListener(logger);
        
        setBackground(Color.pink);
       	setVisible(true);
        validate();
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        repaint();
        wv.start();
        nmea.start();
    }
    
    public static void main(String args[])
    {
    	WindMonitor wm = new WindMonitor();
    }
}