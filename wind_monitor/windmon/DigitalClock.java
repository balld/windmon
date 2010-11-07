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
// import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author BallD
 *
 * A digital clock.
 */
public class DigitalClock extends JPanel implements Runnable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3588190825599838664L;
	private static final Dimension ps = new Dimension(525,30);
    private static int l_font_size = 48;
    private static int s_font_size = 24;

    // Check for update every second
    private static final long sleepAmount = 1000;
    // Update display every 60 seconds
    private static final long updateInterval = 60000;
    // Date/Time format - this should consider updateInterval above.
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm   EEE dd MMM yyyy");
    
    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;
    
    private Font b_font = null;
    private Font l_font = null;
    private Font s_font = null;
    
    
    private long now = -1;
    private long last = -1;

    private Thread thread = null;
    
    public DigitalClock()
    {
        setDoubleBuffered(true);
        b_font = Utils.getFont("LCD-N___.TTF");
        l_font = b_font.deriveFont(Font.PLAIN, l_font_size);
        s_font = b_font.deriveFont(Font.PLAIN, s_font_size);
        
        last = System.currentTimeMillis();
    }

    
    public void setVisible(boolean b)
    {
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
    
    public void paint ( Graphics g )
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
        
        Dimension size = getSize();
        
        String time_str = buildTime();
        
        g2.setColor(getForeground());
        g2.setFont(l_font);
        TextLayout tl = new TextLayout(time_str,
                                       l_font,
                                       g2.getFontRenderContext());
        int h = (int) tl.getBounds().getHeight();
        int y = h + ((size.height - h ) / 2);
        int w = (int) tl.getBounds().getWidth();
        int x = (size.width - w) / 2;
        tl.draw(g2, (float) x, (float) y );
    }

    private String buildTime()
    {
        Date dt = new Date(now);
        return timeFormat.format(dt);
    }
    
    public Dimension getPreferredSize()
    {
        return ps;
    }
}
