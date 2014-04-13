package scrollsexplorer.simpleclient.physics;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import tools3d.utils.Utils3D;
import nifbullet.dyn.NBSimpleDynamicModel;
import nifbullet.dyn.NifBulletTransformListener;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;


public class InstRecoNifBulletBinding implements NifBulletBinding, NifBulletTransformListener
{
	private InstRECOStore instRecoStore;

	private J3dRECOInst instReco;

	public InstRecoNifBulletBinding(J3dRECOInst instReco, InstRECOStore instRecoStore, NBSimpleDynamicModel nifBullet)
	{
		this.instRecoStore = instRecoStore;
		this.instReco = instReco;
		nifBullet.setTransformChangeListener(this);
	}

	// deburner
	private Vector3f newTranslation = new Vector3f();

	private Quat4f newRotation = new Quat4f();

	@Override
	public void transformChanged(Transform3D newTrans, Vector3f linearVelocity, Vector3f rotationalVelocity)
	{		
		newTrans.get(newTranslation);
		Utils3D.safeGetQuat(newTrans, newRotation);

		if (Float.isNaN(newTranslation.x))
		{
			System.out.println("NAN detected in ClientInstRecoNifBulletBinding.setTransform position!");
			return;
		}

		instRecoStore.applyUpdate(instReco, newRotation, newTranslation);
	}

	@Override
	public void applyToModel()
	{
		//ignored due to instant updates to client model
		//System.out.println("applying bindings to model??");
	}

}
