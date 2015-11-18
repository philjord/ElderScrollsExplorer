package scrollsexplorer.simpleclient;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import scrollsexplorer.GameConfig;
import scrollsexplorer.ScrollsExplorer;
import scrollsexplorer.simpleclient.physics.InstRECOStore;
import scrollsexplorer.simpleclient.scenegraph.LoadScreen;
import scrollsexplorer.simpleclient.scenegraph.SimpleSky;
import tools3d.navigation.AvatarLocation;
import utils.source.MediaSources;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dRECODynInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;
import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.common.data.plugin.PluginSubrecord;
import esmmanager.loader.IESMManager;
import esmmanager.tes3.ESMManagerTes3;

public class SimpleBethCellManager implements InstRECOStore
{
	//TODO: bad form only for ActionableMouseOverHandler
	public static BethWorldVisualBranch currentBethWorldVisualBranch;

	public static BethWorldPhysicalBranch currentBethWorldPhysicalBranch;

	public static BethInteriorVisualBranch currentBethInteriorVisualBranch;

	public static BethInteriorPhysicalBranch currentBethInteriorPhysicalBranch;

	private SimpleWalkSetup simpleWalkSetup;

	private AvatarLocation avatarLocation;

	private int currentCellFormId = -1;

	private J3dICellFactory j3dCellFactory;

	private IESMManager esmManager;

	private SimpleSky simpleSky;

	private LoadScreen loadScreen;

	// gate keeper of expensive change cell id call
	private boolean canChangeCell = true;

	public SimpleBethCellManager(SimpleWalkSetup simpleWalkSetup2)
	{
		this.simpleWalkSetup = simpleWalkSetup2;
		this.avatarLocation = simpleWalkSetup2.getAvatarLocation();
	}

	public void updateBranches()
	{
		if (currentBethWorldVisualBranch != null)
			currentBethWorldVisualBranch.updateFromCurrent();
		if (currentBethWorldPhysicalBranch != null)
			currentBethWorldPhysicalBranch.updateFromCurrent();
	}

	/**	
	 * @param meshSource
	 * @param textureSource
	 * @param soundSource
	 */
	public void setSources(GameConfig gameConfig, IESMManager esmManager, MediaSources mediaSources)
	{
		simpleWalkSetup.setVisualDisplayed(false);
		this.esmManager = esmManager;
		j3dCellFactory = gameConfig.j3dCellFactory;
		j3dCellFactory.setSources(esmManager, mediaSources);

		simpleSky = new SimpleSky(gameConfig, mediaSources.getTextureSource());
		simpleWalkSetup.getVisualBranch().addChild(simpleSky);

		loadScreen = new LoadScreen(gameConfig, mediaSources);
		simpleWalkSetup.getViewingPlatform().getPlatformGeometry().addChild(loadScreen);

		simpleWalkSetup.setVisualDisplayed(true);

	}

	public String getCellNameFormIdOf(int doorFormId)
	{
		int cellFormID = j3dCellFactory.getCellIdOfPersistentTarget(doorFormId);
		if (cellFormID != -1 && cellFormID != 0)
		{
			try
			{
				PluginRecord pr = esmManager.getWRLD(cellFormID);
				if (pr == null)
				{
					pr = esmManager.getInteriorCELL(cellFormID);
				}

				if (pr != null)
				{
					List<PluginSubrecord> subrecords = pr.getSubrecords();
					for (PluginSubrecord subrec : subrecords)
					{
						//TES5 has these FULLs as FormIDs, so test for 4 bytes
						if (subrec.getSubrecordType().equals("FULL") && subrec.getSubrecordData().length > 4)
						{
							return new LString(subrec.getSubrecordData()).str;
						}
					}
					return pr.getEditorID();

				}
			}
			catch (DataFormatException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (PluginException e)
			{
				e.printStackTrace();
			}
		}
		return "Unknown Cell";
	}

	/**
	 * ONLY for teleport, so MUST be persistent so only need check wrld and cell pers children
	 * @param targetFormId
	 * @return true if a cell was found and changed to
	 */
	public boolean changeToCellOfTarget(int targetFormId, Vector3f trans, Quat4f rot)
	{
		int cellFormID = j3dCellFactory.getCellIdOfPersistentTarget(targetFormId);
		if (cellFormID > 0)
		{
			setCurrentCellFormId(cellFormID, trans, rot);
			return true;
		}
		else
		{
			System.out.println("No persistence form of id " + targetFormId + ". So no cell change :(");
		}

		return false;
	}

	/**
	 * Use null to indicate morrowind
	 * @param str
	 * @return
	 */
	public boolean changeToCell(String str, Vector3f trans, Quat4f rot)
	{
		if (str == null)
			setCurrentCellFormId(0, trans, rot);
		else
			setCurrentCellFormId(convertNameRefToId(str), trans, rot);
		return true;
	}

	/**
	 * for TES3 conversions
	 * @param str
	 * @return
	 */
	private int convertNameRefToId(String str)
	{
		if (esmManager instanceof esmmanager.tes3.ESMManagerTes3)
		{
			ESMManagerTes3 esmManagerTes3 = (ESMManagerTes3) esmManager;
			return esmManagerTes3.convertNameRefToId(str);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public int getCurrentCellFormId()
	{
		return currentCellFormId;
	}

	public void setCurrentCellFormId(final int newCellFormId, final Vector3f trans, final Quat4f rot)
	{
		if (canChangeCell)
		{
			// use a new thread as generally the Awt thread is coming in and better to let it go
			Thread thread = new Thread()
			{
				public void run()
				{
					canChangeCell = false;

					simpleWalkSetup.setEnabled(false);
					simpleWalkSetup.setVisualDisplayed(false);
					showLoadScreen();

					//NOTE no structure thread as visual are not displayed now from call above (not live at all)

					System.out.println("Setting cell to ID:" + newCellFormId);
					if (currentCellFormId != -1 && currentCellFormId != newCellFormId)
					{
						System.out.println("unloading cell " + currentCellFormId + "...");
						// unload current
						if (currentBethWorldVisualBranch != null)
						{
							currentBethWorldVisualBranch.detach();
							currentBethWorldVisualBranch.unload();

							if (avatarLocation != null)
							{
								avatarLocation.removeAvatarLocationListener(currentBethWorldVisualBranch);
							}
							currentBethWorldVisualBranch = null;
						}
						if (currentBethWorldPhysicalBranch != null)
						{
							currentBethWorldPhysicalBranch.detach();
							if (avatarLocation != null)
							{
								avatarLocation.removeAvatarLocationListener(currentBethWorldPhysicalBranch);
							}
							currentBethWorldPhysicalBranch = null;
						}
						if (currentBethInteriorVisualBranch != null)
						{
							currentBethInteriorVisualBranch.detach();
							currentBethInteriorVisualBranch = null;
						}
						if (currentBethInteriorPhysicalBranch != null)
						{
							currentBethInteriorPhysicalBranch.detach();
							currentBethInteriorPhysicalBranch = null;
						}
					}
					currentCellFormId = newCellFormId;

					//update location to avoid double load as would happen if not done between unload and load	
					simpleWalkSetup.changeLocation(rot, trans);

					try
					{
						// now load new
						if (currentCellFormId != -1)
						{
							System.out.println("loading " + currentCellFormId + "...");
							ScrollsExplorer.dashboard.setCellLoading(1);
							PluginRecord cell = esmManager.getWRLD(currentCellFormId);
							if (cell != null)
							{
								// outside is light
								BethRenderSettings.setGlobalAmbLightLevel(50f / 100f);
								simpleWalkSetup.setGlobalAmbLightLevel(50f / 100f);
								BethRenderSettings.setGlobalDirLightLevel(75 / 100f);
								simpleWalkSetup.setGlobalDirLightLevel(75 / 100f);
								
								currentBethWorldVisualBranch = new BethWorldVisualBranch(currentCellFormId, j3dCellFactory);
								if (avatarLocation != null)
								{
									currentBethWorldVisualBranch.init(avatarLocation.getTransform());
									avatarLocation.addAvatarLocationListener(currentBethWorldVisualBranch);
								}
								// notice init before making live to speed it up
								simpleWalkSetup.getVisualBranch().addChild(currentBethWorldVisualBranch);

								currentBethWorldPhysicalBranch = new BethWorldPhysicalBranch(simpleWalkSetup.getPhysicsSystem(),
										currentCellFormId, j3dCellFactory);
								if (avatarLocation != null)
								{
									currentBethWorldPhysicalBranch.init(avatarLocation.getTransform());
									avatarLocation.addAvatarLocationListener(currentBethWorldPhysicalBranch);
								}
								simpleWalkSetup.getPhysicalBranch().addChild(currentBethWorldPhysicalBranch);
							}
							else
							{
								//must be interior?
								// inside is dim
								BethRenderSettings.setGlobalAmbLightLevel(30f / 100f);
								simpleWalkSetup.setGlobalAmbLightLevel(15f / 100f);
								
								cell = esmManager.getInteriorCELL(currentCellFormId);
								if (cell != null)
								{
									currentBethInteriorVisualBranch = new BethInteriorVisualBranch(currentCellFormId, cell.getEditorID(),
											j3dCellFactory);
									simpleWalkSetup.getVisualBranch().addChild(currentBethInteriorVisualBranch);

									currentBethInteriorPhysicalBranch = new BethInteriorPhysicalBranch(simpleWalkSetup.getPhysicsSystem(),
											currentCellFormId, j3dCellFactory);
									simpleWalkSetup.getPhysicalBranch().addChild(currentBethInteriorPhysicalBranch);

									if (avatarLocation != null)
									{
										//TODO: the unload load part of this should still be called I think
										//currentBethInteriorPhysicalBranch.init(avatarLocation.getTransform());
										//avatarLocation.addAvatarLocationListener(currentBethInteriorPhysicalBranch);
									}
								}
								else
								{
									System.out.println("unknown cell id " + currentCellFormId);

								}
							}
							ScrollsExplorer.dashboard.setCellLoading(-1);
						}
					}
					catch (DataFormatException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					catch (PluginException e)
					{
						e.printStackTrace();
					}
					simpleWalkSetup.setVisualDisplayed(true);
					simpleWalkSetup.setEnabled(true);

					dropLoadScreen();

					canChangeCell = true;
				}
			};
			thread.start();
		}
	}

	public void setAvatarLocationListener(AvatarLocation avatarLocation)
	{
		this.avatarLocation = avatarLocation;
	}

	@Override
	public void applyUpdate(J3dRECOInst instReco, Quat4f newRotation, Vector3f newTranslation)
	{
		//Phil note, I think this is the physics version of this inst reco (for rendering red lines)
		if (instReco instanceof J3dRECODynInst)
		{
			J3dRECODynInst dynInst = ((J3dRECODynInst) instReco);
			Transform3D t = new Transform3D(newRotation, newTranslation, 1f);

			dynInst.setLocation(t);

			// must find teh visual equiv and updte it's root trasnforms
			if (currentBethWorldVisualBranch != null)
			{
				J3dRECODynInst wv = (J3dRECODynInst) currentBethWorldVisualBranch.getJ3dInstRECO(instReco.getRecordId());
				if (wv != null)
					wv.setLocation(t);
			}
			else if (currentBethInteriorVisualBranch != null)
			{
				J3dRECODynInst iv = (J3dRECODynInst) currentBethInteriorVisualBranch.getJ3dInstRECO(instReco.getRecordId());
				if (iv != null)
					iv.setLocation(t);
			}
		}
		else
		{
			//System.out.println("do somethig here? " + instReco);
		}
	}

	public void showLoadScreen()
	{
		simpleSky.setShowSky(false);
		loadScreen.setShowLoadScreen(true);
	}

	public void dropLoadScreen()
	{
		simpleSky.setShowSky(true);
		loadScreen.setShowLoadScreen(false);
	}

}
