/*
 * Created on 28-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * @author BallD
 *
 * Panel containing club logo.
 */
public class LogoPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private static int border_width = 5;
    private Image      logo = null;
    private Image      logo_text = null;

    public LogoPanel ()
    {
        super();
        logo = Utils.getImage(this, "MSCFlag.gif");
        logo_text = Utils.getImage(this, "MSCLogoTextBlue.gif");
        setBackground(Color.BLACK);
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
        return new Dimension (logo.getWidth(this)
                              + logo_text.getWidth(this)
                              + border_width * 2,
                              logo.getHeight(this)
                              + border_width * 2);
    }
}
