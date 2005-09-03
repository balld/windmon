/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.text.DecimalFormat;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDataRecord {
	private static final String recordType = "W1";
	
	private long startTime;
	private long endTime;
	private int numReadings;
	private float minSpeed, aveSpeed, maxSpeed;
	private float aveAngle;
	
	
	/**
	 * @param startTime
	 * @param endTime
	 * @param numReadings
	 * @param minSpeed
	 * @param aveSpeed
	 * @param maxSpeed
	 * @param aveAngle
	 */
	WindDataRecord(long startTime, long endTime, int numReadings,
			float minSpeed, float aveSpeed, float maxSpeed, float aveAngle) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.numReadings = numReadings;
		this.minSpeed = minSpeed;
		this.aveSpeed = aveSpeed;
		this.maxSpeed = maxSpeed;
		this.aveAngle = aveAngle;
	}

	WindDataRecord()
	{
	}
	
	public String toString()
	{
		DecimalFormat df = new DecimalFormat("0.0");
		return (    recordType + "," + startTime + ","
	              + endTime + ","
	    	      + numReadings + ","
		          + df.format(minSpeed) + ","
		          + df.format(aveSpeed) + ","
		          + df.format(maxSpeed) + ","
		          + df.format(aveAngle) );
	}
	
	public static WindDataRecord parse (String s)
	{
		String i;
		WindDataRecord rec = new WindDataRecord();
		
		StringTokenizer tok = new StringTokenizer(s, ",");
		// Record type
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			if (!i.equals(recordType))
			{
				System.err.println("Unrecognised record type: " + i);
				return null;
			}
		}
		else
		{
			System.err.println("Record terminates early: " + s);
		}

		// Start Time
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setStartTime(Long.parseLong(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}

		// End Time
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setEndTime(Long.parseLong(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}

		// Number of Readings
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setNumReadings(Integer.parseInt(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}

		// Min wind speed
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setMinSpeed(Float.parseFloat(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}
		// Ave wind speed
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setAveSpeed(Float.parseFloat(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}
		// Max wind speed
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setMaxSpeed(Float.parseFloat(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}

		// Ave wind angle
		if ( tok.hasMoreTokens())
		{
			i = tok.nextToken();
			rec.setAveAngle(Float.parseFloat(i));
		}
		else
		{
			System.err.println("Record terminates early: " + s);
			return null;
		}
		return rec;
	}
	/**
	 * @return Returns the aveAngle.
	 */
	public float getAveAngle() {
		return aveAngle;
	}
	/**
	 * @param aveAngle The aveAngle to set.
	 */
	public void setAveAngle(float aveAngle) {
		this.aveAngle = aveAngle;
	}
	/**
	 * @return Returns the aveSpeed.
	 */
	public float getAveSpeed() {
		return aveSpeed;
	}
	/**
	 * @param aveSpeed The aveSpeed to set.
	 */
	public void setAveSpeed(float aveSpeed) {
		this.aveSpeed = aveSpeed;
	}
	/**
	 * @return Returns the endTime.
	 */
	public long getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime The endTime to set.
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return Returns the maxSpeed.
	 */
	public float getMaxSpeed() {
		return maxSpeed;
	}
	/**
	 * @param maxSpeed The maxSpeed to set.
	 */
	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	/**
	 * @return Returns the minSpeed.
	 */
	public float getMinSpeed() {
		return minSpeed;
	}
	/**
	 * @param minSpeed The minSpeed to set.
	 */
	public void setMinSpeed(float minSpeed) {
		this.minSpeed = minSpeed;
	}
	/**
	 * @return Returns the numReadings.
	 */
	public int getNumReadings() {
		return numReadings;
	}
	/**
	 * @param numReadings The numReadings to set.
	 */
	public void setNumReadings(int numReadings) {
		this.numReadings = numReadings;
	}
	/**
	 * @return Returns the startTime.
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime The startTime to set.
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
