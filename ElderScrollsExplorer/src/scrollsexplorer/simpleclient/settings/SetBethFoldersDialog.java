package scrollsexplorer.simpleclient.settings;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.ftp.GameMediaFTPdownloader;
import scrollsexplorer.ftp.GameMediaFTPdownloader.CallBack;
import tools.swing.TitledJFileChooser;

public class SetBethFoldersDialog extends JDialog
{
	private HashMap<GameConfig, JTextField> gameFolderFields = new HashMap<GameConfig, JTextField>();

	public SetBethFoldersDialog(Frame frame)
	{
		super(frame, "Game Data (.bsa or .ba2 and .esm) Folders", false);
		this.setLayout(new GridLayout(-1, 1));

		for (final GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			final JTextField gameFolderField = new JTextField("");
			gameFolderFields.put(gameConfig, gameFolderField);
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
					setFolder(gameConfig, gameFolderField);
				}
			});

			gameFtpButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ftpData(gameConfig.ftpFolderName, gameConfig, gameFolderField);
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
				// set all properties in case of manual edits
				for (GameConfig gameConfig : GameConfig.allGameConfigs)
				{
					JTextField gameFolderField = gameFolderFields.get(gameConfig);
					File fileText = new File(gameFolderField.getText());
					if (fileText.exists() && fileText.isDirectory())
					{
						PropertyLoader.properties.setProperty(gameConfig.folderKey, gameFolderField.getText());
						//update gameconfigs based on property changes
						gameConfig.update();
					}
				}
				setVisible(false);
			}
		});
	}

	private void setFolder(GameConfig gameConfig, JTextField output)
	{
		String startFolder = PropertyLoader.properties.getProperty(gameConfig.folderKey, "");
		File confirmedFolder = null;
		while (confirmedFolder == null)
		{
			File pickedfolder = TitledJFileChooser.requestFolderName("Select " + gameConfig.gameName + " data folder", startFolder,
					SetBethFoldersDialog.this);

			// check for cancel
			if (pickedfolder == null)
				return;

			// check to ensure the esm file and at least one bsa file are in the folder
			File checkEsm = new File(pickedfolder, gameConfig.mainESMFile);
			if (!checkEsm.exists())
			{
				int r = JOptionPane.showConfirmDialog(SetBethFoldersDialog.this, "The selected folder does not contain "
						+ gameConfig.mainESMFile + " this game type will not work.\n" + "Do you wish to set anyway?", "ESM file not found",
						JOptionPane.YES_NO_OPTION);

				if (r == JOptionPane.NO_OPTION)
				{
					startFolder = pickedfolder.getAbsolutePath();
					continue;
				}

				// otherwise carry on, perhaps they'll put it there shortly
			}

			int countOfBsa = 0;
			for (File f : pickedfolder.listFiles())
			{
				countOfBsa += f.getName().toLowerCase().endsWith(".bsa") ? 1 : 0;
				countOfBsa += f.getName().toLowerCase().endsWith(".ba2") ? 1 : 0;
			}

			if (countOfBsa == 0)
			{
				int r = JOptionPane.showConfirmDialog(SetBethFoldersDialog.this, "The selected folder does not contain "
						+ " any .bsa files, this game type will not work.\n" + "Do you wish to set anyway?", "No BSA files found",
						JOptionPane.YES_NO_OPTION);

				if (r == JOptionPane.NO_OPTION)
				{
					startFolder = pickedfolder.getAbsolutePath();
					continue;
				}

				// otherwise carry on, perhaps they'll put them there shortly
			}

			confirmedFolder = pickedfolder;
		}

		PropertyLoader.properties.setProperty(gameConfig.folderKey, confirmedFolder.getAbsolutePath());
		output.setText(confirmedFolder.getAbsolutePath());
		gameConfig.update();

	}

	private void ftpData(String folderToDownLoad, final GameConfig gameConfig, final JTextField output)
	{
		GameMediaFTPdownloader ftp = new GameMediaFTPdownloader(this, folderToDownLoad);
		ftp.setCallBack(new CallBack()
		{
			@Override
			public void finished(String outputFolder)
			{
				if (outputFolder != null)
				{
					PropertyLoader.properties.setProperty(gameConfig.folderKey, outputFolder);
					output.setText(outputFolder);
					gameConfig.update();
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
