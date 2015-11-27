package test;

import java.io.IOException;
import java.util.zip.DataFormatException;

import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;
import utils.source.file.FileMeshSource;
import utils.source.file.FileSoundSource;
import utils.source.file.FileTextureSource;
import analyzer.EsmFormatAnalyzer;
import bsa.BSAFileSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;
import esmj3dtes5.data.RecordToRECO;
import esmj3dtes5.j3d.cell.J3dCellFactory;
import esmmanager.EsmFileLocations;
import esmmanager.common.PluginException;
import esmmanager.common.data.record.Record;
import esmmanager.loader.ESMManager;

public class Tes5esmverifier extends EsmFormatAnalyzer
{
	private static ESMManager esmManager;

	private static J3dCellFactory j3dCellFactory;

	public static void main(String args[])
	{
		EsmFormatAnalyzer.recordLoader = new RecordLoader()
		{
			public void loadRecord(Record rec)
			{
				RecordToRECO.makeRECO(rec);

				//TODO: move this toe tes5 an fo3
				if (EsmFormatAnalyzer.LOAD_J3DCELLS && rec.getRecordType().equals("CELL"))
				{
					System.out.println("Trying to j3d cell " + rec.getFormID());
					long start = System.currentTimeMillis();
					if (j3dCellFactory.isWRLD(rec.getFormID()))
					{
						j3dCellFactory.makeBGWRLDPersistent(rec.getFormID(), false);
						j3dCellFactory.makeBGWRLDTemporary(rec.getFormID(), -1, false);
						j3dCellFactory.makeBGWRLDDistant(rec.getFormID(), -1, false);
					}
					else
					{
						j3dCellFactory.makeBGInteriorCELLPersistent(rec.getFormID(), false);
						j3dCellFactory.makeBGInteriorCELLTemporary(rec.getFormID(), false);
						j3dCellFactory.makeBGInteriorCELLDistant(rec.getFormID(), false);
					}
					System.out.println("Loaded in " + (System.currentTimeMillis() - start));
				}
			}
		};

		String esmFile = EsmFileLocations.getSkyrimEsmFile();

		System.out.println("loading file " + esmFile);
		long start = System.currentTimeMillis();

		try
		{
			Thread.currentThread().setPriority(4);
			esmManager = (ESMManager) ESMManager.getESMManager(esmFile);
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			System.out.println("Done in " + (System.currentTimeMillis() - start) + " analyzing...");
			SoundSource ss;
			TextureSource ts;
			MeshSource ms;
			if (EsmFormatAnalyzer.LOAD_BSA_FILES)
			{

				BSAFileSet bsaFileSet = new BSAFileSet(esmFile, true, false);

				ss = new BsaSoundSource(bsaFileSet.getSoundArchives(), new EsmSoundKeyToName(esmManager));
				ts = new BsaTextureSource(bsaFileSet.getTextureArchives());
				ms = new BsaMeshSource(bsaFileSet.getMeshArchives());
			}
			else
			{
				ss = new FileSoundSource();
				ts = new FileTextureSource();
				ms = new FileMeshSource();
			}

			MediaSources mediaSources = new MediaSources(ms, ts, ss);
			j3dCellFactory = new J3dCellFactory();
			j3dCellFactory.setSources(esmManager, mediaSources);
			analyze(esmManager);
			System.out.println("done analyzing");

		}
		catch (PluginException e)
		{
			e.printStackTrace();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
