package windmon;

public interface WindDataPlotter {
    public void setDisplayText(String buffer);
	public abstract void plotData(WindDataRecord records[]);
	public void writeSpeedPlotPNG(String fname, int width, int height);
	public void writeAnglePlotPNG(String fname, int width, int height);
}
