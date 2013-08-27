package scrollsexplorer.simpleclient.physics;

import javax.media.j3d.BranchGroup;

import nifbullet.NavigationProcessorBullet.NbccProvider;
import nifbullet.cha.NBControlledChar;
import tools3d.navigation.AvatarLocation;
import utils.source.MeshSource;
import esmj3d.j3d.cell.GridSpace;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

/**
 * This is a copy of the ServerPhysicsSystem class, with collision removed, keep in synch
 */
public class ClientPhysicsSystem extends PhysicsSystem implements NbccProvider
{
	// model update min time step
	public long CLIENT_MIN_TIME_BETWEEN_BOUND_UPDATES_MS = 10;

	private AvatarLocation avatarLocation;

	private BranchGroup behaviourRoot;

	protected InstRECOStore instRECOStore;

	private MeshSource meshSource;

	public ClientPhysicsSystem(InstRECOStore instRECOStore, AvatarLocation avatarLocation, BranchGroup behaviourRoot, MeshSource meshSource)
	{
		this.avatarLocation = avatarLocation;
		this.behaviourRoot = behaviourRoot;
		this.instRECOStore = instRECOStore;
		this.meshSource = meshSource;
		setMinTimeForBoundUpdate(CLIENT_MIN_TIME_BETWEEN_BOUND_UPDATES_MS);
	}

	/**
	 * Use -1 cell id to indicate just an unload
	 * @param cid
	 * @param j3dCELLTemporary 
	 * @param instRecoToNif
	 */
	public void cellChanged(int cid, J3dCELLGeneral cell)
	{
		unload();

		this.cellId = cid;

		if (cell != null && cid != -1)
		{
			physicsLocaleDynamics = new PhysicsDynamics(instRECOStore, gravity, behaviourRoot, avatarLocation, meshSource);
			loadJ3dCELL(cell);
			System.out.println("Physics objects loaded for cell " + cellId);
		}
	}

	public void loadJ3dGridSpace(GridSpace cell)
	{		
		// add the items
		for (J3dRECOInst instReco : cell.getJ3dRECOsById().values())
		{			
			physicsLocaleDynamics.addRECO(instReco);
		}
	}

	public void unloadJ3dGridSpace(GridSpace cell)
	{		
		// add the items
		for (J3dRECOInst instReco : cell.getJ3dRECOsById().values())
		{			
			physicsLocaleDynamics.removeRECO(instReco);
		}
	}

	public void loadJ3dCELL(J3dCELLGeneral cell)
	{
		// add the items
		for (J3dRECOInst instReco : cell.getJ3dRECOs().values())
		{
			physicsLocaleDynamics.addRECO(instReco);
		}
	}

	public void unloadJ3dCELL(J3dCELLGeneral cell)
	{
		// remove the items
		for (J3dRECOInst instReco : cell.getJ3dRECOs().values())
		{
			physicsLocaleDynamics.removeRECO(instReco);
		}
	}

	public void destroy()
	{
		unload();
		System.out.println("Physics stopped");
	}

	protected void unload()
	{
		if (physicsLocaleDynamics != null)
		{
			physicsLocaleDynamics.destroy();
		}
		this.cellId = -1;

	}

	public PhysicsDynamics getClientPhysics()
	{
		return physicsLocaleDynamics;
	}

	@Override
	public NBControlledChar getNBControlledChar()
	{
		if (physicsLocaleDynamics != null)
		{
			return physicsLocaleDynamics.getMyNifBulletChar();
		}
		return null;
	}

}
