package exporter;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import awt.tools3d.resolution.QueryProperties;
import bsa.gui.BSAFileSetWithStatus;
import esmio.loader.ESMManager;
import esmio.loader.IESMManager;
import esmio.utils.source.EsmSoundKeyToName;
import nif.NifToJ3d;
import scrollsexplorer.PropertyLoader;
import tools.io.ConfigLoader;
import utils.source.MediaSources;

//armor/
//	bosscribe
//	bosunderarmor
//	chinesecommando
//	colonelautmn
//	combatarmor
//	headgear
//	leatherarmor
//	metalarmor
//	oasisvillager
//	powerarmor
// 	underwear	
// characters
// creatures/
//	dog
//	eyebot
//	libertyprime
//	minisentryturret
//	mrgutsy
//	mrhandy
//	protectron
//	robobrain
//	sentrybot
//	sentryturret
//traps
//weapons
//clutter/clfence

public class FOCharsExporter
{

	private static final String[] sourceFolders = new String[]
	{

	"armor\\bosscribe",//
			"armor\\bosunderarmor\\f",//
			"armor\\bosunderarmor\\m",//
			"armor\\chinesecommando",//
			"armor\\colonelautmn",//
			"armor\\combatarmor\\f",//
			"armor\\combatarmor\\m",//
			"armor\\headgear\\bosunderarmor",//
			"armor\\headgear\\chinesecommando",//
			"armor\\headgear\\combatarmor\\m",//
			"armor\\headgear\\hoodoasisdruid",//
			"armor\\headgear\\metalarmor",//
			"armor\\leatherarmor",//
			"armor\\metalarmor",//
			"armor\\oasisdruid",//
			"armor\\powerarmor",//
			"armor\\underwear",//
			"characters\\_1stperson",//
			"characters\\_1stperson\\locomotion",//
			"characters\\_male",//
			"characters\\_male\\idleanims",//
			"characters\\_male\\locomotion",//
			"characters\\_male\\locomotion\\female",//
			"characters\\_male\\locomotion\\hurt",//
			"characters\\_male\\locomotion\\male",//
			"characters\\hair",//
			"characters\\head",//
			"creatures\\deathclaw",//
			"creatures\\deathclaw\\idleanims",//
			"creatures\\deathclaw\\locomotion",//
			"creatures\\deathclaw\\locomotion\\hurt",//
			"creatures\\dog",//
			"creatures\\dog\\idleanims",//
			"creatures\\dog\\locomotion",//
			"creatures\\dog\\locomotion\\hurt",//
			"creatures\\eyebot",//
			"creatures\\eyebot\\idleanims",//
			"creatures\\libertyprime",//
			"creatures\\libertyprime\\idleanims",//
			"creatures\\minisentryturret",//
			"creatures\\minisentryturret\\idleanims",//
			"creatures\\mrgutsy",//
			"creatures\\mrgutsy\\idleanims",//
			"creatures\\mrgutsy\\specialanims",//
			"creatures\\protectron",//
			"creatures\\protectron\\idleanims",//
			"creatures\\protectron\\locomotion",//
			"creatures\\protectron\\locomotion\\hurt",//
			"creatures\\protectron\\specialanims",//
			"creatures\\sentrybot",//
			"creatures\\sentrybot\\idleanims",//
			"creatures\\sentrybot\\locomotion",//
			"creatures\\sentrybot\\locomotion\\hurt",//
			"creatures\\sentryturret",//
			"creatures\\sentryturret\\idleanims",//
			"traps", //
			"weapons\\1handgranadethrow",//
			"weapons\\2handrifle",//
			"clutter\\clfence",//
	};

	public Preferences prefs;

	public static String outputFolderTrees = "F:\\game media\\output\\fochars";

	private FOCharsExporter() throws IOException
	{
		PropertyLoader.load();

		String scrollsFolder = PropertyLoader.properties.getProperty("FallOut3Folder");
		String mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Fallout3.esm";

		IESMManager esmManager = ESMManager.getESMManager(mainESMFile);
		new EsmSoundKeyToName(esmManager);

		BSAFileSetWithStatus bsaFileSet = null;
		BsaRecordedMeshSource meshSource;
		BsaRecordedTextureSource textureSource;
		BsaRecordedSoundSource soundSource;

		bsaFileSet = new BSAFileSetWithStatus(new String[]
		{ scrollsFolder }, true, false);

		meshSource = new BsaRecordedMeshSource(bsaFileSet);
		textureSource = new BsaRecordedTextureSource(bsaFileSet);
		soundSource = new BsaRecordedSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

		MediaSources mediaSources = new MediaSources(meshSource, textureSource, soundSource);

		long startTime = System.currentTimeMillis();
		for (String sourcefolder : sourceFolders)
		{
			System.out.println("starting " + sourcefolder + "...");
			// for each cell picked
			for (String file : meshSource.getFilesInFolder("Meshes\\" + sourcefolder))
			{
				System.out.println("file: " + file);
				if (file.endsWith(".nif"))
					NifToJ3d.loadNif(file, meshSource, textureSource);
				else if (file.endsWith(".kf"))
					NifToJ3d.loadKf(file, meshSource);
				else
					System.out.println("skipping file: " + file);
			}
		}

		File outputFolder = new File(outputFolderTrees);
		try
		{
			ESMBSAExporter.copyToOutput(outputFolder, mediaSources, bsaFileSet);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Export complete in " + (System.currentTimeMillis() - startTime) + "ms");
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
			new FOCharsExporter();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
