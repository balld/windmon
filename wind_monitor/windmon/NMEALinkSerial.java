/*
 * Created on 12-Nov-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package windmon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * @author David
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NMEALinkSerial implements NMEALink, SerialPortEventListener {

	
    private String portName;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private int databits;
    private int stopbits;
    private int parity;
    
    private InputStream is = null;
    private BufferedReader br = null;
    private PrintStream ps = null;

    private SerialPort sPort = null;

    public NMEALinkSerial()
    {
    	configure();
    }

    private void configure()
    {
    	this.portName = Config.getParamAsString("NMEASerialPort");
    	if ( portName == null )
    	{
    		EventLog.log(EventLog.SEV_FATAL, "Serial port name not configured");
    	}
    	/*
    	 * Could take the following settings from config file, but hard code
    	 * for now.
    	 */    	
		this.baudRate = 4800;
		this.flowControlIn = SerialPort.FLOWCONTROL_NONE;
		this.flowControlOut = SerialPort.FLOWCONTROL_NONE;
		this.databits = SerialPort.DATABITS_8;
		this.stopbits = SerialPort.STOPBITS_1;
		this.parity = SerialPort.PARITY_NONE;
    }

    
    /* (non-Javadoc)
	 * @see windmon.NMEALink#getNMEAMessage()
	 */
	public NMEAMessage getNMEAMessage() {
    	try
		{
            String cmd;
            do
            {
                cmd = br.readLine();
                if ( cmd == null )
                {
                    throw new IOException("Null string read");
                }
//                EventLog.log(EventLog.SEV_DEBUG, "Read '" + cmd + "' (" + cmd.length() + " characters)");
            } while ( cmd.length() <= 0 );
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

	/* (non-Javadoc)
	 * @see windmon.NMEALink#sendNMEAMessage(windmon.NMEAMessage)
	 */
	public void sendNMEAMessage(NMEAMessage msg) {
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

	/* (non-Javadoc)
	 * @see windmon.NMEALink#open()
	 */
	public boolean open() {
        CommPortIdentifier portId = null;
        
        // Obtain a CommPortIdentifier object for the port you want to open.
        try {
            portId = 
                CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            EventLog.log(EventLog.SEV_ERROR, "Could not identify port " + portName);
            sPort = null;
            return false;
        }
        
        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of 30 seconds to allow
        // a different application to reliquish the port if the user 
        // wants to.
        try {
            sPort = (SerialPort)portId.open("WeatherView", 30000);
        } catch (PortInUseException e) {
        	EventLog.log(EventLog.SEV_ERROR, "Could not open port " + portName);
            sPort = null;
            return false;
        }
        
        // Set the parameters of the connection. If they won't set, close the
        // port before throwing an exception.
    	try {
    	    sPort.setSerialPortParams(this.baudRate,
    	    		this.databits,
    	    		this.stopbits,
					this.parity);
    	} catch (UnsupportedCommOperationException e) {
    		EventLog.log(EventLog.SEV_ERROR, "Could not configure port " + portName);
    	    sPort.close();
    	    sPort = null;
    	    return false;
    	}
    	
    	// Open the input and output streams for the connection. If they won't
    	// open, close the port before throwing an exception.
    	try {
    	    ps =  new PrintStream(sPort.getOutputStream());
    	    is = sPort.getInputStream();
    	    br = new BufferedReader(new InputStreamReader(is));
    	} catch (IOException e) {
    		EventLog.log(EventLog.SEV_ERROR, "Could not create i/o streams for " + portName +
    	                       ": " + e.getMessage());
    	    sPort.close();
    	    sPort = null;
    	    return false;
    	}
    	
    	try {
    	    // Setting a port timeout will trigger read failures if no message
    	    // is received.
    	    sPort.enableReceiveTimeout(5000);
    	    // In theory, you can ask comm only to supply complete lines,
    	    // but this is not supported on the Linux version at present. We wrap
    	    // the serial port in a BufferedReader (above) instead.
    	    // sPort.enableReceiveFraming((int) '\n');
    	} catch (UnsupportedCommOperationException e)
    	{
    		EventLog.log(EventLog.SEV_ERROR, "Unsupported serial port command: " +
    	                       e.getMessage());
    	}
    	
    	try {
    	    sPort.addEventListener(this);
    	} catch (TooManyListenersException e) {
    	    sPort.close();
    	    EventLog.log(EventLog.SEV_ERROR, "Failed to add port event listener: " +
    	                       e.getMessage());
    	}
    	return true;
	}

	/* (non-Javadoc)
	 * @see windmon.NMEALink#close()
	 */
	public boolean close() {
        sPort.close();
        sPort = null;
        ps = null;
        is = null;
        br = null;
        return true;
	}

	/* (non-Javadoc)
	 * @see windmon.NMEALink#isOpen()
	 */
	public boolean isOpen() {
		if ( sPort == null )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
    public synchronized void serialEvent(SerialPortEvent e) {
        // Determine type of event. Included as an example. Does nothing for now.
        if ( e.getEventType() == SerialPortEvent.DATA_AVAILABLE )
        {
            // nothing for now.
        }
    }


/*
 * THE OLD CODE............
 * 
    private String portName;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private int databits;
    private int stopbits;
    private int parity;
    
    private SerialPort sPort = null;
    private InputStream is = null;
    private OutputStream os = null;
    

    public SerialNMEALink( String portName )
    {
    	this( portName,
    	      4800, 
    		  SerialPort.FLOWCONTROL_NONE,
    		  SerialPort.FLOWCONTROL_NONE,
    		  SerialPort.DATABITS_8,
    		  SerialPort.STOPBITS_1,
    		  SerialPort.PARITY_NONE );
    }
    
    public SerialNMEALink(String portName, 
		    int baudRate,
		    int flowControlIn,
		    int flowControlOut,
		    int databits,
		    int stopbits,
		    int parity) {
        
        this.portName = portName;
        this.baudRate = baudRate;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }
    
    public boolean open()
    {
        CommPortIdentifier portId = null;
        
        // Obtain a CommPortIdentifier object for the port you want to open.
        try {
            portId = 
                CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            System.err.println("Could not identify port " + portName);
            sPort = null;
            return false;
        }
        
        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of 30 seconds to allow
        // a different application to reliquish the port if the user 
        // wants to.
        try {
            sPort = (SerialPort)portId.open("WeatherView", 30000);
        } catch (PortInUseException e) {
            System.err.println("Could not open port " + portName);
            sPort = null;
            return false;
        }
        
        // Set the parameters of the connection. If they won't set, close the
        // port before throwing an exception.
    	try {
    	    sPort.setSerialPortParams(9600,
    	            SerialPort.DATABITS_8,
    	            SerialPort.STOPBITS_1,
    	            SerialPort.PARITY_NONE);
    	} catch (UnsupportedCommOperationException e) {
    	    System.err.println("Could not configure port " + portName);
    	    sPort.close();
    	    sPort = null;
    	    return false;
    	}
    	
    	// Open the input and output streams for the connection. If they won't
    	// open, close the port before throwing an exception.
    	try {
    	    setPrintStream(new PrintStream(sPort.getOutputStream()));
    	    setInputStream(sPort.getInputStream());
    	} catch (IOException e) {
    	    System.err.println("Could not create i/o streams for " + portName +
    	                       ": " + e.getMessage());
    	    sPort.close();
    	    sPort = null;
    	    return false;
    	}
    	
    	try {
    	    // Setting a port timeout will trigger read failures if no message
    	    // is received.
    	    sPort.enableReceiveTimeout(5000);
    	    // To ensure complete commands are read
    	    sPort.enableReceiveFraming((int) '\n');
    	} catch (UnsupportedCommOperationException e)
    	{
    	    System.err.println("Unsupported serial port command: " +
    	                       e.getMessage());
    	}
    	
    	try {
    	    sPort.addEventListener(this);
    	} catch (TooManyListenersException e) {
    	    sPort.close();
    	    System.err.println("Failed to add port event listener: " +
    	                       e.getMessage());
    	}
    	return true;

    }
    
    public boolean close()
    {
        sPort.close();
        setPrintStream(null);
        setInputStream(null);
        return true;
    }
    
    public synchronized void serialEvent(SerialPortEvent e) {
        // Determine type of event.
        if ( e.getEventType() == SerialPortEvent.DATA_AVAILABLE )
        {
            notifyAll();
        }
    }
 */
	
}
