/*
 * Created on 28-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon.retired;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import windmon.Utils;

/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Banner extends JPanel
{
    private static int border_width = 5;
    private Image      logo = null;
    private Image      logo_text = null;

    public Banner ()
    {
        super();
        logo = Utils.getImage(this, "MSCLogo.gif");
        logo_text = Utils.getImage(this, "MSCLogoText.gif");
        
//        EmptyBorder eb = new EmptyBorder(border_width, border_width,
//                                         border_width, border_width);
//        SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
//        setBorder(new CompoundBorder(eb, sbb));
//        setBorder(eb);
        setBackground(Color.white);
    }
    
    public void paint (Graphics g)
    {
        super.paint(g);
        g.drawImage(logo, border_width, border_width, this);
        g.drawImage(logo_text,
                    logo.getWidth(this) + border_width,
                    logo.getHeight(this)
                    - logo_text.getHeight(this)
                    + border_width, this);
    }

    public Dimension getPreferredSize()
    {
        return new Dimension (  logo.getWidth(this)
                              + logo_text.getWidth(this)
                              + border_width * 2,
                              logo.getHeight(this)
                              + border_width * 2);
    }
}
