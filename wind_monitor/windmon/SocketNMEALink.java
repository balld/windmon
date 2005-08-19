/*
 * Created on Jul 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.io.*;
import java.net.*;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SocketNMEALink implements NMEALink
{
    private int portNum = -1;
    private String host = null;
    private Socket nmeaSocket = null;
    
    private boolean openFlag = false;
    
    private InputStream is = null;
    private BufferedReader br = null;
    private PrintStream ps = null;

    public SocketNMEALink(String host, int portNum) {
        this.host = host;
    	this.portNum = portNum;
    }
    
    public boolean open()
    {
    	if ( !isOpen() )
    	{
    		try
			{
    			this.nmeaSocket = new Socket(host, portNum);
    			ps = new PrintStream(nmeaSocket.getOutputStream());
    			is = nmeaSocket.getInputStream();
    			br = new BufferedReader(new InputStreamReader(is));
			}
    		catch (Exception e)
			{
    			System.err.println("Could not open socket to " +
    					host + " on port " + portNum + " : " +
						e.getMessage());
    			e.printStackTrace();
    			ps = null;
    			is = null;
    			br = null;
			}
    	}
    	return isOpen();
    }
    
    public boolean close()
    {
        try {
            is.close();
            ps.close();
            nmeaSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Could not close socket");
            e.printStackTrace();
            return false;
        }
        ps = null;
        is = null;
        br = null;
        return true;
    }

    public NMEAMessage getNMEAMessage()
    {
    	try
		{
            String cmd = br.readLine();
            return new NMEAMessage(cmd);
        }
        catch (IOException e)
        {
            System.err.println("IO Exception reading NMEA message: " +
                               e.getMessage());
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
		return ( ps != null && is != null && br != null);
    }
}