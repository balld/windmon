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
import java.util.*;


/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DigitalClock extends JPanel implements Runnable {

    private static final Dimension ps = new Dimension(525,30);
    private static int l_font_size = 48;
    private static int s_font_size = 24;
    private static final long sleepAmount = 10;
    
    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;
    
    private Font b_font = null;
    private Font l_font = null;
    private Font s_font = null;
    
    // Months (Jan = 0)
    private static String month_str[] = { "Jan", "Feb", "Mar", "Apr",
                                          "May", "Jun", "Jul", "Aug",
                                          "Sep", "Oct", "Nov", "Dec" };
    // Days (Sun=1 to Sat=7)
    private static String dow_str[] = { "", "Sun", "Mon", "Tue", "Wed", "Thu",
                                            "Fri", "Sat" };
    
    private static String num_str[] = { "00", "01", "02", "03", "04", "05",
                                        "06", "07", "08", "09", "10", "11",
                                        "12", "13", "14", "15", "16", "17",
                                        "18", "19", "20", "21", "22", "23",
                                        "24", "25", "26", "27", "28", "29",
                                        "30", "31", "32", "33", "34", "35",
                                        "36", "37", "38", "39", "40", "41",
                                        "42", "43", "44", "45", "46", "47",
                                        "48", "49", "50", "51", "52", "53",
                                        "54", "55", "56", "57", "58", "59" };

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

        while (thread == me)
        {
            now = System.currentTimeMillis();
            if ( now - last > 1000 && isVisible())
            {
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
//        tl.draw(g2,
//                -(float) tl.getBounds().getCenterX(),
//                -(float) tl.getBounds().getCenterY());
        int h = (int) tl.getBounds().getHeight();
        int y = h + ((size.height - h ) / 2);
        int w = (int) tl.getBounds().getWidth();
        int x = (size.width - w) / 2;
        tl.draw(g2, (float) x, (float) y );

        
        
//        int y = g2.getFontMetrics(l_font).getAscent();
//        g2.drawString(time_str, l_font_size/2,y);
    }

    private String buildTime()
    {
        Calendar cal = new GregorianCalendar();
        if ( now >= 0 )
        {
            cal.setTime(new Date(now));
            last = now;
        }
        else if ( last >= 0 )
        {
            cal.setTime(new Date(last));
        }
        else
        {
            return new String("Error : No Time");
        }
        
//        StringBuilder sb = new StringBuilder();
//        Formatter formatter = new Formatter(sb);
        
        // This is how it should be done in Java 1.5+. Not compatible with earlier
        // Java versions.
        // String now_str = 
        //               String.format("%1$tH:%1$tM:%1$tS  %1$te %1$tb %1$tY", cal);
        
        // And now the old way.
        int hour, min, sec, dow, day, month, year;
        
        hour=cal.get(Calendar.HOUR_OF_DAY);
        min =cal.get(Calendar.MINUTE);
        sec =cal.get(Calendar.SECOND);
        
        dow  =cal.get(Calendar.DAY_OF_WEEK);
        day  =cal.get(Calendar.DAY_OF_MONTH);
        month=cal.get(Calendar.MONTH);
        year =cal.get(Calendar.YEAR);
        
        //    String now_str = "18:00:00  7 Jan 2005";
        String str = new String (  num_str[hour] + ":"
                + num_str[min]  + ":"
                + num_str[sec]  + "  "
                + dow_str[dow]  + " "
                + day           + " "
                + month_str[month] + " "
                + year );
        return str;
    }
    
    public Dimension getPreferredSize()
    {
        return ps;
    }
}
