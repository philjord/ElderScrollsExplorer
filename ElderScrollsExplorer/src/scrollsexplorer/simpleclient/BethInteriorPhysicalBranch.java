package scrollsexplorer.simpleclient;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.cell.J3dICELLPersistent;
import esmj3d.j3d.cell.J3dICellFactory;

public class BethInteriorPhysicalBranch extends BranchGroup
{

	private J3dICELLPersistent j3dCELLPersistent;

	private J3dCELLGeneral j3dCELLTemporary;

	public BethInteriorPhysicalBranch(PhysicsSystem clientPhysicsSystem, int interiorCellFormId, J3dICellFactory j3dCellFactory)
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		j3dCELLPersistent = j3dCellFactory.makeBGInteriorCELLPersistent(interiorCellFormId, true);
		addChild((J3dCELLGeneral) j3dCELLPersistent);
		clientPhysicsSystem.cellChanged(interiorCellFormId, (J3dCELLGeneral) j3dCELLPersistent);

		j3dCELLTemporary = j3dCellFactory.makeBGInteriorCELLTemporary(interiorCellFormId, true);
		addChild(j3dCELLTemporary);
		clientPhysicsSystem.loadJ3dCELL(j3dCELLTemporary);

		addChild(j3dCellFactory.makeBGInteriorCELLDistant(interiorCellFormId, true));
		//not added to physics

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
