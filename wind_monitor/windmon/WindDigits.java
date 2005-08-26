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
public class WindDigits extends JPanel implements Runnable {

    private static final Dimension ps = new Dimension(400,400);
    private static int l_font_size = 160;
    private static int s_font_size = 56;
    
    private static int line_spacing = 56;
    private static int alignment = 300;

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

    public void paint ( Graphics g )
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
        
        g2.setColor(Color.GREEN);

        String speed_str = "" + (int) wind_speed;
        String angle_str   = "" + (int) wind_angle;
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
         
//        tl.draw(g2,
//                -(float) tl.getBounds().getCenterX(),
//                -(float) tl.getBounds().getCenterY());
        int h = (int) tls.getBounds().getHeight();
//        int w = (int) tls.getBounds().getWidth();
        int w = (int) tls.getAdvance();
        int y = size.height/2 - line_spacing/2;
        int x = alignment;
        tls.draw(g2, (float) x - w, (float) y );
        tlkts.draw(g2, (float) x, (float) y);

        y = size.height/2 + line_spacing/2 + (int) tld.getBounds().getHeight();
        int y2 = size.height/2 + line_spacing/2
                               + (int) tldeg.getBounds().getHeight();
//        w = (int) tld.getBounds().getWidth();
        w = (int) tld.getAdvance();
        tld.draw(g2, (float) x - w, (float) y);
        tldeg.draw(g2, (float) x, (float) y2);
//        int y = g2.getFontMetrics(l_font).getAscent();
//        g2.drawString(time_str, l_font_size/2,y);
    }

    
    public Dimension getPreferredSize()
    {
        return ps;
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
