package scrollsexplorer.simpleclient.physics;

import javax.vecmath.Vector3f;

import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public abstract class PhysicsSystem
{
	public static Vector3f gravity = new Vector3f(0f, -9.8f, 0f);

	private long MIN_TIME_BETWEEN_BOUND_UPDATES_MS = 50;

	private long MIN_TIME_BETWEEN_STEPS_MS = 30;

	protected PhysicsDynamics physicsLocaleDynamics;

	private boolean paused = false;

	private long lastPhysicsBoundUpdate = 0;

	private long lastPhysicsStepUpdate = 0;

	protected int cellId;

	protected void setMinTimeForBoundUpdate(long newTime)
	{
		MIN_TIME_BETWEEN_BOUND_UPDATES_MS = newTime;
	}

	public void pause()
	{
		paused = true;
		physicsLocaleDynamics.pause();
	}

	public void unpause()
	{
		paused = false;
		physicsLocaleDynamics.unpause();
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void addRECO(J3dRECOInst j3dRECOInst)
	{
		physicsLocaleDynamics.addRECO(j3dRECOInst);
	}

	public void removeRECO(J3dRECOInst j3dRECOInst)
	{
		physicsLocaleDynamics.removeRECO(j3dRECOInst);
	}

	public void physicsTick()
	{

		if (!isPaused() && physicsLocaleDynamics != null)
		{
			long elapsedTimeStep = (System.nanoTime() - lastPhysicsStepUpdate) / 1000000;
		
			if (elapsedTimeStep > MIN_TIME_BETWEEN_STEPS_MS)
			{				 
				physicsLocaleDynamics.dynamicsTick();
				lastPhysicsStepUpdate = System.nanoTime();
			}

			// is it time to update the model from the physics
			long elapsedTime = (System.nanoTime() - lastPhysicsBoundUpdate) / 1000000;
			if (elapsedTime > MIN_TIME_BETWEEN_BOUND_UPDATES_MS)
			{
				physicsLocaleDynamics.applyPhysicsToModel();
				lastPhysicsBoundUpdate = System.nanoTime();
			}
		}
	}

	public PhysicsDynamics getPhysicsLocaleDynamics()
	{
		return physicsLocaleDynamics;
	}
}
