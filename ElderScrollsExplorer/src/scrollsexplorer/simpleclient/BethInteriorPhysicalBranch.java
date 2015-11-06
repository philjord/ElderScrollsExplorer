package scrollsexplorer.simpleclient;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.cell.J3dICELLPersistent;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmmanager.common.data.record.Record;
import esmmanager.common.data.record.Subrecord;

public class BethInteriorPhysicalBranch extends BranchGroup
{
	private J3dICELLPersistent j3dCELLPersistent;

	private J3dCELLGeneral j3dCELLTemporary;

	public BethInteriorPhysicalBranch(PhysicsSystem clientPhysicsSystem, int interiorCellFormId, J3dICellFactory j3dCellFactory)
	{
		this.setName("BethInteriorPhysicalBranch" + interiorCellFormId);
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		j3dCELLPersistent = j3dCellFactory.makeBGInteriorCELLPersistent(interiorCellFormId, true);
		j3dCELLPersistent.getGridSpaces().updateAll();//force add all
		addChild((J3dCELLGeneral) j3dCELLPersistent);
		clientPhysicsSystem.cellChanged(interiorCellFormId, (J3dCELLGeneral) j3dCELLPersistent);

		j3dCELLTemporary = j3dCellFactory.makeBGInteriorCELLTemporary(interiorCellFormId, true);
		addChild(j3dCELLTemporary);
		clientPhysicsSystem.loadJ3dCELL(j3dCELLTemporary);

		//TODO: why the hell was I calling this???
		//addChild(j3dCellFactory.makeBGInteriorCELLDistant(interiorCellFormId, true));
		//not added to physics

	}

	public void handleRecordCreate(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordCreate(record);
		}
	}

	public void handleRecordDelete(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordDelete(record);
		}
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordUpdate(record, updatedSubrecord);
		}

	}

	public J3dRECOInst getJ3dInstRECO(int recoId)
	{
		if (j3dCELLTemporary != null)
		{
			J3dRECOInst jri = j3dCELLTemporary.getJ3dRECOs().get(recoId);
			if (jri != null)
			{
				return jri;
			}
		}

		return j3dCELLPersistent.getGridSpaces().getJ3dInstRECO(recoId);
	}
}
