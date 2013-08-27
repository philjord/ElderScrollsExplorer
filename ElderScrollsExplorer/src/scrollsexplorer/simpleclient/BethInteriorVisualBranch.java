package scrollsexplorer.simpleclient;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.cell.J3dICELLPersistent;
import esmj3d.j3d.cell.J3dICellFactory;

public class BethInteriorVisualBranch extends BranchGroup
{

	private J3dICELLPersistent j3dCELLPersistent;

	public BethInteriorVisualBranch(int interiorCellFormId, String cellFormName, J3dICellFactory j3dCellFactory)
	{

		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		j3dCELLPersistent = j3dCellFactory.makeBGInteriorCELLPersistent(interiorCellFormId, false);
		addChild((J3dCELLGeneral) j3dCELLPersistent);
		addChild(j3dCellFactory.makeBGInteriorCELLTemporary(interiorCellFormId, false));
		addChild(j3dCellFactory.makeBGInteriorCELLDistant(interiorCellFormId, false));

	}

	public void handleRecordCreate(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordCreate(record);
		}
	}

	public void handleRecordDelete(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordDelete(record);
		}
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordUpdate(record, updatedSubrecord);
		}

	}

}
