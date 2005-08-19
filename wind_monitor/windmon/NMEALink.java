package windmon;

public interface NMEALink
{
    public NMEAMessage getNMEAMessage();
    public void sendNMEAMessage(NMEAMessage msg);
    public boolean open();
    public boolean close();
    public boolean isOpen();
}