/*
 * Created on 09-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

/**
 * @author BallD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NMEAController implements Runnable {

	private final double MAX_NMEA_PERIOD = 2.0;
    private Thread thread = null;
    
    private NMEALink link = null;
    private WindDisplay wv = null;
    
    private long sleepAmount = 0;
    
    public NMEAController (WindDisplay wv, NMEALink link)
    {
        this.wv = wv;
        this.link = link;
    }
    
    public void start() {
        if (thread == null) {
            if ( !link.isOpen())
            {
            	if ( !link.open() )
            	{
                    System.err.println("Link not opened. No input available.");
                    return;
            	}
            }
        	thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }

    public void run() {
        NMEAMessage msg = null;
        Thread me = Thread.currentThread();

        while (thread == me && link != null) {
            msg = link.getNMEAMessage();

            if ( msg != null
                 && msg.isValid()
                 && !msg.isProprietary()
                 && msg.getTalkerIDString().equals("WI")
                 && msg.getSentenceIDString().equals("MWV"))
            {
                wv.setWindSpeedAndAngle(Double.parseDouble(msg.getField(2)),
                		                Double.parseDouble(msg.getField(0)));
            }
            try {
                Thread.sleep(sleepAmount);
            } catch (InterruptedException e) { }
        }
        thread = null;
    }
    
    public void autoCalibrate()
    {
        String fields[] = { "IIMWV", "XCL" };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
        // Do other stuff to ensure this completes.
    }
    
    public void abortAutoCalibrate()
    {
        String fields[] = { "IIMWV", "XCL" };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void restoreDefaults()
    {
        String fields[] = { "IIMWV", "0CV" };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void setRefWindDir(double angle) throws Exception
    {
        if ( angle < 0.0 || angle > 359.9 )
        {
            throw new Exception("Illegal angle value :" + angle);
        }
        String fields[] = { "IIMWV", "AHD", Double.toString(angle) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void setWindDirDamping(double damp) throws Exception
    {
        if ( damp < 0.0 || damp > 100.0 )
        {
            throw new Exception("Illegal damping value :" + damp);
        }
        String fields[] = { "IIMWV", "DWD", Double.toString(damp) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void setRefWindSpeed(double speed) throws Exception
    {
        if ( speed < 0.0 )
        {
            throw new Exception("Illegal wind speed value :" + speed);
        }
        String fields[] = { "IIMWV", "ASP", Double.toString(speed) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void setWindSpeedInt(double period) throws Exception
    {
        if ( period < 0.0 || period > MAX_NMEA_PERIOD )
        {
            throw new Exception("Illegal time period :" + period);
        }
        String fields[] = { "IIMWV", "ISP", Double.toString(period) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }

    public void setWindSpeedDamping(double damp) throws Exception
    {
        if ( damp < 0.0 || damp > 100.0 )
        {
            throw new Exception("Illegal damping value :" + damp);
        }
        String fields[] = { "IIMWV", "DSP", Double.toString(damp) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
    
    public void setOutputPeriod(double period) throws Exception
    {
        if ( period < 0.0 || period > MAX_NMEA_PERIOD )
        {
            throw new Exception("Illegal time period :" + period);
        }
        String fields[] = { "IIMWV", "TXP", Double.toString(period) };
        NMEAMessage msg = new NMEAMessage("ATC", fields);
        link.sendNMEAMessage(msg);
    }
}
