package scrollsexplorer.simpleclient.physics;

import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3f;

import nifbullet.BulletNifModel;
import nifbullet.BulletNifModelClassifier;
import nifbullet.cha.NBControlledChar;
import nifbullet.dyn.NBSimpleDynamicModel;
import nifbullet.kin.NBKinematicModel;
import nifbullet.stat.NBStaticModel;
import nifbullet.util.debug.opengl.DebugOutput;
import nifbullet.util.debug.opengl.LWJGL;
import tools3d.navigation.AvatarLocation;
import tools3d.universe.HeadlessUniverse;
import utils.source.MeshSource;
import utils.source.file.FileMeshSource;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;

import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class PhysicsDynamics extends DynamicsEngine
{
	public static HeadlessUniverse headlessUniverse = new HeadlessUniverse();

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
		clientNifBulletCharBinding = new ClientNifBulletCharBinding(avatarLocation, myNifBulletChar);
		instRecoBulletBindings.put(-999, clientNifBulletCharBinding);
		synchronized (dynamicsWorld)
		{
			myNifBulletChar.addToDynamicsWorld(dynamicsWorld);
		}
	}

	public void setDisplayDebug(boolean displayDebug)
	{
		this.displayDebug = displayDebug;
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
		if (j3dRECOInst instanceof J3dLAND)
		{
			createLand((J3dLAND) j3dRECOInst);
		}
		else
		{
			J3dRECOType j3dRECOType = j3dRECOInst.getJ3dRECOType();
			if (j3dRECOType != null && j3dRECOType.physNifFile != null)
			{
				createStatic(j3dRECOInst, j3dRECOType.physNifFile);
			}
		}
		//System.out.println("add called total= " + nifBulletToRecoId.size());
	}

	private void createLand(J3dLAND j3dLAND)
	{
		Transform3D rootTrans = j3dLAND.getLocation(new Transform3D());
		NBStaticModel nb = new NBStaticModel(j3dLAND.getGeometryInfo(), rootTrans);
		if (nb != null)
		{
			// Note we don't listen for physics updates
			recoIdToNifBullet.put(j3dLAND.getRecordId(), nb);
			nifBulletToRecoId.put(nb, j3dLAND.getRecordId());

			synchronized (dynamicsWorld)
			{
				nb.addToDynamicsWorld(dynamicsWorld);
			}
		}
	}

	private void createStatic(J3dRECOInst j3dRECOInst, String physNifFile)
	{
		Transform3D rootTrans = j3dRECOInst.getLocation(new Transform3D());

		if (physNifFile != null && physNifFile.length() > 0)
		{
			BulletNifModel nb = null;
			if (BulletNifModelClassifier.isStaticModel(physNifFile, meshSource))
			{
				// the nif file will have mass of 0 making this static
				nb = new NBStaticModel(physNifFile, meshSource, rootTrans);
			}
			else if (BulletNifModelClassifier.isKinematicModel(physNifFile, new FileMeshSource()))
			{
				// the nif file will have mass of 0 making this kinematic
				nb = new NBKinematicModel(physNifFile, meshSource, rootTrans);
			}
			else
			{
				// probably just smoke effect etc
			}

			if (nb != null)
			{
				// Note we don't listen for physics updates
				recoIdToNifBullet.put(j3dRECOInst.getRecordId(), nb);
				nifBulletToRecoId.put(nb, j3dRECOInst.getRecordId());

				synchronized (dynamicsWorld)
				{
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

			NBSimpleDynamicModel nb = new NBSimpleDynamicModel(model, meshSource);

			if (nb != null)
			{
				//TODO: nif file should have mass, but for custom written might need to check and set set mass 
				//nb.getRootNifBulletbhkCollisionObject().getRigidBody().setMassProps(mass, inertia);

				Vector3f linearVelocity = new Vector3f();
				Vector3f rotationalVelocity = new Vector3f();
				//velo.getVelocities(linearVelocity, rotationalVelocity);
				nb.forceUpdate(rootTrans, linearVelocity, rotationalVelocity);

				NifBulletBinding irnbb = new ClientInstRecoNifBulletBinding(j3dRECOInst, instRecoToNif, nb);

				if (irnbb != null)
				{
					instRecoBulletBindings.put(j3dRECOInst.getRecordId(), irnbb);
				}
				recoIdToNifBullet.put(j3dRECOInst.getRecordId(), nb);
				nifBulletToRecoId.put(nb, j3dRECOInst.getRecordId());
				dynamicsRootBranchGroup.addChild(nb);
				synchronized (dynamicsWorld)
				{
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

	protected void removeRECO(J3dRECOInst j3dRECOInst)
	{
		int recordId = j3dRECOInst.getRecordId();
		BulletNifModel nifBullet = recoIdToNifBullet.get(recordId);
		if (nifBullet != null)
		{
			// remove from physics simulation
			synchronized (dynamicsWorld)
			{
				nifBullet.removeFromDynamicsWorld(dynamicsWorld);
			}
			nifBullet.destroy();
			instRecoBulletBindings.remove(recordId);
			nifBulletToRecoId.remove(nifBullet);
			recoIdToNifBullet.remove(recordId);
		}
		//System.out.println("remove called total= " + nifBulletToRecoId.size());

	}

	public void applyPhysicsToModel()
	{
		for (NifBulletBinding instRecoNifBulletBinding : instRecoBulletBindings.values())
		{
			instRecoNifBulletBinding.applyToModel();
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

	public ClosestRayResultCallback findRayIntersect(Vector3f rayFrom, Vector3f rayTo)
	{
		CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(rayFrom, rayTo);
		getDynamicsWorld().rayTest(rayFrom, rayTo, rayCallback);
		return rayCallback;
	}

}