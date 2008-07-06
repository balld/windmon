/*
 * Created on 23-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon.retired;

import java.awt.Color;
import java.util.Vector;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataSet {
	/* DataSet types */
	public static final int DS_TYPE_XY    = 1;
	public static final int DS_TYPE_RADAR = 2;
	
	/* DataSet Styles */
	public static final int DS_STYLE_LINE   = 1;
	public static final int DS_STYLE_POINTS = 2;

	private int type          = DS_TYPE_XY;
	private int style         = DS_STYLE_LINE;
	private Color color       = Color.RED;
	private int weight        = 1;
	private String label      = "data";
	private Vector dataPoints = new Vector(); /* Vector<DataPoint> */
	
	/**
	 * @param type
	 * @param style
	 * @param color
	 * @param weight
	 * @param label
	 * @param dataPoints
	 */
	public DataSet(int type, int style, Color color, int weight, String label,
			Vector dataPoints) {
		super();
		this.type = type;
		this.style = style;
		this.color = color;
		this.weight = weight;
		this.label = label;
		this.dataPoints = dataPoints;
	}
	
	/**
	 * 
	 */
	public DataSet() {
		super();
	}

	/**
	 * @return Returns the color.
	 */
	public Color getColor() {
		return color;
	}
	/**
	 * @param color The color to set.
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	/**
	 * @return Returns the dataPoints.
	 */
	public Vector getDataPoints() {
		return dataPoints;
	}
	/**
	 * @param dataPoints The dataPoints to set.
	 */
	public void setDataPoints(Vector dataPoints) {
		this.dataPoints = dataPoints;
	}
	/**
	 * @param dataPoints The dataPoints to append.
	 */
	public void addDataPoints(Vector dataPoints) {
		if ( this.dataPoints == null )
		{
			this.dataPoints = new Vector(dataPoints);
		}
		else
		{
			this.dataPoints.addAll(dataPoints);
		}
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return Returns the style.
	 */
	public int getStyle() {
		return style;
	}
	/**
	 * @param style The style to set.
	 */
	public void setStyle(int style) {
		this.style = style;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return Returns the weight.
	 */
	public int getWeight() {
		return weight;
	}
	/**
	 * @param weight The weight to set.
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
