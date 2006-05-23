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
// import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Ticker extends JPanel implements Runnable {

    private static final Dimension ps = new Dimension(400,30);
    private static int l_font_size = 25;

    // Check for update every second
    private static final long sleepAmount = 25;

    // Update display interval in millisec
    private static final long updateIntervalUnit = 50;
    private long updateInterval;

    // Number of pixels text moved on each update
    private static final int xposStepUnit = 5;
    private int xposStep;

    private int xpos = 0;
    
    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;
    
    private Font b_font = null;
    private Font l_font = null;
    
    private long now = -1;
    private long last = -1;

    private String    text = "Set some text please! This is just some dummy text that I have entered to show how this ticker display will work. Hum dee hum dee hum";
    private Image     textImg = null;
    private Graphics2D  g_textImg = null;
    private Dimension textImgSize = null;
    
    private Thread thread = null;
    
    private HashMap stringsMap = new HashMap();
    private Iterator stringsItr = null;
    
    public Ticker()
    {
        readConfig();
        setDoubleBuffered(true);

        b_font = Utils.getFont("LCD-N___.TTF");
        l_font = b_font.deriveFont(Font.PLAIN, l_font_size);
        
        last = System.currentTimeMillis();
    }

    public void readConfig()
    {
        updateInterval = Config.getParamAsLong("TickerRefresh", 2)*updateIntervalUnit;
        xposStep = Config.getParamAsInt("TickerStep", 3) * xposStepUnit;
    }
    
    public void setVisible(boolean b)
    {
        EventLog.log(EventLog.SEV_DEBUG, "Ticker setVisible: " + b);
        super.setVisible(b);
        if ( b == true )
        {
            if ( thread == null )
            {
                this.start();
            }
        }
        else
        {
            this.stop();
        }
    }

    
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
//            thread.setPriority(Thread.MAX_PRIORITY);
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

        long lastIntervalNum = 0;
        while (thread == me)
        {
            now = System.currentTimeMillis();
            if ( now/updateInterval > lastIntervalNum && isVisible())
            {
                lastIntervalNum = now/updateInterval;
                repaint();
            }
            try {
                Thread.sleep(sleepAmount);
            } catch (InterruptedException e) { }
        }
        thread = null;
    }
    
    public synchronized void paint ( Graphics g )
    {
        Dimension size = getSize();
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        if ( textImg == null )
        {
            xpos = size.width;
            prepareText(g2);
        }
        
        int y = (size.height - textImgSize.height) / 2;
        g2.drawImage(textImg, xpos, y, this);
        xpos -= xposStep;

        if ( xpos + textImgSize.width < 0 )
        {
            xpos = size.width;
            prepareText(g2);
        }
    }        

    public void prepareText ( Graphics2D g2 )
    {
        if ( stringsMap.size() <= 0 )
        {
            text = "Default message. No text to display";
        }
        else 
        {
            if ( stringsItr == null || stringsItr.hasNext() == false )
            {
                stringsItr = stringsMap.values().iterator();
            }
            text = (String) stringsItr.next();
        }
        
        TextLayout tl = new TextLayout(text,
                                       l_font,
                                       g2.getFontRenderContext());
        textImgSize = new Dimension ( (int)tl.getBounds().getWidth(),
                                      (int)tl.getBounds().getHeight());
        textImg = (BufferedImage) g2.getDeviceConfiguration().
        createCompatibleImage(textImgSize.width,
                              textImgSize.height);
        g_textImg = (Graphics2D) textImg.getGraphics();
        g_textImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g_textImg.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        g_textImg.setColor(getForeground());
        g_textImg.setFont(l_font);
        TextLayout tl2 = new TextLayout(text,
                l_font,
                g_textImg.getFontRenderContext());

        float y = textImgSize.height; // - tl.getDescent();
        float x = 0;
        
        tl2.draw(g_textImg, x, y );
    }

    
    public Dimension getPreferredSize()
    {
        return ps;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }
    /**
     * @param text The text to set.
     */
    public synchronized void setText(Object key, String text) {
        stringsMap.put(key, text);
    }
}
