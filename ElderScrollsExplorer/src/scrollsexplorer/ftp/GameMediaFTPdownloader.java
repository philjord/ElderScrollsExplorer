package scrollsexplorer.ftp;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.enterprisedt.net.ftp.FTPTransferType;

import tools.swing.FileDownloadProgressThread;
import tools.swing.FileDownloadProgressThread.CancelCallBack;

/**
 *  This class grabs game media from my ftp server, it asks for the password though...
 *
 */
//TODO: swap this whole thing across to commons.net.ftp which is just better
public class GameMediaFTPdownloader extends Thread
{
	private static final String FTP_HOST_NAME = "philjord.ddns.net";

	private static final String FTP_LOGIN = "gamemedia";

	private String ftp_password = "";

	private static final String OUTPUT_FOLDER = "ftpdata\\";

	private String folderToDownLoad;

	private String outputfolder;

	private FTPClient ftp = null;

	private FTPMessageCollector ftpMessageCollector = new FTPMessageCollector();

	private Preferences prefs;

	private CallBack callBack;

	private Component parent;

	private boolean hasCancelled = false;

	public GameMediaFTPdownloader(Component parent, String folderToDownLoad)
	{
		this.setName("GameMediaFTPdownloader");
		this.setDaemon(true);
		this.parent = parent;
		this.folderToDownLoad = folderToDownLoad;
		outputfolder = OUTPUT_FOLDER + folderToDownLoad;
		prefs = Preferences.userNodeForPackage(GameMediaFTPdownloader.class);
		ftp_password = prefs.get("FTP_PASSWORD", "");
	}

	protected void finalize() throws Throwable
	{
		stopFtp();
	}

	/**
	 * stops checking the ftp site and updating the list
	 */
	public void stopFtp()
	{
		try
		{
			if (ftp != null)
			{
				ftp.quit();
				System.out.println("ftp stopped");
				if (callBack != null)
				{
					callBack.failed();
				}
			}
		}
		catch (IOException e)
		{
			//ignore quitting anyway
			//e.printStackTrace();
		}
		catch (FTPException e)
		{
			//ignore quitting anyway
			//e.printStackTrace();
		}
	}

	public void run()
	{
		boolean good_login = false;
		while (!good_login)
		{
			try
			{
				ftp = new FTPClient(FTP_HOST_NAME);
				ftp.setMessageListener(ftpMessageCollector);
				ftp.login(FTP_LOGIN, ftp_password);//throws a 530 for bad login

				good_login = true;// if no exception we are happy
				prefs.put("FTP_PASSWORD", ftp_password);//record the good password

				ftp.setConnectMode(FTPConnectMode.PASV);
				ftp.setType(FTPTransferType.BINARY);
				ftp.chdir(folderToDownLoad);

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

					long destLen = destination.length();
					boolean resume = destLen > 0 && destLen < ftpFile.size(); // is this right? can they be near enough?
					 
					System.out.println("ftpFile.getName() " + ftpFile.getName());
					System.out.println("ftpFile.size() " + ftpFile.size());
					System.out.println("destLen " + destLen);
					
					//if it's too big dump it and force a redownload
					if (destLen > ftpFile.size())
						destination.delete();

					if (!destination.exists() || resume)
					{
						OutputStream out = null;
						try
						{
							FileDownloadProgressThread progT = new FileDownloadProgressThread(parent, outputfolder + "\\"
									+ ftpFile.getName(), ftpFile.size(), destination);
							progT.setCancelCallBack(new CancelCallBack()
							{
								@Override
								public void cancel()
								{
									hasCancelled = true;
									ftp.cancelTransfer();

									System.out.println("transfer cancelled");
								}

							});
							progT.start();

							if (resume)
							{
								ftp.resume();
							}
							out = new FileOutputStream(destination, resume);

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

					// did we cancel the operation?
					if (hasCancelled)
					{
						allSuccessful = false;
						break;
					}

				}
				System.out.println("Download attempt finished");

				if (callBack != null)
				{
					if (allSuccessful)
						callBack.finished(new File(outputfolder).getAbsolutePath());
					else
						callBack.failed();
				}

			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "UnknownHostException  " + FTP_HOST_NAME);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "IOException  " + e);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "ParseException  " + e);
			}
			catch (FTPException e)
			{

				if (e.getReplyCode() == 530)
				{
					ftp_password = JOptionPane.showInputDialog(parent, "Please enter ftp password");
					if (ftp_password == null || ftp_password.length() == 0)
					{
						// if cancel or blank just exit loop, otherwise give it another whorl
						break;
					}
				}
				else if (e.getMessage().equals("Connection timed out."))
				{// TODO: if this is a timeout just restart?
					e.printStackTrace();
					JOptionPane.showMessageDialog(parent, "FTPException time out " + e.getReplyCode() + " " + e);
				}
				else
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(parent, "FTPException  " + e);
				}
			}

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
}
