package scrollsexplorer.simpleclient.mouseover;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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

//TODO: this and the copy in space trader could have the clientphysics replaced with a dynamicsworld object (if avaible?)
//and this would be totally generic and able to be put in nifbullet
public abstract class MouseOverHandler implements ComponentListener
{
	public static final float MAX_MOUSE_RAY_DIST = 100f;// max pick dist 100 meters?

	private static final long MIN_TIME_BETWEEN_STEPS_MS = 500;

	protected Canvas3D canvas3D;

	protected CanvasPickRayGen selectPickCanvas;

	protected MouseEvent lastMouseEvent;

	protected PhysicsSystem clientPhysicsSystem;

	private PeriodicThread mouseOverHandlerThread;

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

		mouseOverHandlerThread = new PeriodicThread("Thread For " + this.getClass().getSimpleName(), MIN_TIME_BETWEEN_STEPS_MS,
				new PeriodicallyUpdated()
				{
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
			canvas3D.removeComponentListener(this);
		}

		// set up new canvas
		this.canvas3D = canvas;
		if (this.canvas3D != null)
		{
			selectPickCanvas = new CanvasPickRayGen(canvas3D);
			selectPickCanvas.setTolerance(0.0f);

			canvas3D.addMouseListener(mouseAdapter);
			canvas3D.addMouseMotionListener(mouseMotionAdapter);
			canvas3D.addComponentListener(this);
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
	public void componentResized(ComponentEvent e)
	{
		screenResized();
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		screenResized();
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
	}
}
