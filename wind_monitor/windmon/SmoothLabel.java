/*
 * Created on 12-Feb-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;


/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SmoothLabel extends JLabel {

    
    /**
     * 
     */
    public SmoothLabel() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param text
     */
    public SmoothLabel(String text) {
        super(text);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param text
     * @param horizontalAlignment
     */
    public SmoothLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param text
     * @param icon
     * @param horizontalAlignment
     */
    public SmoothLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param image
     */
    public SmoothLabel(Icon image) {
        super(image);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param image
     * @param horizontalAlignment
     */
    public SmoothLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        // TODO Auto-generated constructor stub
    }
    protected void paintComponent(Graphics g)
    {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                         RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
    }
}
