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
import java.awt.geom.*;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;


/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDigits extends JPanel implements Runnable, WindDataListener {

    private static final Dimension ps = new Dimension(300,300);
    private static int l_font_size = 120;
    private static int s_font_size = 40;
    
    private static int line_spacing = 20;
    private static int alignment = 250;
    
    // Repaint on every n wind data events
    private static final int sample_interval = 10;
    private int sample_count = sample_interval;

    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;
    
    private Font b_font = null;
    private Font l_font = null;
    private Font s_font = null;
    private Font deg_font = new Font("serif", Font.PLAIN, 56);
    
    /*
     * Accessible variables
     */
    private double wind_speed = 0.0;
    private double wind_angle = 90.0;


    // To indicate re-draw
    private boolean toggle = false;

    
    public WindDigits()
    {
        setDoubleBuffered(true);
        setBackground(Color.BLACK);
        b_font = Utils.getFont("LCD-N___.TTF");
        l_font = b_font.deriveFont(Font.PLAIN, l_font_size);
        s_font = b_font.deriveFont(Font.PLAIN, s_font_size);
    }
    
    public void start ()
    {
        // Start stuff
    }
    
    public void stop ()
    {
        // Stop stuff
    }
    
    public void run ()
    {
        // Run stuff
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
    
    public void paint ( Graphics g )
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        // Indicates frmae re-draw
        g2.setColor(Color.GREEN);
        if ( toggle )
        {
            g2.fillRect(0,0,20,20);
        }
        toggle = !toggle;
        
        Dimension size = getSize();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
        
        g2.setColor(Color.white);
        String speed_str;
        String angle_str;
        String beauf_str;
        String comp_str;
        
        if ( wind_speed < 0 )
        {
            speed_str = "xxx";
            beauf_str = "Fx";
        }
        else
        {
            if ( wind_speed < 10 )
            {
                speed_str = ("" + wind_speed).substring(0, 3);
            }
            else
            {
                speed_str = "" + (int) wind_speed;
            }
            beauf_str = "F" + Utils.speedToBeaufort(wind_speed);
        }
       
        if ( wind_angle < 0 )
        {
            angle_str = "xxx";
            comp_str = "xxx";
        }
        else
        {
            angle_str   = "" + (int) wind_angle;
            comp_str = "N  ";
        }
        String kts = "kts";
        String deg = "o";
        
//        AttributedString speed_astr = new AttributedString(speed_str);
//        AttributedString angle_astr   = new AttributedString(angle_str);
//        
//        speed_astr.addAttribute(TextAttribute.FONT, l_font);
//        int index = speed_str.indexOf("kts");
//        speed_astr.addAttribute(TextAttribute.SUPERSCRIPT,
//                                TextAttribute.SUPERSCRIPT_SUB, index, index+3);
//        AttributedCharacterIterator si = speed_astr.getIterator();
//        
//        angle_astr.addAttribute(TextAttribute.FONT, l_font);
//        index = angle_str.indexOf("o");
//        angle_astr.addAttribute(TextAttribute.SUPERSCRIPT,
//                               TextAttribute.SUPERSCRIPT_SUPER, index, index+1);
//        AttributedCharacterIterator ai = angle_astr.getIterator();
        
        TextLayout tls = new TextLayout(speed_str, l_font,
                                        g2.getFontRenderContext());
        TextLayout tld = new TextLayout(angle_str, l_font,
                                        g2.getFontRenderContext());
        TextLayout tlkts = new TextLayout(kts, s_font,
                                          g2.getFontRenderContext());
        TextLayout tldeg = new TextLayout(deg, deg_font,
                                          g2.getFontRenderContext());
        TextLayout tlb = new TextLayout(beauf_str, l_font,
                g2.getFontRenderContext());
        TextLayout tlc = new TextLayout(comp_str, l_font,
                g2.getFontRenderContext());
         
//        tl.draw(g2,
//                -(float) tl.getBounds().getCenterX(),
//                -(float) tl.getBounds().getCenterY());
        int h = (int) tls.getBounds().getHeight();
//        int w = (int) tls.getBounds().getWidth();
        int w = (int) tls.getAdvance();
        int y = size.height/2 - line_spacing/2;
        int x = 0;
        tlb.draw(g2, (float) x, (float) y );

        x = size.width/2;
        tls.draw(g2, (float) x, (float) y );
        tlkts.draw(g2, (float) x + w, (float) y);

        y = size.height/2 + line_spacing/2 + (int) tld.getBounds().getHeight();
        int y2 = size.height/2 + line_spacing/2
                               + (int) tldeg.getBounds().getHeight();
//        w = (int) tld.getBounds().getWidth();
        w = (int) tld.getAdvance();
        x = 0;
        tlc.draw(g2, (float) x, (float) y );

        x = size.width/2;
        tld.draw(g2, (float) x, (float) y);
        tldeg.draw(g2, (float) x + w, (float) y2);
//        int y = g2.getFontMetrics(l_font).getAscent();
//        g2.drawString(time_str, l_font_size/2,y);
    }

    
    public Dimension getPreferredSize()
    {
        return ps;
    }

    
    public synchronized void windDataEventReceived(WindDataEvent e)
    {
        if ( sample_count >= sample_interval || 
                e.getWindSpeed() > this.getWindSpeed() )
        {
            this.setWindSpeed(e.getWindSpeed());
            this.setWindAngle(e.getWindAngle());
            // always request repaint immediately
            repaint();
            sample_count = 0;
        }
        else
        {
            sample_count++;
        }
    }   

    
    /**
     * @return Returns the wind_angle.
     */
    public double getWindAngle() {
        return wind_angle;
    }
    /**
     * @param wind_angle The wind_angle to set.
     */
    public void setWindAngle(double wind_angle) {
        this.wind_angle = wind_angle;
    }
    /**
     * @return Returns the wind_speed.
     */
    public double getWindSpeed() {
        return wind_speed;
    }
    /**
     * @param wind_speed The wind_speed to set.
     */
    public void setWindSpeed(double wind_speed) {
        this.wind_speed = wind_speed;
    }
}
