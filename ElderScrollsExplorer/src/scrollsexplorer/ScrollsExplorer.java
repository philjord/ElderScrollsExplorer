package scrollsexplorer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import common.config.ConfigLoader;
import esmLoader.common.PluginException;
import esmLoader.common.data.plugin.PluginRecord;
import esmLoader.loader.ESMManager;
import esmj3d.j3d.BethRenderSettings;

public class ScrollsExplorer extends JFrame implements BethRenderSettings.UpdateListener
{
	public static Dashboard dashboard = new Dashboard();

	private SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetup simpleWalkSetup;

	private static JTable table;

	private static DefaultTableModel tableModel;

	private static String[] columnNames = new String[]
	{ "Int/Ext", "Cell Id", "Name" };

	private MediaSources mediaSources;

	public ESMManager esmManager;

	public BSAFileSet bsaFileSet;

	public JButton falloutButton = new JButton("Fall Out");

	public JButton falloutNVButton = new JButton("Fall Out NV");

	public JButton oblivionButton = new JButton("Oblivion");

	public JButton skyrimButton = new JButton("Skyrim");

	public JPanel mainPanel = new JPanel();

	public JPanel buttonPanel = new JPanel();

	public JCheckBoxMenuItem cbLoadAllMenuItem = new JCheckBoxMenuItem("Load all BSA Archives", true);

	public JCheckBoxMenuItem cbBsaMenuItem = new JCheckBoxMenuItem("Use BSA not Files", true);

	public JMenuItem setFolders = new JMenuItem("Set Folders");

	public JMenuItem setGraphics = new JMenuItem("Set Graphics");

	public Preferences prefs;

	private String scrollsFolder = "";

	private String mainESMFile = "";

	public ScrollsExplorer()
	{
		super("ScrollsExplorer");
		try
		{
			PropertyLoader.load();

			prefs = Preferences.userNodeForPackage(ScrollsExplorer.class);

			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(1, 1));
			this.setSize(500, 1000);

			mainPanel.setLayout(new GridLayout(-1, 1));

			JMenuBar menuBar = new JMenuBar();
			menuBar.setOpaque(true);
			JMenu menu = new JMenu("File");
			menu.setMnemonic(70);
			boolean loadAll = Boolean.parseBoolean(prefs.get("load.all", "true"));
			cbLoadAllMenuItem.setSelected(loadAll);
			menu.add(cbLoadAllMenuItem);
			menuBar.add(menu);

			boolean useBsa = Boolean.parseBoolean(prefs.get("use.bsa", "true"));
			cbBsaMenuItem.setSelected(useBsa);
			menu.add(cbBsaMenuItem);

			menu.add(setFolders);
			setFolders.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					setFolders();
				}
			});

			menu.add(setGraphics);
			setGraphics.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					simpleWalkSetup.resetGraphicsSetting();
				}
			});

			this.setJMenuBar(menuBar);
			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

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
					simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(2.28f);
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
					simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(1.8f);
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
					simpleWalkSetup.getAvatarCollisionInfo().setAvatarYHeight(1.8f);
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

			simpleWalkSetup = new SimpleWalkSetup("SimpleBethCellManager");

			buttonPanel.add(simpleWalkSetup.getLocField());
			simpleBethCellManager = new SimpleBethCellManager(simpleWalkSetup);

			ESESettingsPanel eseSettingsPanel = new ESESettingsPanel(simpleWalkSetup);
			BethRenderSettings.addUpdateListener(this);
			this.getContentPane().add(eseSettingsPanel, BorderLayout.NORTH);

			this.getContentPane().invalidate();
			this.getContentPane().validate();
			this.getContentPane().doLayout();
			this.invalidate();
			this.validate();
			this.doLayout();

			this.getContentPane().add(dashboard, BorderLayout.WEST);

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

			enableButtons();

		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	public void closingTime()
	{
		if (esmManager != null)
		{
			PropertyLoader.properties.setProperty("YawPitch" + esmManager.getName(), new YawPitch(simpleWalkSetup.getAvatarLocation()
					.getTransform()).toString());
			PropertyLoader.properties.setProperty("Trans" + esmManager.getName(),
					"" + PropertyCodec.vector3fIn(simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
			PropertyLoader.save();
		}
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
		//in case of nothing selected show dialog, funy infinite loop for recidivst non setters
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
	}

	@Override
	public void renderSettingsUpdated()
	{
		simpleBethCellManager.updateBranches();
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
					ScrollsExplorer.dashboard.setEsmLoading(1);
					prefs.put("use.bsa", Boolean.toString(cbBsaMenuItem.isSelected()));
					prefs.put("load.all", Boolean.toString(cbLoadAllMenuItem.isSelected()));

					mainPanel.removeAll();

					esmManager = ESMManager.getESMManager(mainESMFile);
					bsaFileSet = null;
					if (esmManager != null)
					{
						YawPitch yp = YawPitch.parse(PropertyLoader.properties.getProperty("YawPitch" + esmManager.getName(),
								new YawPitch().toString()));
						Vector3f trans = PropertyCodec.vector3fOut(PropertyLoader.properties.getProperty("Trans" + esmManager.getName(),
								new Vector3f().toString()));
						simpleWalkSetup.getAvatarLocation().set(yp.get(new Quat4f()), trans);
					}

					new EsmSoundKeyToName(esmManager);
					MeshSource meshSource;
					TextureSource textureSource;
					SoundSource soundSource;

					if (cbBsaMenuItem.isSelected())
					{
						if (bsaFileSet == null)
							bsaFileSet = new BSAFileSet(scrollsFolder, cbLoadAllMenuItem.isSelected(), false);

						meshSource = new BsaMeshSource(bsaFileSet);
						textureSource = new BsaTextureSource(bsaFileSet);
						soundSource = new BsaSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));
					}
					else
					{
						FileMediaRoots.setMediaRoots(new String[]
						{ scrollsFolder });
						meshSource = new FileMeshSource();
						textureSource = new FileTextureSource();
						soundSource = new FileSoundSource();

					}

					mediaSources = new MediaSources(meshSource, textureSource, soundSource);

					 

					simpleWalkSetup.configure(meshSource, simpleBethCellManager);
					simpleWalkSetup.setEnabled(false);
					//add skynow
					simpleWalkSetup.addToVisualBranch(SimpleBethCellManager.createBackground(textureSource));

					simpleBethCellManager.setSources(esmManager, mediaSources);

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

					table = new JTable(tableModel);
					table.addMouseListener(new MouseAdapter()
					{
						@Override
						public void mouseClicked(MouseEvent e)
						{
							display(((Integer) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1)));
						}

					});

					table.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));

					mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

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
					System.out.println("esm version == " + esmManager.getVersion());

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
		//TODO: moc external adds final path twice?s from gui picker
		//but picker with manual type is ok
		
		// also
		
		//Exception in thread "Thread-16" java.lang.IllegalArgumentException: Texture: mipmap image not set at level7
		//at javax.media.j3d.TextureRetained.setLive(TextureRetained.java:976)
		
		
		System.out.println(System.getProperty("os.name"));
				System.out.println(System.getProperty("os.arch"));
				
		HashMap<String, String>  env = new HashMap<String, String> ();
		
		env.put("PROCESSOR_ARCHITECTURE", System.getProperty("os.arch"));
		env.put("PROCESSOR_ARCHITEW6432", System.getProperty("os.name"));
		
		
		setEnv(env);
				
		
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

		System.out.println("arch" +arch);
		System.out.println("wow "+wow64Arch);
		
		String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";

		
		
		//Arguments for goodness
		//-Xmx1200m -Xms900m -Dsun.java2d.noddraw=true -Dj3d.sharedctx=true -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC

		//jogl recomends for non phones 
		System.setProperty("jogl.disable.opengles","true");

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

		ScrollsExplorer scrollsExplorer = new ScrollsExplorer();
		scrollsExplorer.setVisible(true);
	}
	
	protected static void setEnv(Map<String, String> newenv)
	{
	  try
	    {
	        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	        theEnvironmentField.setAccessible(true);
	        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	        env.putAll(newenv);
	        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	        theCaseInsensitiveEnvironmentField.setAccessible(true);
	        Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	        cienv.putAll(newenv);
	    }
	    catch (NoSuchFieldException e)
	    {
	      try {
	        Class[] classes = Collections.class.getDeclaredClasses();
	        Map<String, String> env = System.getenv();
	        for(Class cl : classes) {
	            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	                Field field = cl.getDeclaredField("m");
	                field.setAccessible(true);
	                Object obj = field.get(env);
	                Map<String, String> map = (Map<String, String>) obj;
	                map.clear();
	                map.putAll(newenv);
	            }
	        }
	      } catch (Exception e2) {
	        e2.printStackTrace();
	      }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    } 
	}

}