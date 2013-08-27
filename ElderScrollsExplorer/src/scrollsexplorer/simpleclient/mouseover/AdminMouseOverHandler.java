package scrollsexplorer.simpleclient.mouseover;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;

import nifbullet.BulletNifModel;
import nifbullet.NBRigidBody;
import nifbullet.cha.NifBulletChar;
import scrollsexplorer.simpleclient.physics.ClientPhysicsSystem;
import tools3d.hud.Canvas3D2D;
import tools3d.hud.old.HUDText;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;

public class AdminMouseOverHandler extends MouseOverHandler
{
	private HUDText HUDText;

	private BulletNifModel currentBulletNifModel;

	public AdminMouseOverHandler(ClientPhysicsSystem clientPhysicsSystem)
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
		currentBulletNifModel = null;
		//remove old hudtext
		if (HUDText != null)
		{
			HUDText.destroy();
		}
		// set up new canvas
		if (canvas3D != null)
		{
			HUDText = new HUDText((Canvas3D2D) canvas3D, new Rectangle(canvas3D.getWidth() - 500, canvas3D.getHeight() - 50, 490, 30), 10);
		}
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

						int recoId = clientPhysicsSystem.getClientPhysics().getRecordId(bnm);

						// show a name for the pointed at thing, in a general way
						// detach and change info only if we are pointing at a new object3d
						if (currentBulletNifModel != bnm)
						{
							float distance = MAX_MOUSE_RAY_DIST * rayCallback.closestHitFraction;
							HUDText.setText("" + recoId + " : " + distance + " : " + bnm.toString());
						}
						currentBulletNifModel = bnm;
					}
					else
					{
						HUDText.setText("");
						currentBulletNifModel = null;
					}
				}
				else
				{
					HUDText.setText("");
					currentBulletNifModel = null;
				}
			}
		}
	}
}