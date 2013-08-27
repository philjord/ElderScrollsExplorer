package scrollsexplorer.simpleclient.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.HashedOverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.DefaultNearCallback;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.linearmath.Clock;

public abstract class DynamicsEngine
{
	protected Clock timeKeeper = new Clock();

	private BroadphaseInterface broadphase;

	private CollisionDispatcher dispatcher;

	private ConstraintSolver solver;

	private CollisionConfiguration collisionConfiguration;

	protected DynamicsWorld dynamicsWorld;

	private boolean paused = false;

	/**
	 * Note this requries the dynamicsUpdate to be called on a regular basis by whatever teh mian thread is
	 * Try to call it at least 60 per second (16ms) or more often if possible
	 * @param interior
	 * @param gravity
	 * @param physicsScaling
	 */
	public DynamicsEngine(Vector3f gravity)
	{

		// collision configuration contains default setup for memory, collision setup
		collisionConfiguration = new DefaultCollisionConfiguration();

		// use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		dispatcher.setNearCallback(new DefaultNearCallback());

		broadphase = new DbvtBroadphase(new HashedOverlappingPairCache());

		//set up the ghost pair call back thing
		broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());

		// the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
		solver = new SequentialImpulseConstraintSolver();

		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

		// register algorithm  for giimpact
		GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);

		dynamicsWorld.setGravity(gravity);

		timeKeeper.reset();

	}

	public void dynamicsTick()
	{
		long dtms = timeKeeper.getTimeMicroseconds();
		timeKeeper.reset();
		dynamicsPreStep();

		// step the simulation
		if (dynamicsWorld != null)
		{
			try
			{
				long dtms2 = timeKeeper.getTimeMicroseconds();
				//note timeStep is seconds not ms AND you must have a sub step count! make him 5ish
				synchronized (dynamicsWorld)
				{
					dynamicsWorld.stepSimulation(dtms / 1000000f, 5);
				}
				if ((timeKeeper.getTimeMicroseconds() - dtms2) > 20000)
				{
					System.out.println("dynamicsTick step took millisecs of " + ((timeKeeper.getTimeMicroseconds() - dtms2) / 1000));
					System.out.println("dynamicsWorld.getNumCollisionObjects() " + dynamicsWorld.getNumCollisionObjects());
				}
			}
			catch (NullPointerException e)
			{
				//DbvtBroadphase.setAabb being a dick
				System.out.println("" + e.getMessage());
			}
			catch (ClassCastException e)
			{
				//probably BvhTriangleMeshShape cannot be cast to com.bulletphysics.collision.shapes.CompoundShape
				// CollisionDispatcher findAlgorithm(body0, body1, null) is finding the compund algorithm but but one not compound

				//is this for an unknown type maybe?
				//DefaultCollisionConfiguration().getCollisionAlgorithmCreateFunc ??? 

				//seems like this guy GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);

				System.out.println("" + e.getMessage());
			}

		}

		dynamicsPostStep();
	}

	protected abstract void dynamicsPostStep();

	protected abstract void dynamicsPreStep();

	@Override
	public void finalize()
	{
		destroy();
	}

	public synchronized void destroy()
	{
	}

	public DynamicsWorld getDynamicsWorld()
	{
		return dynamicsWorld;
	}

	public void pause()
	{
		if (isPaused())
		{
			System.err.println("Pause called, but already paused? for physics " + this);
		}
		paused = true;
	}

	public void unpause()
	{
		if (!isPaused())
		{
			System.err.println("Unpause called, but not paused? for physics " + this);
		}
		paused = false;
	}

	public boolean isPaused()
	{
		return paused;
	}

}
