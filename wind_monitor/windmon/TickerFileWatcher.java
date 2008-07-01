/*
 * Created on 31-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
// import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TickerFileWatcher implements Runnable {

    private long sleepAmount = 25;
    private String filename = null;
    
    private Ticker ticker = null;
    
    private Thread thread = null;
    
    public TickerFileWatcher(Ticker t)
    {
        readConfig();
        this.ticker = t;
    }

    public TickerFileWatcher()
    {
        this(null);
    }

    public void readConfig()
    {
        sleepAmount = Config.getParamAsLong("TickerFileCheckIntervalSec", 60)*1000;
        filename = Config.getParamAsString("TickerFilename", null);
    }
    
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }


    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }
    
    public void run ()
    {
        Thread me = Thread.currentThread();
        boolean opened = false;

        long lastIntervalNum = 0;
        while (thread == me)
        {
            if ( filename != null && ticker != null )
            {
                // Open file
                BufferedReader br = null;
                opened = true;
                try
                {
                    br = new BufferedReader(new FileReader(filename));
                }
                catch (Exception e)
                {
                    EventLog.log(EventLog.SEV_ERROR, "Unable to open ticker file '" + filename + "'");
                    e.printStackTrace();
                    opened = false;
                }
                
                // Read each line and append to buffer
                if ( opened == true )
                {
                    StringBuffer buff = new StringBuffer();
                    String line;
                    try
                    {
                        while ( (line = br.readLine()) != null)
                        {
                            if ( buff.length() > 0 )
                            {
                                buff.append("   " + line);
                            }
                            else
                            {
                                buff.append(line);
                            }
                        }
                        br.close();
                    }
                    catch (Exception e)
                    {
                        EventLog.log(EventLog.SEV_ERROR, "Unable to read ticker file '" + filename + "'");
                        e.printStackTrace();
                    }
                    // Write text to ticker.
                    ticker.setText(this, buff.toString());
                    EventLog.log(EventLog.SEV_DEBUG, "Updated ticker from file '" + filename + "'");
                }
            }
            try {
                Thread.sleep(sleepAmount);
            } catch (InterruptedException e) { }
        }
        thread = null;
    }   
    /**
     * @return Returns the ticker.
     */
    public Ticker getTicker() {
        return ticker;
    }
    /**
     * @param ticker The ticker to set.
     */
    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }
}
