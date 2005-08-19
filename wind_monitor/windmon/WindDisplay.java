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
public class WindDisplay extends JPanel implements Runnable
{
    private static int WIDTH = 850, HEIGHT = 600;
    private static WindDisplay wv = null;

    private Banner bn = null;
    private WindDial wg = null;
    private WindDigits wd = null;
    private DigitalClock dc = null;
    private OptionsPanel op = null;
    private NMEAController nmea = null;
    
    private double windSpeed = 0.0;
    private double maxWindSpeed = 80.0;
    private double windAngle = 0.0;
    
    private long sleepAmount = 10;
    private long display_update_interval = 1000;
    private boolean up = true;
    
    private boolean update = true;
    
    private NMEALink link = null;
    
    // Port name. Default is 4.
    private String portName = "";
    
    public Thread thread;
    
    public WindDisplay()
    {
        super();
        setDoubleBuffered(true);
        setBorder(new EmptyBorder(5,5,5,5));
        setLayout(new BorderLayout(5,5));

        bn = new Banner();
        wg = new WindDial();
        wg.setMaxSpeed(maxWindSpeed);
        wg.setSpeed(windSpeed);
        wg.setWindAngle(windAngle);

        wd = new WindDigits();
        wd.setWindSpeed(windSpeed);
        wd.setWindAngle(windAngle);
        
        dc = new DigitalClock();
//        dc.start();
        
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BorderLayout());
        jp1.setBackground(Color.white);
        jp1.add(bn, BorderLayout.WEST);
        jp1.add(dc, BorderLayout.CENTER);
        
        
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wg, wd);
//        splitPane.setContinuousLayout(true);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setDividerLocation(200);
//        add(splitPane, BorderLayout.CENTER);

        add(new Border3D(jp1), BorderLayout.NORTH);
        add(new Border3D(wg), BorderLayout.CENTER);
        add(new Border3D(wd), BorderLayout.EAST);
//        add(new Border3D(dc), BorderLayout.SOUTH);
//        add(dc, BorderLayout.CENTER);
        link = new SocketNMEALink("msc001", 2468);
        nmea = new NMEAController(this, link);

        op = new OptionsPanel(nmea);
        add(new Border3D(op), BorderLayout.SOUTH);
    }
    
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        dc.start();
        nmea.start();
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
                if ( ( System.currentTimeMillis() - last_display_update )
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
    /**
     * @return Returns the windAngle.
     */
    public double getWindAngle() {
        return windAngle;
    }
    /**
     * @param windAngle The windAngle to set.
     */
    public  synchronized void setWindAngle(double windAngle) {
        this.windAngle = windAngle;
        // Trigger re-draw
        update=true;
        notifyAll();
    }
    /**
     * @return Returns the windSpeed.
     */
    public double getWindSpeed() {
        return windSpeed;
    }
    /**
     * @param windSpeed The windSpeed to set.
     */
    public  synchronized void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
        // Trigger re-draw
        update=true;
        notifyAll();
    }

    public  synchronized void setWindSpeedAndAngle(double windSpeed, double windAngle) {
        this.windSpeed = windSpeed;
        this.windAngle = windAngle;
        // Trigger re-draw
        update=true;
        notifyAll();
    }
}