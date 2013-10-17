package windmon;

import java.text.NumberFormat;

import org.jfree.chart.axis.NumberTickUnit;

public class JFreeCompassTickUnit extends NumberTickUnit {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] COMPASS_POINTS_22_5 = {
			"  N", //   0.0
			"NNE", //  22.5
			" NE", //  45.0
			"ENE", //  67.5
			"  E", //  90.0
			"ESE", // 112.5 
			" SE", // 135.0
			"SSE", // 157.5
			"  S", // 180.0
			"SSW", // 202.5
			" SW", // 225.0
			"WSW", // 247.5
			"  W", // 270.0
			"WNW", // 292.5
			" NW", // 315.0
			"NNW"}; // 337.5
	
	public JFreeCompassTickUnit (double size, NumberFormat formatter)
	{
		super(size, formatter);
	}
	
	public JFreeCompassTickUnit (double size)
	{
		this(size, NumberFormat.getNumberInstance());
	}
	
	public java.lang.String valueToString(double valueIn)
	{
		String label = null;
		
		// Value may be outside 0 - 359.9999 degrees. Calculate modulo
		double value = valueIn % 360.0;
		// If value was <0, will also need to move into positive range
		if ( value < 0.0 )
			value += 360.0;
		
		// If its not a multiple of 22.5, pass up to NumberTickUnit
		if ( value % 22.5 != 0.0 )
		{
			label = super.valueToString(value);
		}
		else
		{
			int i = (int) (value / 22.5);
			label = COMPASS_POINTS_22_5[i];
		}
		
		return label;
	}
}
