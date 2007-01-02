/*
 * Created on 31-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDigits2 extends JPanel implements WindDataListener {

//    private static final Dimension ps = new Dimension(300,300);
    private static int l_font_size = 120;
    private static int s_font_size = 20;
    
    private static int line_spacing = 20;
    private static int alignment = 25;
    
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
    
    // Labels
    private SmoothLabel label_kts, label_deg, label_beauf, label_comp;

    /*
     * Accessible variables
     */
    private double wind_speed = 0.0;
    private double wind_angle = 90.0;

    DecimalFormat df = new DecimalFormat("000");
    
    public WindDigits2()
    {
        setDoubleBuffered(true);
        setBackground(Color.WHITE);
        b_font = Utils.getFont("LCD-N___.TTF");
        l_font = b_font.deriveFont(Font.PLAIN, l_font_size);
        s_font = b_font.deriveFont(Font.PLAIN, s_font_size);

        label_kts = new SmoothLabel("", SmoothLabel.CENTER);
        label_deg = new SmoothLabel("", SmoothLabel.CENTER);
        label_beauf = new SmoothLabel("", SmoothLabel.CENTER);
        label_comp = new SmoothLabel("", SmoothLabel.CENTER);

        label_kts.setFont(l_font);
        label_deg.setFont(l_font);
        label_beauf.setFont(l_font);
        label_comp.setFont(l_font);

        label_kts.setBackground(Color.BLACK);
        label_deg.setBackground(Color.BLACK);
        label_beauf.setBackground(Color.BLACK);
        label_comp.setBackground(Color.BLACK);
        label_kts.setForeground(Color.WHITE);
        label_deg.setForeground(Color.WHITE);
        label_beauf.setForeground(Color.WHITE);
        label_comp.setForeground(Color.WHITE);

        label_kts.setOpaque(true);
        label_deg.setOpaque(true);
        label_beauf.setOpaque(true);
        label_comp.setOpaque(true);
        
        GridLayout gl = new GridLayout(1,4);
//        gl.setHgap(5);
//        gl.setVgap(5);
        this.setLayout(gl);

        this.add(createDisplay(label_kts,"knots", s_font));
        this.add(createDisplay(label_beauf,"beaufort", s_font));
        this.add(createDisplay(label_deg, "degrees", s_font));
        this.add(createDisplay(label_comp, "compass", s_font));

    }
    
    public void updateLabels ()
    {
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
            angle_str   = df.format(wind_angle);
            comp_str = Utils.angleToCompass(wind_angle);
        }
        label_kts.setText(speed_str);
        label_deg.setText(angle_str);
        label_beauf.setText(beauf_str);
        label_comp.setText(comp_str);
    }

    
//    public Dimension getPreferredSize()
//    {
//        return ps;
//    }

    
    public synchronized void windDataEventReceived(WindDataEvent e)
    {
        if ( sample_count >= sample_interval
             || e.getWindSpeed() > this.getWindSpeed()
             || e.getWindSpeed() < 0.0f )
        {
            this.setWindSpeed(e.getWindSpeed());
            this.setWindAngle(e.getWindAngle());
            // Update labels
            updateLabels();
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
    
    private static JPanel createDisplay( JLabel lbl,
                                          String title,
                                          Font title_font)
    {
        JPanel jp = new JPanel();
        SmoothLabel title_label = new SmoothLabel(title, SmoothLabel.CENTER);
        title_label.setFont(title_font);
        title_label.setOpaque(lbl.isOpaque());
        title_label.setBackground(lbl.getBackground());
        title_label.setForeground(lbl.getForeground());
        
        jp.setLayout(new BorderLayout());
        jp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));

        jp.add(title_label, BorderLayout.NORTH);
        jp.add(lbl, BorderLayout.CENTER);
        
        return jp;
    }
}
