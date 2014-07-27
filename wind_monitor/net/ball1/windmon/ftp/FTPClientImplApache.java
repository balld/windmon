package net.ball1.windmon.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;

import windmon.Utils;

public class FTPClientImplApache implements FTPClient {
  
  private static final long LOGOUT_SLEEP_MS = 1000;

  org.apache.commons.net.ftp.FTPClient apacheFtp;
  private boolean binary;

  public FTPClientImplApache() {
    super();
    FTPClientConfig config = new FTPClientConfig();
    apacheFtp = new org.apache.commons.net.ftp.FTPClient();
    apacheFtp.configure(config);
    apacheFtp.setDefaultTimeout(30000);
  }

  @Override
  public void connect(String host) throws IOException {
    connect(host, 21);

  }

  @Override
  public void connect(String host, int port) throws IOException {
    connect(host, port, "anonymous", "anonymous");
  }

  @Override
  public void connect(String host, int port, String user, String pass)
      throws IOException {
    apacheFtp.connect(host, port);
    apacheFtp.login(user, pass);
    ascii();
  }

  @Override
  public void disconnect() throws IOException {
    apacheFtp.abort();
    apacheFtp.logout();
    Utils.justSleep(LOGOUT_SLEEP_MS);
    apacheFtp.disconnect();
  }

  @Override
  public String pwd() throws IOException {
    return apacheFtp.printWorkingDirectory();
  }

  @Override
  public boolean cwd(String dir) throws IOException {
    return apacheFtp.changeWorkingDirectory(dir);
  }

  @Override
  public boolean stor(File file) throws IOException {
    return stor(new FileInputStream(file), file.getName());
  }

  @Override
  public boolean stor(InputStream inputStream, String filename)
      throws IOException {
    return apacheFtp.storeFile(filename,  inputStream);
  }

  @Override
  public boolean bin() throws IOException {
    boolean result = apacheFtp.setFileType(FTP.BINARY_FILE_TYPE);
    if (result) {
      binary = true;
    }
    return result;
  }

  @Override
  public boolean ascii() throws IOException {
    boolean result = apacheFtp.setFileType(FTP.ASCII_FILE_TYPE);
    if (result) {
      binary = false;
    }
    return result;
  }

  @Override
  public void rename(String fromName, String toName) throws IOException {
    boolean result = apacheFtp.rename(fromName, toName);
    if (!result) {
      throw new IOException("FTP Failed to rename file from '" + fromName + "' to '" + toName + "' : " + apacheFtp.getReplyCode() + " - " + apacheFtp.getReplyString());
    }
  }

  @Override
  public void delete(String filename) throws IOException {
    boolean result = apacheFtp.deleteFile(filename);
    if (!result) {
      throw new IOException("FTP Failed to delete file '" + filename + "': " + apacheFtp.getReplyCode() + " - " + apacheFtp.getReplyString());
    }
  }

  @Override
  public boolean isConnected() {
    return apacheFtp.isConnected() && apacheFtp.isAvailable();
  }

  @Override
  public boolean isBinary() {
    return binary;
  }

}
