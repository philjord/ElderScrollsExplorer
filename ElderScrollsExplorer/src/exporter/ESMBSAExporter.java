package exporter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;

import javax.swing.BorderFactory;
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

import awt.tools3d.resolution.QueryProperties;
import bsa.gui.BSAFileSetWithStatus;
import bsaio.ArchiveEntry;
import bsaio.ArchiveFile;
import esmio.common.PluginException;
import esmio.common.data.plugin.PluginRecord;
import esmio.loader.CELLDIALPointer;
import esmio.loader.ESMManager;
import esmio.loader.IESMManager;
import esmio.utils.source.EsmSoundKeyToName;
import esmj3d.j3d.cell.J3dICellFactory;
import scrollsexplorer.GameConfig;
import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.settings.SetBethFoldersDialog;
import tools.io.ConfigLoader;
import tools.io.FileCopy;
import tools.swing.TitledJFileChooser;
import tools.swing.TitledPanel;
import tools.swing.VerticalFlowLayout;
import utils.source.MediaSources;

public class ESMBSAExporter extends JFrame
{
	public static String OUTPUT_FOLDER_KEY = "outputFolder";

	private static JTable table;

	private static DefaultTableModel tableModel;

	private static String[] columnNames = new String[] { " ", "Int/Ext", "Cell Id", "Name" };

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSAFileSetWithStatus bsaFileSet;

	private GameConfig selectedGameConfig = null;

	private HashMap<GameConfig, JButton> gameButtons = new HashMap<GameConfig, JButton>();

	public JPanel mainPanel = new JPanel();

	public JPanel buttonPanel = new JPanel();

	public JPanel options = new JPanel();

	public JButton export = new JButton("Export");

	public JSpinner cellLevels = new JSpinner();

	public JMenuItem setFolders = new JMenuItem("Set Folders");

	private JTextField outputFolderField = new JTextField("");

	private JButton outputSetButton = new JButton("...");

	public Preferences prefs;

	private J3dICellFactory j3dCellFactory;

	public ESMBSAExporter() throws IOException
	{
		super("ScrollsExplorer");

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
		setFolders.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setFolders();
			}
		});

		this.setJMenuBar(menuBar);
		// this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		// this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setLayout(new GridLayout(-1, 3));

		for (final GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			JButton gameButton = new JButton(gameConfig.gameName);
			buttonPanel.add(gameButton);
			gameButton.setEnabled(false);
			gameButtons.put(gameConfig, gameButton);
			gameButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setSelectedGameConfig(gameConfig);
				}
			});
		}

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

		export.addActionListener(new ActionListener() {
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
		outputFolderField.setText(PropertyLoader.properties.getProperty(OUTPUT_FOLDER_KEY, ""));
		outputPanel.add(outputSetButton, BorderLayout.EAST);

		options.add(new JPanel());
		options.add(new TitledPanel("Output Folder", outputPanel));

		outputSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File sf = TitledJFileChooser.requestFolderName("Select Output Folder",
						PropertyLoader.properties.getProperty(OUTPUT_FOLDER_KEY, ""), ESMBSAExporter.this);
				if (sf != null)
				{
					PropertyLoader.properties.setProperty(OUTPUT_FOLDER_KEY, sf.getAbsolutePath());
					outputFolderField.setText(sf.getAbsolutePath());
					enableButtons();
				}
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				closingTime();
			}
		});

		enableButtons();

	}

	private void setFolders()
	{
		SetBethFoldersDialog setBethFoldersDialog = new SetBethFoldersDialog(this);
		setBethFoldersDialog.setSize(300, 400);
		setBethFoldersDialog.setVisible(true);
		enableButtons();
	}

	private void enableButtons()
	{
		boolean noFolderSet = true;
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			JButton gameButton = gameButtons.get(gameConfig);
			gameButton.setEnabled(gameConfig.scrollsFolder != null);
			noFolderSet = noFolderSet && (gameConfig.scrollsFolder == null);
		}

		//in case of nothing selected show dialog, funny infinite loop for recidivist non-setters
		if (noFolderSet)
		{
			//showUserGuide();
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
	private void setSelectedGameConfig(GameConfig newGameConfig)
	{

		selectedGameConfig = newGameConfig;
		//simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(selectedGameConfig.avatarYHeight);

		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			JButton gameButton = gameButtons.get(gameConfig);
			gameButton.setEnabled(false);
		}

		Thread t = new Thread() {
			public void run()
			{
				synchronized (selectedGameConfig)
				{

					esmManager = ESMManager.getESMManager(selectedGameConfig.getESMPath());
					bsaFileSet = null;

					new EsmSoundKeyToName(esmManager);
					BsaRecordedMeshSource meshSource;
					BsaRecordedTextureSource textureSource;
					BsaRecordedSoundSource soundSource;

					// note skyrim added
					if (bsaFileSet == null)
						bsaFileSet = new BSAFileSetWithStatus(new String[] { selectedGameConfig.scrollsFolder }, true, false);

					meshSource = new BsaRecordedMeshSource(bsaFileSet);
					textureSource = new BsaRecordedTextureSource(bsaFileSet);
					soundSource = new BsaRecordedSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

					mediaSources = new MediaSources(meshSource, textureSource, soundSource);

					j3dCellFactory = selectedGameConfig.j3dCellFactory;
					j3dCellFactory.setSources(esmManager, mediaSources);

					tableModel = new DefaultTableModel(columnNames, 0) {
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

						for (CELLDIALPointer cp : esmManager.getAllInteriorCELLFormIds())
						{
							int formId = cp.formId;
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
						// TODO: exteriors, step through all cells possible (lod loads should be
						// fine) test
						// TODO: nested levels for exterior (e.g. for all houses inside cheydinhal

						PluginRecord cell = esmManager.getWRLD(formId);
						if (cell != null)
						{
							//currentBethWorldVisualBranch = new BethWorldVisualBranch(currentCellFormId, j3dCellFactory);

							String lodWorldFormId = j3dCellFactory.getLODWorldName(formId);

							//lods
							int[] scales = new int[] { 32, 16, 8, 4 };

							//obliv only has one scale
							float version = esmManager.getVersion();
							if (version == 1.0f || version == 0.8f)
							{
								scales = new int[] { 32 };
							}

							for (int scale : scales)
							{
								for (int x = -96; x < 96; x += scale)
								{
									for (int y = -96; y < 96; y += scale)
									{
										long xyStartTime = System.currentTimeMillis();
										Object od = j3dCellFactory.makeLODLandscape(x, y, scale, lodWorldFormId);
										if (od != null)
											System.out.println("x " + x + " y " + y + " scale " + scale + " complete in "
													+ (System.currentTimeMillis() - xyStartTime) + "ms");
									}
								}
							}

							//persistents
							j3dCellFactory.makeBGWRLDPersistent(formId, false);
							//distants, nears
							for (int x = -96; x <= 96; x++)
							{
								for (int y = -96; y <= 96; y++)
								{
									long xyStartTime = System.currentTimeMillis();
									Object od = j3dCellFactory.makeBGWRLDDistant(formId, x, y, false);
									Object ot = j3dCellFactory.makeBGWRLDTemporary(formId, x, y, false);
									if (od != null || ot != null)
										System.out.println(
												"x " + x + " y " + y + " complete in " + (System.currentTimeMillis() - xyStartTime) + "ms");

								}
							}
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
		try
		{
			File outputFolder = new File(outputFolderField.getText());
			copyToOutput(outputFolder, mediaSources, bsaFileSet);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Export complete in " + (System.currentTimeMillis() - startTime) + "ms");

	}

	public static void copyToOutput(File outputFolder, MediaSources mediaSources, BSAFileSetWithStatus bsaFileSet) throws IOException
	{

		System.out.println("Nifs");
		BsaRecordedMeshSource meshSource = (BsaRecordedMeshSource) mediaSources.getMeshSource();
		HashSet<String> meshRequestedFiles = meshSource.requestedFiles;
		for (String nifName : meshRequestedFiles)
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
				FileCopy.copyInputStreamToFile(inputStream, dest);
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
		for (String texName : ts)
		{
			System.out.print("Processing " + texName);
			texName = texName.toLowerCase();

			// remove incorrect file path prefix, if it exists
			if (texName.startsWith("data\\"))
			{
				texName = texName.substring(5);
			}

			// add the textures path part
			if (!texName.startsWith("textures"))
			{
				texName = "textures\\" + texName;
			}

			InputStream inputStream = null;
			for (ArchiveFile archiveFile : bsaFileSet)
			{
				ArchiveEntry archiveEntry = archiveFile.getEntry(texName);
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
				File dest = new File(outputFolder.getAbsolutePath() + "\\" + texName);

				dest.getParentFile().mkdirs();
				dest.createNewFile();
				System.out.print(" copying ");
				FileCopy.copyInputStreamToFile(inputStream, dest);
				System.out.print("done");

			}
			else
			{
				System.out.println("Can't find " + texName + " in Bsas");
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
				FileCopy.copyInputStreamToFile(inputStream, dest);
				System.out.print("done");

			}
			else
			{
				System.out.println("Can't find " + soun + " in Bsas");
			}
			System.out.println("");
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
		try
		{
			ESMBSAExporter scrollsExplorer = new ESMBSAExporter();
			scrollsExplorer.setVisible(true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
