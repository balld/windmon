/*
 * Created on 20-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.JDialog;
import java.awt.Dimension;


/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ButtonOptions extends ActionButton {

	JFrame fr;
	
	public ButtonOptions(String caption, Icon img, JFrame fr)
	{
		super(caption, img);
		this.fr = fr;
	}
	/* (non-Javadoc)
	 * @see windmon.ActionButton#actionePerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		JDialog dialog = new JDialog (fr, "Options", true);
		NMEAController nmea = NMEAController.getInstance();
		if ( nmea == null )
		{
			return;
		}
		
		OptionsPanel op = new OptionsPanel(nmea);
		dialog.getContentPane().add(op);
		dialog.setSize(new Dimension(650,300));
		dialog.validate();
		dialog.setVisible(true);
	}
}
