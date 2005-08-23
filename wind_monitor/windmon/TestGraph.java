/*
 * Created on 24-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.util.Vector;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestGraph extends Graph {

	private static final double xmin = -10.0;
	private static final double xmax = 5.0;
	private static final double ymin = -7.0;
	private static final double ymax = 7.0;
	private static final double xorig = 1.0;
	private static final double yorig = -2.0;
	
	public TestGraph()
	{
		super();
		setXmin(xmin);
		setXmax(xmax);
		setYmin(ymin);
		setYmax(ymax);
		setXorig(xorig);
		setYorig(yorig);
		
		setXscale(makeScale(xmin, xmax, 2));
		setYscale(makeScale(ymin, ymax, 1));
	}
	
	private static Vector makeScale(double min, double max, int interval)
	{
		Vector v = new Vector();
		for ( int i = (int) Math.ceil(min); i <= (int) max; i++)
		{
			v.add(new ScalePoint((double)i, "" + i));
		}
		
		return v;
	}
}
