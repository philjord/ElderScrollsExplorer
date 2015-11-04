package scrollsexplorer.simpleclient.physics;

import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
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
				//did it quit?
				if (!LWJGL.isDoRun())
					displayDebug = false;
			}
		}
		else
		{
			if (debugOutputInited)
			{
				DebugOutput.disposeDebug();
				debugOutputInited = false;
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

	public BulletNifModel createRECO(J3dRECOInst j3dRECOInst)
	{
		if (recoIdToNifBullet.containsKey(j3dRECOInst.getRecordId()))
		{
			System.out.println("PhysicsDynamics, already loaded key " + j3dRECOInst.getRecordId() + " of " + j3dRECOInst);
			new Throwable("Thread:" + Thread.currentThread()).printStackTrace();
		}

		if (j3dRECOInst instanceof J3dLAND)
		{
			return createLand((J3dLAND) j3dRECOInst);
		}
		else
		{
			J3dRECOType j3dRECOType = j3dRECOInst.getJ3dRECOType();

			if (j3dRECOType != null && j3dRECOType.physNifFile != null)
			{
				return createStaticOrDynamic(j3dRECOInst, j3dRECOType.physNifFile);
			}
			else
			{
				System.out.println("j3dRECOType null or null phys " + j3dRECOType);
			}
		}
		//System.out.println("add called total= " + nifBulletToRecoId.size());
		return null;
	}

	private NBSimpleModel createLand(J3dLAND j3dLAND)
	{
		Transform3D rootTrans = j3dLAND.getLocation(new Transform3D());
		NBSimpleModel nb = new NBSimpleModel(j3dLAND.getGeometryInfo(), rootTrans);
		if (nb != null)
		{
			synchronized (recoIdToNifBullet)
			{
				recoIdToNifBullet.put(j3dLAND.getRecordId(), nb);
				nifBulletToRecoId.put(nb, j3dLAND.getRecordId());
			}
		}
		return nb;
	}

	/**
	 * the loading from file must occur not on the physics thread
	 * it will be happily done well before time, 
	 * then addRECO should happen on the physics tick  thread. 	
	 * 
	 * @param j3dRECOInst
	 * @param physNifFile
	 */
	private BulletNifModel createStaticOrDynamic(J3dRECOInst j3dRECOInst, String physNifFile)
	{
		BulletNifModel nb = null;

		//root should have scale in it
		Transform3D rootTrans = j3dRECOInst.getLocation(new Transform3D());

		if (physNifFile != null && physNifFile.length() > 0)
		{

			if (BulletNifModelClassifier.isStaticModel(physNifFile, meshSource))
			{
				// the nif file will have mass of 0 making this static
				nb = new NBSimpleModel(physNifFile, meshSource, rootTrans);
			}
			else if (BulletNifModelClassifier.isKinematicModel(physNifFile, meshSource))
			{
				// the nif file will have mass of 0 making this kinematic
				nb = new NBSimpleModel(physNifFile, meshSource, rootTrans);
			}
			else if (BulletNifModelClassifier.isSimpleDynamicModel(physNifFile, meshSource, 0))
			{
				createDynamic(j3dRECOInst, physNifFile);
			}
			else if (BulletNifModelClassifier.isComplexDynamic(physNifFile, meshSource))
			{
				//TODO: this bad boy right here
			}
			else
			{
				//TODO: lots of plants have this check them out 
				//System.out.println("crazy type? " + physNifFile);
				// probably just smoke effect etc, complex dynamic rag doll
			}

			if (nb != null)
			{
				synchronized (recoIdToNifBullet)
				{
					recoIdToNifBullet.put(j3dRECOInst.getRecordId(), nb);
					nifBulletToRecoId.put(nb, j3dRECOInst.getRecordId());
				}
			}

		}
		else
		{
			//Lights and alsorts of things can have no model or physics
			//System.out.println("why null phys? " + j3dRECOInst);
		}

		return nb;
	}

	private NBSimpleDynamicModel createDynamic(J3dRECOInst j3dRECOInst, String model)
	{
		NBSimpleDynamicModel nb = null;
		Transform3D rootTrans = j3dRECOInst.getLocation(new Transform3D());

		if (model != null && model.length() > 0)
		{

			//VELO velo = instReco.velocity;

			nb = new NBSimpleDynamicModel(model, meshSource, 0);

			if (nb != null)
			{
				//TODO: nif file should have mass, but for custom written might need to check and set set mass 
				//nb.getRootNifBulletbhkCollisionObject().getRigidBody().setMassProps(mass, inertia);

				Vector3f linearVelocity = new Vector3f();
				Vector3f rotationalVelocity = new Vector3f();
				//velo.getVelocities(linearVelocity, rotationalVelocity);
				nb.forceUpdate(rootTrans, linearVelocity, rotationalVelocity);

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

		return nb;
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
			// TODO: this seems dodgy perhaps an exception here, surely it should be dynamic or kinematic?
			//remove re-create and re-add
			removeRECO(j3dRECOInst);
			createRECO(j3dRECOInst);
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
			else
			{
				//wow TES3 door have no animation, they look like they just artifically pivot around 
				System.out.println("updateRECOToggleOpen door with no controller, probably travel door "
						+ j3dRECOInst.getJ3dRECOType().getName());
			}
		}

	}

	/**
	 * Note I need to carefully watch for the addChild to the java3d scene graph
	 * it must be done to a non live root node and then added later via a strucutre update behaviour
	 * dynamicsRootBranchGroup.addChild((NBSimpleModel) nb);
	 * @param j3dRECOInst
	 */
	protected void addRECO(J3dRECOInst j3dRECOInst)
	{
		//NOTE a create must have been called for this J3dRECOInst
		int recordId = j3dRECOInst.getRecordId();
		BulletNifModel nifBullet = recoIdToNifBullet.get(recordId);
		if (nifBullet != null)
		{
			// add to physics simulation
			synchronized (dynamicsWorld)
			{
				nifBullet.addToDynamicsWorld(dynamicsWorld);

				//TODO: this guy is added things to a live scene graph, definately problem chance??
				if (nifBullet instanceof Node)
					dynamicsRootBranchGroup.addChild((Node) nifBullet);

				if (nifBullet instanceof NBSimpleDynamicModel)
				{
					NifBulletBinding irnbb = new InstRecoNifBulletBinding(j3dRECOInst, instRecoToNif, (NBSimpleDynamicModel) nifBullet);
					instRecoBulletBindings.put(j3dRECOInst.getRecordId(), irnbb);
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
				//TODO: this guy is added things to a live scene graph, definately problem chance??
				if (nifBullet instanceof Node)
					dynamicsRootBranchGroup.removeChild((Node) nifBullet);

				nifBullet.removeFromDynamicsWorld();
				nifBullet.destroy();
				instRecoBulletBindings.remove(recordId);
			}
			synchronized (recoIdToNifBullet)
			{
				nifBulletToRecoId.remove(nifBullet);
				recoIdToNifBullet.remove(recordId);
			}

		}
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
		synchronized (recoIdToNifBullet)
		{
			for (BulletNifModel bnm : recoIdToNifBullet.values())
			{
				if (bnm instanceof NBSimpleDynamicModel)
				{
					ret.dynCount++;
				}
				else if (bnm instanceof NBSimpleModel)
				{
					NBSimpleModel sm = (NBSimpleModel) bnm;
					ret.kinCount += sm.hasKinematics() ? 1 : 0;
					ret.staCount += !sm.hasKinematics() ? 1 : 0;
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
