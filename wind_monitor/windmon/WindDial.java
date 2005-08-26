/*
 * Created on 30-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
// import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDial extends JPanel {

    private static final Dimension ps = new Dimension ( 400, 400 );
    /*
     * Beaufort Scale Definition.
     * Array index is beaufort rating (0 - 12)
     * Array value is upper wind speed in knots.
     */
    private static final int beaufort[] = {
                                           0,  // F0
                                           3,  // F1
                                           6,  // F2
                                           10, // F3
                                           16, // F4
                                           21, // F5
                                           27, // F6
                                           33, // F7
                                           40, // F8
                                           47, // F9
                                           55, // F10
                                           63, // F11
                                           Integer.MAX_VALUE // F12
                                           };
    private static final String rose_points[] = {
                                                "N",
                                                "NE",
                                                "E",
                                                "SE",
                                                "S",
                                                "SW",
                                                "W",
                                                "NW"
                                                };
    /*
     * static constant definitions
     */
    private static Color s_dial_col = Color.black;
    private static Color border_col = Color.gray;
    private static Color scale_col = Color.white;
	private static Color needle_fill_col = Color.red;
	private static Color needle_line_col = Color.white;
    private static Color bg_col = Color.darkGray;
    private static Color arrow_col_low =  new Color(50, 50, 128);
    private static Color arrow_col_high = new Color(100, 100, 255);
    private static Color rose_col_high = arrow_col_high;
    private static Color rose_col_low  = arrow_col_low;
    private static Color rose_char_col = Color.WHITE;
    
    /*
     * Wind speed dial layout settings
     */
    private static int s_border_depth = 5;
    private static int s_bdigit_inset = 10;
    private static int s_bdigit_depth = 6;
    private static int s_bscale_depth = 10;
    private static int s_scale_inset = 5;
    private static int s_scale_depth = 10;
    private static int s_digit_depth = 0;
    private static int s_spindle_diam = 20;
    private static double s_zero_angle = 20.0;
    private static int s_arrow_len = 10;
    private static int s_arrow_width = 5;
    private static int s_scale_interval = 5;
    
    private static Font scale_font = new Font("serif", Font.PLAIN, 12);
    private static Font bscale_font = new Font("serif", Font.PLAIN, 18);
//    private static double sf_width = 0.6;
//    private static double sf_height = 0.75;
    
    /*
     * Wind direction dial layout settings
     */
    private static int d_digit_inset = 10;
    private static int d_rose_inset  = 10;
    private static int d_needle_inset = 10;
    private static int d_needle_diam = 60;
    private static Font d_rose_font = new Font("serif", Font.PLAIN, 18);

    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;

    
    // instance variables
    private Toolkit toolkit = null;
    private Image     s_dial = null;
    private Graphics2D  s_dial_g = null;
    private Image     d_dial = null;
    private Graphics2D  d_dial_g = null;
    
    // Layout variables derived at start-up and on re-size
    private int       diameter = 0;
    private int       radius   = 0;
    private Point     centre   = null;
    private int       s_diameter = 0;
    private int       s_radius   = 0;
    private int       s_needle_len = 0;
    private static int d_needle_len = 0;

    private Dimension panel_size = null;
    
    /*
     * Accessible variables
     */
    private double max_speed = 67.0;
    private double wind_speed = 0.0;
    private double wind_angle = 90.0;
    
    WindDial()
    {
        setDoubleBuffered(true);
        toolkit = getToolkit();
        setBackground(Color.BLACK);
    }

    public void setSpeed ( double d )
    {
        wind_speed = d;
    }
    
    public double getSpeed()
    {
        return wind_speed;
    }

    public void setWindAngle ( double d )
    {
        wind_angle = d;
    }
    
    public double getWindAngle()
    {
        return wind_angle;
    }
    
    public void paint(Graphics g)
    {
        // Run super class paint method
        super.paint(g);

        AffineTransform at = new AffineTransform();
        
        // Create Graphics2D object for advanced rendering.
        Graphics2D g2 = (Graphics2D) g;

        // Obtain the current size of this component
        Dimension size = getSize();
        
        // Set rendering options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        // Check for uninitiated background images or Panel re-size
        if ( s_dial == null || d_dial == null
             || !panel_size.equals(size))
        {
            prepareDial(g2);
        }
        
        // Draw the wind direction dial background
        g2.drawImage(d_dial, 0, 0, this);

        d_needle_diam = radius - s_radius;
        // Draw the wind direction needle
        int yd_points[] = { 0,
                            -d_needle_len + d_needle_diam/2,
                            -d_needle_len + d_needle_diam/2,
                            -d_needle_len,
                            -d_needle_len + d_needle_diam/2,
                            -d_needle_len + d_needle_diam/2,
                            0};
        int xd_points[] = {-d_needle_diam/4,
                           -d_needle_diam/4,
                           -d_needle_diam/2,
                            0,
                            d_needle_diam/2,
                            d_needle_diam/4,
                            d_needle_diam/4 };
        at.setToTranslation(centre.x, centre.y);
        at.rotate(Math.toRadians(wind_angle));
        Shape dir_needle = at.createTransformedShape(new Polygon(xd_points, yd_points, 7));
		g2.setColor(needle_fill_col);     
        g2.fill(dir_needle);
		g2.setColor(needle_line_col);     
		g2.draw(dir_needle);
        
        
        
        // Draw the wind speed dial background
        g2.drawImage(s_dial, 0, 0, this);
        
        // Draw the wind speed needle
        g2.setColor(needle_fill_col);
        g2.fillOval(centre.x - s_spindle_diam/2,
                    centre.y - s_spindle_diam/2,
                     s_spindle_diam, s_spindle_diam);

        int ys_points[] = { 0,
                           0,
                           s_needle_len};
        int xs_points[] = { s_spindle_diam/2,
                           -s_spindle_diam/2,
                           0 };
        
        at.setToTranslation(centre.x, centre.y);
        at.rotate(Math.toRadians(s_zero_angle +
                                   (360.0 - 2*s_zero_angle)
                                  *(wind_speed/max_speed)));
        g2.fill(at.createTransformedShape(new Polygon(xs_points, ys_points, 3)));
    }

    private void prepareDial(Graphics2D g2)
    {
        Dimension size = getSize();
        panel_size = new Dimension (size.width, size.height);

        // Dispose of old graphics object. Good practice I think.
        if ( s_dial_g != null)
        {
            s_dial_g.dispose();
            s_dial_g = null;
        }
        
        // Overall size of circular area enclosed by this component.
        diameter = Math.min(size.width, size.height);
        radius   = diameter/2;
//        centre   = new Point (radius, radius);
        centre   = new Point (size.width/2, size.height/2);
        
        prepareWindSpeedDial(g2);
        prepareWindDirectionDial(g2);
    }
    
    private void prepareWindDirectionDial(Graphics2D g2)
    {
        AffineTransform at = new AffineTransform();

        // Calculate wind direction dial geometry
        int d_digit_offset = radius - d_digit_inset;
        int d_rose_length  = d_digit_offset - d_rose_inset;
        int d_rose_width  = d_rose_length / 3;
        d_needle_len = d_rose_length - d_needle_inset;

        // Create buffered image for direction dial background
        d_dial = (BufferedImage) g2.getDeviceConfiguration().
                                     createCompatibleImage(panel_size.width,
                                                          panel_size.height,
                                                          Transparency.BITMASK);  
        d_dial_g = (Graphics2D) d_dial.getGraphics();

        // Set rendering properties        
        d_dial_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        d_dial_g.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        // Create temporary graphics object to apply transforms
        Graphics2D temp_g = (Graphics2D) d_dial_g.create();
        temp_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        temp_g.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
        
        // Create the basic compass rose shapes
        int x1_points[] = { 0, d_rose_width/2, d_rose_length };
        int y1_points[] = { 0, d_rose_width/2, 0             };
        int x2_points[] = { 0, d_rose_width/2,  d_rose_length };
        int y2_points[] = { 0, -d_rose_width/2, 0             };
        
        Shape upper_rose = new Polygon(x1_points, y1_points, 3);
        Shape lower_rose = new Polygon(x2_points, y2_points, 3);


        for ( int i = 0; i < rose_points.length; i++ )
        {
            double angle= (2.0 * Math.PI * i/ (double)rose_points.length)-Math.PI;
            at.setToTranslation((double) centre.x, (double) centre.y);
            at.rotate(angle);
            temp_g.setTransform(at);
            temp_g.setColor(rose_col_high);
            temp_g.fill(upper_rose);
            temp_g.setColor(rose_col_low);
            temp_g.fill(lower_rose);

            at.setToTranslation((double)centre.x, (double)centre.y);
            // Rotate font to correct position on dial.
            at.rotate(angle);
            // Translate font to edge of dial
            at.translate(0, d_digit_offset);
            // Rotate the font to maintain horizontal orienation
            at.rotate(-angle);
            
            temp_g.setTransform(at);
            temp_g.setColor(rose_char_col);
            TextLayout tl = new TextLayout(rose_points[i],
                                           d_rose_font,
                                           temp_g.getFontRenderContext());
            tl.draw(temp_g,
                    -(float) tl.getBounds().getCenterX(),
                    -(float) tl.getBounds().getCenterY());
        }
        temp_g.dispose();
    }
    
    
    private void prepareWindSpeedDial(Graphics2D g2)
    {
        AffineTransform at = new AffineTransform();

        // Calculate wind speed dial geometry
        s_diameter = (diameter * 2) / 3;
        s_radius   = s_diameter / 2;
        int s_bdigit_offset = s_radius - s_border_depth - s_bdigit_inset;
        int s_bscale_outer  = s_bdigit_offset - s_bdigit_depth;
        int s_bscale_inner  = s_bscale_outer - s_bscale_depth;
        int s_scale_outer = s_bscale_inner - s_scale_inset;
        int s_scale_inner = s_scale_outer - s_arrow_len;
        int s_digit_offset = s_scale_inner - s_scale_depth;
        s_needle_len = s_digit_offset - s_digit_depth;
        
            
        // Create buffered image for speed dial background
        s_dial = (BufferedImage) g2.getDeviceConfiguration().
                                    createCompatibleImage(panel_size.width,
                                                          panel_size.height,
                                                          Transparency.BITMASK);
        s_dial_g = (Graphics2D) s_dial.getGraphics();

        // Set rendering properties        
        s_dial_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        s_dial_g.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
        
        // Draw the wind speed dial border
        s_dial_g.setColor(border_col);
        s_dial_g.fillOval(centre.x - s_radius,
                        centre.y - s_radius,
                        s_diameter,
                        s_diameter);
        
        // Draw wind speed dial inner.
        s_dial_g.setColor(s_dial_col);
        s_dial_g.fillOval(centre.x - s_radius + s_border_depth,
                        centre.y - s_radius + s_border_depth,
                        s_diameter - 2*s_border_depth,
                        s_diameter - 2*s_border_depth);

        
        // Create arc which spans full scale deflection of speed dial.
        Arc2D arc = new Arc2D.Double(centre.x - s_bscale_inner,
                                     centre.y - s_bscale_inner,
                                     (s_bscale_inner) * 2.0,
                                     (s_bscale_inner) * 2.0,
                                     - 90.0 - s_zero_angle, -360.0 + 2.0*s_zero_angle,
                                     Arc2D.OPEN );
        
        // Caclulate number of degrees deflection per wind-speed knot.
        double deg_per_k = (360.0 - (2.0 * s_zero_angle))/max_speed;
        // Create temporary graphics object to apply transforms
        Graphics2D temp_g = (Graphics2D) s_dial_g.create();
        temp_g.setColor(scale_col);

        /*
         * Draw Beaufort wind speed scale
         */
        s_dial_g.setStroke(new BasicStroke(5.0f));
        temp_g.setStroke(new BasicStroke(3.0f));
        for ( int i = 0; i <= 11; i++ )
        {
            double low  = (double) beaufort[i];
            double high = (double) beaufort[i+1];
            if ( high > max_speed)
            {
                high = max_speed;
            }
            double angle_start = - s_zero_angle - 90.0 - low * deg_per_k;
            double angle_extent = -(high-low)*deg_per_k;
            double angle_end   = angle_start + angle_extent;
            double angle_mid   = angle_start + (angle_extent/2.0);
            
            arc.setAngleStart(angle_start);
            arc.setAngleExtent(angle_extent);
            
            // Fade colour from green to red. Pure red from force 8 upwards
            int red = i>7?255:(255*i)/7;
            int green = i>7?0:(255*(7-i))/7;
            s_dial_g.setColor(new Color(red, green, 0));
            s_dial_g.draw(arc);

            at.setToTranslation((double)centre.x, (double)centre.y);
            // Rotate marker to correct position on dial.
            at.rotate(Math.toRadians(-angle_start));
            // Translate font to edge of dial
            at.translate(s_bscale_inner, 0);
            temp_g.setTransform(at);
            temp_g.drawLine(0, 0, s_bscale_depth, 0);

            // Reset transform
            at.setToTranslation((double)centre.x, (double)centre.y);
            // Rotate digit to correct position on dial.
            at.rotate(Math.toRadians(-angle_mid-90));
            // Translate font to edge of dial
            at.translate(0, s_bdigit_offset);
            // Rotate the font to maintain horizontal orienation
            at.rotate(Math.toRadians(angle_mid+90));
            
            temp_g.setTransform(at);
            TextLayout tl = new TextLayout(Integer.toString(i+1),
                                           bscale_font,
                                           temp_g.getFontRenderContext());
            tl.draw(temp_g,
                    -(float) tl.getBounds().getCenterX(),
                    -(float) tl.getBounds().getCenterY());

            if ( high >= max_speed)
            {
                // We have reached the top of the available scale
                // Can't display any more beaufort.
                break;
            }
        }
        

        /*
         * Draw knot wind speed scale
         */
        int num_scales = (int) max_speed / s_scale_interval;
        double angle_inc  = deg_per_k * s_scale_interval;
        for ( int i = 0; i <= num_scales; i++ )
        {
            double angle = Math.toRadians(s_zero_angle 
                                          + (angle_inc * (double) i));
            temp_g.setColor(scale_col);
            //
            // Print the scale digit
            //
            at.setToTranslation((double)centre.x, (double)centre.y);
            // Rotate font to correct position on dial.
            at.rotate(angle);
            // Translate font to edge of dial
            at.translate(0, s_digit_offset);
            // Rotate the font to maintain horizontal orienation
            at.rotate(-angle);
            
            temp_g.setTransform(at);
            TextLayout tl = new TextLayout(Integer.toString(i*s_scale_interval),
                                           scale_font,
                                           temp_g.getFontRenderContext());
            tl.draw(temp_g,
                    -(float) tl.getBounds().getCenterX(),
                    -(float) tl.getBounds().getCenterY());
            //
            // Print the scale marker
            //
            at.setToTranslation((double)centre.x, (double)centre.y);
            // Rotate font to correct position on dial.
            at.rotate(angle);
            // Translate font to edge of dial
            at.translate(0,s_scale_outer);
            temp_g.setTransform(at);

            temp_g.setColor(arrow_col_high);
            int x1_points[] = { -s_arrow_width, 0, 0 };
            int y1_points[] = { -s_arrow_len, -s_arrow_len, 0 };
            temp_g.fill(new Polygon(x1_points, y1_points, 3));

            temp_g.setColor(arrow_col_low);
            int x2_points[] = { 0, s_arrow_width, 0 };
            int y2_points[] = { -s_arrow_len, -s_arrow_len, 0 };
            temp_g.fill(new Polygon(x2_points, y2_points, 3));
        }
        temp_g.dispose();

        // Draw scale labels
        s_dial_g.setColor(scale_col);
        TextLayout tl1 = new TextLayout("knots",
                                        scale_font,
                                        temp_g.getFontRenderContext());
        tl1.draw(s_dial_g,
                 (float) (centre.x - tl1.getBounds().getCenterX()),
                 (float) (centre.y + s_digit_offset - tl1.getBounds().getCenterY()));

        s_dial_g.setColor(scale_col);
        TextLayout tl2 = new TextLayout("Beaufort",
                                        scale_font,
                                        temp_g.getFontRenderContext());
        tl2.draw(s_dial_g,
                 (float) (centre.x - tl2.getBounds().getCenterX()),
                 (float) (centre.y + s_bdigit_offset - tl2.getBounds().getCenterY()));
    }

    /**
     * @return Returns the max_speed.
     */
    public double getMaxSpeed()
    {
        return max_speed;
    }
    /**
     * @param max_speed The max_speed to set.
     */
    public void setMaxSpeed(double max_speed)
    {
        this.max_speed = max_speed;
        // Force re-draw of dials
        this.s_dial = null;
    }

    public Dimension getPreferredSize()
    {
        return ps;
    }
}
