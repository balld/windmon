package windmon;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.ball1.windmon.ftp.FTPClient;

public class FTPTaskCallable implements Callable<Boolean> {
  
  private static final int FTP_COMMAND_TIMEOUT_SECS = 30; 

  private static final Logger logger = Logger.getLogger(FTPTaskCallable.class.getName());

  private static ExecutorService executor = Executors.newSingleThreadExecutor();
  
  private FTPClient ftp;
  private FTPTask task;
  /**
   * @param ftp
   * @param task
   */
  public FTPTaskCallable(FTPClient ftp, FTPTask task) {
    super();
    this.ftp = ftp;
    this.task = task;
  }
  /* (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Boolean call() throws Exception {
    return Boolean.valueOf(task.executeTask(ftp));
  }
  
  /**
   * Execute FTP task, with timeout functionality as some FTP servers seem to hang
   * indefinitely on disconnect.
   * @param ftp
   * @param task
   * @return
   */
  public static boolean executeFtpTask(FTPClient ftp, FTPTask task) {
    boolean result = false;

    // Create Callable and Future for FTP task
    Callable<Boolean> callable = new FTPTaskCallable(ftp, task);
    FutureTask<Boolean> future = new FutureTask<Boolean>(callable);
    
    // Execute task in background thread
    executor.execute(future);
    
    // Get result of task, with timeout
    try {
      Boolean res = future.get(FTP_COMMAND_TIMEOUT_SECS, TimeUnit.SECONDS);
      if (res == null) {
        // Shouldn't ever happen?
        logger.warning("No result from FTP operation");
      } else {
        // Set result to return later
        result = res.booleanValue();
      }
    } catch (Exception e) {
      // Most likely a timeout.
      String msg = e.getMessage();
      if (msg == null) {
        msg = e.getClass().getName();
      }
      logger.warning("FTP operation failed: " + msg);
    }
    
    // Make sure we don't leave the task running.
    if (!future.isDone() && ! future.isCancelled()) {
      future.cancel(true);
    }
    
    return result;
  }
}
