package windmon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NMEAController implements Runnable {
  private static final Logger logger = Logger.getLogger(NMEAController.class.getName());

  private static NMEAController instance = null;
  private final double MAX_NMEA_PERIOD = 2.0;
  private Thread thread = null;
  private NMEALink link = null;
  private NMEASocketServer server = null;
  private long retryInterval = 0;
  private float dirZeroOffset= 0.0f;


  private List<WindDataListener> listeners = new ArrayList<WindDataListener>();

  private NMEAController (NMEALink link, NMEASocketServer server)
  {
    this.link = link;
    this.server = server;
    instance = this;
    this.configure();
  }

  private void configure()
  {
    retryInterval = Config.getParamAsLong("NMEALinkRetryIntervalSec", 10) * 1000l;
    dirZeroOffset = Config.getParamAsFloat("DirectionZeroOffset",  0.0f);
  }
  public static NMEAController getInstance()
  {
    return instance;
  }

  public static NMEAController getCreateInstance(NMEALink link, NMEASocketServer server)
  {
    if (instance == null)
    {
      instance = new NMEAController(link, server);
    }
    return instance;
  }


  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
    if (server != null) {
      server.start();
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
    boolean display_cleared = false;

    while (thread == me && link != null)
    {
      logger.info("Checking for connection...");
          while (!link.isOpen())
          {
            if ( !link.open() )
            {
              if ( !display_cleared )
              {
                dispatchWindDataEvent(-1.0f, -1.0f);
                display_cleared = true;
              }
              logger.info("Link not opened. Will retry...");
              Utils.justSleep(retryInterval);
            }
          }
          display_cleared = false;

          do
          {
            msg = link.getNMEAMessage();

            if ( msg != null
                && msg.isValid()
                && !msg.isProprietary()
                && msg.getTalkerIDString().equals("WI")
                && msg.getSentenceIDString().equals("MWV"))
            {
              dispatchWindDataEvent(applyZeroOffset(Float.parseFloat(msg.getField(0))),
                  Float.parseFloat(msg.getField(2)));
              if (server != null) {
                server.sendMessage(msg);
              }
            }
          } while (msg != null );
          dispatchWindDataEvent(-1.0f, -1.0f);
          display_cleared = true;
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

  public void addWindDataListener (WindDataListener l)
  {
    listeners.add(l);
  }

  public void removeWindDataListener (WindDataListener l)
  {
    listeners.remove(l);
  }

  protected void dispatchWindDataEvent(float angle, float speed)
  {
    WindDataEvent wde = new WindDataEvent(speed, angle);
    for (int i = 0; i < listeners.size(); i++)
    {
      ((WindDataListener)listeners.get(i)).windDataEventReceived(wde);
    }
  }
  
  /**
   * Apply zero offset parameter to direction in NMEA message.
   * Ensure range is in range 0.0 to 360.0.
   * @param rawDir
   * @return Direction with zero offset applied.
   */
  private float applyZeroOffset(float rawDir) {
    float correctedDir = rawDir + dirZeroOffset;
    while (correctedDir > 360.0) {
      correctedDir -= 360.0f;
    }
    while (correctedDir < 0.0f) {
      correctedDir += 360.0f;
    }
    return correctedDir;
  }
}
