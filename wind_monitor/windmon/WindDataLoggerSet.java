/*
 * Created on 25-Aug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WindDataLoggerSet implements Cloneable{
	
	protected long startPeriod, endPeriod;
	protected int numReadings = 0;
	protected float maxSpeed, minSpeed;
	protected double sumSpeed;
	protected double sumX, sumY;
	
	/**
	 * @param startPeriod
	 * @param endPeriod
	 */
	public WindDataLoggerSet()
	{
	}

	public void reset(long startPeriod)
	{
		this.startPeriod = startPeriod;
		endPeriod = Long.MAX_VALUE;
		numReadings = 0;
		maxSpeed = 0.0f;
		minSpeed = Float.MAX_VALUE;
		sumSpeed = 0.0f;
		sumX = 0.0;
		sumY = 0.0;
	}
	public void logData(float speed, float angle)
	{
		numReadings++;
		if ( speed > maxSpeed )
		{
			maxSpeed = speed;
		}
		if ( speed < minSpeed )
		{
			minSpeed = speed;
		}
		sumSpeed += speed;
		sumX += Math.sin(Math.toRadians(angle));
		sumY += Math.cos(Math.toRadians(angle));
	}
	
	public WindDataRecord generateWindDataRecord()
	{
		float aveAngle = 0.0f;
		float aveSpeed = 0.0f;
		if ( numReadings == 0)
		{
			minSpeed = -1.0f;
			maxSpeed = -1.0f;
			aveSpeed = -1.0f;
			aveAngle = -1.0f;
		}
		else
		{
			// Average speed. Obvious!
			aveSpeed = (float) (sumSpeed / numReadings);
			
			// Average angle. This is calculated as a vector average, i.e. X and Y
			// components individually.

			// Special cases
			if ( sumY == 0.0 && sumX == 0.0 )
			{
				// No average direction(unlikely!). Default to North
				aveAngle = 0.0f;
			}
			else if ( sumY == 0.0 )
			{
				if ( sumX > 0.0 )
					aveAngle = 90.0f;
				else
					aveAngle = 270.0f;
			}
			else
			{
				aveAngle = (float) (Math.toDegrees(Math.atan(sumX / sumY)));
				if ( sumY < 0.0)
				{
					aveAngle += 180.0f;
				}
				else if ( sumY > 0.0f && sumX < 0.0f )
				{
					aveAngle += 360.0f;
				}
			}
				
		}
		WindDataRecord rec = new WindDataRecord(startPeriod,
				                                endPeriod,
												numReadings,
												minSpeed,
												aveSpeed,
												maxSpeed,
												aveAngle);
		return (rec);
	}
	/**
	 * @return Returns the endPeriod.
	 */
	public long getEndPeriod() {
		return endPeriod;
	}
	/**
	 * @param endPeriod The endPeriod to set.
	 */
	public void setEndPeriod(long endPeriod) {
		this.endPeriod = endPeriod;
	}
	/**
	 * @return Returns the startPeriod.
	 */
	public long getStartPeriod() {
		return startPeriod;
	}
	/**
	 * @param startPeriod The startPeriod to set.
	 */
	public void setStartPeriod(long startPeriod) {
		this.startPeriod = startPeriod;
	}
	
	public Object clone()
	{
		try {
			return super.clone();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
