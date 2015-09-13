package scrollsexplorer.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPMessageListener;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This class loads all files from the ftp directory
 * specified in the statics. These file are expected to be
 * byte arrays of open servers, for open server selection 
 * @author pj
 * 
 * @see common.CommonConstants#FTP_HOST_NAME
 * @see common.CommonConstants#FTP_LOGIN
 * @see common.CommonConstants#FTP_PASSWORD
 * @see common.CommonConstants#FTP_OPEN_SERVER_DIR
 * @see client.ClientConstants#ADDRESS_CATCHER_DELAY_FTP
 *
 */
public class GameMediaFTPdownloader implements FTPMessageListener
{
	private static final String FTP_HOST_NAME = "philjord.ddns.net";

	private static final String FTP_LOGIN = "gamemedia";

	private String ftp_password = "";

	private static final String OUTPUT_FOLDER = "ftpdata\\";

	private String folderToDownLoad;

	private String outputfolder;

	private Thread thread;

	private FTPClient ftp = null;

	//private FTPMessageCollector listener = new FTPMessageCollector();

	private Preferences prefs;

	private CallBack callBack;

	public GameMediaFTPdownloader(String folderToDownLoad)
	{
		this.folderToDownLoad = folderToDownLoad;
		outputfolder = OUTPUT_FOLDER + folderToDownLoad;
		prefs = Preferences.userNodeForPackage(GameMediaFTPdownloader.class);
		ftp_password = prefs.get("FTP_PASSWORD", "");
	}

	protected void finalize() throws Throwable
	{
		stop();
	}

	/**
	 * stops checking the ftp site and updating the lsit
	 */
	public void stop()
	{
		try
		{
			if (ftp != null)
			{
				ftp.quit();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (FTPException e)
		{
			e.printStackTrace();
		}
	}

	public void start()
	{
		boolean good_login = false;
		while (!good_login)
		{
			try
			{
				ftp = new FTPClient(FTP_HOST_NAME);
				ftp.setMessageListener(this);
				ftp.login(FTP_LOGIN, ftp_password);//throws a 530 for bad login
				good_login = true;

				prefs.put("FTP_PASSWORD", ftp_password);//record the good password
				ftp.setConnectMode(FTPConnectMode.PASV);
				ftp.setType(FTPTransferType.BINARY);
				ftp.chdir(folderToDownLoad);
				thread = new Thread(new FTPThread());
				thread.setName("FTP download of " + folderToDownLoad);
				thread.start();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "UnknownHostException  " + FTP_HOST_NAME);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "IOException  " + e);
			}
			catch (FTPException e)
			{
				if (e.getReplyCode() == 530)
				{
					ftp_password = JOptionPane.showInputDialog("Please enter ftp password");
					if (ftp_password == null || ftp_password.length() == 0)
					{
						// if cancel or blank just exit loop, otherwise give it another whorl
						break;
					}
				}
				else
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "FTPException  " + e);
				}
			}
		}
	}

	private class FTPThread implements Runnable
	{
		public void run()
		{
			try
			{
				boolean allSuccessful = true;
				// get directory and go through file   
				FTPFile[] files = ftp.dirDetails(".");
				for (FTPFile ftpFile : files)
				{
					File destination = new File(outputfolder + "\\" + ftpFile.getName());
					System.out.println("DownLoading " + folderToDownLoad + "\\" + ftpFile.getName());
					System.out.println("To " + destination.getAbsolutePath());

					if (!destination.getParentFile().exists())
						destination.getParentFile().mkdirs();

					if (!destination.exists())
					{
						OutputStream out = null;
						try
						{
							out = new FileOutputStream(destination);
							FTPThreadProgress progT = new FTPThreadProgress(ftpFile, destination);
							progT.setName("Progress of FTP download of " + outputfolder + "\\" + ftpFile.getName());
							progT.setDaemon(true);
							progT.start();

							ftp.get(out, ftpFile.getName());
							out.close();
							progT.stopNow();
						}
						catch (Exception e)
						{
							allSuccessful = false;

							e.printStackTrace();
						}
						finally
						{
							if (out != null)
							{
								try
								{
									out.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					else
					{
						System.out.println("File exists!");
					}
				}
				System.out.println("Download complete");

				if (callBack != null)
				{
					if (allSuccessful)
						callBack.finished(new File(outputfolder).getAbsolutePath());
					else
						callBack.failed();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (FTPException e)
			{
				e.printStackTrace();
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class FTPThreadProgress extends Thread
	{
		private File destination;

		private ProgressMonitor progressMonitor;

		private boolean stop = false;

		public FTPThreadProgress(FTPFile ftpFile, File destination)
		{
			this.destination = destination;
			progressMonitor = new ProgressMonitor(null, "Progress of FTP download of " + outputfolder + "\\" + ftpFile.getName(), "", 0,
					(int) ftpFile.size());
		}

		public void run()
		{
			while (!stop)
			{
				progressMonitor.setProgress((int) destination.length());
			}
		}

		public void stopNow()
		{
			stop = true;
		}
	}

	public void setCallBack(CallBack callBack)
	{
		this.callBack = callBack;
	}

	public static interface CallBack
	{
		public void finished(String outputFolder);

		public void failed();
	}

	@Override
	public void logCommand(String command)
	{
		//System.out.println("logCommand: " + command);
	}

	@Override
	public void logReply(String reply)
	{
		//System.out.println("logReply; " + reply);
	}
}
