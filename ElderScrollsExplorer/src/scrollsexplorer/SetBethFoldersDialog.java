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

import tools.swing.TitledJFileChooser;

public class SetBethFoldersDialog extends JDialog
{
	private JTextField oblivionFolderField = new JTextField("");

	private JButton oblivionSetButton = new JButton("...");

	private JTextField fallout3FolderField = new JTextField("");

	private JButton fallout3SetButton = new JButton("...");

	private JTextField falloutNVFolderField = new JTextField("");

	private JButton falloutNVSetButton = new JButton("...");

	private JTextField skyrimFolderField = new JTextField("");

	private JButton skyrimSetButton = new JButton("...");

	public SetBethFoldersDialog(Frame f)
	{
		super(f, "Set Esm and Bsa Folders", true);
		this.setLayout(new GridLayout(-1, 1));

		JPanel oblivionPanel = new JPanel();
		oblivionPanel.setBorder(BorderFactory.createTitledBorder("Oblivion folder"));
		oblivionPanel.setLayout(new BorderLayout());
		oblivionPanel.add(oblivionFolderField, BorderLayout.CENTER);
		oblivionFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY, ""));
		oblivionPanel.add(oblivionSetButton, BorderLayout.EAST);
		add(oblivionPanel);

		oblivionSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File sf = TitledJFileChooser.requestFolderName("Select Oblivion Folder",
						PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY, ""), SetBethFoldersDialog.this);
				if (sf != null)
				{
					PropertyLoader.properties.setProperty(PropertyLoader.OBLIVION_FOLDER_KEY, sf.getAbsolutePath());
					oblivionFolderField.setText(sf.getAbsolutePath());
				}
			}

		});

		JPanel fallout3Panel = new JPanel();
		fallout3Panel.setBorder(BorderFactory.createTitledBorder("Fallout3 folder"));
		fallout3Panel.setLayout(new BorderLayout());
		fallout3Panel.add(fallout3FolderField, BorderLayout.CENTER);
		fallout3FolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY, ""));
		fallout3Panel.add(fallout3SetButton, BorderLayout.EAST);
		add(fallout3Panel);

		fallout3SetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File sf = TitledJFileChooser.requestFolderName("Select Fallout3 Folder",
						PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY, ""), SetBethFoldersDialog.this);
				if (sf != null)
				{
					PropertyLoader.properties.setProperty(PropertyLoader.FALLOUT3_FOLDER_KEY, sf.getAbsolutePath());
					fallout3FolderField.setText(sf.getAbsolutePath());
				}
			}

		});

		JPanel falloutNVPanel = new JPanel();
		falloutNVPanel.setBorder(BorderFactory.createTitledBorder("FalloutNV folder"));
		falloutNVPanel.setLayout(new BorderLayout());
		falloutNVPanel.add(falloutNVFolderField, BorderLayout.CENTER);
		falloutNVFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY, ""));
		falloutNVPanel.add(falloutNVSetButton, BorderLayout.EAST);
		add(falloutNVPanel);

		falloutNVSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File sf = TitledJFileChooser.requestFolderName("Select FalloutNV Folder",
						PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY, ""), SetBethFoldersDialog.this);
				if (sf != null)
				{
					PropertyLoader.properties.setProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY, sf.getAbsolutePath());
					falloutNVFolderField.setText(sf.getAbsolutePath());
				}
			}

		});

		JPanel skyrimPanel = new JPanel();
		skyrimPanel.setBorder(BorderFactory.createTitledBorder("Skyrim folder"));
		skyrimPanel.setLayout(new BorderLayout());
		skyrimPanel.add(skyrimFolderField, BorderLayout.CENTER);
		skyrimFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY, ""));
		skyrimPanel.add(skyrimSetButton, BorderLayout.EAST);
		add(skyrimPanel);

		skyrimSetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File sf = TitledJFileChooser.requestFolderName("Select Skyrim Folder",
						PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY, ""), SetBethFoldersDialog.this);
				if (sf != null)
				{
					PropertyLoader.properties.setProperty(PropertyLoader.SKYRIM_FOLDER_KEY, sf.getAbsolutePath());
					skyrimFolderField.setText(sf.getAbsolutePath());
				}
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

	
}
