/*
 * Created on 23-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.Color;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Ruler {
	
	public static final int RULER_TYPE_X     = 1;
	public static final int RULER_TYPE_Y     = 2;
	public static final int RULER_TYPE_RADAR = 3;
	
	public static final int RULER_STYLE_LINE = 1;
	
	private double val  = 0.0;
	private int type    = RULER_TYPE_X;
	private int style   = RULER_STYLE_LINE;
	private Color color = Color.RED;
	private int weight  = 1;
	
	/**
	 * 
	 */
	public Ruler() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param val
	 * @param type
	 * @param style
	 * @param color
	 * @param weight
	 */
	public Ruler(double val, int type, int style, Color color, int weight) {
		super();
		this.val = val;
		this.type = type;
		this.style = style;
		this.color = color;
		this.weight = weight;
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
	 * @return Returns the val.
	 */
	public double getVal() {
		return val;
	}
	/**
	 * @param val The val to set.
	 */
	public void setVal(double val) {
		this.val = val;
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
