/*
 * Created on 30-Jan-2005
 */
package windmon;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * @author David Ball
 *
 * Graphical UI component that displays wind speed and direction on an 
 * analogue-style anemometer dial.
 */
public class WindDial extends JPanel implements WindDataListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Colour Scheme Constants
	 */
	public static final int COL_SCHEME_BLACK  = 1;
	public static final int COL_SCHEME_BLUE  = 2;
	
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
    private Color border_col = Color.gray;
    private Color scale_col = Color.white;
    private Color needle_fill_col_low = new Color (200, 0, 0);
	private Color needle_fill_col_high = new Color (255, 0, 0);
	private Color needle_line_col = Color.white;
    private Color arrow_col_low =  new Color(50, 50, 128);
    private Color arrow_col_high = new Color(100, 100, 255);
    private Color rose_col_high = arrow_col_high;
    private Color rose_col_low  = arrow_col_low;
    private Color rose_char_col = Color.WHITE;
	private Color needle_faint_col = Color.lightGray;
    
    /*
     * Wind speed dial layout settings
     */
    private int s_border_depth = 5;
    private int s_bdigit_inset = 10;
    private int s_bdigit_depth = 6;
    private int s_bscale_depth = 10;
    private int s_scale_inset = 5;
    private int s_scale_depth = 10;
    private int s_digit_depth = 0;
    private int s_spindle_diam = 20;
    private double s_zero_angle = 20.0;
    private int s_arrow_len = 10;
    private int s_arrow_width = 5;
    private int s_scale_interval = 5;
    private float s_bscale_line = 3.0f;
    private float s_barc_line = 5.0f;
    
    private Font scale_font = new Font("serif", Font.PLAIN, 12);
    private Font bscale_font = new Font("serif", Font.PLAIN, 18);
//    private double sf_width = 0.6;
//    private double sf_height = 0.75;
    
    /*
     * Wind direction dial layout settings
     */
    private int d_digit_inset = 10;
    private int d_rose_inset  = 10;
    private int d_needle_inset = 10;
    private int d_needle_diam = 60;
    private Font d_rose_font = new Font("serif", Font.PLAIN, 18);

    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;

    /* Loaded Images */
    Image s_dial_image = null;
    
    // instance variables
    private BufferedImage     speedDialImage = null;
    private Graphics2D  s_dial_g = null;
    private BufferedImage     directionDialImage = null;
    private Graphics2D  d_dial_g = null;
    
    // Layout variables derived at start-up and on re-size
    private int       diameter = 0;
    private int       radius   = 0;
    private Point     centre   = null;
    private int       s_diameter = 0;
    private int       s_radius   = 0;
    private int       s_needle_len = 0;
    private int       d_needle_len = 0;

    private Dimension panel_size = null;
    
    /*
     * Accessible variables
     */
    private double max_speed = 67.0;
    private double wind_speed = -1.0;
    private double wind_angle = -1.0; 
    private double wind_speed_high = -1.0;
    private double wind_speed_low = -1.0;

    public WindDial()
    {
        this(COL_SCHEME_BLACK);
    }
    
    public WindDial ( int col_scheme )
	{
    	setDoubleBuffered(true);
    	switch ( col_scheme )
		{
    	case COL_SCHEME_BLACK:
            s_dial_image = Utils.getImage(this, "blackball512.png");
            setBackground(Color.BLACK);
            border_col = Color.gray;
            scale_col = Color.white;
        	needle_fill_col_low = new Color (220, 0, 0);
        	needle_fill_col_high = new Color (255, 0, 0);
        	needle_line_col = Color.white;
            arrow_col_low =  new Color(50, 50, 128);
            arrow_col_high = new Color(100, 100, 255);
            rose_col_high = arrow_col_high;
            rose_col_low  = arrow_col_low;
            rose_char_col = Color.WHITE;
        	needle_faint_col = Color.lightGray;
        	break;
    	case COL_SCHEME_BLUE:
            s_dial_image = Utils.getImage(this, "mscblueball512.png");
            setBackground(Color.WHITE);
            border_col = Color.gray;
            scale_col = Color.BLACK;
        	needle_fill_col_low = new Color (220, 0, 0);
        	needle_fill_col_high = new Color (255, 0, 0);
        	needle_line_col = Color.BLACK;
            arrow_col_low =  new Color(0, 32, 90);
            arrow_col_high = arrow_col_low.brighter();
            rose_col_high = arrow_col_high;
            rose_col_low  = arrow_col_low;
            rose_char_col = Color.BLACK;
        	needle_faint_col = Color.DARK_GRAY;
        	break;
		}
    	
	}

    public synchronized void windDataEventReceived(WindDataEvent e)
    {
        this.setSpeed(e.getWindSpeed());
        this.setWindAngle(e.getWindAngle());
        // always request repaint immediately
        repaint();
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
//        super.paint(g);
        justPaint(g);
    }
    

    public void justPaint(Graphics g)
        {
        AffineTransform at = new AffineTransform();
        
        // Create Graphics2D object for advanced rendering.
        Graphics2D g2 = (Graphics2D) g;
        
        // Obtain the current size of this component
        Dimension size = getSize();
        
        // Set rendering options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        // Check for uninitiated background images or Panel re-size
        if ( speedDialImage == null || directionDialImage == null
             || !panel_size.equals(size))
        {
            prepareDial(g2);
        }
        
        // Draw the wind direction dial background
        g2.drawImage(directionDialImage, 0, 0, this);

        d_needle_diam = radius - s_radius;
        // Draw the wind direction needle if wind_angle > zero
        if ( wind_angle >= 0.0 )
        {
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
            int yd_points_l[] = { 0,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len + d_needle_diam/2,
                    0};
            int xd_points_l[] = {-d_needle_diam/4,
                    -d_needle_diam/4,
                    -d_needle_diam/2,
                    0,
                    0,
                    0,
                    0 };
            int yd_points_r[] = { 0,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len,
                    -d_needle_len + d_needle_diam/2,
                    -d_needle_len + d_needle_diam/2,
                    0};
            int xd_points_r[] = {0,
                    0,
                    0,
                    0,
                    d_needle_diam/2,
                    d_needle_diam/4,
                    d_needle_diam/4 };
            at.setToTranslation(centre.x, centre.y);
            at.rotate(Math.toRadians(wind_angle));
            Shape dir_needle = at.createTransformedShape(new Polygon(xd_points, yd_points, 7));
            Shape dir_needle_l = at.createTransformedShape(new Polygon(xd_points_l, yd_points_l, 7));
            Shape dir_needle_r = at.createTransformedShape(new Polygon(xd_points_r, yd_points_r, 7));
            g2.setColor(needle_fill_col_high);     
            g2.fill(dir_needle_l);
            g2.setColor(needle_fill_col_low);     
            g2.fill(dir_needle_r);
            g2.setColor(needle_line_col);     
            g2.draw(dir_needle);
        }        
        
        
        // Draw the wind speed dial background
        g2.drawImage(speedDialImage, 0, 0, this);

        // Coordinates for needle
        int ys_points[] = { 0,
                0,
                s_needle_len};
        int xs_points[] = { s_spindle_diam/2,
                -s_spindle_diam/2,
                0 };

        int ys_points_l[] = { 0,
                0,
                s_needle_len};
        int xs_points_l[] = { 0,
                -s_spindle_diam/2,
                0 };
        int ys_points_r[] = { 0,
                0,
                s_needle_len};
        int xs_points_r[] = { s_spindle_diam/2,
                0,
                0 };

        
        // High and low speed needles
        if ( wind_speed_high >= 0.0 )
        {
            // Draw the wind speed needle
            g2.setColor(needle_faint_col);
//            g2.drawOval(centre.x - s_spindle_diam/2,
//                        centre.y - s_spindle_diam/2,
//                         s_spindle_diam, s_spindle_diam);

            at.setToTranslation(centre.x, centre.y);
            at.rotate(Math.toRadians(s_zero_angle +
                                       (360.0 - 2*s_zero_angle)
                                      *(wind_speed_high/max_speed)));
            g2.draw(at.createTransformedShape(new Polygon(xs_points, ys_points, 3)));
        }
        if ( wind_speed_low >= 0.0 )
        {
            // Draw the wind speed needle
            at.setToTranslation(centre.x, centre.y);
            at.rotate(Math.toRadians(s_zero_angle +
                                       (360.0 - 2*s_zero_angle)
                                      *(wind_speed_low/max_speed)));
            g2.setColor(needle_faint_col);
            g2.draw(at.createTransformedShape(new Polygon(xs_points, ys_points, 3)));
//            g2.drawOval(centre.x - s_spindle_diam/2,
//                        centre.y - s_spindle_diam/2,
//                         s_spindle_diam, s_spindle_diam);
        }

        
        // Draw the wind speed needle (if wind_speed non-negative
        if (wind_speed >= 0.0)
        {
            at.setToTranslation(centre.x, centre.y);
            at.rotate(Math.toRadians(s_zero_angle +
                    (360.0 - 2*s_zero_angle)
                    *(wind_speed/max_speed)));

            g2.setColor(needle_fill_col_high);
            g2.fill(at.createTransformedShape(new Polygon(xs_points_l, ys_points_l, 3)));
            g2.setColor(needle_fill_col_low);
            g2.fill(at.createTransformedShape(new Polygon(xs_points_r, ys_points_r, 3)));
            g2.fillOval(centre.x - s_spindle_diam/2,
                    centre.y - s_spindle_diam/2,
                    s_spindle_diam, s_spindle_diam);
        }
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
        
        // Adjust Various Size Parameters
        /*
         * Wind speed dial layout settings
         */
        s_border_depth = 5*diameter/ps.width;
        s_bdigit_inset = 5+(5*diameter/ps.width);
        s_bdigit_depth = 6*diameter/ps.width;
        s_bscale_depth = 10*diameter/ps.width;
        s_scale_inset = 5*diameter/ps.width;
        s_scale_depth = 10*diameter/ps.width;
        s_digit_depth = 0*diameter/ps.width;
        s_spindle_diam = 20*diameter/ps.width;
        s_arrow_len = 10*diameter/ps.width;
        s_arrow_width = 5*diameter/ps.width;
        s_bscale_line = 3.0f*diameter/ps.width;
        s_barc_line = 5.0f*diameter/ps.width;

        s_zero_angle = 20.0 + (10*ps.width/diameter);
        if ( diameter >= 300 )
        {
            s_scale_interval = 5;
        }
        else
        {
            s_scale_interval = 10;
        }

        scale_font = new Font("serif", Font.PLAIN, 6+(8*diameter/ps.width));
        bscale_font = new Font("serif", Font.PLAIN, 4+(12*diameter/ps.width));
        
        /*
         * Wind direction dial layout settings
         */
        d_digit_inset = 10*diameter/ps.width;
        d_rose_inset  = 10*diameter/ps.width;
        d_needle_inset = 10*diameter/ps.width;
        d_needle_diam = 60*diameter/ps.width;
        d_rose_font = new Font("serif", Font.PLAIN, 4+(14*diameter/ps.width));

        
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
        directionDialImage = (BufferedImage) g2.getDeviceConfiguration().
                                     createCompatibleImage(panel_size.width,
                                                          panel_size.height,
                                                          Transparency.OPAQUE);  
        d_dial_g = (Graphics2D) directionDialImage.getGraphics();
        d_dial_g.setColor(getBackground());
        d_dial_g.fillRect(0, 0, panel_size.width, panel_size.height);
        
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
        speedDialImage = (BufferedImage) g2.getDeviceConfiguration().
                                    createCompatibleImage(panel_size.width,
                                                          panel_size.height,
                                                          Transparency.BITMASK);
        s_dial_g = (Graphics2D) speedDialImage.getGraphics();

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
        // Draw scaled background image
        s_dial_g.drawImage(s_dial_image, 
                centre.x - s_radius + s_border_depth,
                centre.y - s_radius + s_border_depth,
                s_diameter - 2*s_border_depth,
                s_diameter - 2*s_border_depth,
                this);

        
        // Create arc which spans full scale deflection of speed dial.
        Arc2D arc = new Arc2D.Double(centre.x - s_bscale_inner,
                                     centre.y - s_bscale_inner,
                                     (s_bscale_inner) * 2.0,
                                     (s_bscale_inner) * 2.0,
                                     - 90.0 - s_zero_angle, -360.0 + 2.0*s_zero_angle,
                                     Arc2D.OPEN );
        
        // Calculate number of degrees deflection per wind-speed knot.
        double deg_per_k = (360.0 - (2.0 * s_zero_angle))/max_speed;
        // Create temporary graphics object to apply transforms
        Graphics2D temp_g = (Graphics2D) s_dial_g.create();
        temp_g.setColor(scale_col);

        /*
         * Draw Beaufort wind speed scale
         */
        s_dial_g.setStroke(new BasicStroke(s_barc_line));
        temp_g.setStroke(new BasicStroke(s_bscale_line));
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
            // double angle_end   = angle_start + angle_extent;
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
                                        s_dial_g.getFontRenderContext());
        tl1.draw(s_dial_g,
                 (float) (centre.x - tl1.getBounds().getCenterX()),
                 (float) (centre.y + s_digit_offset - tl1.getBounds().getHeight()));

        s_dial_g.setColor(scale_col);
        TextLayout tl2 = new TextLayout("Beaufort",
                                        scale_font,
                                        s_dial_g.getFontRenderContext());
        tl2.draw(s_dial_g,
                 (float) (centre.x - tl2.getBounds().getCenterX()),
                 (float) (centre.y + s_bdigit_offset - tl2.getBounds().getHeight()));
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
        this.speedDialImage = null;
    }

    public Dimension getPreferredSize()
    {
        return ps;
    }
	/**
	 * @return Returns the wind_speed_high.
	 */
	public double getWindSpeedHigh() {
		return wind_speed_high;
	}
	/**
	 * @param wind_speed_high The wind_speed_high to set.
	 */
	public void setWindSpeedHigh(double wind_speed_high) {
		this.wind_speed_high = wind_speed_high;
	}
	/**
	 * @return Returns the wind_speed_low.
	 */
	public double getWindSpeedLow() {
		return wind_speed_low;
	}
	/**
	 * @param wind_speed_low The wind_speed_low to set.
	 */
	public void setWindSpeedLow(double wind_speed_low) {
		this.wind_speed_low = wind_speed_low;
	}
    
    /**
     * @param args
     */
    public static void main(String args[])
    {
        JFrame jf = new JFrame("WindDial Test");
        jf.setSize(200,200);
        WindDial wd = new WindDial();
        
        jf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) { 
            }
            public void windowIconified(WindowEvent e) { 
            }
        });

        jf.getContentPane().removeAll();
        jf.getContentPane().setLayout(new BorderLayout(0,0));
        jf.getContentPane().add(wd, BorderLayout.CENTER);
        
        jf.setVisible(true);
        jf.validate();
        jf.requestFocus();
    }

	public BufferedImage getDirectionDialImage() {
		return directionDialImage;
	}

	public BufferedImage getSpeedDialImage() {
		return speedDialImage;
	}
}
