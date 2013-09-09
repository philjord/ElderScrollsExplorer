package scrollsexplorer.simpleclient.mouseover;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;

import nifbullet.BulletNifModel;
import nifbullet.NBRigidBody;
import nifbullet.cha.NifBulletChar;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.physics.ClientPhysicsSystem;
import tools3d.hud.Canvas3D2D;
import tools3d.hud.old.HUDText;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;

import esmj3d.data.shared.records.CommonREFR;
import esmj3d.data.shared.subrecords.XTEL;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOStatInst;
import esmj3d.j3d.j3drecords.type.J3dDOOR;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class ActionableMouseOverHandler extends MouseOverHandler
{
	public static final float INTERACT_MAX_DIST = 2.6f;

	private J3dRECOInst currentActionable = null;

	private static Object currentActionableMonitor = new Object();

	private HUDText HUDText;

	public ActionableMouseOverHandler(ClientPhysicsSystem clientPhysicsSystem)
	{
		super(clientPhysicsSystem);
	}

	@Override
	public void doMouseMoved(MouseEvent e)
	{
		super.doMouseMoved(e);
		if (canvas3D != null && HUDText != null)
		{
			HUDText.setLocation((canvas3D.getWidth() / 2) - 70, (canvas3D.getHeight() / 2));
		}
	}

	@Override
	public void doMouseReleased(MouseEvent e)
	{
		// the show info call below on the java3d behavior thread might change currentactionable on us
		synchronized (currentActionableMonitor)
		{
			if (currentActionable != null && e.getButton() == MouseEvent.BUTTON1)
			{
				if (currentActionable instanceof J3dRECOStatInst)
				{
					// if the mouse release listener is working we can't change the currentActionable until it's finished
					synchronized (currentActionableMonitor)
					{
						J3dRECOStatInst j3dRECOStatInst = (J3dRECOStatInst) currentActionable;
						// sort out the actionable if  
						if (j3dRECOStatInst.getInstRECO() instanceof CommonREFR)
						{
							CommonREFR commonREFR = (CommonREFR) j3dRECOStatInst.getInstRECO();
							XTEL xtel = commonREFR.XTEL;
							if (xtel != null)
							{
								// doorFormId is not a cell id! use teh cell of from id call
								SimpleBethCellManager.simpleBethCellManager.setCurrentCellFormIdOf(xtel.doorFormId);
								SimpleBethCellManager.simpleBethCellManager.setLocation(xtel.x, xtel.y, xtel.z, xtel.rx, xtel.ry, xtel.rz);
							}
							else
							{
								//possibly a door that needs opening/closing
								J3dRECOType j3dRECOType = j3dRECOStatInst.getJ3dRECOType();

								if (j3dRECOType instanceof J3dDOOR)
								{
									J3dDOOR j3dDOOR = (J3dDOOR) j3dRECOType;
									j3dDOOR.toggleOpen();
									
									//TODO: now to tell physics all about it??
								}

							}
						}

					}
				}
			}
		}
	}

	@Override
	public void setConfig(Canvas3D canvas)
	{
		super.setConfig(canvas);
		currentActionable = null;

		//remove old hudtext
		if (HUDText != null)
		{
			HUDText.destroy();
		}

		// set up new canvas
		if (canvas3D != null)
		{
			HUDText = new HUDText((Canvas3D2D) canvas3D,
					new Rectangle((canvas3D.getWidth() / 2) - 70, (canvas3D.getHeight() / 2), 140, 60), 16);
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

						J3dRECOInst j3dInstRECO = SimpleBethCellManager.currentBethWorldVisualBranch.getJ3dInstRECO(recoId);

						if (j3dInstRECO != null && j3dInstRECO instanceof J3dRECOStatInst)
						{
							// if the mouse release listener is working we can't change the currentActionable until it's finished
							synchronized (currentActionableMonitor)
							{
								// sort out the actionable if  
								if (j3dInstRECO.getInstRECO() instanceof CommonREFR)
								{
									CommonREFR commonREFR = (CommonREFR) j3dInstRECO.getInstRECO();
									XTEL xtel = commonREFR.XTEL;

									J3dRECOType j3dRECOType = j3dInstRECO.getJ3dRECOType();

									if (xtel != null)
									{
										// if less than the max interact then set interactable
										// if not then set hudtext (in light grey) but don't allow actions								

										float distance = MAX_MOUSE_RAY_DIST * rayCallback.closestHitFraction;
										if (distance < INTERACT_MAX_DIST)
										{

											HUDText.setText("DOOR to " + xtel.doorFormId);
											currentActionable = j3dInstRECO;
										}
										else
										{
											HUDText.setTextGreyed("DOOR to  " + xtel.doorFormId + " (dist)");
											currentActionable = null; // nothing to action yet										
										}
									}
									else if (j3dRECOType instanceof J3dDOOR)
									{
										float distance = MAX_MOUSE_RAY_DIST * rayCallback.closestHitFraction;
										if (distance < INTERACT_MAX_DIST)
										{

											HUDText.setText("Open/Close DOOR");
											currentActionable = j3dInstRECO;
										}
										else
										{
											HUDText.setTextGreyed("Open/Close DOOR (dist)");
											currentActionable = null; // nothing to action yet										
										}
									}
									else
									{
										HUDText.setText("");
										currentActionable = null;
									}
								}
							}

						}
						else
						{
							HUDText.setText("");
							currentActionable = null;
						}

					}
					else
					{
						HUDText.setText("");
						currentActionable = null;
					}
				}
				else
				{
					HUDText.setText("");
					currentActionable = null;
				}
			}
		}

	}
}