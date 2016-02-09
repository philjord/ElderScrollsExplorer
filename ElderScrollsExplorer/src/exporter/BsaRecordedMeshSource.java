package exporter;

import java.util.HashSet;

import nif.NifFile;
import bsa.gui.BSAFileSetWithStatus;
import bsa.source.BsaMeshSource;

public class BsaRecordedMeshSource extends BsaMeshSource
{

	public HashSet<String> requestedFiles = new HashSet<String>();

	public BsaRecordedMeshSource(BSAFileSetWithStatus bsaFileSet)
	{
		super(bsaFileSet);
	}

	@Override
	public NifFile getNifFile(String nifName)
	{
		requestedFiles.add(nifName);
		return super.getNifFile(nifName);
	}
}
