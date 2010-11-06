/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

/**
 * @author david
 *
 * Interface for a UI classes that graphically plot wind data.
 */
public interface WindDataPlotter {
    public void setDisplayText(String buffer);
	public abstract void plotData(WindDataRecord records[]);
	public void writeSpeedPlotPNG(String fname, int width, int height);
	public void writeAnglePlotPNG(String fname, int width, int height);
}
