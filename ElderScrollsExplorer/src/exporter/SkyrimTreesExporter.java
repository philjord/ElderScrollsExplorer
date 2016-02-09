package exporter;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import awt.tools3d.resolution.QueryProperties;
import nif.NifToJ3d;
import scrollsexplorer.PropertyLoader;
import set.BSAFileSet;
import tools.io.ConfigLoader;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;

public class SkyrimTreesExporter
{

	public Preferences prefs;

	public static String outputFolderTrees = "F:\\game media\\output\\skyrimTrees";

	private SkyrimTreesExporter() throws IOException
	{
		PropertyLoader.load();

		String scrollsFolder = PropertyLoader.properties.getProperty("SkyrimFolder");
		String mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Skyrim.esm";

		IESMManager esmManager = ESMManager.getESMManager(mainESMFile);
		new EsmSoundKeyToName(esmManager);

		BSAFileSet bsaFileSet = null;
		BsaRecordedMeshSource meshSource;
		BsaRecordedTextureSource textureSource;
		BsaRecordedSoundSource soundSource;

		String plusSkyrim = PropertyLoader.properties.getProperty("SkyrimFolder");

		bsaFileSet = new BSAFileSet(new String[]
		{ plusSkyrim }, true, false);

		meshSource = new BsaRecordedMeshSource(bsaFileSet);
		textureSource = new BsaRecordedTextureSource(bsaFileSet);
		soundSource = new BsaRecordedSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

		MediaSources mediaSources = new MediaSources(meshSource, textureSource, soundSource);

		long startTime = System.currentTimeMillis();
		System.out.println("starting trees...");
		// for each cell picked
		for (String tree : meshSource.getFilesInFolder("Meshes\\landscape\\trees"))
		{
			System.out.println("Tree: " + tree);
			NifToJ3d.loadNif(tree, meshSource, textureSource);
		}
		
		for (String tree : meshSource.getFilesInFolder("Meshes\\landscape\\plants"))
		{
			System.out.println("Tree: " + tree);
			NifToJ3d.loadNif(tree, meshSource, textureSource);
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
			new SkyrimTreesExporter();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
