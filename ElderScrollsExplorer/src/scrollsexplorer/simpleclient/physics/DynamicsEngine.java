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
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.linearmath.Clock;

import awt.tools3d.mixed3d2d.hud.hudelements.HUDPhysicsState.HUDPhysicsStateData;

public abstract class DynamicsEngine implements HUDPhysicsStateData
{
	protected Clock timeKeeper = new Clock();

	private BroadphaseInterface broadphase;

	private CollisionDispatcher dispatcher;

	private ConstraintSolver solver;

	private CollisionConfiguration collisionConfiguration;

	protected DynamicsWorld dynamicsWorld;

	private boolean paused = true;

	private boolean skipStepSim = false;

	private long[] recentStepTimes = new long[10];

	private int recentStepTimesHead = 0;

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

	public boolean isSkipStepSim()
	{
		return skipStepSim;
	}

	public void setSkipStepSim(boolean skipStepSim)
	{
		this.skipStepSim = skipStepSim;
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
					if (!skipStepSim)
					{
						dynamicsWorld.stepSimulation(dtms / 1000000f, 5);
					}
					else
					{
						//fire actions any way?
						for (int a = 0; a < dynamicsWorld.getNumActions(); a++)
						{
							ActionInterface ai = dynamicsWorld.getAction(a);
							ai.updateAction(dynamicsWorld, dtms / 1000000f);
						}
					}
				}

				// chuck it in the recent step time
				addStepTime(((timeKeeper.getTimeMicroseconds() - dtms2) / 1000));
			}
			catch (NullPointerException e)
			{
				//DbvtBroadphase.setAabb being a dick
				System.out.println("" + e + " " + e.getStackTrace()[0]);
			}
			catch (ClassCastException e)
			{
				//probably BvhTriangleMeshShape cannot be cast to com.bulletphysics.collision.shapes.CompoundShape
				// CollisionDispatcher findAlgorithm(body0, body1, null) is finding the compund algorithm but but one not compound

				//is this for an unknown type maybe?
				//DefaultCollisionConfiguration().getCollisionAlgorithmCreateFunc ??? 

				//seems like this guy GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);

				System.out.println("" + e + " " + e.getStackTrace()[0]);
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

	public void pause()
	{
		paused = true;
	}

	public void unpause()
	{
		paused = false;
	}

	public boolean isPaused()
	{
		return paused;
	}

	private void addStepTime(long time)
	{
		recentStepTimes[recentStepTimesHead] = time;
		recentStepTimesHead++;
		recentStepTimesHead = recentStepTimesHead >= recentStepTimes.length ? 0 : recentStepTimesHead;

	}

	public int getAverageStepTimeMS()
	{
		int average = 0;

		for (long time : recentStepTimes)
			average += time;

		return average / recentStepTimes.length;
	}

	public int getNumCollisionObjects()
	{
		return dynamicsWorld.getNumCollisionObjects();
	}
}
