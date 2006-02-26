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
	
	private JFreeChart speedChart;
	private JFreeChart angleChart;

	private DateAxis speedTimeAxis;
	private NumberAxis speedKnotsAxis;
	private DateAxis angleTimeAxis;
	private NumberAxis angleBearingAxis;
    
    private JTextPane ta;
    private Font b_font = null;
    private Font ta_font = null;
    private static int ta_font_size = 18;
    
    // Chart Clor Scheme
    private Color chartBackground = Color.WHITE;
    private Color plotBackground = new Color(0,32,90);
    private Color chartForeground = Color.BLACK;
    private Color plotForeground = Color.WHITE;

    public JFreeChartPlotter()
    {
        this(COL_SCHEME_BLUE);
    }
        
    
	public JFreeChartPlotter(int colScheme)
	{
		super();
        
        // Colour Scheme
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
		
		createCharts();

		speedChartPanel = new ChartPanel(speedChart);
		speedChartPanel.setMouseZoomable(false, false);
        speedChartPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(100, 100, 255),
                new Color(50, 50, 128)));
//		speedChartPanel.setSize(this.getSize());
        angleChartPanel = new ChartPanel(angleChart);
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
        this.repaint();
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
		
		// If records is empty, then clean out the graphs
		if ( records.length == 0 )
		{
			min.clear();
			ave.clear();
			max.clear();
			angle.clear();
		}
		else
		{
			// Add new data points to graph
			for (int i = 0; i<records.length; i++)
			{
				Second t = new Second(new Date(records[i].getEndTime()));
				// Assume if a time is in one chart, its in all
				if ( min.getDataItem(t) == null )
				{
					min.add(t, records[i].getMinSpeed());
					ave.add(t, records[i].getAveSpeed());
					max.add(t, records[i].getMaxSpeed());
					angle.add(t, records[i].getAveAngle());
				}
				if ( records[i].getMaxSpeed() > maxSpeed )
					maxSpeed = records[i].getMaxSpeed();
			}
			
			// Delete obsolete data points from graph (no longer in records).
			Second s = new Second(new Date(records[0].getEndTime()));
			int iend = 0;
			while ( iend < min.getItemCount() && min.getDataItem(iend).getPeriod().compareTo(s) < 0 )
			{
				iend++;
			}
			
			if ( iend > 0 )
			{
				min.delete(0, iend-1);
				ave.delete(0, iend-1);
				max.delete(0, iend-1);
				angle.delete(0, iend-1);
			}
		}
		speedKnotsAxis.setUpperBound(maxSpeed * 1.10);
	}
	
	private void createCharts()
	{
		speedChart = ChartFactory.createTimeSeriesChart(
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
		
		//
		// Angle Graph
		//
		angleChart = ChartFactory.createTimeSeriesChart(
				"Wind Angle",  // title
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
	}

	public void writeSpeedPlotPNG(String fname, int width, int height)
	{
		File tmpfile = new File(fname + ".tmp");
		try
		{
			ChartUtilities.saveChartAsPNG(tmpfile, speedChart, width, height);
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
			ChartUtilities.saveChartAsPNG(tmpfile, angleChart, width, height);
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
}
