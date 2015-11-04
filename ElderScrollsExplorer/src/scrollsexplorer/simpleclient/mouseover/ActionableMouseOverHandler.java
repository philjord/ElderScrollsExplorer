package scrollsexplorer.simpleclient.mouseover;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import nifbullet.BulletNifModel;
import nifbullet.NBRigidBody;
import nifbullet.cha.NifBulletChar;
import scrollsexplorer.simpleclient.SimpleBethCellManager;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.hud.hudelements.HUDText;
import tools3d.utils.Utils3D;
import utils.ESConfig;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;

import esmj3d.data.shared.records.CommonREFR;
import esmj3d.data.shared.records.GenericCONT;
import esmj3d.data.shared.records.GenericDOOR;
import esmj3d.data.shared.subrecords.XTEL;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOStatInst;
import esmj3d.j3d.j3drecords.type.J3dCONT;
import esmj3d.j3d.j3drecords.type.J3dDOOR;
import esmj3d.j3d.j3drecords.type.J3dRECOType;

public class ActionableMouseOverHandler extends MouseOverHandler
{
	public static final float INTERACT_MAX_DIST = 2.6f;

	private SimpleBethCellManager simpleBethCellManager;

	private CurrentActionTargetData currentActionTargetData = new CurrentActionTargetData();

	private static Object currentActionableMonitor = new Object();

	private HUDText HUDText;

	private int hudWidth = 300;

	private int hudHeight = 60;

	public ActionableMouseOverHandler(PhysicsSystem clientPhysicsSystem, SimpleBethCellManager simpleBethCellManager)
	{
		super(clientPhysicsSystem);
		this.simpleBethCellManager = simpleBethCellManager;
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

			if (!canvas3D.getView().getCompatibilityModeEnable())
			{
				HUDText = new HUDText((Canvas3D2D) canvas3D, new Rectangle((canvas3D.getWidth() / 2) - (hudWidth / 2),
						(canvas3D.getHeight() / 2) - (hudHeight / 2), hudWidth, hudHeight), 16);
			}
			else
			{
				HUDText = new HUDText((Canvas3D2D) canvas3D, new Rectangle(0, 80, hudWidth, hudHeight), 16);
			}
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
								Vector3f t = getTrans(xtel.x, xtel.y, xtel.z);
								// TODO: for now lift up, but when pelvis set right stop this
								t.y += 0.75;
								Quat4f r = getRot(xtel.rx, xtel.ry, xtel.rz);

								if (xtel.doorFormId != 0)
								{
									simpleBethCellManager.changeToCellOfTarget(xtel.doorFormId, t, r);
								}
								//TES3 won't have formId set yet
								else if (commonREFR instanceof esmj3dtes3.data.records.REFR)
								{
									esmj3dtes3.data.records.REFR refr = (esmj3dtes3.data.records.REFR) commonREFR;
									// DNAM is the target cell name
									if (refr.DNAM != null)
									{
										simpleBethCellManager.changeToCell(refr.DNAM.str, t, r);
									}
									else
									{
										simpleBethCellManager.changeToCell(null, t, r);
									}
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

									clientPhysicsSystem.getPhysicsLocaleDynamics().updateRECOToggleOpen(j3dRECOStatInst, j3dDOOR.isOpen());

									//also update physics view, but assume much
									J3dRECOInst phyJ3dInstRECO = null;
									if (SimpleBethCellManager.currentBethWorldPhysicalBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethWorldPhysicalBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}
									else if (SimpleBethCellManager.currentBethInteriorPhysicalBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethInteriorPhysicalBranch
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

									clientPhysicsSystem.getPhysicsLocaleDynamics().updateRECOToggleOpen(j3dRECOStatInst, true);

									//also update physics view, but assume much
									J3dRECOInst phyJ3dInstRECO = null;
									if (SimpleBethCellManager.currentBethWorldPhysicalBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethWorldPhysicalBranch
												.getJ3dInstRECO(j3dRECOStatInst.getRecordId());
									}
									else if (SimpleBethCellManager.currentBethInteriorPhysicalBranch != null)
									{
										phyJ3dInstRECO = SimpleBethCellManager.currentBethInteriorPhysicalBranch
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

					// might be one of 2 ways to get the model out of the user pointer
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

									if (j3dRECOType instanceof J3dDOOR)
									{
										J3dDOOR j3dDOOR = (J3dDOOR) j3dRECOType;
										GenericDOOR genericDOOR = (GenericDOOR) j3dDOOR.getRECO();

										if (xtel != null)
										{
											if (xtel.doorFormId != 0)
											{
												if (currentActionTargetData.cellName == null)
												{
													currentActionTargetData.cellName = simpleBethCellManager
															.getCellNameFormIdOf(xtel.doorFormId);
												}

												// if less than the max interact then set interactable
												// if not then set hudtext (in light grey) but don't allow actions
												currentActionTargetData.hudText = "To " + currentActionTargetData.cellName;
												if (currentActionTargetData.distance < INTERACT_MAX_DIST)
													currentActionTargetData.currentActionable = j3dInstRECO;
												else
													currentActionTargetData.currentActionable = null;
											}
											else if (commonREFR instanceof esmj3dtes3.data.records.REFR)
											{
												esmj3dtes3.data.records.REFR refr = (esmj3dtes3.data.records.REFR) commonREFR;

												if (refr.DNAM != null)
												{ // DNAM is the target cell name
													currentActionTargetData.hudText = "To " + refr.DNAM.str;
												}
												else
												{
													//DOOR with null DNAM mean take me to morrowind
													currentActionTargetData.hudText = "Travel to Morrowind";
												}
												if (currentActionTargetData.distance < INTERACT_MAX_DIST)
													currentActionTargetData.currentActionable = j3dInstRECO;
												else
													currentActionTargetData.currentActionable = null;
											}
										}
										else
										{

											String ext = "";
											if (genericDOOR.FULL != null)
												ext = " " + genericDOOR.FULL.str;
											currentActionTargetData.hudText = (j3dDOOR.isOpen() ? "Close" : "Open") + ext;

											if (currentActionTargetData.distance < INTERACT_MAX_DIST)
												currentActionTargetData.currentActionable = j3dInstRECO;
											else
												currentActionTargetData.currentActionable = null; // nothing to action yet										
										}
									}
									else if (j3dRECOType instanceof J3dCONT)
									{
										J3dCONT j3dCONT = (J3dCONT) j3dRECOType;
										GenericCONT genericCONT = (GenericCONT) j3dCONT.getRECO();

										String ext = " container";
										if (genericCONT.FULL != null)
											ext = " " + genericCONT.FULL.str;
										currentActionTargetData.hudText = "Look in" + ext;

										if (currentActionTargetData.distance < INTERACT_MAX_DIST)
											currentActionTargetData.currentActionable = j3dInstRECO;
										else
											currentActionTargetData.currentActionable = null; // nothing to action yet										
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
			if (currentActionTargetData.currentActionable == null)
				HUDText.setTextGreyed(currentActionTargetData.hudText);
			else
				HUDText.setText(currentActionTargetData.hudText);
		}

	}

	@Override
	protected void screenResized()
	{
		if (!canvas3D.getView().getCompatibilityModeEnable())
			HUDText.setLocation((canvas3D.getWidth() / 2) - (hudWidth / 2), (canvas3D.getHeight() / 2) - (hudHeight / 2));
		else
			HUDText.setLocation(0, 80);
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

	private static Vector3f getTrans(float x, float y, float z)
	{
		return new Vector3f(x * ESConfig.ES_TO_METERS_SCALE, z * ESConfig.ES_TO_METERS_SCALE, -y * ESConfig.ES_TO_METERS_SCALE);
	}

	private static Quat4f getRot(float rx, float ry, float rz)
	{
		Transform3D transform = new Transform3D();

		Transform3D xrotT = new Transform3D();
		xrotT.rotX(-rx);
		Transform3D zrotT = new Transform3D();
		zrotT.rotZ(ry);
		Transform3D yrotT = new Transform3D();
		yrotT.rotY(-rz);

		xrotT.mul(zrotT);
		xrotT.mul(yrotT);

		transform.set(xrotT);

		Quat4f q = new Quat4f();
		Utils3D.safeGetQuat(transform, q);
		return q;
	}
}