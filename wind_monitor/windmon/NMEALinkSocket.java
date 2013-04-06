package windmon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class NMEALinkSocket implements NMEALink, Runnable
{
    private int portNum = -1;
    private String host = null;
    private Socket nmeaSocket = null;
    
    private InputStream is = null;
    private BufferedReader br = null;
    private PrintStream ps = null;
    private Thread thread = null;
    
    private int linkReadTimeout = 10000; // 10 seconds

    public NMEALinkSocket(String host, int portNum) {
        this.host = host;
    	this.portNum = portNum;
    }

    
    public void start() {
        if (thread == null) {
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
        Thread me = Thread.currentThread();

        while (thread == me && isOpen())
        {
        	Utils.justSleep(10000);
        }
        this.close();
        thread = null;
    }

    
    
    public boolean open()
    {
    	if ( !isOpen() )
    	{
    		try
			{
    			this.nmeaSocket = new Socket(host, portNum);
//    			nmeaSocket.setKeepAlive(false);
    			nmeaSocket.setSoTimeout(linkReadTimeout);
    			ps = new PrintStream(nmeaSocket.getOutputStream());
    			is = nmeaSocket.getInputStream();
    			br = new BufferedReader(new InputStreamReader(is));
			}
    		catch (Exception e)
			{
    			EventLog.log(EventLog.SEV_ERROR, "Could not open socket to " +
    					host + " on port " + portNum + " : " +
						e.getMessage());
//    			e.printStackTrace();
    			ps = null;
    			is = null;
    			br = null;
			}
    	}
    	if ( isOpen() )
    	{
//    		this.start();
			EventLog.log(EventLog.SEV_INFO, "Opened socket to " +
					host + " on port " + portNum + " : ");
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    public boolean close()
    {
        try {
        	// Belt and braces - first close should close them all
//        	br.close();
//            is.close();
//            ps.close();
        	nmeaSocket.shutdownInput();
        	nmeaSocket.shutdownOutput();
            nmeaSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Could not close socket");
            e.printStackTrace();
//            return false;
        }
        nmeaSocket = null;
        ps = null;
        is = null;
        br = null;
		EventLog.log(EventLog.SEV_INFO, "Closed socket to " +
				host + " on port " + portNum + " : ");
        return true;
    }

    public NMEAMessage getNMEAMessage()
    {
    	try
		{
            String cmd = br.readLine();
            if ( cmd == null )
            {
            	throw new IOException("Null string read");
            }
//            EventLog.log(EventLog.SEV_DEBUG, "Read " + cmd);
            return new NMEAMessage(cmd);
        }
        catch (Exception e)
        {
            EventLog.log(EventLog.SEV_ERROR ,"Exception reading NMEA message: " +
                               e.getMessage());
            // Primitive stuff, but if read fails we assume socket lost.
            this.close();
            return null;
        }
    }
    
    public void sendNMEAMessage(NMEAMessage msg)
    {
        try
        {
            ps.print(msg.getMessageString() + "\r\n");
        }
        catch (Exception ex)
        {
            System.err.println("Error send message to NMEA: "
                                + msg.getMessageString());
            ex.printStackTrace();
        }
    }
    
    public boolean isOpen()
    {
		return ( nmeaSocket != null && !nmeaSocket.isInputShutdown());
    }
}