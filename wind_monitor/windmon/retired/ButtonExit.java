/*
 * Created on 20-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon.retired;

import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.Icon;


/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ButtonExit extends ActionButton {

	JFrame fr;
	
	public ButtonExit(String caption, Icon img, JFrame frm)
	{
		super(caption, img);
		this.fr = frm;
	}
	/* (non-Javadoc)
	 * @see windmon.ActionButton#actionePerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		System.exit(0);
	}
}
