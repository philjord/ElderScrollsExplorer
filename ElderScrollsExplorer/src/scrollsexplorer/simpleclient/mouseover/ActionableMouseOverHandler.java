package scrollsexplorer.simpleclient.mouseover;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;

import nifbullet.BulletNifModel;
import nifbullet.NBRigidBody;
import nifbullet.cha.NifBulletChar;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.hud.hudelements.HUDText;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;

import esmj3d.data.shared.records.CommonREFR;
import esmj3d.data.shared.subrecords.XTEL;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOStatInst;
import esmj3d.j3d.j3drecords.type.J3dCONT;
import esmj3d.j3d.j3drecords.type.J3dDOOR;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class ActionableMouseOverHandler extends MouseOverHandler
{
	public static final float INTERACT_MAX_DIST = 2.6f;

	private CurrentActionTargetData currentActionTargetData = new CurrentActionTargetData();

	private static Object currentActionableMonitor = new Object();

	private HUDText HUDText;

	private int hudWidth = 250;

	private int hudHeight = 60;

	public ActionableMouseOverHandler(PhysicsSystem clientPhysicsSystem)
	{
		super(clientPhysicsSystem);
	}

	@Override
	public void setConfig(Canvas3D canvas)
	{
		super.setConfig(canvas);
		currentActionTargetData = new CurrentActionTargetData();

		//remove old hudtext
		if (HUDText != null)
		{
			HUDText.removeFromCanvas();
		}

		// set up new canvas
		if (canvas3D != null)
		{
			HUDText = new HUDText((Canvas3D2D) canvas3D, new Rectangle((canvas3D.getWidth() / 2) - (hudWidth / 2),
					(canvas3D.getHeight() / 2) - (hudHeight / 2), hudWidth, hudHeight), 16);
		}

	}

	@Override
	public void doMouseReleased(MouseEvent e)
	{
		// the show info call below on the java3d behavior thread might change currentactionable on us
		synchronized (currentActionableMonitor)
		{
			if (currentActionTargetData != null && e.getButton() == MouseEvent.BUTTON1)
			{
				if (currentActionTargetData.currentActionable != null
						&& currentActionTargetData.currentActionable instanceof J3dRECOStatInst)
				{
					// if the mouse release listener is working we can't change the currentActionable until it's finished
					synchronized (currentActionableMonitor)
					{
						J3dRECOStatInst j3dRECOStatInst = (J3dRECOStatInst) currentActionTargetData.currentActionable;
						// sort out the actionable if  
						if (j3dRECOStatInst.getInstRECO() instanceof CommonREFR)
						{
							CommonREFR commonREFR = (CommonREFR) j3dRECOStatInst.getInstRECO();
							XTEL xtel = commonREFR.XTEL;
							if (xtel != null)
							{
								if (SimpleBethCellManager.simpleBethCellManager.changeToCellOfTarget(xtel.doorFormId))
								{
									SimpleBethCellManager.simpleBethCellManager.setLocation(xtel.x, xtel.y, xtel.z, xtel.rx, xtel.ry,
											xtel.rz);
								}
							}
							else
							{

								J3dRECOType j3dRECOType = j3dRECOStatInst.getJ3dRECOType();

								//possibly a door that needs opening/closing etc
								if (j3dRECOType instanceof J3dDOOR)
								{
									J3dDOOR j3dDOOR = (J3dDOOR) j3dRECOType;
									j3dDOOR.toggleOpen();

									clientPhysicsSystem.getClientPhysics().updateRECOToggleOpen(j3dRECOStatInst, j3dDOOR.isOpen());

									//also update physics view, but assume much
									J3dRECOInst phyJ3dInstRECO = null;
									if (SimpleBethCellManager.currentBethWorldVisualBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethWorldPhysicalBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}
									else if (SimpleBethCellManager.currentBethInteriorVisualBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethInteriorVisualBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}
									if (phyJ3dInstRECO != null)
									{
										J3dRECOStatInst phyJ3dRECOStatInst = (J3dRECOStatInst) phyJ3dInstRECO;
										J3dRECOType phyJ3dRECOType = phyJ3dRECOStatInst.getJ3dRECOType();
										J3dDOOR phyJ3dDOOR = (J3dDOOR) phyJ3dRECOType;
										phyJ3dDOOR.toggleOpen();
									}

								}
								else if (j3dRECOType instanceof J3dCONT)
								{
									J3dCONT j3dCONT = (J3dCONT) j3dRECOType;
									j3dCONT.setOpen(true);

									clientPhysicsSystem.getClientPhysics().updateRECOToggleOpen(j3dRECOStatInst, true);

									//also update physics view, but assume much
									J3dRECOInst phyJ3dInstRECO = null;
									if (SimpleBethCellManager.currentBethWorldVisualBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethWorldPhysicalBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}
									else if (SimpleBethCellManager.currentBethInteriorVisualBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethInteriorVisualBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}

									if (phyJ3dInstRECO != null)
									{
										J3dRECOStatInst phyJ3dRECOStatInst = (J3dRECOStatInst) phyJ3dInstRECO;
										J3dRECOType phyJ3dRECOType = phyJ3dRECOStatInst.getJ3dRECOType();
										J3dCONT phyJ3dCONT = (J3dCONT) phyJ3dRECOType;
										phyJ3dCONT.setOpen(true);
									}

									System.out.println("Big Fat container opening thingy now!");
								}
								//TODO: type   FLOR, MISC etc

							}
						}

					}
				}
			}
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

						if (recoId != currentActionTargetData.recoId)
						{
							currentActionTargetData.clear();
							currentActionTargetData.recoId = recoId;
						}

						J3dRECOInst j3dInstRECO = null;
						if (SimpleBethCellManager.currentBethWorldVisualBranch != null)
						{
							j3dInstRECO = SimpleBethCellManager.currentBethWorldVisualBranch.getJ3dInstRECO(recoId);
						}
						else if (SimpleBethCellManager.currentBethInteriorVisualBranch != null)
						{
							j3dInstRECO = SimpleBethCellManager.currentBethInteriorVisualBranch.getJ3dInstRECO(recoId);
						}

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
									currentActionTargetData.distance = MAX_MOUSE_RAY_DIST * rayCallback.closestHitFraction;

									if (xtel != null && xtel.doorFormId != 0)
									{
										if (currentActionTargetData.cellName == null)
										{
											currentActionTargetData.cellName = SimpleBethCellManager.simpleBethCellManager
													.getCellNameFormIdOf(xtel.doorFormId);
										}

										// if less than the max interact then set interactable
										// if not then set hudtext (in light grey) but don't allow actions
										if (currentActionTargetData.distance < INTERACT_MAX_DIST)
										{
											currentActionTargetData.hudText = "To " + currentActionTargetData.cellName;
											currentActionTargetData.currentActionable = j3dInstRECO;
										}
										else
										{
											currentActionTargetData.hudText = "To " + currentActionTargetData.cellName + " (dist)";
											currentActionTargetData.currentActionable = null; // nothing to action yet										
										}
									}
									else if (j3dRECOType instanceof J3dDOOR)
									{

										if (currentActionTargetData.distance < INTERACT_MAX_DIST)
										{
											currentActionTargetData.hudText = "Open/Close DOOR";
											currentActionTargetData.currentActionable = j3dInstRECO;
										}
										else
										{
											currentActionTargetData.hudText = "Open/Close DOOR (dist)";
											currentActionTargetData.currentActionable = null; // nothing to action yet										
										}
									}
									else if (j3dRECOType instanceof J3dCONT)
									{

										if (currentActionTargetData.distance < INTERACT_MAX_DIST)
										{
											currentActionTargetData.hudText = "Open Container";
											currentActionTargetData.currentActionable = j3dInstRECO;
										}
										else
										{
											currentActionTargetData.hudText = "Open Container (dist)";
											currentActionTargetData.currentActionable = null; // nothing to action yet										
										}
									}
									else
									{
										currentActionTargetData.clear();
									}
								}
							}
						}
						else
						{
							currentActionTargetData.clear();
						}
					}
					else
					{
						currentActionTargetData.clear();
					}
				}
				else
				{
					currentActionTargetData.clear();
				}
			}
			HUDText.setText(currentActionTargetData.hudText);
		}

	}

	@Override
	protected void screenResized()
	{
		HUDText.setLocation((canvas3D.getWidth() / 2) - (hudWidth / 2), (canvas3D.getHeight() / 2) - (hudHeight / 2));

	}

	private class CurrentActionTargetData
	{
		public String hudText = "";

		public int recoId;

		public J3dRECOInst currentActionable = null;

		public float distance = 999;

		public String cellName = "";

		public void clear()
		{
			hudText = "";
			currentActionable = null;
			distance = 999;
			cellName = null;
		}
	}
}