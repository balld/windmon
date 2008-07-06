/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.StringContent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.text.TextBox;
import org.jfree.ui.RectangleInsets;



/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JFreeChartPlotter extends JPanel implements WindDataPlotter {

    public static final int COL_SCHEME_BLACK  = 1;
    public static final int COL_SCHEME_BLUE  = 2;

    private double speedDefaultMax = 2.0; 
	
	private ChartPanel speedChartPanel = null;
	private ChartPanel angleChartPanel = null;

	private TimeSeriesCollection speedDataset = null;
	private TimeSeriesCollection angleDataset = null;

	TimeSeries min;
	TimeSeries ave;
	TimeSeries max;
	TimeSeries angle;
	
    private JFreeChart screenSpeedChart;
    private JFreeChart screenAngleChart;
    private JFreeChart imageSpeedChart;
    private JFreeChart imageAngleChart;

	private DateAxis speedTimeAxis;
	private NumberAxis speedKnotsAxis;
	private DateAxis angleTimeAxis;
	private NumberAxis angleBearingAxis;
    
    private JTextPane ta;
    private Font b_font = null;
    private Font ta_font = null;
    private static int ta_font_size = 18;
    
    // Need to store number Axis and update
    private Vector numberAxisVec = new Vector();

    public JFreeChartPlotter()
    {
        this(COL_SCHEME_BLACK, COL_SCHEME_BLUE);
    }
        
    
	public JFreeChartPlotter(int screenColScheme, int imageColScheme)
	{
		super();
        
        // Colour Scheme

        setBackground(Color.BLACK);
        
        setPreferredSize(new Dimension(500, 300));

        speedDataset = new TimeSeriesCollection();
        speedDataset.setDomainIsPointsInTime(true);
        angleDataset = new TimeSeriesCollection();
        angleDataset.setDomainIsPointsInTime(true);

		min = new TimeSeries("Min", Second.class);
		ave = new TimeSeries("Ave", Second.class);
		max = new TimeSeries("Max", Second.class);
		angle = new TimeSeries("Direction", Second.class);

        speedDataset.addSeries(max);
        speedDataset.addSeries(ave);
		speedDataset.addSeries(min);
        angleDataset.addSeries(angle);
		
		screenSpeedChart = createSpeedChart(screenColScheme);
        screenAngleChart = createAngleChart(screenColScheme);
        imageSpeedChart = createSpeedChart(imageColScheme);
        imageAngleChart = createAngleChart(imageColScheme);

		speedChartPanel = new ChartPanel(screenSpeedChart);
		speedChartPanel.setMouseZoomable(false, false);
        speedChartPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));
//		speedChartPanel.setSize(this.getSize());
        angleChartPanel = new ChartPanel(screenAngleChart);
		angleChartPanel.setMouseZoomable(false, false);
        angleChartPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));
//		angleChartPanel.setSize(this.getSize());

        GridLayout gl = new GridLayout(2,1);
        //gl.setHgap(5);
        //gl.setVgap(5);
        setLayout(gl);
        add(speedChartPanel);
        add(angleChartPanel);

//        JPanel jp2 = new JPanel();
//        jp2.setLayout(new GridLayout(2,1));
//        ta = new JTextPane();
//        ta.setContentType("text/html");
//        b_font = Utils.getFont("LCD-N___.TTF");
//        b_font = Font.getFont("Arial");
//        ta_font = b_font.deriveFont(Font.PLAIN, ta_font_size);
//        Utils.setJTextPaneFont(ta, ta_font, Color.BLACK);
//        jp2.add(ta);

//        DigitalClock dc = new DigitalClock();
//        dc.setBackground(Color.BLACK);
//        dc.setForeground(Color.WHITE);
//        jp2.add(dc);
//        dc.start();
//        
//        add(jp2);
	}

	/* (non-Javadoc)
	 * @see windmon.WindDataPlotter#plotData(windmon.WindDataRecord[])
	 */
	public void plotData(WindDataRecord[] records) {
		// TODO Auto-generated method stub
		updateDatasets(records);
        // this.repaint();
	}

    public void setDisplayText(String buffer)
    {
//        ta.setText(buffer);
//        Utils.setJTextPaneFont(ta, ta_font, Color.BLACK);
//        ta.repaint();
    }
    
	private void updateDatasets(WindDataRecord[] records)
	{
		double maxSpeed = speedDefaultMax;
		min.setNotify(false);
        ave.setNotify(false);
        max.setNotify(false);
        angle.setNotify(false);

        min.clear();
        ave.clear();
        max.clear();
        angle.clear();

        for ( int i = 0; i < records.length; i++ )
        {
            Second t = new Second(new Date(records[i].getEndTime()));
            // Assume if a time is in one chart, its in all
            min.add(t, records[i].getMinSpeed());
            ave.add(t, records[i].getAveSpeed());
            max.add(t, records[i].getMaxSpeed());
            angle.add(t, records[i].getAveAngle());
            
            if ( records[i].getMaxSpeed() > maxSpeed )
                maxSpeed = records[i].getMaxSpeed();
//            EventLog.log(EventLog.SEV_DEBUG, "Added data item :" + records[i]);
        }
			
        setNumberAxisUpperBound(maxSpeed * 1.10);

        min.setNotify(true);
        ave.setNotify(true);
        max.setNotify(true);
        angle.setNotify(true);

	}
	
	private JFreeChart createSpeedChart(int colScheme)
	{
        // Chart Clor Scheme
        Color chartBackground = Color.WHITE;
        Color plotBackground = new Color(0,32,90);
        Color chartForeground = Color.BLACK;
        Color plotForeground = Color.WHITE;

        switch (colScheme)
        {
        case COL_SCHEME_BLUE:
            chartBackground = Color.WHITE;
            plotBackground = new Color(0,32,90);
            chartForeground = Color.BLACK;
            plotForeground = Color.WHITE;
            break;
        default:
            chartBackground = Color.BLACK;
            plotBackground = Color.BLACK;
            chartForeground = Color.WHITE;
            plotForeground = Color.WHITE;
            break;
        }
	   
        JFreeChart speedChart = ChartFactory.createTimeSeriesChart(
				"Wind Speed",  // title
				"Time",        // x-axis label
				"Knots",       // y-axis label
				speedDataset,       // data
				true,          // create legend?
				false,         // generate tooltips?
				false          // generate URLs?
		);
		
		speedChart.setBackgroundPaint(chartBackground);
		speedChart.getTitle().setPaint(chartForeground);
		
		XYPlot plot = (XYPlot) speedChart.getPlot();
		plot.setBackgroundPaint(plotBackground);
		plot.setDomainGridlinePaint(plotForeground);
		plot.setRangeGridlinePaint(plotForeground);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setDefaultShapesVisible(false);
			renderer.setDefaultShapesFilled(false);
			renderer.setDefaultLinesVisible(true);
		}
 
		speedTimeAxis = (DateAxis) plot.getDomainAxis();
		speedTimeAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
		speedTimeAxis.setLabelPaint(chartForeground);
		speedTimeAxis.setAxisLinePaint(chartForeground);
		speedTimeAxis.setTickLabelPaint(chartForeground);
		
		speedKnotsAxis = (NumberAxis) plot.getRangeAxis();
		speedKnotsAxis.setLowerBound(0.0);
		speedKnotsAxis.setUpperBound(speedDefaultMax * 1.10);
		speedKnotsAxis.setLabelPaint(chartForeground);
		speedKnotsAxis.setAxisLinePaint(chartForeground);
		speedKnotsAxis.setTickLabelPaint(chartForeground);
        addNumberAxis(speedKnotsAxis);

        return speedChart;
    }
    
    private JFreeChart createAngleChart(int colScheme)
    {
        // Chart Clor Scheme
        Color chartBackground = Color.WHITE;
        Color plotBackground = new Color(0,32,90);
        Color chartForeground = Color.BLACK;
        Color plotForeground = Color.WHITE;

        switch (colScheme)
        {
        case COL_SCHEME_BLUE:
            chartBackground = Color.WHITE;
            plotBackground = new Color(0,32,90);
            chartForeground = Color.BLACK;
            plotForeground = Color.WHITE;
            break;
        default:
            chartBackground = Color.BLACK;
            plotBackground = Color.BLACK;
            chartForeground = Color.WHITE;
            plotForeground = Color.WHITE;
            break;
        }
       
		JFreeChart angleChart = ChartFactory.createTimeSeriesChart(
				"Wind Direction",  // title
				"Time",        // x-axis label
				"Bearing",       // y-axis label
				angleDataset,       // data
				true,          // create legend?
				false,         // generate tooltips?
				false          // generate URLs?
		);
		
		angleChart.setBackgroundPaint(chartBackground);
		angleChart.getTitle().setPaint(chartForeground);
		
		XYPlot a_plot = (XYPlot) angleChart.getPlot();
		a_plot.setBackgroundPaint(plotBackground);
		a_plot.setDomainGridlinePaint(plotForeground);
		a_plot.setRangeGridlinePaint(plotForeground);
		a_plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		a_plot.setDomainCrosshairVisible(true);
		a_plot.setRangeCrosshairVisible(true);

		XYItemRenderer ar = a_plot.getRenderer();
		if (ar instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ar;
			renderer.setDefaultShapesVisible(true);
			renderer.setDefaultShapesFilled(false);
			renderer.setDefaultLinesVisible(false);
            renderer.setSeriesShape(0, new Rectangle(-1, -1, 2, 2), false);
            renderer.setSeriesVisibleInLegend(new Boolean(false), false);
		}
 
		angleTimeAxis = (DateAxis) a_plot.getDomainAxis();
		angleTimeAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
		angleTimeAxis.setLabelPaint(chartForeground);
		angleTimeAxis.setAxisLinePaint(chartForeground);
		angleTimeAxis.setTickLabelPaint(chartForeground);
		
		angleBearingAxis = (NumberAxis) a_plot.getRangeAxis();
		angleBearingAxis.setLowerBound(0.0);
		angleBearingAxis.setUpperBound(360.0);
		angleBearingAxis.setLabelPaint(chartForeground);
		angleBearingAxis.setAxisLinePaint(chartForeground);
		angleBearingAxis.setTickLabelPaint(chartForeground);
		angleBearingAxis.setTickUnit(new JFreeCompassTickUnit(45.0));
        
        return angleChart;
	}

	public void writeSpeedPlotPNG(String fname, int width, int height)
	{
        File tmpfile = new File(fname + ".tmp");
		try
		{
			ChartUtilities.saveChartAsPNG(tmpfile, imageSpeedChart, width, height);
		}
		catch (Exception e)
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not create image file '" + fname + "'");
			e.printStackTrace();
		}

		File file = new File(fname);
		if ( tmpfile.renameTo(file) != true )
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not rename image to '" + fname + "'");
		}
	}
	

	public void writeAnglePlotPNG(String fname, int width, int height)
	{
		File tmpfile = new File(fname + ".tmp");
		try
		{
			ChartUtilities.saveChartAsPNG(tmpfile, imageAngleChart, width, height);
		}
		catch (Exception e)
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not create image file '" + fname + "'");
			e.printStackTrace();
		}

		File file = new File(fname);
		if ( tmpfile.renameTo(file) != true )
		{
			EventLog.log(EventLog.SEV_ERROR, "Could not rename image to '" + fname + "'");
		}
	}
    
    private void addNumberAxis(NumberAxis na)
    {
        numberAxisVec.add(na);
    }
    
    private void removeNumberAxis(NumberAxis na)
    {
        numberAxisVec.remove(na);
    }
    
    private void setNumberAxisUpperBound(double d)
    {
        Iterator iter = numberAxisVec.iterator();
        while ( iter.hasNext() )
        {
            ((NumberAxis) iter.next()).setUpperBound(d);
        }
    }
    
    
}
