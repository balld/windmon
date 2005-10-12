/*
 * @(#)WindDisplay.java	0.1 26/01/2005
 * 
 * Copyright (c) 2005 David Ball All Rights Reserved.
 * 
 */

/*
 * @(#)WeatherView.java	0.1 26/01/2005
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
 * Weather View Application
 *
 * @version @(#)WeatherView.java	0.1 26/01/2005
 * @author David Ball
 */
public class WindDisplay extends JPanel implements Runnable, WindDataListener
{
    private Banner bn = null;
    private WindDial wg = null;
    private WindDigits wd = null;
    private DigitalClock dc = null;
    
    private double windSpeed = 0.0;
    private double maxWindSpeed = 80.0;
    private double windAngle = 0.0;
    
    private long display_update_interval = 500;
    
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

        bn = new Banner();
        wg = new WindDial();
        wg.setMaxSpeed(maxWindSpeed);
        wg.setSpeed(windSpeed);
        wg.setWindAngle(windAngle);

        wd = new WindDigits();
        wd.setWindSpeed(windSpeed);
        wd.setWindAngle(windAngle);

        // Create digital clock and image
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
        add(wd, BorderLayout.EAST);
//        add(new Border3D(dc), BorderLayout.SOUTH);
//        add(dc, BorderLayout.CENTER);

//        op = new OptionsPanel(nmea);
//        add(new Border3D(op), BorderLayout.SOUTH);
    }
    
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        // Start digital clock
//        dc.start();
    }


    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
        dc.stop();
    }

    public void run() {
        long last_display_update = 0;
        boolean doDigitalUpdate = false;
        NMEAMessage msg = null;

        Thread me = Thread.currentThread();

        while (thread == me) {
            synchronized (this)
            {
                while ( update == false )
                {
                    try
                    {
                        wait();
                    }
                    catch (Exception e)
                    {
//                        No action required.
                    }
                }
                wg.setSpeed(windSpeed);
                wg.setWindAngle(windAngle);
                if ( windSpeed < 0 || windAngle < 0 
                        || ( System.currentTimeMillis() - last_display_update )
                            > display_update_interval)
                {
                	wd.setWindSpeed(windSpeed);
                	wd.setWindAngle(windAngle);
                	last_display_update = System.currentTimeMillis();
                	doDigitalUpdate = true;
                }
                update=false;
            } // Updated values captured. Paint asynchronously 
            wg.repaint();
            if ( doDigitalUpdate )
            {
                wd.repaint();
                doDigitalUpdate = false;
            }
        }
        thread = null;
    }

    
    /**
     * @return Returns the maxWindSpeed.
     */
    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }
    /**
     * @param maxWindSpeed The maxWindSpeed to set.
     */
    public synchronized void setMaxWindSpeed(double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
        wg.setMaxSpeed(maxWindSpeed);
        // Trigger re-draw
        update=true;
        notifyAll();
    }

    public synchronized void windDataEventReceived(WindDataEvent e)
    {
        this.windSpeed = e.getWindSpeed();
        this.windAngle = e.getWindAngle();
        // Trigger re-draw
        update=true;
        notifyAll();
    }
}