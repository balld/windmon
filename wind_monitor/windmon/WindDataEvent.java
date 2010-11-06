/*
 * Created on 25-Aug-2005
 */
package windmon;

/**
 * @author David
 *
 * An event containing wind speed and direction data.
 */
public class WindDataEvent {
	private float windSpeed = 0.0f;
	private float windAngle = 0.0f;
	

	/**
	 * 
	 */
	public WindDataEvent() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param windSpeed
	 * @param windAngle
	 */
	public WindDataEvent(float windSpeed, float windAngle) {
		super();
		this.windSpeed = windSpeed;
		this.windAngle = windAngle;
	}
	/**
	 * @return Returns the windAngle.
	 */
	public float getWindAngle() {
		return windAngle;
	}
	/**
	 * @param windAngle The windAngle to set.
	 */
	public void setWindAngle(float windAngle) {
		this.windAngle = windAngle;
	}
	/**
	 * @return Returns the windSpeed.
	 */
	public float getWindSpeed() {
		return windSpeed;
	}
	/**
	 * @param windSpeed The windSpeed to set.
	 */
	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}
}
