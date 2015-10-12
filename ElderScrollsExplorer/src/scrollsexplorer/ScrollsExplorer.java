package scrollsexplorer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import nativeLinker.LWJGLLinker;
import scrollsexplorer.simpleclient.ESESettingsPanel;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.SimpleWalkSetup;
import tools.TitledPanel;
import tools.swing.UserGuideDisplay;
import tools.swing.VerticalFlowLayout;
import tools3d.resolution.QueryProperties;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;
import utils.source.file.FileMediaRoots;
import utils.source.file.FileMeshSource;
import utils.source.file.FileSoundSource;
import utils.source.file.FileTextureSource;
import bsa.BSAFileSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import client.BootStrap;

import com.gg.slider.SideBar;
import com.gg.slider.SideBar.SideBarMode;
import com.gg.slider.SidebarSection;

import common.config.ConfigLoader;
import esmLoader.common.PluginException;
import esmLoader.common.data.plugin.PluginRecord;
import esmLoader.loader.ESMManager;
import esmLoader.loader.IESMManager;
import esmj3d.j3d.BethRenderSettings;

public class ScrollsExplorer extends JFrame implements BethRenderSettings.UpdateListener
{
	public static Dashboard dashboard = new Dashboard();

	private SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetup simpleWalkSetup;

	private static JTable table;

	private ESESettingsPanel eseSettingsPanel;

	private static DefaultTableModel tableModel;

	private static String[] columnNames = new String[]
	{ "Int/Ext", "Cell Id", "Name" };

	private MediaSources mediaSources;

	public IESMManager esmManager;

	public BSAFileSet bsaFileSet;

	private GameConfig selectedGameConfig = null;

	private HashMap<GameConfig, JButton> gameButtons = new HashMap<GameConfig, JButton>();

	public JPanel mainPanel = new JPanel();

	public JPanel buttonPanel = new JPanel();

	public JPanel quickEdit = new JPanel();

	public JCheckBoxMenuItem cbLoadAllMenuItem = new JCheckBoxMenuItem("Load all BSA Archives", true);

	public JCheckBoxMenuItem cbBsaMenuItem = new JCheckBoxMenuItem("Use BSA not Files", true);

	public JMenuItem setFolders = new JMenuItem("Set Folders");

	public JMenuItem setGraphics = new JMenuItem("Set Graphics");

	public JMenuItem showUserGuide = new JMenuItem("User Guide");

	private UserGuideDisplay ugd = new UserGuideDisplay();

	private Preferences prefs;

	public ScrollsExplorer()
	{
		super("ScrollsExplorer");

		try
		{
			PropertyLoader.load();

			prefs = Preferences.userNodeForPackage(ScrollsExplorer.class);

			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

			fileMenu.add(setFolders);
			setFolders.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					setFolders();
				}
			});

			fileMenu.add(setGraphics);
			setGraphics.addActionListener(new ActionListener()
			{
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
			showUserGuide.addActionListener(new ActionListener()
			{
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
				gameButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						setSelectedGameConfig(gameConfig);
					}
				});
			}

			simpleWalkSetup = new SimpleWalkSetup("SimpleBethCellManager");
			quickEdit.setLayout(new VerticalFlowLayout());
			quickEdit.add(new TitledPanel("Location", simpleWalkSetup.getLocField()));
			quickEdit.add(new TitledPanel("Go To", simpleWalkSetup.getWarpField()));
			mainPanel.add(buttonPanel, BorderLayout.NORTH);
			table = new JTable();
			mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

			mainPanel.add(dashboard, BorderLayout.SOUTH);

			simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

			eseSettingsPanel = new ESESettingsPanel(simpleWalkSetup);
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
			SidebarSection ss2 = new SidebarSection(sideBar, "Quick Edit", quickEdit, null);
			sideBar.addSection(ss2);
			SidebarSection ss4 = new SidebarSection(sideBar, "Graphics", eseSettingsPanel, null);
			sideBar.addSection(ss4);

			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(sideBar, BorderLayout.WEST);

			this.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent arg0)
				{
					closingTime();
				}
			});

			simpleWalkSetup.getJFrame().addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent arg0)
				{
					closingTime();
				}
			});
			setVisible(true);// need to be visible in case of set folders
			// MY system for guaranteee rendering of a component (test this)
			this.setFont(this.getFont());
			enableButtons();

		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		// MY system for guaranteee rendering of a component (test this)
		this.setFont(this.getFont());
	}

	protected void showUserGuide()
	{
		ugd.display(this, "docs\\userGuide.htm");
	}

	public void closingTime()
	{
		if (esmManager != null)
		{
			PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(), new YawPitch(simpleWalkSetup.getAvatarLocation()
					.getTransform()).toString());
			PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
					"" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
		}
		PropertyLoader.save();
	}

	private void setFolders()
	{
		SetBethFoldersDialog setBethFoldersDialog = new SetBethFoldersDialog(this);
		setBethFoldersDialog.setSize(400, 350);
		setBethFoldersDialog.setVisible(true);
		enableButtons();
	}

	private void enableButtons()
	{
		boolean noFoldersSet = true;
		for (GameConfig gameConfig : GameConfig.allGameConfigs)
		{
			JButton gameButton = gameButtons.get(gameConfig);
			// must have no game selected and have a folder
			boolean enable = selectedGameConfig == null && gameConfig.scrollsFolder != null;
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

		Thread t = new Thread()
		{
			public void run()
			{
				synchronized (selectedGameConfig)
				{
					ScrollsExplorer.dashboard.setEsmLoading(1);
					prefs.put("use.bsa", Boolean.toString(cbBsaMenuItem.isSelected()));
					prefs.put("load.all", Boolean.toString(cbLoadAllMenuItem.isSelected()));

					esmManager = ESMManager.getESMManager(selectedGameConfig.getESMPath());
					bsaFileSet = null;
					if (esmManager != null)
					{
						YawPitch yp = YawPitch.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(),
								new YawPitch().toString()));
						Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
								new Vector3f().toString()));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);

						new EsmSoundKeyToName(esmManager);
						MeshSource meshSource;
						TextureSource textureSource;
						SoundSource soundSource;

						if (cbBsaMenuItem.isSelected())
						{
							if (bsaFileSet == null)
							{
								bsaFileSet = new BSAFileSet(new String[]
								{ selectedGameConfig.scrollsFolder }, cbLoadAllMenuItem.isSelected(), false);
							}

							if (bsaFileSet.size() == 0)
							{
								JOptionPane.showMessageDialog(ScrollsExplorer.this, selectedGameConfig.scrollsFolder
										+ " contains no *.bsa files nothing can be loaded");
								setFolders();
								ScrollsExplorer.dashboard.setEsmLoading(-1);
								return;
							}

							meshSource = new BsaMeshSource(bsaFileSet);
							textureSource = new BsaTextureSource(bsaFileSet);
							soundSource = new BsaSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));
						}
						else
						{
							FileMediaRoots.setMediaRoots(new String[]
							{ selectedGameConfig.scrollsFolder });
							meshSource = new FileMeshSource();
							textureSource = new FileTextureSource();
							soundSource = new FileSoundSource();
						}

						mediaSources = new MediaSources(meshSource, textureSource, soundSource);

						simpleWalkSetup.configure(meshSource, simpleBethCellManager);
						simpleWalkSetup.setEnabled(false);
						//add skynow
						simpleWalkSetup.addToVisualBranch(SimpleBethCellManager.createBackground(textureSource));

						simpleBethCellManager.setSources(selectedGameConfig, esmManager, mediaSources);

						tableModel = new DefaultTableModel(columnNames, 0)
						{
							@Override
							public boolean isCellEditable(int row, int column)
							{
								return false; // disallow editing of the table
							}

							@Override
							@SuppressWarnings("unchecked")
							public Class<? extends Object> getColumnClass(int c)
							{
								return getValueAt(0, c).getClass();
							}
						};

						table.setModel(tableModel);
						table.addMouseListener(new MouseAdapter()
						{
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
								tableModel.addRow(new Object[]
								{ "Ext", formId, pr });
							}

							for (Integer formId : esmManager.getAllInteriorCELLFormIds())
							{
								PluginRecord pr = esmManager.getInteriorCELL(formId);
								tableModel.addRow(new Object[]
								{ "Int", formId, pr });
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
					}
					else
					{
						JOptionPane.showMessageDialog(ScrollsExplorer.this, selectedGameConfig.mainESMFile
								+ " is not in folder set for game " + selectedGameConfig.gameName);
						setFolders();
					}
					mainPanel.validate();
					mainPanel.invalidate();
					mainPanel.doLayout();
					mainPanel.repaint();

					ScrollsExplorer.dashboard.setEsmLoading(-1);
				}

			}
		};
		t.start();
	}

	private void display(final int cellformid)
	{
		Thread t = new Thread()
		{
			public void run()
			{
				//crappy no doubles system
				synchronized (simpleBethCellManager)
				{
					simpleWalkSetup.setEnabled(false);
					System.out.println("loading and displaying cell " + cellformid);

					simpleBethCellManager.setCurrentCellFormId(cellformid);

					System.out.println("Cell loaded and dispalyed " + cellformid);
					simpleWalkSetup.setEnabled(true);
					//	simpleWalkSetup.warp(new Vector3f(0, 10000, 0));
				}
			}
		};
		t.start();
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
		//-Xmx1200m -Xms900m -Dj3d.cacheAutoComputeBounds=true -Dsun.java2d.noddraw=true 
		//-Dj3d.sharedctx=true -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC

		//Can't see a noticable diff in perf with this one
		//-Dj3d.soleUser=true 				If set to true, allow the sole-user optimization; otherwise, sole-user is disabled 

		// still can't tell if this improves things
		// it'ss only help if interleave is on
		//-Dj3d.optimizedForSpace=false		If set to true, optimize by-ref geometry for space; otherwise, optimize for rendering speed
		
		// some other interesting settings
		//java -server -XX:CompileThreshold=2 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods 
		
		String versionString = BootStrap.ZIP_PREFIX + "-" + BootStrap.MAJOR_VERSION + "-" + BootStrap.MINOR_VERSION;
		System.out.println("VERSION: " + versionString);
		System.err.println("VERSION: " + versionString);

		//jogl recomends for non phones 
		System.setProperty("jogl.disable.opengles", "true");

		//DDS requires no installed java3D
		if (QueryProperties.checkForInstalledJ3d())
		{
			System.exit(0);
		}

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
