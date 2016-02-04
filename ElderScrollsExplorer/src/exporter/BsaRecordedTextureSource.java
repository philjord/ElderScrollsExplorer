package exporter;

import java.io.InputStream;
import java.util.HashSet;

import javax.media.j3d.Texture;

import bsa.BSAFileSet;
import bsa.source.BsaTextureSource;

public class BsaRecordedTextureSource extends BsaTextureSource
{

	public HashSet<String> requestedFiles = new HashSet<String>();

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
	

	@Override
	public InputStream getInputStream(String texName)
	{
		requestedFiles.add(texName);
		return super.getInputStream(texName);
	}
}
