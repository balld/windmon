/*
 * Created on 07-Nov-2010
 */
package windmon;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import net.ball1.windmon.ftp.FTPClient;
import net.ball1.windmon.ftp.FTPClientImplApache;

/**
 * @author David
 *
 * Asynchronous FTP client. Tasks are queued and then executed in the order submitted.
 */
public class FTPTaskQueue implements Runnable {
	private static final Logger logger = Logger.getLogger(FTPTaskQueue.class.getName());
	
	private ArrayBlockingQueue<FTPTask> queue = new ArrayBlockingQueue<FTPTask>(128);
    private Thread thread = null;
    private FTPClient ftp = null;
    
    private String ftpHost;
    private String ftpUser;
    private String ftpPassword;
    private String ftpRemoteDirectory;

	/**
	 * 
	 */
	public FTPTaskQueue(String host, String user, String pass, String dir) {
		super();
		ftpHost = host;
		ftpUser = user;
		ftpPassword = pass;
		ftpRemoteDirectory = dir;
	}
	
	public synchronized void addTask(FTPTask task)
	{
		try {
			if (!queue.add(task)) {
				throw new IllegalStateException("Queue add failed.");
			}
		} catch (Exception e) {
        	logger.severe("Failed to add FTP task to queue: " + e.getMessage());
		}
		
		if (thread == null) {
			this.start();
		}
	}
	
    public synchronized void start() {
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
        
        if (ftp != null) {
        	try {
        		ftp.disconnect();
        	} catch (Exception e) {
        	}
        }
        ftp = null;
    }
    
    public void run ()
    {
    	FTPTask task = null;
        Thread me = Thread.currentThread();
        while (thread == me) {
        	try {
        		task = queue.take();
        	} catch (Exception e) {
        		continue;
        	}
        	
        	if (task != null) {
        		if (verifyConnection()) {
        			task.executeTask(ftp);
        		} else {
        			Utils.justSleep(1000);
            		logger.warning( "FTP connection down. Clearing queue.");
            		queue.clear();
        		}
        	}
        }
        thread = null;
    }

	
	private boolean verifyConnection() {
    	boolean connected = false;
    	
    	if (ftp == null) {
//    		ftp = new FTPClientImplJibble();
    		ftp = new FTPClientImplApache();
    	}

    	if (ftp.isConnected()) {
    		return true;
    	}
    	
    	try {
    		ftp.connect(ftpHost, 21, ftpUser, ftpPassword);
    		connected = true;
    		
    		if(!ftp.cwd(ftpRemoteDirectory)) {
    			throw new IOException("Could not change to remote directory '" + ftpRemoteDirectory + "'.");
    		}

    		logger.info("Opened FTP connection to '" + 
    				ftpHost + "' directory '" + ftpRemoteDirectory + "' as user '" + ftpUser +"'.");
    	} catch (Exception e) {
    		logger.severe("Could not open FTP connection to '" + 
    				ftpHost + "' as user '" + ftpUser +"': " + e.getMessage());
    		if (connected) {
    			try {
    				ftp.disconnect();
    				connected = false;
    			} catch (Exception e2) {
    			}
    		}
    	}
    	
    	if (connected) {
    		return true;
    	} else {
    		return false;
    	}
	}
}
