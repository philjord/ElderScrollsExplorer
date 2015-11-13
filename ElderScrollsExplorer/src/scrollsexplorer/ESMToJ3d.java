package scrollsexplorer;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.media.j3d.BranchGroup;

import utils.ESMUtils;
import utils.source.MediaSources;
import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginGroup;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.common.data.record.Record;
import esmmanager.loader.ESMManager;
import esmmanager.loader.IESMManager;

public class ESMToJ3d
{
	public static BranchGroup makeBGWRLDExtBlockCELL(ESMManager esmManager, int cellType, int wrldFormId, int x, int y, boolean makePhys,
			MediaSources mediaSources)
	{

		int formId = esmManager.getWRLDExtBlockCELLId(wrldFormId, x, y);
		if (formId != -1)
		{
			return makeBGCELL(esmManager, cellType, formId, makePhys, mediaSources);
		}
		else
		{
			System.out.println("wrldFormId " + wrldFormId + " x " + x + " y " + y + " is not a cell location");
			return null;
		}
	}

	public static BranchGroup makeBGCELL(IESMManager esmManager, int cellType, int formId, boolean makePhys, MediaSources mediaSources)
	{
		try
		{
			// let's see if it's interior
			PluginRecord record = esmManager.getInteriorCELL(formId);
			PluginGroup cellChildren = null;
			if (record != null)
			{
				cellChildren = esmManager.getInteriorCELLChildren(formId);
			}
			else
			{
				record = esmManager.getWRLDExtBlockCELL(formId);
				if (record != null)
				{
					cellChildren = esmManager.getWRLDExtBlockCELLChildren(formId);
				}
				else
				{
					System.out.println("formId for makeBGCELL " + formId + " can't be found");
				}
			}

			if (cellChildren != null)
			{
				List<Record> childRecords = ESMUtils.getChildren(cellChildren, cellType);
				float version = esmManager.getVersion();

				if (cellType == PluginGroup.CELL_TEMPORARY)
				{
					//Note teh table listing function in CellDisplay works but doesn't use this?
					if (version == 0.94f)
					{
						if (esmManager.getName().equals("Skyrim.esm"))
						{
							return new esmj3dfo4.j3d.cell.J3dCELLTemporary(esmManager, new Record(record), childRecords, makePhys,
									mediaSources);

						}
						else
						{
							return new esmj3dfo3.j3d.cell.J3dCELLTemporary(esmManager, new Record(record), childRecords, makePhys,
									mediaSources);
						}
					}
					else if (version == 1.32f)
					{
						return new esmj3dfo3.j3d.cell.J3dCELLTemporary(esmManager, new Record(record), childRecords, makePhys, mediaSources);
					}
					else if (version == 1.0f || version == 0.8f)
					{
						return new esmj3dtes4.j3d.cell.J3dCELLTemporary(esmManager, new Record(record), childRecords, makePhys,
								mediaSources);
					}
					else if (version == 1.2f)
					{
						return new esmj3dtes3.j3d.cell.J3dCELLTemporary(esmManager, new Record(record), childRecords, makePhys,
								mediaSources);
					}
					else
					{
						System.out.println("Bad esm version! " + version + " in " + esmManager.getName());
					}
				}
				else if (cellType == PluginGroup.CELL_PERSISTENT)
				{
					//Note the table listing function in CellDisplay works but doesn't use this?
					if (version == 0.94f)
					{
						if (esmManager.getName().equals("Skyrim.esm"))
						{
							return new esmj3dfo4.j3d.cell.J3dCELLPersistent(null, esmManager, new Record(record), childRecords, makePhys,
									mediaSources);
						}
						else
						{
							return new esmj3dfo3.j3d.cell.J3dCELLPersistent(null, esmManager, new Record(record), childRecords, makePhys,
									mediaSources);
						}
					}
					else if (version == 1.32f)
					{
						return new esmj3dfo3.j3d.cell.J3dCELLPersistent(null, esmManager, new Record(record), childRecords, makePhys,
								mediaSources);
					}
					else if (version == 1.0f || version == 0.8f)
					{
						return new esmj3dtes4.j3d.cell.J3dCELLPersistent(null, esmManager, new Record(record), childRecords, makePhys,
								mediaSources);
					}
					else if (version == 1.2f)
					{
						return new esmj3dtes3.j3d.cell.J3dCELLPersistent(null, esmManager, new Record(record), childRecords, makePhys,
								mediaSources);
					}
					else
					{
						System.out.println("Bad esm version! " + version + " in " + esmManager.getName());
					}
				}
			}

		}
		catch (PluginException e1)
		{
			e1.printStackTrace();
		}
		catch (DataFormatException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		return null;
	}

	public static Record getCELL(ESMManager esmManager, int formId)
	{
		try
		{
			// let's see if it's interior
			PluginRecord record = esmManager.getInteriorCELL(formId);
			if (record == null)
			{
				//ok let's try exterior
				record = esmManager.getWRLDExtBlockCELL(formId);
			}
			return new Record(record);
		}
		catch (PluginException e1)
		{
			e1.printStackTrace();
		}
		catch (DataFormatException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		return null;
	}

}
