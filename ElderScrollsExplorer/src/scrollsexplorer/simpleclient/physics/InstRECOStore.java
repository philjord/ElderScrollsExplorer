package scrollsexplorer.simpleclient.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public interface InstRECOStore
{
	public void applyCharChange(J3dRECOInst instReco, Quat4f newRotation, Vector3f newTranslation);
}
