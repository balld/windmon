/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

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
import org.jfree.ui.RectangleInsets;



/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JFreeChartPlotter extends JPanel implements WindDataPlotter {

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
	
	public JFreeChartPlotter()
	{
		super();
		setBackground(Color.red);
		setPreferredSize(new Dimension(600, 300));
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
//		speedChartPanel.setSize(this.getSize());
        angleChartPanel = new ChartPanel(angleChart);
		angleChartPanel.setMouseZoomable(false, false);
//		angleChartPanel.setSize(this.getSize());

        GridLayout gl = new GridLayout(1,2);
        setLayout(gl);
        add(speedChartPanel);
        add(angleChartPanel);
	}

	/* (non-Javadoc)
	 * @see windmon.WindDataPlotter#plotData(windmon.WindDataRecord[])
	 */
	public void plotData(WindDataRecord[] records) {
		// TODO Auto-generated method stub
		updateDatasets(records);
        this.repaint();
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
		
		speedChart.setBackgroundPaint(Color.black);
		speedChart.getTitle().setPaint(Color.WHITE);
		
		XYPlot plot = (XYPlot) speedChart.getPlot();
		plot.setBackgroundPaint(Color.black);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
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
		speedTimeAxis.setLabelPaint(Color.WHITE);
		speedTimeAxis.setAxisLinePaint(Color.WHITE);
		speedTimeAxis.setTickLabelPaint(Color.WHITE);
		
		speedKnotsAxis = (NumberAxis) plot.getRangeAxis();
		speedKnotsAxis.setLowerBound(0.0);
		speedKnotsAxis.setUpperBound(speedDefaultMax * 1.10);
		speedKnotsAxis.setLabelPaint(Color.WHITE);
		speedKnotsAxis.setAxisLinePaint(Color.WHITE);
		speedKnotsAxis.setTickLabelPaint(Color.WHITE);
		
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
		
		angleChart.setBackgroundPaint(Color.black);
		angleChart.getTitle().setPaint(Color.WHITE);
		
		XYPlot a_plot = (XYPlot) angleChart.getPlot();
		a_plot.setBackgroundPaint(Color.black);
		a_plot.setDomainGridlinePaint(Color.white);
		a_plot.setRangeGridlinePaint(Color.white);
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
		}
 
		angleTimeAxis = (DateAxis) a_plot.getDomainAxis();
		angleTimeAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
		angleTimeAxis.setLabelPaint(Color.WHITE);
		angleTimeAxis.setAxisLinePaint(Color.WHITE);
		angleTimeAxis.setTickLabelPaint(Color.WHITE);
		
		angleBearingAxis = (NumberAxis) a_plot.getRangeAxis();
		angleBearingAxis.setLowerBound(0.0);
		angleBearingAxis.setUpperBound(360.0);
		angleBearingAxis.setLabelPaint(Color.WHITE);
		angleBearingAxis.setAxisLinePaint(Color.WHITE);
		angleBearingAxis.setTickLabelPaint(Color.WHITE);
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
