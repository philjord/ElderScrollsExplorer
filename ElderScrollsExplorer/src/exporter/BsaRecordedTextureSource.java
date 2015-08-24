package exporter;

import java.util.LinkedHashSet;

import javax.media.j3d.Texture;

import bsa.BSAFileSet;
import bsa.source.BsaTextureSource;

public class BsaRecordedTextureSource extends BsaTextureSource
{
	public LinkedHashSet<String> requestedFiles = new LinkedHashSet<String>();

	public BsaRecordedTextureSource(BSAFileSet bsaFileSet)
	{
		super(bsaFileSet);
	}

	@Override
	public Texture getTexture(String texName)
	{
		requestedFiles.add(texName);
		return super.getTexture(texName);
	}
}
