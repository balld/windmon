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
import javax.swing.JDialog;
import javax.swing.JTextPane;
import java.awt.Dimension;


/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ButtonAbout extends ActionButton {

	JFrame fr;
	
	public ButtonAbout(String caption, Icon img, JFrame fr)
	{
		super(caption, img);
		this.fr = fr;
	}
	/* (non-Javadoc)
	 * @see windmon.ActionButton#actionePerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		JDialog dialog = new JDialog (fr, "About Wind Monitor", true);
		JTextPane tp = new JTextPane();
		tp.setText("Wind Monitor v0.1a\n" +
				"\n" +
				"NMEA visual display software\n" +
				"by David Ball.\n" );
		tp.setEditable(false);
		dialog.getContentPane().add(tp);
		dialog.setSize(new Dimension(400,400));
		dialog.validate();
		dialog.setVisible(true);
	}
}
