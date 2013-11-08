package scrollsexplorer.simpleclient.mouseover;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.PickRay;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools3d.mixed3d2d.CanvasPickRayGen;
import tools3d.utils.Utils3D;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.DynamicsWorld;

//TODO: this and the copy in space trader could have the clientphysics replaced with a dynamicsworld object (if avaible?)
//and this would be totally generic and able to be put in nifbullet
public abstract class MouseOverHandler extends BranchGroup
{
	public static final float MAX_MOUSE_RAY_DIST = 100f;// max pick dist 100 meters?

	public static final int FREQUENCY = 15;

	protected Canvas3D canvas3D;

	protected CanvasPickRayGen selectPickCanvas;

	protected MouseEvent lastMouseEvent;

	protected PhysicsSystem clientPhysicsSystem;

	private MouseOverTimer mouseOverTimer = new MouseOverTimer();

	private MouseAdapter mouseAdapter = new MouseAdapter()
	{
		public void mouseExited(MouseEvent e)
		{
			doMouseExited(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			doMouseReleased(e);
		}
	};

	private MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter()
	{
		public void mouseMoved(MouseEvent e)
		{
			doMouseMoved(e);
		}
	};

	public MouseOverHandler(PhysicsSystem clientPhysicsSystem)
	{
		this.clientPhysicsSystem = clientPhysicsSystem;
		setCapability(BranchGroup.ALLOW_DETACH);
		// add the picking timer to the universe
		mouseOverTimer.setSchedulingBounds(Utils3D.defaultBounds);
		mouseOverTimer.setEnable(true);
		addChild(mouseOverTimer);

	}

	public void doMouseMoved(MouseEvent e)
	{
		// record the mouse move for the picker to use when it next wakes up
		lastMouseEvent = e;
	}

	public void doMouseExited(@SuppressWarnings("unused") MouseEvent e)
	{
		lastMouseEvent = null;
	}

	public abstract void doMouseReleased(MouseEvent e);

	public void setConfig(Canvas3D canvas)
	{
		// de-register on the old canvas
		if (this.canvas3D != null)
		{
			canvas3D.removeMouseListener(mouseAdapter);
			canvas3D.removeMouseMotionListener(mouseMotionAdapter);
		}

		// set up new canvas
		this.canvas3D = canvas;
		if (this.canvas3D != null)
		{
			selectPickCanvas = new CanvasPickRayGen(canvas3D);
			selectPickCanvas.setTolerance(0.0f);

			canvas3D.addMouseListener(mouseAdapter);
			canvas3D.addMouseMotionListener(mouseMotionAdapter);
		}

	}

	protected ClosestRayResultCallback findRayIntersect(MouseEvent mouseEvent)
	{
		if (clientPhysicsSystem != null)
		{
			if (mouseEvent != null)
			{
				selectPickCanvas.setShapeLocation(mouseEvent);
			}
			else
			{
				// if there is no mousy then just use the center of the screen
				selectPickCanvas.setShapeLocation(canvas3D.getWidth() / 2, canvas3D.getHeight() / 2);
			}

			PickRay pr = (PickRay) selectPickCanvas.getPickShape();

			Point3d o = new Point3d();
			Vector3d d = new Vector3d();
			pr.get(o, d);

			// make a to point by adding 100 meters of the direction normal on
			Vector3f diff = new Vector3f(d);
			diff.normalize();
			diff.scale(MAX_MOUSE_RAY_DIST);

			Vector3f rayFrom = new Vector3f(o);
			Vector3f rayTo = new Vector3f(o);
			rayTo.add(diff);

			CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(rayFrom, rayTo);

			DynamicsWorld dynamicsWorld = clientPhysicsSystem.getClientPhysics().getDynamicsWorld();
			dynamicsWorld.rayTest(rayFrom, rayTo, rayCallback);
			return rayCallback;
		}
		return null;
	}

	protected abstract void processMouseOver(MouseEvent mouseEvent);

	private class MouseOverTimer extends Behavior
	{
		private WakeupOnElapsedFrames FPSWakeUp = new WakeupOnElapsedFrames(FREQUENCY);

		@Override
		public void initialize()
		{
			wakeupOn(FPSWakeUp);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void processStimulus(Enumeration critera)
		{
			if (lastMouseEvent != null)
			{
				//TODO: bugger bugger bugger, not on physics thread, the process code will drop NPE form time to time
				// I'm going to have to do all this on the physics stepping thread proper
				// or is that the navprocessbullet updated by a temporal behavior itself?
				// in which case whose calling really add and remove?
				// cos also AI is asking physics for GroundY and stuff too
				//TODO: Until we get every body talking to physics on the same clock this crap will happen
				try
				{
					processMouseOver(lastMouseEvent);
				}
				catch (Exception e)
				{
					System.out.println("MouseOverHandler.processMouseOver exception: " + e);
					e.printStackTrace();
				}
			}

			// Set the trigger for the behavior
			wakeupOn(FPSWakeUp);
		}
	}

}
