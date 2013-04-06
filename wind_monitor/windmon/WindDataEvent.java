package windmon;

public class WindDataEvent {
	private float windSpeed = 0.0f;
	private float windAngle = 0.0f;
	

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
