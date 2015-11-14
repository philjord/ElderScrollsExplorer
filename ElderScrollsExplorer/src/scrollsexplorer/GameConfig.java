package scrollsexplorer;

import java.util.ArrayList;

import esmj3d.j3d.cell.J3dICellFactory;

public class GameConfig
{
	public static ArrayList<GameConfig> allGameConfigs = new ArrayList<GameConfig>();

	public String gameName;

	public J3dICellFactory j3dCellFactory;

	public String skyTexture;

	public String loadScreen;

	public float avatarYHeight;

	public String folderKey;

	public String scrollsFolder = null;

	public String mainESMFile;

	public String ftpFolderName;

	public GameConfig(String gameName, J3dICellFactory j3dCellFactory, String skyTexture, String loadScreen, float avatarYHeight,
			String folderKey, String mainESMFile, String ftpFolderName)
	{
		this.gameName = gameName;
		this.j3dCellFactory = j3dCellFactory;
		this.skyTexture = skyTexture;
		this.loadScreen = loadScreen;
		this.avatarYHeight = avatarYHeight;
		this.folderKey = folderKey;
		this.ftpFolderName = ftpFolderName;
		this.mainESMFile = mainESMFile;
		update();
	}

	public void update()
	{
		this.scrollsFolder = PropertyLoader.properties.getProperty(folderKey);
	}

	public String getESMPath()
	{
		return scrollsFolder + PropertyLoader.fileSep + mainESMFile;
	}

	static
	{
		allGameConfigs.add(new GameConfig("TESIII: Morrowind", //
				new esmj3dtes3.j3d.cell.J3dCellFactory(),//
				"textures\\tx_sky_clear.dds",//
				"meshes\\i\\in_v_arena_01.nif",//
				2.28f, //
				"MorrowindFolder",//
				"Morrowind.esm",//
				"morrowind"));

		allGameConfigs.add(new GameConfig("TESIV: Oblivion", //
				new esmj3dtes4.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\cloudsclear.dds",//
				"meshes\\architecture\\cloudrulertemple\\testcloudrulerint.nif",//
				2.28f, //
				"OblivionFolder",//
				"Oblivion.esm",//
				"oblivion"));

		allGameConfigs.add(new GameConfig("FO3: Fallout 3", //
				new esmj3dfo3.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\urbancloudovercastlower01.dds",//
				"meshes\\interface\\loading\\loadinganim01.nif",//
				2.28f, //
				"FallOut3Folder",//
				"Fallout3.esm",//
				"fallout3"));

		allGameConfigs.add(new GameConfig("FONV: Fallout New Vegas", //
				new esmj3dfo3.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\urbancloudovercastlower01.dds",//
				"meshes\\interface\\loading\\loadinganim01.nif",//
				2.28f, //
				"FalloutNVFolder",//
				"FalloutNV.esm",//
				"falloutnv"));

		allGameConfigs.add(new GameConfig("TESV: Skyrim", //
				new esmj3dfo4.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\skyrimcloudsupper04.dds",//
				"meshes\\loadscreenart\\loadscreenadventure01.nif",//
				2.28f, //
				"SkyrimFolder",//
				"Skyrim.esm",//
				"skyrim"));

		allGameConfigs.add(new GameConfig("FO4: Fallout 4", //
				new esmj3dfo4.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\skyrimcloudsupper04.dds",//hahhah!
				"meshes\\LoadScreenArt\\Armor01PowerArmor1.nif",//
				2.28f, //
				"FallOut4Folder",//
				"Fallout4.esm",//
				"fallout4"));

		allGameConfigs.add(new GameConfig("Hunter Sneaker", //
				new esmj3dtes4.j3d.cell.J3dCellFactory(),//
				"textures\\sky\\cloudsclear.dds",//
				"meshes\\architecture\\cloudrulertemple\\testcloudrulerint.nif",//
				2.28f, //
				"HunterSneakerFolder",//
				"OblivionHS.esm",//
				"huntersneaker"));
	}
	//morrowind
	//meshes\i\in_v_arena.nif
	//meshes\i\in_de_shipwreckul_lg.nif
	//meshes\i\in_dae_hall_l_stair_curve_01.nif

	// obliv
	//meshes\dungeons\chargen\prisonhall04.nif
	//meshes\dungeons\cloudrulertemple\testcloudrulerint.nif
	//meshes\dungeons\cathedral\cathedralstenintback01.nif

	//FO3 + FONV
	//meshes\interface\loading\loadinganim01.nif

	//skyrim
	// all from meshes\loadscreenart  with black background

	//FO4
	// all from meshes\loadscreenart  with black background

	/**	
	 
	public void setSources(IESMManager esmManager, MediaSources mediaSources)
	{

		float version = esmManager.getVersion();

		if (version == 0.94f)
		{
			if (esmManager.getName().equals("Skyrim.esm"))
			{
				j3dCellFactory = new esmj3dtes5.j3d.cell.J3dCellFactory(esmManager, esmManager, mediaSources);
			}
			else
			{

				j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager, esmManager, mediaSources);
			}
		}
		else if (version == 1.32f)
		{
			j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager, esmManager, mediaSources);
		}
		else if (version == 1.0f || version == 0.8f)
		{
			j3dCellFactory = new esmj3dtes4.j3d.cell.J3dCellFactory(esmManager, esmManager, mediaSources);
		}
		else if (version == 1.2f)
		{
			j3dCellFactory = new esmj3dtes3.j3d.cell.J3dCellFactory(esmManager, esmManager, mediaSources);
		}
		else
		{
			System.out.println("Bad esm version! " + version + " in " + esmManager.getName());
		}

		//System.out.println("j3dCellFactory = " + j3dCellFactory);
	}*/
}
