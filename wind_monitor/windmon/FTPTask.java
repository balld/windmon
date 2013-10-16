/*
 * Created on 07-Nov-2010
 *
 */
package windmon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import net.ball1.windmon.ftp.FTPClient;

/**
 * @author David
 *
 * Representation of a single FTP task, e.g. send a file.
 */
public class FTPTask {
	private static final Logger logger = Logger.getLogger(FTPTask.class.getName());
	
	private static enum Command {

	  CMD_NOT_SET,
	  CMD_SEND,
	  CMD_REMOTE_RENAME,
	  CMD_REMOTE_DELETE,
	  CMD_LOCAL_RENAME,
	  CMD_LOCAL_DELETE;
	}
	
	private Command command = Command.CMD_NOT_SET;
	private Object arg1 = null;
	private Object arg2 = null;
	private Object arg3 = null;

	/**
	 *  Private constructor. Don't call directly, instantiate with createFTPTask method. 
	 */
	private FTPTask() {
		super();
	}

	
	private FTPTask(Command cmd, Object arg1, Object arg2, Object arg3) {
		super();
		this.command = cmd;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
	}
	
	public boolean executeTask(FTPClient ftp) {
        boolean result = false;
        logger.info("Executing FTP command: " + command);
		switch (command) {
		    case CMD_SEND:          result = executeSend(ftp); break;
		    case CMD_REMOTE_RENAME: result = executeRemoteRename(ftp); break;
		    case CMD_REMOTE_DELETE: result = executeRemoteDelete(ftp); break;
		    case CMD_LOCAL_RENAME:  result = executeLocalRename(ftp); break;
		    case CMD_LOCAL_DELETE:  result = executeLocalDelete(ftp); break;
		    default: logger.severe("Unknown FTP task type: " + command); break;
		}
		
		return result;
	}

	public static FTPTask createSendTask(String localFile, String remoteFile, boolean binMode) {
		return new FTPTask(Command.CMD_SEND, localFile, remoteFile, new Boolean(binMode));
	}

	public static FTPTask createRemoteRenameTask(String fromFile, String toFile) {
		return new FTPTask(Command.CMD_REMOTE_RENAME, fromFile, toFile, null);
	}

	public static FTPTask createRemoteDeleteTask(String remoteFile) {
		return new FTPTask(Command.CMD_REMOTE_DELETE, remoteFile, null, null);
	}

	public static FTPTask createLocalRenameTask(String fromFile, String toFile) {
		return new FTPTask(Command.CMD_LOCAL_RENAME, fromFile, toFile, null);
	}

	public static FTPTask createLocalDeleteTask(String localFile) {
		return new FTPTask(Command.CMD_LOCAL_DELETE, localFile, null, null);
	}

	private boolean executeSend(FTPClient ftp) {
		String localFile = (String)arg1;
		String remoteFile = (String)arg2;
		boolean binary = ((Boolean)arg3).booleanValue();

		try {
			if (binary && !ftp.isBinary()) {
				if (!ftp.bin()) {
					throw new IOException("Failed to set binary mode.");
				}
			} else if (!binary && ftp.isBinary()) {
				if (!ftp.ascii()) {
					throw new IOException("Failed to set ascii mode.");
				}
			}
			if (!ftp.stor(new FileInputStream(new File(localFile)), remoteFile)) {
				throw new IOException("Command returned failure.");
			}
        	logger.info("Successful FTP transfer local file '" + 
        			localFile + "' to remote file '" + remoteFile + "'");
		} catch (Exception e) {
        	logger.severe("Failed FTP transfer local file '" + 
        			localFile + "' to remote file '" + remoteFile + "': " + e.getMessage());
        	return false;
		}
		
		return true;
	}
	
	private boolean executeRemoteRename(FTPClient ftp) {
		String fromFile = (String) arg1;
		String toFile = (String) arg2;
		
		try {
			ftp.rename(fromFile, toFile);
        	logger.info("Successful FTP rename remote file from '" + 
        			fromFile + "' to '" + toFile + "'");
		} catch (Exception e) {
        	logger.severe("Failed FTP rename remote file from '" + 
        			fromFile + "' to '" + toFile + "': " + e.getMessage());
        	return false;
		}
		return true;
	}
	
	private boolean executeRemoteDelete(FTPClient ftp) {
        String remoteFile = (String)arg1;
		try {
			ftp.delete(remoteFile);
        	logger.info(
					"Successful FTP delete remote file '" + remoteFile + "'");
		} catch (Exception e) {
        	logger.severe("Failed FTP delete remote file '" + 
        			remoteFile + "': " + e.getMessage());
        	return false;
		}
		return true;
	}

	
	private boolean executeLocalRename(FTPClient ftp) {
		String fromFile = (String) arg1;
		String toFile = (String) arg2;

		try {
        	File f1 = new File(fromFile);
        	File f2 = new File(toFile);
        	// This rename is atomic action which indicates all files
			// are ready
        	f1.renameTo(f2);
        	logger.info("Renamed local file '" + 
        			fromFile + "' to '" + toFile + "'.");
		} catch (Exception e) {
        	logger.severe("Failed rename local file '" + 
        			fromFile + "' to '" + toFile + "'.");
        	return false;
		}
		return true;
	}
	
	
	private boolean executeLocalDelete(FTPClient ftp) {
		String localFile = (String)arg1;

		try {
			File f = new File(localFile);
			if (!f.delete()) {
				throw new IOException("Delete Failed.");
			}
        	logger.info("Deleted local file '" + 
        			localFile + "'.");
		} catch (Exception e) {
        	logger.severe("Failed delete local file '" + 
        			localFile + "': " + e.getMessage());
        	return false;
		}
		return true;
	}
}

