package windmon;

import java.util.logging.Logger;

public class NMEALinkStub implements NMEALink
{
	private static final Logger logger = Logger.getLogger(NMEALinkStub.class.getName());
	
    // Flag indicating whether link is open
    private boolean openFlag = false;

    // Current wind wind direction velocity
    private double dir = 0.0;
    private double vel = 0.0;
    
    // Time of last message generated
    private long lastMsgTime = 0;
    
    // Minimum interval between messages (ms)
    private static final long interval = 100;
    // Sleep duration between time checks for next message (ms)
    private static final long sleepStep = 20;
    
    // This stub uses a constant reference direction and velocity, then
    // 'modulates' the output values using SIN curve and random amplitude
    private static final double refDir = 270.0; // West
    private static final double refVel = 20.0;  // 20 knots
    
    // sin input 'angle' in radians
    private double radDir = 0.0;
    private double radVel = 0.0;
    
    // amplitude ranges
    private static final double ampMinDir = 10.0;
    private static final double ampMaxDir = 30.0;
    private static final double ampMinVel = 5.0;
    private static final double ampMaxVel = 15.0;

    // current amplitude
    private double ampDir = ampMinDir;
    private double ampVel = ampMinVel;
    
    // sin input step sizes (fraction of PI works best)
    private static double radDirStep = Math.PI / 10.0;
    private static double radVelStep = Math.PI / 15.0;
    

    public NMEALinkStub() {
    }

    public boolean open()
    {
        if ( openFlag != true )
        {
            logger.finest("Opened stub link");
            openFlag = true;
            return true;
        }
        else
        {
            logger.severe("Can't open stub link. Already open");
            return false;
        }
    }
    
    public boolean close()
    {
        if ( openFlag == true )
        {
            logger.finest("Closed stub link");
            openFlag = false;
            return true;
        }
        else
        {
            logger.severe("Can't close stub link. Not open");
            return false;
        }
    }

    public NMEAMessage getNMEAMessage()
    {
        // Only return message if the link is open.
        if ( openFlag != true )
        {
            return null;
        }
        
        while ( System.currentTimeMillis() - this.lastMsgTime < interval )
        {
            try
            {
                Thread.sleep(sleepStep);
            } catch (Exception e) {
              // Ignore
            }
            
        }
        lastMsgTime = System.currentTimeMillis();
        
        // Calculate direction and wind speed from SIN curve
        dir = refDir + ampDir * Math.sin(radDir);
        vel = refVel + ampVel * Math.sin(radVel);
        String cmd = "$WIMWV," + dir + ",R," + vel + ",N,A*FF";

        // Update direction
        double oldRadDir = radDir;
        radDir += radDirStep;
        if ( radDir > 2.0 * Math.PI )
        {
            // Completed oscillation. Choose new random amplitude.
            radDir = 0.0;
            ampDir = ampMinDir + Math.random() * (ampMaxDir - ampMinDir);
        }
        else if ( radDir > Math.PI && oldRadDir <= Math.PI )
        {
            // Half oscillation complete. Change amplitude
            ampDir = ampMinDir + Math.random() * (ampMaxDir - ampMinDir);
        }

        // Update velocity
        double oldRadVel = radVel;
        radVel += radVelStep;
        if ( radVel > 2.0 * Math.PI )
        {
            // Completed oscillation. Choose new random amplitude.
            radVel = 0.0;
            ampVel = ampMinVel + Math.random() * (ampMaxVel - ampMinVel);
        }
        else if ( radVel > Math.PI && oldRadVel <= Math.PI )
        {
            // Half oscillation complete. Change amplitude
            ampVel = ampMinVel + Math.random() * (ampMaxVel - ampMinVel);
        }
        return new NMEAMessage(cmd);
    }
    
    public void sendNMEAMessage(NMEAMessage msg)
    {
        // Nowhere to send message so output to log
        logger.info("Stub sending : " + msg);
    }
    
    public boolean isOpen()
    {
		return openFlag;
    }
}