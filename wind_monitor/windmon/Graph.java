/*
 * Created on 30-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
// import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Vector;

/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Graph extends JPanel {
	// Preferred dimensions
    private static final Dimension ps = new Dimension ( 400, 400 );
    
    private Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object Rendering = RenderingHints.VALUE_RENDER_SPEED;
    private boolean anti_alias = true;
    
    // Graphics Related Variables
    private Toolkit toolkit = null;
    private Image     buffer = null;
    private Graphics2D  buffer_g = null;
    
    // Layout variables derived at start-up and on re-size
    private Dimension dim = null;
    private Insets ins = null;

    // Private data set vector
    private Vector dataSets = new Vector();
    private Vector xrules = new Vector();
    private Vector yrules = new Vector();
    
    // Public variables (gets and sets needed)
    public double ymin = -1.0;
    public double ymax = 1.0; 
    public double xmin = -1.0;
    public double xmax = 1.0;
    public double xorig = 0.0;
    public double yorig = 0.0;
    public String title = "Graph Title";
    public String xlabel = "X Label";
    public String ylabel = "Y Label";
    public Vector xscale = null;
    public Vector yscale = null;
    public Font titleFont = new Font("serif", Font.PLAIN, 18);
    public Font labelFont = new Font("serif", Font.PLAIN, 12);
    public Font xscaleFont = new Font("serif", Font.PLAIN, 10);
    public Font yscaleFont = new Font("serif", Font.PLAIN, 10);
    public Color titleColor = Color.RED; 
    public Color labelColor = Color.RED;
    public Color xscaleColor = Color.YELLOW;
    public Color yscaleColor = Color.GREEN;
    public Color xaxisColor = Color.YELLOW;
    public Color yaxisColor = Color.GREEN;
    public Color plotAreaColor = Color.GRAY;
    
    // private layout variables
    
    Graph()
    {
        setDoubleBuffered(true);
        toolkit = getToolkit();
        setBackground(Color.lightGray);
        setPreferredSize(ps);
    }

    
    public void paint(Graphics g)
    {
        // Run super class paint method
        super.paint(g);

        AffineTransform at = new AffineTransform();
        
        // Create Graphics2D object for advanced rendering.
        Graphics2D g2 = (Graphics2D) g;

        // Obtain the current size of this component
        Dimension size = getSize();
        
        // Set rendering options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        // Check for uninitiated background images or Panel re-size
        if ( buffer == null || !dim.equals(size))
        {
            renderGraph(g2);
        }
        
        // Draw the wind direction dial background
        g2.drawImage(buffer, 0, 0, this);
    }

    private void renderGraph(Graphics2D g2)
    {
        this.dim = getSize();
        this.ins = getInsets();
        
        // Dispose of old graphics object. Good practice I think.
        if ( buffer_g != null)
        {
            buffer_g.dispose();
            buffer_g = null;
        }

        // Create buffered image
        buffer = (BufferedImage) g2.getDeviceConfiguration().
                                     createCompatibleImage(dim.width,
                                                          dim.height,
                                                          Transparency.BITMASK);  
        buffer_g = (Graphics2D) buffer.getGraphics();

        // Set rendering properties        
        buffer_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        buffer_g.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);
    }

    
	/**
	 * @return Returns the labelColor.
	 */
	public Color getLabelColor() {
		return labelColor;
	}
	/**
	 * @param labelColor The labelColor to set.
	 */
	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}
	/**
	 * @return Returns the labelFont.
	 */
	public Font getLabelFont() {
		return labelFont;
	}
	/**
	 * @param labelFont The labelFont to set.
	 */
	public void setLabelFont(Font labelFont) {
		this.labelFont = labelFont;
	}
	/**
	 * @return Returns the plotAreaColor.
	 */
	public Color getPlotAreaColor() {
		return plotAreaColor;
	}
	/**
	 * @param plotAreaColor The plotAreaColor to set.
	 */
	public void setPlotAreaColor(Color plotAreaColor) {
		this.plotAreaColor = plotAreaColor;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the titleColor.
	 */
	public Color getTitleColor() {
		return titleColor;
	}
	/**
	 * @param titleColor The titleColor to set.
	 */
	public void setTitleColor(Color titleColor) {
		this.titleColor = titleColor;
	}
	/**
	 * @return Returns the titleFont.
	 */
	public Font getTitleFont() {
		return titleFont;
	}
	/**
	 * @param titleFont The titleFont to set.
	 */
	public void setTitleFont(Font titleFont) {
		this.titleFont = titleFont;
	}
	/**
	 * @return Returns the xaxisColor.
	 */
	public Color getXaxisColor() {
		return xaxisColor;
	}
	/**
	 * @param xaxisColor The xaxisColor to set.
	 */
	public void setXaxisColor(Color xaxisColor) {
		this.xaxisColor = xaxisColor;
	}
	/**
	 * @return Returns the xlabel.
	 */
	public String getXlabel() {
		return xlabel;
	}
	/**
	 * @param xlabel The xlabel to set.
	 */
	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}
	/**
	 * @return Returns the xmax.
	 */
	public double getXmax() {
		return xmax;
	}
	/**
	 * @param xmax The xmax to set.
	 */
	public void setXmax(double xmax) {
		this.xmax = xmax;
	}
	/**
	 * @return Returns the xmin.
	 */
	public double getXmin() {
		return xmin;
	}
	/**
	 * @param xmin The xmin to set.
	 */
	public void setXmin(double xmin) {
		this.xmin = xmin;
	}
	/**
	 * @return Returns the xorig.
	 */
	public double getXorig() {
		return xorig;
	}
	/**
	 * @param xorig The xorig to set.
	 */
	public void setXorig(double xorig) {
		this.xorig = xorig;
	}
	/**
	 * @return Returns the xscale.
	 */
	public Vector getXscale() {
		return xscale;
	}
	/**
	 * @param xscale The xscale to set.
	 */
	public void setXscale(Vector xscale) {
		this.xscale = xscale;
	}
	/**
	 * @return Returns the xscaleColor.
	 */
	public Color getXscaleColor() {
		return xscaleColor;
	}
	/**
	 * @param xscaleColor The xscaleColor to set.
	 */
	public void setXscaleColor(Color xscaleColor) {
		this.xscaleColor = xscaleColor;
	}
	/**
	 * @return Returns the xscaleFont.
	 */
	public Font getXscaleFont() {
		return xscaleFont;
	}
	/**
	 * @param xscaleFont The xscaleFont to set.
	 */
	public void setXscaleFont(Font xscaleFont) {
		this.xscaleFont = xscaleFont;
	}
	/**
	 * @return Returns the yaxisColor.
	 */
	public Color getYaxisColor() {
		return yaxisColor;
	}
	/**
	 * @param yaxisColor The yaxisColor to set.
	 */
	public void setYaxisColor(Color yaxisColor) {
		this.yaxisColor = yaxisColor;
	}
	/**
	 * @return Returns the ylabel.
	 */
	public String getYlabel() {
		return ylabel;
	}
	/**
	 * @param ylabel The ylabel to set.
	 */
	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}
	/**
	 * @return Returns the ymax.
	 */
	public double getYmax() {
		return ymax;
	}
	/**
	 * @param ymax The ymax to set.
	 */
	public void setYmax(double ymax) {
		this.ymax = ymax;
	}
	/**
	 * @return Returns the ymin.
	 */
	public double getYmin() {
		return ymin;
	}
	/**
	 * @param ymin The ymin to set.
	 */
	public void setYmin(double ymin) {
		this.ymin = ymin;
	}
	/**
	 * @return Returns the yorig.
	 */
	public double getYorig() {
		return yorig;
	}
	/**
	 * @param yorig The yorig to set.
	 */
	public void setYorig(double yorig) {
		this.yorig = yorig;
	}
	/**
	 * @return Returns the yscale.
	 */
	public Vector getYscale() {
		return yscale;
	}
	/**
	 * @param yscale The yscale to set.
	 */
	public void setYscale(Vector yscale) {
		this.yscale = yscale;
	}
	/**
	 * @return Returns the yscaleColor.
	 */
	public Color getYscaleColor() {
		return yscaleColor;
	}
	/**
	 * @param yscaleColor The yscaleColor to set.
	 */
	public void setYscaleColor(Color yscaleColor) {
		this.yscaleColor = yscaleColor;
	}
	/**
	 * @return Returns the yscaleFont.
	 */
	public Font getYscaleFont() {
		return yscaleFont;
	}
	/**
	 * @param yscaleFont The yscaleFont to set.
	 */
	public void setYscaleFont(Font yscaleFont) {
		this.yscaleFont = yscaleFont;
	}
}
