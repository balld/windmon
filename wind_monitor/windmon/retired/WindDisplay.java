/*
 * @(#)WindDisplay.java	0.1 26/01/2005
 * 
 * Copyright (c) 2005 David Ball All Rights Reserved.
 * 
 */

/*
 * @(#)WeatherView.java	0.1 26/01/2005
 */


package windmon.retired;

import java.awt.*;
import java.awt.event.*;
// import java.awt.font.TextLayout;
// import java.awt.font.FontRenderContext;
// import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import windmon.DigitalClock;
import windmon.WindDataEvent;
import windmon.WindDataListener;
import windmon.WindDial;
import windmon.WindDigits2;


/**
 * Weather View Application
 *
 * @version @(#)WeatherView.java	0.1 26/01/2005
 * @author David Ball
 */
public class WindDisplay extends JPanel implements WindDataListener
{
    private Banner bn = null;
    private WindDial wg = null;
    private WindDigits2 wd = null;
    private DigitalClock dc = null;
    
    private double windSpeed = 0.0;
    private double maxWindSpeed = 80.0;
    private double windAngle = 0.0;
    
    private long display_update_interval = 2000;
    
    private boolean update = true;
    
    public Thread thread;
    
    public WindDisplay()
    {
        super();
        setDoubleBuffered(true);
//        setBorder(new EmptyBorder(5,5,5,5));
        setBorder(new EmptyBorder(0,0,0,0));
//        setLayout(new BorderLayout(5,5));
        setLayout(new BorderLayout(0,0));

        wg = new WindDial(WindDial.COL_SCHEME_BLACK);
        wg.setMaxSpeed(maxWindSpeed);
        wg.setSpeed(windSpeed);
        wg.setWindAngle(windAngle);
        wg.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));

        wd = new WindDigits2();
        wd.setWindSpeed(windSpeed);
        wd.setWindAngle(windAngle);
        wd.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));

//         Create digital clock and image
//        bn = new Banner();
//        dc = new DigitalClock();
//        JPanel jp1 = new JPanel();
//        jp1.setLayout(new BorderLayout());
//        jp1.setBackground(Color.white);
//        jp1.add(bn, BorderLayout.WEST);
//        jp1.add(dc, BorderLayout.CENTER);
        
        
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wg, wd);
//        splitPane.setContinuousLayout(true);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setDividerLocation(200);
//        add(splitPane, BorderLayout.CENTER);

//        add(new Border3D(jp1), BorderLayout.NORTH);
//        add(new Border3D(wg), BorderLayout.CENTER);
//        add(new Border3D(wd), BorderLayout.EAST);

        // Add digital clock and image
//        add(jp1, BorderLayout.NORTH);
        add(wg, BorderLayout.CENTER);
        add(wd, BorderLayout.SOUTH);
//        add(new Border3D(dc), BorderLayout.SOUTH);
//        add(dc, BorderLayout.CENTER);
        // Start digital clock
        //      dc.start();

//        op = new OptionsPanel(nmea);
//        add(new Border3D(op), BorderLayout.SOUTH);
    }
    
    public synchronized void windDataEventReceived(WindDataEvent e)
    {
        wg.windDataEventReceived(e);
        wd.windDataEventReceived(e);
    }
}