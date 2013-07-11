package net.ball1.windmon.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FTPClient {

	/**
	 * Connects to the default port of an FTP server and logs in as
	 * anonymous/anonymous.
	 */
	public abstract void connect(String host) throws IOException;

	/**
	 * Connects to an FTP server and logs in as anonymous/anonymous.
	 */
	public abstract void connect(String host, int port) throws IOException;

	/**
	 * Connects to an FTP server and logs in with the supplied username
	 * and password.
	 */
	public abstract void connect(String host, int port, String user, String pass)
			throws IOException;

	/**
	 * Disconnects from the FTP server.
	 */
	public abstract void disconnect() throws IOException;

	/**
	 * Returns the working directory of the FTP server it is connected to.
	 */
	public abstract String pwd() throws IOException;

	/**
	 * Changes the working directory (like cd). Returns true if successful.
	 */
	public abstract boolean cwd(String dir) throws IOException;

	/**
	 * Sends a file to be stored on the FTP server.
	 * Returns true if the file transfer was successful.
	 * The file is sent in passive mode to avoid NAT or firewall problems
	 * at the client end.
	 */
	public abstract boolean stor(File file) throws IOException;

	/**
	 * Sends a file to be stored on the FTP server.
	 * Returns true if the file transfer was successful.
	 * The file is sent in passive mode to avoid NAT or firewall problems
	 * at the client end.
	 */
	public abstract boolean stor(InputStream inputStream, String filename)
			throws IOException;

	/**
	 * Enter binary mode for sending binary files.
	 */
	public abstract boolean bin() throws IOException;

	/**
	 * Enter ASCII mode for sending text files. This is usually the default
	 * mode. Make sure you use binary mode if you are sending images or
	 * other binary data, as ASCII mode is likely to corrupt them.
	 */
	public abstract boolean ascii() throws IOException;

	/**
	 * Renames a remote file.
	 */
	public abstract void rename(String fromName, String toName)
			throws IOException;

	/**
	 * Deletes a remote file.
	 */
	public abstract void delete(String filename) throws IOException;

	public abstract boolean isConnected();

	public abstract boolean isBinary();

}