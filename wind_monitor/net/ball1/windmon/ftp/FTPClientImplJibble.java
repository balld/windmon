package net.ball1.windmon.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jibble.simpleftp.SimpleFTP;

public class FTPClientImplJibble implements FTPClient {
	SimpleFTP simpleFtp;

	public FTPClientImplJibble() {
		super();
		simpleFtp = new SimpleFTP();
	}

	public void connect(String host) throws IOException {
		simpleFtp.connect(host);
	}

	public void connect(String host, int port) throws IOException {
		simpleFtp.connect(host, port);
	}

	public void connect(String host, int port, String user, String pass)
			throws IOException {
		simpleFtp.connect(host, port, user, pass);
	}

	public void disconnect() throws IOException {
		simpleFtp.disconnect();
	}

	public String pwd() throws IOException {
		return simpleFtp.pwd();
	}

	public boolean cwd(String dir) throws IOException {
		return simpleFtp.cwd(dir);
	}

	public boolean stor(File file) throws IOException {
		return simpleFtp.stor(file);
	}

	public boolean stor(InputStream inputStream, String filename)
			throws IOException {
		return simpleFtp.stor(inputStream, filename);
	}

	public boolean bin() throws IOException {
		return simpleFtp.bin();
	}

	public boolean ascii() throws IOException {
		return simpleFtp.ascii();
	}

	public void rename(String fromName, String toName) throws IOException {
		simpleFtp.rename(fromName, toName);
	}

	public void delete(String filename) throws IOException {
		simpleFtp.delete(filename);
	}

	public boolean isConnected() {
		return simpleFtp.isConnected();
	}

	public boolean isBinary() {
		return simpleFtp.isBinary();
	}
}
