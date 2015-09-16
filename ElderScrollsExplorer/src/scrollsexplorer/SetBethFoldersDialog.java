package scrollsexplorer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scrollsexplorer.ftp.GameMediaFTPdownloader;
import scrollsexplorer.ftp.GameMediaFTPdownloader.CallBack;
import tools.swing.TitledJFileChooser;

public class SetBethFoldersDialog extends JDialog
{
	public SetBethFoldersDialog(Frame f)
	{
		super(f, "Game Data (Esm and Bsa) Folders", true);
		this.setLayout(new GridLayout(-1, 1));

		for (final GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			final JTextField gameFolderField = new JTextField("");
			JButton gameSetButton = new JButton("...");
			JButton gameFtpButton = new JButton("FTP");

			JPanel gamePanel = new JPanel();
			gamePanel.setBorder(BorderFactory.createTitledBorder(gameConfig.gameName + " data folder"));
			gamePanel.setLayout(new BorderLayout());
			gamePanel.add(gameFolderField, BorderLayout.CENTER);
			gameFolderField.setText(PropertyLoader.properties.getProperty(gameConfig.folderKey, ""));
			gamePanel.add(gameSetButton, BorderLayout.EAST);
			gamePanel.add(gameFtpButton, BorderLayout.WEST);
			add(gamePanel);

			gameSetButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setFolder("Select " + gameConfig.gameName + " data folder", gameConfig.folderKey, gameFolderField);
				}

			});
			gameFtpButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ftpData(gameConfig.ftpFolderName, gameConfig.folderKey, gameFolderField);
				}
			});
		}

		JPanel buttonPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);
		add(buttonPanel);

		closeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
			}
		});
	}

	private void setFolder(String title, String propKey, JTextField output)
	{
		File sf = TitledJFileChooser
				.requestFolderName(title, PropertyLoader.properties.getProperty(propKey, ""), SetBethFoldersDialog.this);
		if (sf != null)
		{
			PropertyLoader.properties.setProperty(propKey, sf.getAbsolutePath());
			output.setText(sf.getAbsolutePath());
		}
	}

	private void ftpData(String folderToDownLoad, final String propKey, final JTextField output)
	{
		GameMediaFTPdownloader ftp = new GameMediaFTPdownloader(this, folderToDownLoad);
		ftp.setCallBack(new CallBack()
		{
			@Override
			public void finished(String outputFolder)
			{
				if (outputFolder != null)
				{
					PropertyLoader.properties.setProperty(propKey, outputFolder);
					output.setText(outputFolder);
				}
			}

			@Override
			public void failed()
			{
				System.out.println("Failed :(");
			}
		});
		ftp.start();
	}
}
