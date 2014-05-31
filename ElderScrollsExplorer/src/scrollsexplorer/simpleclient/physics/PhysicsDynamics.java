package scrollsexplorer.simpleclient.physics;

import java.util.ArrayList;
import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import nif.j3d.animation.J3dNiControllerManager;
import nif.j3d.animation.J3dNiControllerSequence;
import nifbullet.BulletNifModel;
import nifbullet.BulletNifModelClassifier;
import nifbullet.cha.NBControlledChar;
import nifbullet.dyn.NBSimpleDynamicModel;
import nifbullet.simple.NBSimpleModel;
import nifbullet.util.debug.opengl.DebugOutput;
import nifbullet.util.debug.opengl.LWJGL;
import tools3d.navigation.AvatarLocation;
import tools3d.utils.Utils3D;
import utils.source.MeshSource;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;

import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class PhysicsDynamics extends DynamicsEngine
{
	//public static HeadlessUniverse headlessUniverse = new HeadlessUniverse();

	protected InstRECOStore instRecoToNif;

	private HashMap<Integer, NifBulletBinding> instRecoBulletBindings = new HashMap<Integer, NifBulletBinding>();

	private HashMap<BulletNifModel, Integer> nifBulletToRecoId = new HashMap<BulletNifModel, Integer>();

	private HashMap<Integer, BulletNifModel> recoIdToNifBullet = new HashMap<Integer, BulletNifModel>();

	private BranchGroup dynamicsRootBranchGroup;

	private boolean displayDebug = false;

	private static boolean debugOutputInited = false;

	private AvatarLocation avatarLocation;

	private NBControlledChar myNifBulletChar;

	private ClientNifBulletCharBinding clientNifBulletCharBinding;

	private MeshSource meshSource;

	public PhysicsDynamics(InstRECOStore instRecoToNif, Vector3f gravity, BranchGroup rootGroup, AvatarLocation avatarLocation,
			MeshSource meshSource)
	{
		super(gravity);
		this.meshSource = meshSource;

		this.instRecoToNif = instRecoToNif;
		dynamicsRootBranchGroup = new BranchGroup();
		dynamicsRootBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		dynamicsRootBranchGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		dynamicsRootBranchGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		rootGroup.addChild(dynamicsRootBranchGroup);

		this.avatarLocation = avatarLocation;

		Transform3D rootTrans = new Transform3D(avatarLocation.getTransform());
		myNifBulletChar = new NBControlledChar(rootTrans);
		synchronized (dynamicsWorld)
		{
			clientNifBulletCharBinding = new ClientNifBulletCharBinding(avatarLocation, myNifBulletChar);
			instRecoBulletBindings.put(-999, clientNifBulletCharBinding);
			myNifBulletChar.addToDynamicsWorld(dynamicsWorld);
		}
	}

	public void setDisplayDebug(boolean displayDebug)
	{
		this.displayDebug = displayDebug;
	}

	boolean isDisplayDebug()
	{
		return this.displayDebug;
	}

	@Override
	protected void dynamicsPreStep()
	{
	}

	@Override
	protected void dynamicsPostStep()
	{

		if (displayDebug)
		{
			if (!debugOutputInited)
			{
				DebugOutput.initDebug(dynamicsWorld, avatarLocation);
				debugOutputInited = true;
			}
			synchronized (dynamicsWorld)
			{
				LWJGL.step();
			}
		}
		else
		{
			if (debugOutputInited)
			{
				//TODO: I should hide teh debug output, but might have to dispose and re create in fact
			}
		}
	}

	public NBControlledChar getMyNifBulletChar()
	{
		return myNifBulletChar;
	}

	// we must clean up before being collected
	@Override
	public void finalize()
	{
		destroy();
	}

	public void destroy()
	{
		clear();
		dynamicsRootBranchGroup.detach();
	}

	public void clear()
	{
		instRecoBulletBindings.clear();
	}

	public void addRECO(J3dRECOInst j3dRECOInst)
	{
		if (recoIdToNifBullet.containsKey(j3dRECOInst.getRecordId()))
		{
			System.out.println("PhysicsDynamics, already loaded " + j3dRECOInst);
			//new Throwable("Thread:" + Thread.currentThread()).printStackTrace();
		}

		if (j3dRECOInst instanceof J3dLAND)
		{
			createLand((J3dLAND) j3dRECOInst);
		}
		else
		{
			J3dRECOType j3dRECOType = j3dRECOInst.getJ3dRECOType();

			if (j3dRECOType != null && j3dRECOType.physNifFile != null)
			{
				createStaticOrDynamic(j3dRECOInst, j3dRECOType.physNifFile);
			}
		}
		//System.out.println("add called total= " + nifBulletToRecoId.size());
	}

	private void createLand(J3dLAND j3dLAND)
	{
		Transform3D rootTrans = j3dLAND.getLocation(new Transform3D());
		NBSimpleModel nb = new NBSimpleModel(j3dLAND.getGeometryInfo(), rootTrans);
		if (nb != null)
		{
			synchronized (dynamicsWorld)
			{
				// Note we don't listen for physics updates
				recoIdToNifBullet.put(j3dLAND.getRecordId(), nb);
				nifBulletToRecoId.put(nb, j3dLAND.getRecordId());
				nb.addToDynamicsWorld(dynamicsWorld);
			}
		}
	}

	private void createStaticOrDynamic(J3dRECOInst j3dRECOInst, String physNifFile)
	{
		//root should have scale in it		

		Transform3D rootTrans = j3dRECOInst.getLocation(new Transform3D());

		if (physNifFile != null && physNifFile.length() > 0)
		{
			BulletNifModel nb = null;

			if (BulletNifModelClassifier.isStaticModel(physNifFile, meshSource))
			{
				// the nif file will have mass of 0 making this static
				nb = new NBSimpleModel(physNifFile, meshSource, rootTrans);
			}
			else if (BulletNifModelClassifier.isKinematicModel(physNifFile, meshSource))
			{
				// the nif file will have mass of 0 making this kinematic
				nb = new NBSimpleModel(physNifFile, meshSource, rootTrans);
				dynamicsRootBranchGroup.addChild((NBSimpleModel) nb);
			}
			else if (BulletNifModelClassifier.isSimpleDynamicModel(physNifFile, meshSource, 0))
			{
				createDynamic(j3dRECOInst, physNifFile);
			}
			else
			{
				// probably just smoke effect etc, complex dynamic rag doll
			}

			if (nb != null)
			{

				synchronized (dynamicsWorld)
				{
					// Note we don't listen for physics updates
					recoIdToNifBullet.put(j3dRECOInst.getRecordId(), nb);
					nifBulletToRecoId.put(nb, j3dRECOInst.getRecordId());
					nb.addToDynamicsWorld(dynamicsWorld);
				}
			}

		}
	}

	private void createDynamic(J3dRECOInst j3dRECOInst, String model)
	{
		Transform3D rootTrans = j3dRECOInst.getLocation(new Transform3D());

		if (model != null && model.length() > 0)
		{

			//VELO velo = instReco.velocity;

			NBSimpleDynamicModel nb = new NBSimpleDynamicModel(model, meshSource, 0);

			if (nb != null)
			{
				//TODO: nif file should have mass, but for custom written might need to check and set set mass 
				//nb.getRootNifBulletbhkCollisionObject().getRigidBody().setMassProps(mass, inertia);

				Vector3f linearVelocity = new Vector3f();
				Vector3f rotationalVelocity = new Vector3f();
				//velo.getVelocities(linearVelocity, rotationalVelocity);
				nb.forceUpdate(rootTrans, linearVelocity, rotationalVelocity);

				NifBulletBinding irnbb = new InstRecoNifBulletBinding(j3dRECOInst, instRecoToNif, nb);
				
				synchronized (dynamicsWorld)
				{
					if (irnbb != null)
					{
						instRecoBulletBindings.put(j3dRECOInst.getRecordId(), irnbb);
					}

					recoIdToNifBullet.put(j3dRECOInst.getRecordId(), nb);
					nifBulletToRecoId.put(nb, j3dRECOInst.getRecordId());
					dynamicsRootBranchGroup.addChild(nb);
					nb.addToDynamicsWorld(dynamicsWorld);
				}
			}
			else
			{
				System.out.println("IOException for model in reco " + j3dRECOInst + " file = " + model);
			}

		}
		else
		{
			System.out.println("no model for createDynamic " + j3dRECOInst.getRecordId());
		}
	}

	public void updateRECOROTR(J3dRECOInst j3dRECOInst, Transform3D newTrans)
	{

		BulletNifModel nifBullet = recoIdToNifBullet.get(j3dRECOInst.getRecordId());
		if (nifBullet instanceof NBSimpleDynamicModel)
		{
			Quat4f q = new Quat4f();
			Vector3f v = new Vector3f();
			Utils3D.safeGetQuat(newTrans, q);
			newTrans.get(v);
			((NBSimpleDynamicModel) nifBullet).setTransform(q, v);
		}
		else if (nifBullet instanceof NBSimpleModel)
		{
			//remove and readd
			removeRECO(j3dRECOInst);
			addRECO(j3dRECOInst);
		}

	}

	public void updateRECOToggleOpen(J3dRECOInst j3dRECOInst, boolean isOpen)
	{
		BulletNifModel nifBullet = recoIdToNifBullet.get(j3dRECOInst.getRecordId());
		if (nifBullet instanceof NBSimpleModel)
		{
			NBSimpleModel nbKinematicModel = (NBSimpleModel) nifBullet;
			String seq = isOpen ? "Open" : "Close";// inst has already been updated (this is post)

			J3dNiControllerManager ncm = nbKinematicModel.getJ3dNiControllerManager();
			if (ncm != null)
			{
				J3dNiControllerSequence s = ncm.getSequence(seq);
				if (s != null)
				{
					s.fireSequenceOnce();
				}
			}
		}

	}

	protected void removeRECO(J3dRECOInst j3dRECOInst)
	{
		int recordId = j3dRECOInst.getRecordId();
		BulletNifModel nifBullet = recoIdToNifBullet.get(recordId);
		if (nifBullet != null)
		{
			// remove from physics simulation
			synchronized (dynamicsWorld)
			{
				nifBullet.removeFromDynamicsWorld();
				nifBullet.destroy();
				instRecoBulletBindings.remove(recordId);
				nifBulletToRecoId.remove(nifBullet);
				recoIdToNifBullet.remove(recordId);
			}

		}
		//System.out.println("remove called total= " + nifBulletToRecoId.size());

	}

	public void applyPhysicsToModel()
	{
		synchronized (dynamicsWorld)
		{
			for (NifBulletBinding instRecoNifBulletBinding : instRecoBulletBindings.values())
			{
				instRecoNifBulletBinding.applyToModel();
			}
		}

	}

	/**
	 * Interface for thing to get physics object out to inspect details
	 * Don't hang onto the pointer (damn you!)
	 * @param recordId
	 * @return
	 */
	public BulletNifModel getNifBullet(int recordId)
	{
		return recoIdToNifBullet.get(recordId);
	}

	public int getRecordId(BulletNifModel nifBullet)
	{
		synchronized (dynamicsWorld)
		{
			Integer id = nifBulletToRecoId.get(nifBullet);
			if (id == null)
			{
				return -1;
			}
			else
			{
				return id.intValue();
			}
		}
	}

	public ClosestRayResultCallback findRayIntersect(Vector3f rayFrom, Vector3f rayTo)
	{
		synchronized (dynamicsWorld)
		{
			CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(rayFrom, rayTo);
			dynamicsWorld.rayTest(rayFrom, rayTo, rayCallback);
			return rayCallback;
		}
	}

	public PhysicsStatus getPhysicsStatus()
	{
		PhysicsStatus ret = new PhysicsStatus();
		synchronized (dynamicsWorld)
		{
			for (BulletNifModel bnm : recoIdToNifBullet.values())
			{
				if (bnm instanceof NBSimpleDynamicModel)
				{
					ret.dynCount++;
				}
				else if (bnm instanceof NBSimpleModel)
				{
					ret.kinCount++;
					ret.staCount++;
				}
			}
		}
		return ret;
	}

	public static class PhysicsStatus
	{
		public int dynCount = 0;

		public int kinCount = 0;

		public int staCount = 0;

		public long averageStepTimeMS = 0;
	}

}
