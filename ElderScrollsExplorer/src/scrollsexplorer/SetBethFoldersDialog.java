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
	private JTextField morrowindFolderField = new JTextField("");

	private JButton morrowindSetButton = new JButton("...");

	private JButton morrowindFtpButton = new JButton("FTP");

	private JTextField oblivionFolderField = new JTextField("");

	private JButton oblivionSetButton = new JButton("...");

	private JButton oblivionFtpButton = new JButton("FTP");

	private JTextField fallout3FolderField = new JTextField("");

	private JButton fallout3SetButton = new JButton("...");

	private JButton fallout3FtpButton = new JButton("FTP");

	private JTextField falloutNVFolderField = new JTextField("");

	private JButton falloutNVSetButton = new JButton("...");

	private JButton falloutNVFtpButton = new JButton("FTP");

	private JTextField skyrimFolderField = new JTextField("");

	private JButton skyrimSetButton = new JButton("...");

	private JButton skyrimFtpButton = new JButton("FTP");

	public SetBethFoldersDialog(Frame f)
	{
		super(f, "Set Esm and Bsa Folders", true);
		this.setLayout(new GridLayout(-1, 1));

		JPanel morrowindPanel = new JPanel();
		morrowindPanel.setBorder(BorderFactory.createTitledBorder("Morrowind folder"));
		morrowindPanel.setLayout(new BorderLayout());
		morrowindPanel.add(morrowindFolderField, BorderLayout.CENTER);
		morrowindFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.MORROWIND_FOLDER_KEY, ""));
		morrowindPanel.add(morrowindSetButton, BorderLayout.EAST);
		morrowindPanel.add(morrowindFtpButton, BorderLayout.WEST);
		add(morrowindPanel);

		morrowindSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFolder("Select Morrowind Folder", PropertyLoader.MORROWIND_FOLDER_KEY, morrowindFolderField);
			}

		});
		morrowindFtpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftpData("morrowind", PropertyLoader.MORROWIND_FOLDER_KEY, morrowindFolderField);
			}
		});

		JPanel oblivionPanel = new JPanel();
		oblivionPanel.setBorder(BorderFactory.createTitledBorder("Oblivion folder"));
		oblivionPanel.setLayout(new BorderLayout());
		oblivionPanel.add(oblivionFolderField, BorderLayout.CENTER);
		oblivionFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY, ""));
		oblivionPanel.add(oblivionSetButton, BorderLayout.EAST);
		oblivionPanel.add(oblivionFtpButton, BorderLayout.WEST);
		add(oblivionPanel);

		oblivionSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFolder("Select Oblivion Folder", PropertyLoader.OBLIVION_FOLDER_KEY, oblivionFolderField);
			}

		});
		oblivionFtpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftpData("oblivion", PropertyLoader.OBLIVION_FOLDER_KEY, oblivionFolderField);
			}
		});

		JPanel fallout3Panel = new JPanel();
		fallout3Panel.setBorder(BorderFactory.createTitledBorder("Fallout3 folder"));
		fallout3Panel.setLayout(new BorderLayout());
		fallout3Panel.add(fallout3FolderField, BorderLayout.CENTER);
		fallout3FolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY, ""));
		fallout3Panel.add(fallout3SetButton, BorderLayout.EAST);
		fallout3Panel.add(fallout3FtpButton, BorderLayout.WEST);
		add(fallout3Panel);

		fallout3SetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFolder("Select Fallout3 Folder", PropertyLoader.FALLOUT3_FOLDER_KEY, fallout3FolderField);
			}

		});
		fallout3FtpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftpData("fallout", PropertyLoader.FALLOUT3_FOLDER_KEY, fallout3FolderField);
			}
		});

		JPanel falloutNVPanel = new JPanel();
		falloutNVPanel.setBorder(BorderFactory.createTitledBorder("FalloutNV folder"));
		falloutNVPanel.setLayout(new BorderLayout());
		falloutNVPanel.add(falloutNVFolderField, BorderLayout.CENTER);
		falloutNVFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY, ""));
		falloutNVPanel.add(falloutNVSetButton, BorderLayout.EAST);
		falloutNVPanel.add(falloutNVFtpButton, BorderLayout.WEST);
		add(falloutNVPanel);

		falloutNVSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFolder("Select FalloutNV Folder", PropertyLoader.FALLOUTNV_FOLDER_KEY, falloutNVFolderField);
			}

		});
		falloutNVFtpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftpData("falloutNV", PropertyLoader.FALLOUTNV_FOLDER_KEY, falloutNVFolderField);
			}
		});

		JPanel skyrimPanel = new JPanel();
		skyrimPanel.setBorder(BorderFactory.createTitledBorder("Skyrim folder"));
		skyrimPanel.setLayout(new BorderLayout());
		skyrimPanel.add(skyrimFolderField, BorderLayout.CENTER);
		skyrimFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY, ""));
		skyrimPanel.add(skyrimSetButton, BorderLayout.EAST);
		skyrimPanel.add(skyrimFtpButton, BorderLayout.WEST);
		add(skyrimPanel);

		skyrimSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFolder("Select Skyrim Folder", PropertyLoader.SKYRIM_FOLDER_KEY, skyrimFolderField);
			}

		});
		skyrimFtpButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ftpData("skyrim", PropertyLoader.SKYRIM_FOLDER_KEY, skyrimFolderField);
			}
		});

		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel);

		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
			}
		});

		cancelButton.addActionListener(new ActionListener()
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
