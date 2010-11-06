package windmon;

/**
 * @author David
 *
 * Interface for classes that implemente data link to an NMEA device.
 */
public interface NMEALink
{
    public NMEAMessage getNMEAMessage();
    public void sendNMEAMessage(NMEAMessage msg);
    public boolean open();
    public boolean close();
    public boolean isOpen();
}