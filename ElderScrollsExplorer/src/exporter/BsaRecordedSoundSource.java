package exporter;

import java.util.HashSet;

import org.jogamp.java3d.MediaContainer;

import bsa.gui.BSAFileSetWithStatus;
import bsa.source.BsaSoundSource;
import esmio.utils.source.EsmSoundKeyToName;

public class BsaRecordedSoundSource extends BsaSoundSource
{

	public HashSet<String> requestedFiles = new HashSet<String>();

	public BsaRecordedSoundSource(BSAFileSetWithStatus bsaFileSet, EsmSoundKeyToName esmSoundKeyToName)
	{
		super(bsaFileSet, esmSoundKeyToName);
	}
	@Override
	public MediaContainer getMediaContainer(String mediaName)
	{
		requestedFiles.add(mediaName);
		return super.getMediaContainer(mediaName);
	}
	 
}
