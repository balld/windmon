/*
 * Created on 30-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon.retired;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;

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
    
    // If anything has changed since last render, we need to know
    private boolean refresh = true;
    
    // Layout variables derived at start-up and on re-size
    private Dimension dim = null;
    private Insets ins = null;

    // Private data set vector
    private Vector dataSets = new Vector(); /* Vector<DataSet> */
    private Vector rulers = new Vector();   /* Vector<Ruler> */
    
    // Accessible variables (gets and sets needed)
    private double ymin = -1.0;
    private double ymax = 1.0; 
    private double xmin = -1.0;
    private double xmax = 1.0;
    private double xorig = 0.0;
    private double yorig = 0.0;
    private String title = "Graph Title";
    private String xlabel = "X Label";
    private String ylabel = "Y Label";
    private Vector xscale = null;
    private Vector yscale = null;
    private Font titleFont = new Font("serif", Font.PLAIN, 18);
    private Font labelFont = new Font("serif", Font.PLAIN, 12);
    private Font xscaleFont = new Font("serif", Font.PLAIN, 10);
    private Font yscaleFont = new Font("serif", Font.PLAIN, 10);
    private Color titleColor = Color.RED; 
    private Color labelColor = Color.RED;
    private Color xscaleColor = Color.YELLOW;
    private Color yscaleColor = Color.GREEN;
    private Color xaxisColor = Color.YELLOW;
    private Color yaxisColor = Color.GREEN;
    private Color plotAreaColor = Color.BLACK;
    private Color background = Color.GRAY;
    
    // Boundary Coordinates of data plot area - set later
    private int plotTop, plotBottom, plotLeft, plotRight, xplotOrig, yplotOrig;

    // Other layout values we set later.
    private int labelFontHeight, titleFontHeight, xscaleFontHeight,
	            yscaleFontWidth;
    private int xaxisTotalHeight, yaxisTotalHeight;
    private int xlen, ylen;

    // Other layout values we set now but may modify later
    private int xlabelSpacing = 5;
    private int ylabelSpacing = 5;
    private int titleSpacing  = 5;
    private int scaleMarkHeight = 5;
    private int yscaleMaxChars  = 5;
    
    private double xrange, yrange;

    
    Graph()
    {
        setDoubleBuffered(true);
        toolkit = getToolkit();
        setBackground(background);
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
        Insets insets = getInsets();
        
        // Set rendering options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

        // Check for uninitiated background images or Panel re-size
        if ( buffer == null || !dim.equals(size) || !ins.equals(insets) 
             || refresh == true)
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
        this.refresh = false;
        
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
        
        // OK - lets work out all these dimensions!
        labelFontHeight = buffer_g.getFontMetrics(labelFont).getHeight();
        titleFontHeight = buffer_g.getFontMetrics(titleFont).getHeight();
        xscaleFontHeight = buffer_g.getFontMetrics(xscaleFont).getHeight();
        yscaleFontWidth = buffer_g.getFontMetrics(yscaleFont).getMaxAdvance();
        
        xaxisTotalHeight =   labelFontHeight
		                   + xlabelSpacing
						   + xscaleFontHeight
						   + scaleMarkHeight;
        
        yaxisTotalHeight =   labelFontHeight
		                   + ylabelSpacing
						   + yscaleFontWidth * yscaleMaxChars
						   + scaleMarkHeight;
        
        plotTop = ins.top + titleFontHeight + titleSpacing;
        plotBottom = dim.height - ins.bottom - xaxisTotalHeight;
        plotLeft = ins.left + yaxisTotalHeight;
        plotRight = dim.width - ins.right - yaxisTotalHeight;
        
        xlen = plotRight - plotLeft;
        ylen = plotBottom - plotTop;
        
        /* Calculate ranges in scale */
        xrange = xmax - xmin;
        yrange = ymax - ymin;
        
        /* Offset of x-axis from plotTop */
        xplotOrig =   plotTop
		            + (int) (((double)ylen) * (ymax - yorig)/yrange);

        /* Offset of y-axis from plotLeft */
        yplotOrig =   plotLeft
		            + (int) (((double)xlen) * (xorig - xmin)/xrange);

        /* Shade in the plot area */
        buffer_g.setColor(plotAreaColor);
        buffer_g.fill(new Rectangle(plotLeft, plotTop, xlen, ylen));
        
        /* Draw the axis */
        buffer_g.setColor(xaxisColor);
        buffer_g.drawLine(plotLeft, xplotOrig, plotRight, xplotOrig);
        for (int i = 0; i < xscale.size(); i++)
        {
        	ScalePoint p = (ScalePoint) xscale.elementAt(i);
        	int offset = plotLeft + (int)((double)xlen * (p.getValue() - xmin) / xrange);
            buffer_g.setColor(xaxisColor);
        	buffer_g.drawLine(offset, xplotOrig,
        			          offset, xplotOrig + scaleMarkHeight);
            buffer_g.setColor(xscaleColor);
        	TextLayout tl = new TextLayout(p.getLabel(),
        			                       xscaleFont,
					                       buffer_g.getFontRenderContext());
        	tl.draw(buffer_g,
        			offset -(float) tl.getBounds().getCenterX(),
        			xplotOrig + scaleMarkHeight + xscaleFontHeight);
        }
        
        buffer_g.setColor(yaxisColor);
        buffer_g.drawLine(yplotOrig, plotTop, yplotOrig, plotBottom);
        for (int i = 0; i < yscale.size(); i++)
        {
        	ScalePoint p = (ScalePoint) yscale.elementAt(i);
        	int offset = plotTop + (int)((double)ylen * (ymax - p.getValue()) / yrange);
            buffer_g.setColor(yaxisColor);
        	buffer_g.drawLine(yplotOrig, offset,
        			          yplotOrig - scaleMarkHeight, offset);
            buffer_g.setColor(yscaleColor);
        	TextLayout tl = new TextLayout(p.getLabel(),
        			                       yscaleFont,
					                       buffer_g.getFontRenderContext());
        	tl.draw(buffer_g,
        			yplotOrig - scaleMarkHeight - (float) tl.getBounds().getWidth()
					          - 2,
        			offset - (float) tl.getBounds().getCenterY());
        }
        
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
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
		this.refresh = true;
	}
}
