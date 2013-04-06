package windmon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Utils {

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
    
    /**
     * Loads an image. Creates media tracker to wait for image to be loaded.
     * Catches and reports exception if load fails.
     * @param cmp An AWT component needed to create a 
     *  MediaTracker object.
     * @param name Name of the image file (from images/ directory)
     */
    public static Image getImage(Component cmp, String name) {
        Image img = null;
        
        URLClassLoader urlLoader = 
            (URLClassLoader)cmp.getClass().getClassLoader();
        if ( urlLoader != null )
        {
            URL fileLoc = urlLoader.findResource("images/" + name);
            img = cmp.getToolkit().createImage(fileLoc);
        }
        else
        {
            img = cmp.getToolkit().createImage("images/" + name);
        }
        
        MediaTracker tracker = new MediaTracker(cmp);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error loading image " + name);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        if ( img == null)
        {
            System.out.println("Null image " + name);
        }
        return img;
    }
    
    public static Font getFont(String name)
    {
        Font font = null;
        String fName = "/fonts/" + name;
        try {
            InputStream is = Utils.class.getResourceAsStream(fName);
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            System.err.println(fName + " not loaded.  Using serif font.");
            font = new Font("serif", Font.PLAIN, 24);
        }
        return font;
    }


    public static File openFile(String name)
    {
        File file = null;
        URLClassLoader urlLoader = 
            (URLClassLoader)Utils.class.getClassLoader();
        if ( urlLoader != null )
        {
            try
            {
                // This is how you do it Java5.0
//                URI uri = urlLoader.findResource(name).toURI();
                URI uri = new URI(urlLoader.findResource(name).toString());
                file = new File(uri);
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        
        return file;
    }
    
    /**
     * Tiles a rectangular region of an AWT Graphics context with
     * a specified image. Dead cool this!.
     * @param img Image which will be tiled in a repeating pattern.
     * @param comp AWT Component needed to draw images.
     * @param g Graphics into which tiling is done.
     * @param x Left coordinate of tiled rectangle.
     * @param y Top coordinate of tiled rectangle.
     * @param width Width of the tiled rectangle.
     * @param height Height of the tiled rectangle.
     */
    public static void tileImage(Image img, Component comp, 
            Graphics g, int x, int y, 
            int width, int height) {
        // Image and Graphics must not be null
        if (img == null || g == null) {
            throw new NullPointerException("Image/Graphics null");
        }
        // Store the current clip of the Graphics so can restore
        // it later. Then set clip to specified rectangle.
        Shape origclip = g.getClip();
        g.setClip(x, y, width, height);
        
        // Get the image dimensions.
        int imgwidth = img.getWidth(comp);
        int imgheight = img.getHeight(comp);
        
        // Tile away!
        int ypos = y;
        while(ypos <= (y + height)) {
            int xpos = x;
            while(xpos <= (x + width)) {
                g.drawImage(img, xpos, ypos, comp);
                xpos += imgwidth;
            }
            ypos += imgheight;
        }
        // Restore original clip to Graphics.
        g.setClip(origclip);
    }
    
    /**
     * Determines if the modifiers of a MouseEvent indicate that
     * the left mouse button is down.
     * @param ev MouseEvent to check
     * @return boolean value, true if left button is down.
     */
    public static boolean isLeftButton(MouseEvent ev) {
        return ((ev.getModifiers() & InputEvent.BUTTON1_MASK) 
                == InputEvent.BUTTON1_MASK);
    }
    
    /**
     * Determines if the modifiers of a MouseEvent indicate that
     * the middle mouse button is down.
     * @param ev MouseEvent to check
     * @return boolean value, true if middle button is down.
     */
    public static boolean isMiddleButton(MouseEvent ev) {
        return ((ev.getModifiers() & InputEvent.BUTTON2_MASK) 
                == InputEvent.BUTTON2_MASK);
    }
    
    /**
     * Determines if the modifiers of a MouseEvent indicate that
     * the right mouse button is down.
     * @param ev MouseEvent to check
     * @return boolean value, true if right button is down.
     */
    public static boolean isRightButton(MouseEvent ev) {
        return ((ev.getModifiers() & InputEvent.BUTTON3_MASK) 
                == InputEvent.BUTTON3_MASK);
    }
    
    /**
     * Simple method that strips InterruptedExceptions from the
     * Thread.sleep() method.
     * @param dur A long value specifying sleep time in ms.
     */
    public static void justSleep(long dur) {
        try {
            Thread.sleep(dur);
        } catch(Exception e) {
            // Not important.
        }
    }
    
    /**
     * Returns the AWT Frame to which the given component 
     * ultimately belongs.
     * @param comp AWT Component
     * @return The Frame which 'owns' this component.
     */
    public static Frame getFrame(Component comp) {
        Component curr = comp;
        while( (curr != null) && !(curr instanceof Frame) ) {
            curr = curr.getParent();
        }
        return (Frame) curr;
    }
    
    /**
     * Draw a 3D rectangle just like the draw3DRect() method
     * of the AWT Graphics class but with the addition of a
     * thickness parameter.
     * @param g Graphics context
     * @param x horizontal location of top left of 3DRect
     * @param y vertical location of top left of 3DRect
     * @param width Width of 3DRect in pixels
     * @param height Height of 3DRect in pixels.
     * @param raise true is 3DRect raises upward, false
     *  if 3DRect is to appear inset.
     * @param thickness The thickness of the 3D border.
     */
    public static void draw3DRect(Graphics g, int x, int y, 
            int width, int height, 
            boolean raise, 
            int thickness) {
        int w = width;
        int h = height;
        
        for(int i = 0; i < thickness; i++) {
            g.draw3DRect(x+i, y+i, w-2*i, h-2*i, raise);
        }
    }
    
    /**
     * This method adds a Component to a Container which
     * has a GridBagLayout manager. Save loads of typing.
     * @param gridbag The GridBagLayout of the Container.
     * @param constraints The constraints to be used.
     * @param comp The Component to be added to the 
     *  Container.
     * @param container The Container.
     * @param gx The column location of comp
     * @param gy The row location of comp
     * @param gw The width of comp in columns.
     * @param gh The height of comp in rows.
     * @param wx The weighting of comps width
     * @param wy The weighting of comps height.
     */
    public static void addComponentToGridBag(
            GridBagLayout gridbag,
            GridBagConstraints constraints,
            Component comp,
            Container container,
            int gx, int gy, int gw, int gh, 
            int wx, int wy) {
        buildConstraints(constraints, gx, gy, gw, gh, wx, wy);
        gridbag.setConstraints(comp, constraints);
        container.add(comp);
    }
    
    /**
     * Useful method for quickly setting several key values
     * in a GridBagConstraints Object.
     * @param gdc The GridBagConstraints object to be set
     * @param gx The column location
     * @param gy The row location
     * @param gw The width in columns.
     * @param gh The height in rows.
     * @param wx The width weighting
     * @param wy The height weighting
     */
    public static void buildConstraints(
            GridBagConstraints gbc, int gx, int gy,
            int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(5,5,5,5);
    }
    
    /**
     * Redirects the standard error stream to a file of the
     * given name in the current directory. Compiling this
     * generates a deprecation error cos it uses old
     * PrintStream stuff. This can't be helped.
     * @param filename Filename to which the standard error 
     *  output will be directed. If the file already exists
     *  then the new output is appended after adding a time
     *  stamp.
     */
    public static void setStandardError(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            if (file.isDirectory()) {
                System.err.println(
                "Can't redirect stderr to a directory!");
                return;
            }
            System.err.println(
            "Error file exists, will append");
        }
        try {       
            // Create a fileoutput stream which duplicates
            // all println output to stdout
            FileOutputStream fileout = 
                new FileOutputStream(filename, true);
            PrintStream newerr = new PrintStream(fileout);
            System.setErr(newerr);
            System.err.println("\n\n" + 
                    java.util.Calendar.getInstance().getTime() +
            "\n");
            System.out.println("stderr --> " + filename);
        } catch(Exception e) {
            System.out.println("stderr --> " + filename + 
            "failed.");
            System.out.println(e);
        }
    }
    
/*
    public static SerialPort openDefaultSerialPort(String name)
    {
        SerialPort sPort = null;
        CommPortIdentifier portId = null;
        
        // Obtain a CommPortIdentifier object for the port you want to open.
        try {
            portId = 
                CommPortIdentifier.getPortIdentifier(name);
        } catch (NoSuchPortException e) {
            System.err.println("Could not identify port " + name);
            return null;
        }
        
        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of 30 seconds to allow
        // a different application to reliquish the port if the user 
        // wants to.
        try {
            sPort = (SerialPort)portId.open("WeatherView", 30000);
        } catch (PortInUseException e) {
            System.err.println("Could not open port " + name);
            return null;
        }
        
        // Set the parameters of the connection. If they won't set, close the
        // port before throwing an exception.
    	try {
    	    sPort.setSerialPortParams(9600,
    	            SerialPort.DATABITS_8,
    	            SerialPort.STOPBITS_1,
    	            SerialPort.PARITY_NONE);
    	} catch (UnsupportedCommOperationException e) {
    	    System.err.println("Could not configure port " + name);
    	    sPort.close();
    	    return null;
    	}
        return sPort;
    }
    */
    
    public static java.util.Properties getEnv()
    {
        java.util.Properties jvmEnv = System.getProperties();
        java.util.Properties envVars = new java.util.Properties();
        
        try
        {
            if ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "SunOS" ) )
            {
                envVars.load( 
                     Runtime.getRuntime().exec( "/bin/env" ).getInputStream() );
            }
            else if (jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "WinNT"))
            {
                envVars.load(
                           Runtime.getRuntime().exec( "set" ).getInputStream());
            }
            else
            {
                System.err.println("Unable to load environment variables for " +
                          "for unknown OS " + jvmEnv.getProperty( "os.name" ));
                return null;
            }
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        
        envVars.list( System.out );
        return envVars;
    }
    
    /**
     * Utility method for setting the font and color of a JTextPane. The
     * result is roughly equivalent to calling setFont(...) and 
     * setForeground(...) on an AWT TextArea.
     */
    public static void setJTextPaneFont(JTextPane jtp, Font font, Color c) {
        // Start with the current input attributes for the JTextPane. This
        // should ensure that we do not wipe out any existing attributes
        // (such as alignment or other paragraph attributes) currently
        // set on the text area.
        MutableAttributeSet attrs = jtp.getInputAttributes();

        // Set the font family, size, and style, based on properties of
        // the Font object. Note that JTextPane supports a number of
        // character attributes beyond those supported by the Font class.
        // For example, underline, strike-through, super- and sub-script.
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

        // Set the font color
        StyleConstants.setForeground(attrs, c);

        // Retrieve the pane's document object
        StyledDocument doc = jtp.getStyledDocument();

        // Replace the style for the entire document. We exceed the length
        // of the document by 1 so that text entered at the end of the
        // document uses the attributes.
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
    }
    
    public static String speedToBeaufort(double speed)
    {
        int f = 0;
        while ( speed > (double) beaufort[f] )
        {
            f++;
        }
        return ("" + f);
    }
    
    public static String angleToCompass(double angle)
    {
        angle = angle % 360.0;
        if ( angle < 22.5 )
            return "N ";
        else if ( angle < 67.5 )
            return "NE";
        else if ( angle < 112.5 )
            return "E ";
        else if ( angle < 157.5 )
            return "SE";
        else if ( angle < 202.5 )
            return "S ";
        else if ( angle < 247.5 )
            return "SW";
        else if ( angle < 292.5 )
            return "W ";
        else if ( angle < 337.5 )
            return "NW";
        else
            return "N ";
    }
    
    public static boolean createDirectoryIfNotExists(String s) {
    	File path = new File(s);
    	if ( path.exists())
    	{
    		if (path.isDirectory()) {
    			return true;
    		} else {
    			EventLog.log(EventLog.SEV_ERROR,
    					"Path '" + s + "' exists but is not a directory");
    			return false;
    		}
    	} else {
    		if ( path.mkdirs() == true ) {
    			EventLog.log(EventLog.SEV_INFO,
    					"Directory '" + s + "' created");
    			return true;
    		} else {
    			EventLog.log(EventLog.SEV_FATAL,
    					"Direcory '" + s + "' could not be created");
    			return false;
    		}				
    	}
    }    	

    /**
     * Return the greatest common divisor
     */
    public static long gcd(long a, long b) {
    	if (b==0) { 
    		return a;
    	} else {
    		return gcd(b, a % b);
    	}
    } 
}
