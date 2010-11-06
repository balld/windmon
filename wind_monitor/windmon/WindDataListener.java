/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

/**
 * @author David
 *
 * Interface for classes that register as listeners to receive wind data events.
 */
public interface WindDataListener {
	public void windDataEventReceived(WindDataEvent e);
}
