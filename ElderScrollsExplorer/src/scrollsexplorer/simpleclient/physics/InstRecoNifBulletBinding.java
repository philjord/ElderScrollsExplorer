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

	private Transform3D prevTrans = new Transform3D();

	private Transform3D nextTrans = new Transform3D();

	@Override
	public void transformChanged(Transform3D newTrans, Vector3f linearVelocity, Vector3f rotationalVelocity)
	{
		nextTrans.set(newTrans);
	}

	@Override
	public void applyToModel()
	{
		if (!nextTrans.epsilonEquals(prevTrans, 0.0001))
		{
			nextTrans.get(newTranslation);
			Utils3D.safeGetQuat(nextTrans, newRotation);	
			//TODO: I should not have to call this here, transformChanged above should only receive good transfroms
			newRotation.normalize();
			
			if (Float.isNaN(newTranslation.x))
			{
				System.out.println("NAN detected in ClientInstRecoNifBulletBinding.setTransform position!");
				return;
			}
			instRecoStore.applyUpdate(instReco, newRotation, newTranslation);

			prevTrans.set(nextTrans);
		}
	}

}
