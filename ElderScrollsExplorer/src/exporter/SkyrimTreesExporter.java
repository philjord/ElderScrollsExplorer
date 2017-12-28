package exporter;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import org.jogamp.java3d.compressedtexture.CompressedTextureLoader;

import awt.tools3d.resolution.QueryProperties;
import bsa.gui.BSAFileSetWithStatus;
import esmio.loader.ESMManager;
import esmio.loader.IESMManager;
import esmio.utils.source.EsmSoundKeyToName;
import nif.NifToJ3d;
import nif.appearance.NiGeometryAppearanceFactoryShader;
import scrollsexplorer.PropertyLoader;
import tools.io.ConfigLoader;
import utils.source.MediaSources;

public class SkyrimTreesExporter
{

	public Preferences prefs;

	public static String outputFolderTrees = "F:\\game_media\\output\\skyrimTrees";

	private SkyrimTreesExporter() throws IOException
	{
		NiGeometryAppearanceFactoryShader.setAsDefault();
		CompressedTextureLoader.setAnisotropicFilterDegree(8);
		
		PropertyLoader.load();

		String scrollsFolder = PropertyLoader.properties.getProperty("SkyrimFolder");
		String mainESMFile = scrollsFolder + PropertyLoader.fileSep + "Skyrim.esm";

		IESMManager esmManager = ESMManager.getESMManager(mainESMFile);
		new EsmSoundKeyToName(esmManager);

		BSAFileSetWithStatus bsaFileSet = null;
		BsaRecordedMeshSource meshSource;
		BsaRecordedTextureSource textureSource;
		BsaRecordedSoundSource soundSource;

		String plusSkyrim = PropertyLoader.properties.getProperty("SkyrimFolder");

		bsaFileSet = new BSAFileSetWithStatus(new String[]
		{ plusSkyrim }, true, false);

		meshSource = new BsaRecordedMeshSource(bsaFileSet);
		textureSource = new BsaRecordedTextureSource(bsaFileSet);
		soundSource = new BsaRecordedSoundSource(bsaFileSet, new EsmSoundKeyToName(esmManager));

		MediaSources mediaSources = new MediaSources(meshSource, textureSource, soundSource);

		long startTime = System.currentTimeMillis();
		System.out.println("starting trees/plants nif load and record...");
		// for each cell picked
		for (String tree : meshSource.getFilesInFolder("Meshes\\landscape\\trees"))
		{
			System.out.println("Tree: " + tree);
			NifToJ3d.loadNif(tree, meshSource, textureSource);
		}
		
		for (String tree : meshSource.getFilesInFolder("Meshes\\landscape\\plants"))
		{
			System.out.println("Plant: " + tree);
			NifToJ3d.loadNif(tree, meshSource, textureSource);
		}
		System.out.println("finished trees/plants load, starting copy");
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
