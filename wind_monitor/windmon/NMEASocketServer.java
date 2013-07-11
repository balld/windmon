package windmon;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class NMEASocketServer implements Runnable
{
	private static final Logger logger = Logger.getLogger(NMEASocketServer.class.getName());
    private int portNum = -1;
    
    private Thread thread = null;
    private ServerSocket serverSocket;
    
    private List<ClientSocket> sockets;
    
    private class ClientSocket {
    	private Socket socket;
    	private PrintWriter writer;
		public ClientSocket(Socket socket, PrintWriter writer) {
			super();
			this.socket = socket;
			this.writer = writer;
		}
		public Socket getSocket() {
			return socket;
		}
		public PrintWriter getWriter() {
			return writer;
		}
    }
    
    public NMEASocketServer(int portNum) {
    	this.portNum = portNum;
    	sockets = new ArrayList<ClientSocket>();
    }

    
    public void start() {
        if (thread == null) {
        	thread = new Thread(this);
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

        while (thread == me)
        {
        	if (serverSocket == null || serverSocket.isClosed()) {
        		try {
        			serverSocket = new ServerSocket(portNum);
        			logger.info("Created server socket on port " + portNum);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			logger.severe("Failed to open listening socket on port " + portNum + ": " + e.getLocalizedMessage());
        			serverSocket = null;
        			Utils.justSleep(10000); // Sleep 10 seconds before retrying.

        		}
        	}
        	if (serverSocket != null) {
        		try {
					Socket socket = serverSocket.accept();
					socket.setKeepAlive(false);
					ClientSocket clientSocket = new ClientSocket(socket, new PrintWriter(socket.getOutputStream(), true));
					sockets.add(clientSocket);
	    			logger.info("Opened NMEA client connection from " + socket.getInetAddress());
				} catch (IOException e) {
					logger.severe("Failed to accept NMEA client connection: " + e.getLocalizedMessage());
				}
        	}
        }
        this.close();
        thread = null;
    }
    
    
    /**
     * @return
     */
    public boolean close()
    {
    	// TODO - CLose server socket
    	// TODO - Close all client sockets
        return true;
    }
    
    /**
     * @param msg
     */
    public void sendMessage(NMEAMessage msg) {
    	Iterator<ClientSocket> it = sockets.iterator();
    	while (it.hasNext()) {
    		ClientSocket cs = it.next();
			// TODO - Detect close properly 
    		if (!cs.getSocket().isInputShutdown()) {
    			cs.getWriter().println(msg.getMessageString());
    		} else {
    			logger.info("Closing connection from " + cs.getSocket().getInetAddress());
    			try {cs.getSocket().close();} catch (IOException e) {/* ignore */}
    			cs.getWriter().close();
    			it.remove();
    		}
    	}
    }
}