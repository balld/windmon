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
public class WindMonitor
{
    private static int WIDTH = 850, HEIGHT = 600;
    private static WindDisplay wv = null;
    
    public static void main(String args[])
    {
        JFrame frame = new JFrame("Wind Monitor");
        frame.getAccessibleContext().setAccessibleDescription(
                                              "Wind Monitoring Application");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
                if (wv != null) { wv.start(); }
            }
            public void windowIconified(WindowEvent e) { 
                if (wv != null) { wv.stop(); }
            }
        });
        
//        frame.setSize(WIDTH, HEIGHT);
//        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
//        frame.setLocation(d.width/2 - WIDTH/2, d.height/2 - HEIGHT/2);
        wv = new WindDisplay();


        // Use package class as image observer, else this won't work!
        frame.setIconImage(Utils.getImage(wv, "MSCLogo.gif"));

        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(wv, BorderLayout.CENTER);
        
        Graph graph = new TestGraph();
        frame.getContentPane().add(graph, BorderLayout.SOUTH);
        
        JMenuBar mbar = new JMenuBar();
        JMenu mfile = new JMenu("File");
        JMenu mhelp = new JMenu("Help");
        Action exit = new ButtonExit("Exit",
        		                     new ImageIcon("images/icon_exit.gif"), frame);
        Action options = new ButtonOptions("Options",
                                     new ImageIcon("images/icon_options.gif"), frame);
        
        Action about = new ButtonAbout("About Wind Monitor",
        		                       new ImageIcon("images/icon_help.gif"), frame);

        mfile.add(options);
        mfile.addSeparator();
        mfile.add(exit);
        
        mhelp.add(about);
        
        mbar.add(mfile);
        mbar.add(mhelp);
        frame.getContentPane().add(mbar, BorderLayout.NORTH);
        // frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        frame.setBackground(Color.pink);
       	frame.setVisible(true);
        frame.validate();
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.repaint();
        wv.start();
    }
}