package scrollsexplorer.simpleclient.mouseover;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.PickRay;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.clock.PeriodicThread;
import tools.clock.PeriodicallyUpdated;
import tools3d.mixed3d2d.CanvasPickRayGen;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

//TODO: this and the copy in space trader could have the clientphysics replaced with a dynamicsworld object (if avaible?)
//and this would be totally generic and able to be put in nifbullet
public abstract class MouseOverHandler implements WindowListener
{
	public static final float MAX_MOUSE_RAY_DIST = 100f;// max pick dist 100 meters?

	private static final long MIN_TIME_BETWEEN_STEPS_MS = 125;

	protected Canvas3D canvas3D;

	protected CanvasPickRayGen selectPickCanvas;

	protected MouseEvent lastMouseEvent;

	protected PhysicsSystem clientPhysicsSystem;

	private PeriodicThread mouseOverHandlerThread;

	private MouseAdapter mouseAdapter = new MouseAdapter() {
		public void mouseExited(MouseEvent e)
		{
			doMouseExited(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			doMouseReleased(e);
		}

		public void mouseMoved(MouseEvent e)
		{
			doMouseMoved(e);
		}
	};

	public MouseOverHandler(PhysicsSystem clientPhysicsSystem)
	{
		this.clientPhysicsSystem = clientPhysicsSystem;

		mouseOverHandlerThread = new PeriodicThread("Thread For " + this.getClass().getSimpleName(), MIN_TIME_BETWEEN_STEPS_MS,
				new PeriodicallyUpdated() {
					public void runUpdate()
					{
						try
						{
							if (lastMouseEvent != null)
							{
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
						}
						catch (Exception e)
						{
							System.out.println("PhysicsSystem exception " + e + " " + e.getStackTrace()[0]);
						}
					}
				});
		mouseOverHandlerThread.start();

	}

	public void doMouseMoved(MouseEvent e)
	{
		// record the mouse move for the picker to use when it next wakes up
		lastMouseEvent = e;
	}

	public void doMouseExited(MouseEvent e)
	{
		lastMouseEvent = null;
	}

	public abstract void doMouseReleased(MouseEvent e);

	public void setConfig(Canvas3D canvas)
	{
		// de-register on the old canvas
		if (this.canvas3D != null)
		{
			canvas3D.getGLWindow().removeMouseListener(mouseAdapter);
			canvas3D.getGLWindow().removeWindowListener(this);
		}

		// set up new canvas
		this.canvas3D = canvas;
		if (this.canvas3D != null)
		{
			selectPickCanvas = new CanvasPickRayGen(canvas3D);
			selectPickCanvas.setTolerance(0.0f);

			canvas3D.getGLWindow().addMouseListener(mouseAdapter);
			canvas3D.getGLWindow().addWindowListener(this);
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

			CollisionWorld.ClosestRayResultCallback rayCallback = clientPhysicsSystem.findRayIntersect(rayFrom, rayTo);
			return rayCallback;
		}
		return null;
	}

	protected abstract void processMouseOver(MouseEvent mouseEvent);

	/**
	 * Override to change pos etc
	 */
	protected void screenResized()
	{

	}

	@Override
	public void windowResized(WindowEvent e)
	{
		screenResized();
	}

	@Override
	public void windowMoved(WindowEvent e)
	{
	}

	@Override
	public void windowDestroyNotify(WindowEvent e)
	{

	}

	@Override
	public void windowDestroyed(WindowEvent e)
	{

	}

	@Override
	public void windowGainedFocus(WindowEvent e)
	{

	}

	@Override
	public void windowLostFocus(WindowEvent e)
	{

	}

	@Override
	public void windowRepaint(WindowUpdateEvent e)
	{

	}

}
