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
    private Image     textImg_Curr = null;
    private Image     textImg_Pend = null;
    private Graphics2D  g_textImg = null;
    private Dimension textImgSize = null;
    
    private Thread thread = null;
    
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
        if ( textImg_Curr == null )
        {
            if ( textImg_Pend == null )
            {
                prepareText(g2);
            }
            xpos = size.width;
            textImg_Curr = textImg_Pend;
        }
        
        
        int y = (size.height - textImgSize.height) / 2;
        g2.drawImage(textImg_Curr, xpos, y, this);
        xpos -= xposStep;
        if ( xpos + textImgSize.width < 0 )
        {
            xpos = size.width;
            if ( textImg_Pend == null )
            {
                // Text Updated
                prepareText(g2);
            }
            textImg_Curr = textImg_Pend;
        }
    }        

    public void prepareText ( Graphics2D g2 )
    {
        TextLayout tl = new TextLayout(text,
                                       l_font,
                                       g2.getFontRenderContext());
        textImgSize = new Dimension ( (int)tl.getBounds().getWidth(),
                                      (int)tl.getBounds().getHeight());
        textImg_Pend = (BufferedImage) g2.getDeviceConfiguration().
        createCompatibleImage(textImgSize.width,
                              textImgSize.height);
        g_textImg = (Graphics2D) textImg_Pend.getGraphics();
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
    public synchronized void setText(String text) {
        this.text = text;
        // Force refresh of text buffer image.
        if ( g_textImg != null )
        {
            textImg_Pend = null;
            g_textImg.dispose();
            g_textImg = null;
        }
    }
}
