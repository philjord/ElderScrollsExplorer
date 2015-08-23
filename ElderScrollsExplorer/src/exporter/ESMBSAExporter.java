package exporter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import FO3Archive.ArchiveEntry;
import FO3Archive.ArchiveFile;
import bsa.BSAFileSet;
import common.config.ConfigLoader;
import esmLoader.common.PluginException;
import esmLoader.common.data.plugin.PluginRecord;
import esmLoader.loader.ESMManager;
import esmLoader.loader.IESMManager;
import esmj3d.j3d.cell.J3dICellFactory;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.SetBethFoldersDialog;
import tools.TitledPanel;
import tools.swing.TitledJFileChooser;
import tools.swing.VerticalFlowLayout;
import tools3d.resolution.QueryProperties;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;

public class ESMBSAExporter extends JFrame
{
	private static JTable table;

	private static DefaultTableModel tableModel;

	private static String[] columnNames = new String[] { " ", "Int/Ext", "Cell Id", "Name" };

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSAFileSet bsaFileSet;

	public JButton falloutButton = new JButton("Fall Out");

	public JButton falloutNVButton = new JButton("Fall Out NV");

	public JButton oblivionButton = new JButton("Oblivion");

	public JButton skyrimButton = new JButton("Skyrim");

	public JPanel mainPanel = new JPanel();

	public JPanel buttonPanel = new JPanel();

	public JPanel options = new JPanel();

	public JButton export = new JButton("Export");

	public JSpinner cellLevels = new JSpinner();

	public JMenuItem setFolders = new JMenuItem("Set Folders");

	private JTextField outputFolderField = new JTextField("");

	private JButton outputSetButton = new JButton("...");

	public Preferences prefs;

	private String scrollsFolder = "";

	private String mainESMFile = "";

	private J3dICellFactory j3dCellFactory;

	public ESMBSAExporter()
	{
		super("ScrollsExplorer");
		try
		{
			PropertyLoader.load();

			prefs = Preferences.userNodeForPackage(ESMBSAExporter.class);

			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(1, 1));
			this.setSize(500, 1000);

			mainPanel.setLayout(new BorderLayout());

			JMenuBar menuBar = new JMenuBar();
			menuBar.setOpaque(true);
			JMenu menu = new JMenu("File");
			menu.setMnemonic(70);

			menuBar.add(menu);

			menu.add(setFolders);
			setFolders.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					setFolders();
				}
			});

			this.setJMenuBar(menuBar);
			// this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			// this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

			buttonPanel.add(oblivionButton);
			buttonPanel.add(falloutButton);
			buttonPanel.add(falloutNVButton);
			buttonPanel.add(skyrimButton);

			oblivionButton.setEnabled(false);
			falloutButton.setEnabled(false);
			falloutNVButton.setEnabled(false);
			skyrimButton.setEnabled(false);

			oblivionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollsFolder = PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY);
					mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Oblivion.esm";
					loadUpPickers();
				}
			});
			falloutButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollsFolder = PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY);
					mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Fallout3.esm";
					loadUpPickers();
				}
			});

			falloutNVButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollsFolder = PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY);
					mainESMFile = scrollsFolder + PropertyLoader.fileSep + "FalloutNV.esm";
					loadUpPickers();
				}
			});
			skyrimButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollsFolder = PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY);
					mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Skyrim.esm";
					loadUpPickers();
				}
			});

			options.setLayout(new VerticalFlowLayout());

			options.add(new TitledPanel("Export", export));
			options.add(new JPanel());
			options.add(new TitledPanel("Cell Levels", cellLevels));

			mainPanel.add(buttonPanel, BorderLayout.NORTH);
			table = new JTable();
			mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

			this.getContentPane().invalidate();
			this.getContentPane().validate();
			this.getContentPane().doLayout();
			this.invalidate();
			this.validate();
			this.doLayout();

			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(options, BorderLayout.WEST);

			export.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					export();
				}
			});

			JPanel outputPanel = new JPanel();
			outputPanel.setBorder(BorderFactory.createTitledBorder("Output folder"));
			outputPanel.setLayout(new BorderLayout());
			outputPanel.add(outputFolderField, BorderLayout.CENTER);
			outputFolderField.setText(PropertyLoader.properties.getProperty(PropertyLoader.OUTPUT_FOLDER_KEY, ""));
			outputPanel.add(outputSetButton, BorderLayout.EAST);

			options.add(new JPanel());
			options.add(new TitledPanel("Output Folder", outputPanel));

			outputSetButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					File sf = TitledJFileChooser.requestFolderName("Select Output Folder",
							PropertyLoader.properties.getProperty(PropertyLoader.OUTPUT_FOLDER_KEY, ""),
							ESMBSAExporter.this);
					if (sf != null)
					{
						PropertyLoader.properties.setProperty(PropertyLoader.OUTPUT_FOLDER_KEY, sf.getAbsolutePath());
						outputFolderField.setText(sf.getAbsolutePath());
						enableButtons();
					}
				}
			});

			this.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent arg0)
				{
					closingTime();
				}
			});

			enableButtons();

		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * 
	 * @param meshSource
	 * @param textureSource
	 * @param soundSource
	 */
	public void setSources(IESMManager esmManager2, MediaSources mediaSources)
	{
		this.esmManager = esmManager2;

		float version = esmManager2.getVersion();

		if (version == 0.94f)
		{
			if (esmManager2.getName().equals("Skyrim.esm"))
			{
				j3dCellFactory = new esmj3dtes5.j3d.cell.J3dCellFactory(esmManager2, esmManager2, mediaSources);
			}
			else
			{

				j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager2, esmManager2, mediaSources);
			}
		}
		else if (version == 1.32f)
		{
			j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager2, esmManager2, mediaSources);
		}
		else if (version == 1.0f || version == 0.8f)
		{
			j3dCellFactory = new esmj3dtes4.j3d.cell.J3dCellFactory(esmManager2, esmManager2, mediaSources);
		}
		else
		{
			System.out.println("Bad esm version! " + version + " in " + esmManager2.getName());
		}

		// System.out.println("j3dCellFactory = " + j3dCellFactory);
	}

	private void setFolders()
	{
		SetBethFoldersDialog setBethFoldersDialog = new SetBethFoldersDialog(this);
		setBethFoldersDialog.setSize(300, 250);
		setBethFoldersDialog.setVisible(true);
		enableButtons();
	}

	private void enableButtons()
	{
		// in case of nothing selected show dialog, funy infinite loop for
		// recidivst non setters
		if (PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY) != null
				|| PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY) != null
				|| PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY) != null
				|| PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY) != null)
		{
			String oblivionFolder = PropertyLoader.properties.getProperty(PropertyLoader.OBLIVION_FOLDER_KEY);
			oblivionButton.setEnabled(oblivionFolder != null);
			String fallOut3Folder = PropertyLoader.properties.getProperty(PropertyLoader.FALLOUT3_FOLDER_KEY);
			falloutButton.setEnabled(fallOut3Folder != null);
			String falloutNVFolder = PropertyLoader.properties.getProperty(PropertyLoader.FALLOUTNV_FOLDER_KEY);
			falloutNVButton.setEnabled(falloutNVFolder != null);
			String skyrimFolder = PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY);
			skyrimButton.setEnabled(skyrimFolder != null);
		}
		else
		{
			setFolders();
		}
		mainPanel.validate();
		mainPanel.invalidate();
		mainPanel.doLayout();
		mainPanel.repaint();

		File outputFolder = new File(outputFolderField.getText());
		export.setEnabled(outputFolder.exists() && outputFolder.isDirectory());
	}

	/**
	 
	 */
	private void loadUpPickers()
	{
		Thread t = new Thread()
		{
			public void run()
			{
				synchronized (mainESMFile)
				{
					esmManager = ESMManager.getESMManager(mainESMFile);
					bsaFileSet = null;

					new EsmSoundKeyToName(esmManager);
					BsaRecordedMeshSource meshSource;
					BsaRecordedTextureSource textureSource;
					BsaRecordedSoundSource soundSource;

					String plusSkyrim = PropertyLoader.properties.getProperty(PropertyLoader.SKYRIM_FOLDER_KEY);

					// note skyrim added
					if (bsaFileSet == null)
						bsaFileSet = new BSAFileSet(new String[] { scrollsFolder, plusSkyrim }, true, false);

					meshSource = new BsaRecordedMeshSource(bsaFileSet);
					textureSource = new BsaRecordedTextureSource(bsaFileSet);
					soundSource = new BsaRecordedSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

					mediaSources = new MediaSources(meshSource, textureSource, soundSource);

					setSources(esmManager, mediaSources);

					tableModel = new DefaultTableModel(columnNames, 0)
					{
						@Override
						public boolean isCellEditable(int row, int column)
						{
							return column == 0; // ticks only
						}

						@Override
						public Class<? extends Object> getColumnClass(int c)
						{
							return getValueAt(0, c).getClass();
						}
					};

					table.setModel(tableModel);

					table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));

					try
					{
						for (Integer formId : esmManager.getAllWRLDTopGroupFormIds())
						{
							PluginRecord pr = esmManager.getWRLD(formId);
							tableModel.addRow(new Object[] { false, "Ext", formId, pr });
						}

						for (Integer formId : esmManager.getAllInteriorCELLFormIds())
						{
							PluginRecord pr = esmManager.getInteriorCELL(formId);
							tableModel.addRow(new Object[] { false, "Int", formId, pr });
						}
					}
					catch (DataFormatException e1)
					{
						e1.printStackTrace();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					catch (PluginException e1)
					{
						e1.printStackTrace();
					}

					table.getColumnModel().getColumn(0).setMaxWidth(20);
					table.getColumnModel().getColumn(1).setMaxWidth(30);
					table.getColumnModel().getColumn(2).setMaxWidth(60);

					mainPanel.validate();
					mainPanel.invalidate();
					mainPanel.doLayout();
					mainPanel.repaint();

				}
			}
		};
		t.start();
	}

	private void export()
	{
		// TODO: exteriors, step through all cells possible (lod loads should be
		// fine)
		// TODO: nested levels
		// TODO: animations for each CREA or CHAR how to find all animations
		// TODO: sounds, found in nifs

		long startTime = System.currentTimeMillis();
		// for each cell picked
		for (int i = 0; i < tableModel.getRowCount(); i++)
		{

			boolean checked = (Boolean) tableModel.getValueAt(i, 0);
			if (checked)
			{
				long cellStartTime = System.currentTimeMillis();

				String intExt = (String) tableModel.getValueAt(i, 1);
				int formId = (Integer) tableModel.getValueAt(i, 2);

				System.out.println("" + intExt + " id =" + formId + " loading...");

				try
				{
					if (intExt.equals("Ext"))
					{
						PluginRecord cell = esmManager.getWRLD(formId);
						if (cell != null)
						{
							// currentBethWorldVisualBranch = new
							// BethWorldVisualBranch(currentCellFormId,
							// j3dCellFactory);
							System.out.println("ext todo");
						}
						else
						{
							System.out.println("unknown cell id " + formId);
						}
					}
					else
					{
						// must be interior
						PluginRecord cell = esmManager.getInteriorCELL(formId);
						if (cell != null)
						{
							j3dCellFactory.makeBGInteriorCELLPersistent(formId, false);
							j3dCellFactory.makeBGInteriorCELLTemporary(formId, false);
							j3dCellFactory.makeBGInteriorCELLDistant(formId, false);
						}
						else
						{
							System.out.println("unknown cell id " + formId);
						}
					}
				}
				catch (DataFormatException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (PluginException e)
				{
					e.printStackTrace();
				}

				System.out.println("Cell load complete in " + (System.currentTimeMillis() - cellStartTime) + "ms");
			}

		}

		File outputFolder = new File(outputFolderField.getText());
		try
		{
			System.out.println("Nifs");
			HashSet<String> ms = ((BsaRecordedMeshSource) mediaSources.getMeshSource()).requestedFiles;
			for (String nifName : ms)
			{
				System.out.print("Processing " + nifName);
				if (!nifName.toLowerCase().startsWith("meshes"))
				{
					nifName = "Meshes\\" + nifName;
				}

				InputStream inputStream = null;
				for (ArchiveFile archiveFile : bsaFileSet)
				{
					ArchiveEntry archiveEntry = archiveFile.getEntry(nifName);
					if (archiveEntry != null)
					{
						inputStream = archiveFile.getInputStream(archiveEntry);
						System.out.print(" found input stream ");
						// don't check others
						break;
					}
				}

				if (inputStream != null)
				{
					File dest = new File(outputFolder.getAbsolutePath() + "\\" + nifName);

					dest.getParentFile().mkdirs();
					dest.createNewFile();
					System.out.print(" copying ");
					copyInputStreamToFile(inputStream, dest);
					System.out.print("done");

				}
				else
				{
					System.out.println("Can't find " + nifName + " in Bsas");
				}
				System.out.println("");
			}

			HashSet<String> ts = ((BsaRecordedTextureSource) mediaSources.getTextureSource()).requestedFiles;
			System.out.println("Textures");
			for (String tex : ts)
			{
				System.out.print("Processing " + tex);
				if (!tex.toLowerCase().startsWith("textures"))
				{
					tex = "textures\\" + tex;
				}

				InputStream inputStream = null;
				for (ArchiveFile archiveFile : bsaFileSet)
				{
					ArchiveEntry archiveEntry = archiveFile.getEntry(tex);
					if (archiveEntry != null)
					{
						inputStream = archiveFile.getInputStream(archiveEntry);
						System.out.print(" found input stream ");
						// don't check others
						break;
					}
				}

				if (inputStream != null)
				{
					File dest = new File(outputFolder.getAbsolutePath() + "\\" + tex);

					dest.getParentFile().mkdirs();
					dest.createNewFile();
					System.out.print(" copying ");
					copyInputStreamToFile(inputStream, dest);
					System.out.print("done");

				}
				else
				{
					System.out.println("Can't find " + tex + " in Bsas");
				}
				System.out.println("");
			}
			HashSet<String> ss = ((BsaRecordedSoundSource) mediaSources.getSoundSource()).requestedFiles;
			System.out.println("Sounds");
			for (String soun : ss)
			{
				System.out.print("Processing " + soun);
				if (!soun.toLowerCase().startsWith("sound"))
				{
					soun = "sound\\" + soun;
				}

				InputStream inputStream = null;
				for (ArchiveFile archiveFile : bsaFileSet)
				{
					ArchiveEntry archiveEntry = archiveFile.getEntry(soun);
					if (archiveEntry != null)
					{
						inputStream = archiveFile.getInputStream(archiveEntry);
						System.out.print(" found input stream ");
						// don't check others
						break;
					}
				}

				if (inputStream != null)
				{
					File dest = new File(outputFolder.getAbsolutePath() + "\\" + soun);

					dest.getParentFile().mkdirs();
					dest.createNewFile();
					System.out.print(" copying ");
					copyInputStreamToFile(inputStream, dest);
					System.out.print("done");

				}
				else
				{
					System.out.println("Can't find " + soun + " in Bsas");
				}
				System.out.println("");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Export complete in " + (System.currentTimeMillis() - startTime) + "ms");
	}

	private void copyInputStreamToFile(InputStream in, File file)
	{
		OutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			byte[] buf = new byte[1024 * 1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		}
		catch (Exception e)
		{
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

	public void closingTime()
	{
		PropertyLoader.save();
	}

	public static void main(String[] args)
	{

		// DDS requires no installed java3D
		if (QueryProperties.checkForInstalledJ3d())
		{
			System.exit(0);
		}

		ConfigLoader.loadConfig(args);

		ESMBSAExporter scrollsExplorer = new ESMBSAExporter();
		scrollsExplorer.setVisible(true);
	}

}