package scrollsexplorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.gg.slider.SideBar;
import com.gg.slider.SideBar.SideBarMode;
import com.gg.slider.SidebarSection;
import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

import archive.BSArchiveSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import client.BootStrap;
import esmj3d.j3d.BethRenderSettings;
import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;
import nativeLinker.LWJGLLinker;
import nif.BgsmSource;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.SimpleWalkSetup;
import scrollsexplorer.simpleclient.SimpleWalkSetupInterface;
import scrollsexplorer.simpleclient.settings.GeneralSettingsPanel;
import scrollsexplorer.simpleclient.settings.GraphicsSettingsPanel;
import scrollsexplorer.simpleclient.settings.SetBethFoldersDialog;
import scrollsexplorer.simpleclient.settings.ShowOutlinesPanel;
import tools.TitledPanel;
import tools.io.ConfigLoader;
import tools.swing.UserGuideDisplay;
import tools.swing.VerticalFlowLayout;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileSoundSource;
import utils.source.file.FileTextureSource;

public class ScrollsExplorer extends JFrame implements BethRenderSettings.UpdateListener, LocationUpdateListener
{
	public Dashboard dashboard = new Dashboard();

	private SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetupInterface simpleWalkSetup;

	private static JTable table;

	private GraphicsSettingsPanel graphicsSettingsPanel;

	private ShowOutlinesPanel showOutlinesPanel;

	private GeneralSettingsPanel generalSettingsPanel;

	private DefaultTableModel tableModel;

	private String[] columnNames = new String[] { "Int/Ext", "Cell Id", "Name" };

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSArchiveSet bsaFileSet;

	private GameConfig selectedGameConfig = null;

	private HashMap<GameConfig, JButton> gameButtons = new HashMap<GameConfig, JButton>();

	public JPanel mainPanel = new JPanel();

	public JPanel buttonPanel = new JPanel();

	public JPanel quickEdit = new JPanel();

	public JCheckBoxMenuItem cbLoadAllMenuItem = new JCheckBoxMenuItem("Load all BSA Archives", true);

	public JCheckBoxMenuItem cbBsaMenuItem = new JCheckBoxMenuItem("Use BSA not Files", true);

	public JCheckBoxMenuItem cbAzertyKB = new JCheckBoxMenuItem("Azerty", false);

	public JMenuItem setFolders = new JMenuItem("Set Folders");

	public JMenuItem setGraphics = new JMenuItem("Set Graphics");

	public JMenuItem showUserGuide = new JMenuItem("User Guide");

	private UserGuideDisplay ugd = new UserGuideDisplay();

	private Preferences prefs;

	private boolean autoLoadStartCell = true;

	public ScrollsExplorer()
	{
		super("ScrollsExplorer");

		try
		{
			PropertyLoader.load();

			prefs = Preferences.userNodeForPackage(ScrollsExplorerNewt.class);

			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(1, 1));
			this.setSize(600, 800);

			mainPanel.setLayout(new BorderLayout());

			JMenuBar menuBar = new JMenuBar();
			menuBar.setOpaque(true);
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			menuBar.add(fileMenu);

			boolean loadAll = Boolean.parseBoolean(prefs.get("load.all", "true"));
			cbLoadAllMenuItem.setSelected(loadAll);
			fileMenu.add(cbLoadAllMenuItem);

			boolean useBsa = Boolean.parseBoolean(prefs.get("use.bsa", "true"));
			cbBsaMenuItem.setSelected(useBsa);
			fileMenu.add(cbBsaMenuItem);

			boolean useAzerty = Boolean.parseBoolean(prefs.get("use.azerty", "false"));
			cbAzertyKB.setSelected(useAzerty);
			fileMenu.add(cbAzertyKB);
			cbAzertyKB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					simpleWalkSetup.setAzerty(cbAzertyKB.isSelected());
				}
			});

			fileMenu.add(setFolders);
			setFolders.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					setFolders();
				}
			});

			fileMenu.add(setGraphics);
			setGraphics.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					simpleWalkSetup.resetGraphicsSetting();
				}
			});
			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			menuBar.add(helpMenu);

			helpMenu.add(showUserGuide);
			showUserGuide.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					showUserGuide();
				}
			});

			this.setJMenuBar(menuBar);
			//this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			//this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

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

			simpleWalkSetup = new SimpleWalkSetup("SimpleBethCellManager");
			simpleWalkSetup.setAzerty(cbAzertyKB.isSelected());
			quickEdit.setLayout(new VerticalFlowLayout());
			quickEdit.add(new TitledPanel("Location", locField));
			quickEdit.add(new TitledPanel("Go To", locField));
			mainPanel.add(buttonPanel, BorderLayout.NORTH);
			table = new JTable();
			mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

			mainPanel.add(dashboard.getMainPanel(), BorderLayout.SOUTH);

			simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

			graphicsSettingsPanel = new GraphicsSettingsPanel();
			showOutlinesPanel = new ShowOutlinesPanel(simpleWalkSetup);
			generalSettingsPanel = new GeneralSettingsPanel(this);
			BethRenderSettings.addUpdateListener(this);

			this.getContentPane().invalidate();
			this.getContentPane().validate();
			this.getContentPane().doLayout();
			this.invalidate();
			this.validate();
			this.doLayout();

			SideBar sideBar = new SideBar(SideBarMode.TOP_LEVEL, true, 200, true);
			//SidebarSection ss1 = new SidebarSection(sideBar, "dashboard", dashboard, null);
			//sideBar.addSection(ss1);
			SidebarSection ss2 = new SidebarSection(sideBar, "Avartar", quickEdit, null);
			sideBar.addSection(ss2);
			SidebarSection ss3 = new SidebarSection(sideBar, "General", generalSettingsPanel, null);
			sideBar.addSection(ss3);
			SidebarSection ss4 = new SidebarSection(sideBar, "Graphics", graphicsSettingsPanel, null);
			sideBar.addSection(ss4);
			SidebarSection ss5 = new SidebarSection(sideBar, "Outlines", showOutlinesPanel, null);
			sideBar.addSection(ss5);

			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(sideBar, BorderLayout.WEST);

			this.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent arg0)
				{
					//Just until the real window listens to damn events properly!					
					simpleWalkSetup.closingTime();
					closingTime();
					System.exit(0);
				}
			});
			

			setVisible(true);// need to be visible in case of set folders
			// My system for guarantees rendering of a component (test this)
			this.setFont(this.getFont());
			enableButtons();

		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		// MY system for guaranteee rendering of a component (test this)
		this.setFont(this.getFont());

		warpPanel.setLayout(new FlowLayout());
		warpPanel.add(warpField);
		warpField.setSize(200, 20);
		ActionListener warpActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String warp = warpField.getText().trim();
				String[] parts = warp.split("[^\\d-]+");

				if (parts.length == 3)
				{
					simpleWalkSetup.warp(new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
				}
			}
		};
		warpField.addActionListener(warpActionListener);
		JButton warpButton = new JButton("Go");
		warpPanel.add(warpButton);
		warpButton.addActionListener(warpActionListener);

		simpleWalkSetup.getAvatarLocation().addAvatarLocationListener(this);
	}

	private JTextField locField = new JTextField("0000,0000,0000");

	private JPanel warpPanel = new JPanel();

	private JTextField warpField = new JTextField("                ");

	public Component getLocField()
	{
		return locField;
	}

	public Component getWarpField()
	{
		return warpPanel;
	}

	private long lastLocationUpdate = 0;

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		if (System.currentTimeMillis() - lastLocationUpdate > 200)
		{
			//oddly this is mildly expensive so only update 5 times per second
			locField.setText(("" + trans.x).split("\\.")[0] + "," + ("" + trans.y).split("\\.")[0] + "," + ("" + trans.z).split("\\.")[0]);
			lastLocationUpdate = System.currentTimeMillis();
		}
	}

	protected void showUserGuide()
	{
		ugd.display(this, "docs\\userGuide.htm");
	}

	public void closingTime()
	{
		if (esmManager != null)
		{
			PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(),
					new YawPitch(simpleWalkSetup.getAvatarLocation().getTransform()).toString());
			PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
					"" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
			PropertyLoader.properties.setProperty("CellId" + esmManager.getName(), "" + simpleBethCellManager.getCurrentCellFormId());
		}
		PropertyLoader.save();

		prefs.put("use.bsa", Boolean.toString(cbBsaMenuItem.isSelected()));
		prefs.put("load.all", Boolean.toString(cbLoadAllMenuItem.isSelected()));
		prefs.put("use.azerty", Boolean.toString(cbAzertyKB.isSelected()));

	}

	private void setFolders()
	{
		SetBethFoldersDialog setBethFoldersDialog = new SetBethFoldersDialog(this);
		setBethFoldersDialog.setSize(400, 400);
		setBethFoldersDialog.setVisible(true);
		setBethFoldersDialog.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e)
			{
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
			}

			@Override
			public void componentShown(ComponentEvent e)
			{
			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
				enableButtons();
			}
		});

	}

	private void enableButtons()
	{
		boolean noFoldersSet = true;
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			JButton gameButton = gameButtons.get(gameConfig);
			// must have no game selected and have a folder and folder must ahve right files
			boolean enable = selectedGameConfig == null && gameConfig.scrollsFolder != null && hasESMAndBSAFiles(gameConfig);
			gameButton.setEnabled(enable);
			noFoldersSet = noFoldersSet && gameConfig.scrollsFolder == null;
		}

		//in case of nothing selected show dialog, funny infinite loop for recidivist non-setters
		if (noFoldersSet)
		{
			showUserGuide();
			setFolders();
		}
		mainPanel.validate();
		mainPanel.invalidate();
		mainPanel.doLayout();
		mainPanel.repaint();
	}

	private static boolean hasESMAndBSAFiles(GameConfig gameConfig)
	{
		// check to ensure the esm file and at least one bsa file are in the folder
		File checkEsm = new File(gameConfig.scrollsFolder, gameConfig.mainESMFile);
		if (!checkEsm.exists())
		{
			return false;
		}

		int countOfBsa = 0;
		File checkBsa = new File(gameConfig.scrollsFolder);
		for (File f : checkBsa.listFiles())
		{
			countOfBsa += f.getName().toLowerCase().endsWith(".bsa") ? 1 : 0;
			countOfBsa += f.getName().toLowerCase().endsWith(".ba2") ? 1 : 0;
		}

		if (countOfBsa == 0)
		{
			return false;
		}

		return true;
	}

	@Override
	public void renderSettingsUpdated()
	{
		simpleBethCellManager.updateBranches();
	}

	/**
	 
	 */
	private void setSelectedGameConfig(GameConfig newGameConfig)
	{
		selectedGameConfig = newGameConfig;
		enableButtons();
		simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(selectedGameConfig.avatarYHeight);

		Thread t = new Thread() {
			public void run()
			{
				synchronized (selectedGameConfig)
				{
					IDashboard.dashboard.setEsmLoading(1);

					esmManager = ESMManager.getESMManager(selectedGameConfig.getESMPath());
					bsaFileSet = null;
					if (esmManager != null)
					{
						YawPitch yp = YawPitch
								.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(), new YawPitch().toString()));
						Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
								selectedGameConfig.startLocation.toString()));
						int prevCellformid = Integer.parseInt(PropertyLoader.properties.getProperty("CellId" + esmManager.getName(), "-1"));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);

						if (prevCellformid == -1)
						{
							prevCellformid = selectedGameConfig.startCellId;
						}

						new EsmSoundKeyToName(esmManager);
						MeshSource meshSource;
						TextureSource textureSource;
						SoundSource soundSource;

						if (cbBsaMenuItem.isSelected())
						{
							if (bsaFileSet == null)
							{
								bsaFileSet = new BSArchiveSet(new String[] { selectedGameConfig.scrollsFolder },
										cbLoadAllMenuItem.isSelected(), false);
							}

							if (bsaFileSet.size() == 0)
							{
								JOptionPane.showMessageDialog(ScrollsExplorer.this,
										selectedGameConfig.scrollsFolder + " contains no *.bsa files nothing can be loaded");
								setFolders();
								IDashboard.dashboard.setEsmLoading(-1);
								return;
							}

							meshSource = new BsaMeshSource(bsaFileSet);
							textureSource = new BsaTextureSource(bsaFileSet);
							soundSource = new BsaSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));
						}
						else
						{
							FileMediaRoots.setMediaRoots(new String[] { selectedGameConfig.scrollsFolder });
							meshSource = new FileMeshSource();
							textureSource = new FileTextureSource();
							soundSource = new FileSoundSource();
						}

						//Just for the crazy new fallout 4 system
						BgsmSource.setBgsmSource(meshSource);

						mediaSources = new MediaSources(meshSource, textureSource, soundSource);

						simpleWalkSetup.configure(meshSource, simpleBethCellManager);
						simpleWalkSetup.setEnabled(false);

						//FIXME: stops working once fully running, but responds up to that point
						// that is to say the button no longer sends anything through
						// button won't work off the event thread, so I need to add my own system in and ignore the button
						// button only runs if display is called on the window but that cuts FPS in half
						simpleWalkSetup.getWindow().setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
						simpleWalkSetup.getWindow().addWindowListener(new WindowAdapter() {
							@Override
							public void windowDestroyNotify(WindowEvent arg0)
							{								
								simpleWalkSetup.closingTime();
								closingTime();
								System.exit(0);
							}

						});
						simpleWalkSetup.getWindow().addKeyListener(new com.jogamp.newt.event.KeyAdapter() {
							@Override
							public void keyPressed(com.jogamp.newt.event.KeyEvent e)
							{
								if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
								{									
									simpleWalkSetup.closingTime();
									closingTime();
									System.exit(0);
								}
							}
						});

						// I could use the j3dcellfactory now? with the cached cell records?
						simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

						tableModel = new DefaultTableModel(columnNames, 0) {
							@Override
							public boolean isCellEditable(int row, int column)
							{
								return false; // disallow editing of the table
							}

							@Override
							public Class<? extends Object> getColumnClass(int c)
							{
								return getValueAt(0, c).getClass();
							}
						};

						table.setModel(tableModel);
						table.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e)
							{
								display(((Integer) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1)));
							}

						});

						table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));

						try
						{
							for (Integer formId : esmManager.getAllWRLDTopGroupFormIds())
							{
								PluginRecord pr = esmManager.getWRLD(formId);
								if (prevCellformid == formId)
									tableModel.insertRow(0, new Object[] { "Ext", formId, pr });
								else
									tableModel.addRow(new Object[] { "Ext", formId, pr });
							}

							for (Integer formId : esmManager.getAllInteriorCELLFormIds())
							{
								PluginRecord pr = esmManager.getInteriorCELL(formId);
								if (prevCellformid == formId)
									tableModel.insertRow(0, new Object[] { "Int", formId, pr });
								else
									tableModel.addRow(new Object[] { "Int", formId, pr });
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

						table.getColumnModel().getColumn(0).setMaxWidth(30);
						table.getColumnModel().getColumn(1).setMaxWidth(60);

						if (autoLoadStartCell)
						{
							display(prevCellformid);
						}
					}
					else
					{
						JOptionPane.showMessageDialog(ScrollsExplorer.this,
								selectedGameConfig.mainESMFile + " is not in folder set for game " + selectedGameConfig.gameName);
						setFolders();
					}
					mainPanel.validate();
					mainPanel.invalidate();
					mainPanel.doLayout();
					mainPanel.repaint();

					IDashboard.dashboard.setEsmLoading(-1);
				}

			}
		};
		t.start();
	}

	private void display(final int cellformid)
	{
		Vector3f t = simpleWalkSetup.getAvatarLocation().get(new Vector3f());
		Quat4f r = simpleWalkSetup.getAvatarLocation().get(new Quat4f());
		simpleBethCellManager.setCurrentCellFormId(cellformid, t, r);
	}

	public boolean isAutoLoadStartCell()
	{
		return autoLoadStartCell;
	}

	public void setAutoLoadStartCell(boolean autoLoadStartCell)
	{
		this.autoLoadStartCell = autoLoadStartCell;
	}

	public SimpleBethCellManager getSimpleBethCellManager()
	{
		return simpleBethCellManager;
	}

	public SimpleWalkSetupInterface getSimpleWalkSetup()
	{
		return simpleWalkSetup;
	}

	private static void setDebug(boolean b)
	{
		if (b)
		{
			System.out.println("DEBUG ON");
			// leave settings alone for optional debug parts
		}
		else
		{

		}
	}

	public static void main(String[] args)
	{
		//Arguments for goodness
		//-Xmx1200m -Xms900m  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Dsun.java2d.noddraw=true
		//-Dj3d.cacheAutoComputeBounds=true -Dj3d.sharedctx=true
		//-Dj3d.stencilClear=true  -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -server -Djava.ext.dirs=.\none\

		// some other interesting settings
		//java -server -XX:CompileThreshold=2 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods

		//-Dj3d.implicitAntialiasing=true check it why not set? MacOSX needs for AA, if set always AA

		String versionString = BootStrap.ZIP_PREFIX + "-" + BootStrap.MAJOR_VERSION + "-" + BootStrap.MINOR_VERSION;
		System.out.println("VERSION: " + versionString);
		System.err.println("VERSION: " + versionString);

		//jogl recommends for non phones 
		//System.setProperty("jogl.disable.opengles", "true");
		System.setProperty("sun.awt.noerasebackground", "true");

		ConfigLoader.loadConfig(args);

		// always load lwjgl for jbullet debug
		new LWJGLLinker();

		if (args.length > 0 && args[0].equals("debug"))
		{
			ScrollsExplorer.setDebug(true);
		}
		else
		{
			ScrollsExplorer.setDebug(false);
		}

		new ScrollsExplorer();
	}

}
