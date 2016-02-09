package scrollsexplorer.simpleclient.mouseover;

import javax.media.j3d.Canvas3D;
import javax.vecmath.Point2f;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.jogamp.newt.event.MouseEvent;

import nifbullet.BulletNifModel;
import nifbullet.NBRigidBody;
import nifbullet.cha.NifBulletChar;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.curvehud.elements.HUDText;

public class AdminMouseOverHandler extends MouseOverHandler
{
	private HUDText HUDText;

	private int hudWidth = 500;

	private int hudHeight = 50;

	public AdminMouseOverHandler(PhysicsSystem clientPhysicsSystem)
	{
		super(clientPhysicsSystem);
	}

	@Override
	public void doMouseReleased(MouseEvent e)
	{
		//nothing for now
	}

	@Override
	public void setConfig(Canvas3D canvas)
	{
		super.setConfig(canvas);

		//remove old hudtext
		if (HUDText != null)
		{
			HUDText.removeFromCanvas();

		}
		// set up new canvas
		if (canvas3D != null)
		{
			//HUDText = new HUDText((Canvas3D2D) canvas3D, new Rectangle((int) (canvas3D.getWidth() * 0.95f) - hudWidth,
			//		(int) (canvas3D.getHeight() * 0.95f) - hudHeight, hudWidth, hudHeight), 10);
			HUDText = new HUDText((Canvas3D2D) canvas3D, new Point2f(0.1f, -0.85f), 10);
		}
	}

	@Override
	protected void screenResized()
	{
		HUDText.setLocation((int) (canvas3D.getWidth() * 0.95f) - hudWidth, (int) (canvas3D.getHeight() * 0.95f) - hudHeight);
	}

	@Override
	protected void processMouseOver(MouseEvent mouseEvent)
	{
		if (clientPhysicsSystem != null)
		{
			ClosestRayResultCallback rayCallback = findRayIntersect(mouseEvent);
			if (rayCallback != null && rayCallback.hasHit())
			{
				RigidBody body = RigidBody.upcast(rayCallback.collisionObject);

				if (body != null)
				{

					BulletNifModel bnm = null;

					// might be one of 2 ways to get teh model out of the user pointer
					if (body.getUserPointer() instanceof NBRigidBody)
					{
						NBRigidBody nBRigidBody = (NBRigidBody) body.getUserPointer();
						bnm = nBRigidBody.getParentModel();
					}
					else if (body.getUserPointer() instanceof NifBulletChar)
					{
						bnm = (NifBulletChar) body.getUserPointer();
					}

					if (bnm != null)
					{

						int recoId = clientPhysicsSystem.getPhysicsLocaleDynamics().getRecordId(bnm);

						// show a name for the pointed at thing, in a general way

						float distance = MAX_MOUSE_RAY_DIST * rayCallback.closestHitFraction;
						HUDText.setText("" + recoId + " : " + distance + " : " + bnm.toString());
						//System.out.println("" + recoId + " : " + distance + " : " + bnm.toString());

					}
					else
					{
						HUDText.setText("");
					}
				}
				else
				{
					HUDText.setText("");
				}
			}
		}
	}

}