/*
 * Created on Aug 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon.retired;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import java.util.Hashtable;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ActionButton extends AbstractAction implements Action {

	Hashtable properties;

	public ActionButton(String caption, Icon img)
	{
		properties = new Hashtable();
		properties.put(DEFAULT, caption);
		properties.put(NAME, caption);
		properties.put(SHORT_DESCRIPTION, caption);
		properties.put(SMALL_ICON, img);
	}
	
	public void putValue(String key, Object value)
	{
		properties.put(key, value);
	}
	
	public Object getValue(String key)
	{
		return properties.get(key);
	}
	
	public abstract void actionPerformed(ActionEvent e);
}
