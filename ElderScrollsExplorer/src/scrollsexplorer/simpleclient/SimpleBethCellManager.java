package scrollsexplorer.simpleclient;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import scrollsexplorer.GameConfig;
import scrollsexplorer.ScrollsExplorer;
import scrollsexplorer.simpleclient.physics.InstRECOStore;
import tools3d.navigation.AvatarLocation;
import tools3d.utils.Utils3D;
import utils.ESConfig;
import utils.source.MediaSources;
import utils.source.TextureSource;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import esmLoader.common.PluginException;
import esmLoader.common.data.plugin.PluginRecord;
import esmLoader.common.data.plugin.PluginSubrecord;
import esmLoader.loader.IESMManager;
import esmLoader.tes3.ESMManagerTes3;
import esmj3d.data.shared.subrecords.LString;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dRECODynInst;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public class SimpleBethCellManager implements InstRECOStore
{
	//TODO: bad form only for ActionableMouseOverHandler
	public static BethWorldVisualBranch currentBethWorldVisualBranch;

	public static BethWorldPhysicalBranch currentBethWorldPhysicalBranch;

	public static BethInteriorVisualBranch currentBethInteriorVisualBranch;

	public static BethInteriorPhysicalBranch currentBethInteriorPhysicalBranch;

	//TODO: more bad form only for ActionableMouseOverHandler
	public static SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetup simpleWalkSetup;

	private AvatarLocation avatarLocation;

	private int currentCellFormId = -1;

	private J3dICellFactory j3dCellFactory;

	private IESMManager esmManager;

	public SimpleBethCellManager(SimpleWalkSetup simpleWalkSetup2)
	{
		this.simpleWalkSetup = simpleWalkSetup2;
		this.avatarLocation = simpleWalkSetup2.getAvatarLocation();
		simpleBethCellManager = this;
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
		this.esmManager = esmManager;
		j3dCellFactory = gameConfig.j3dCellFactory;
		j3dCellFactory.setSources(esmManager, esmManager, mediaSources);
	}

	public String getCellNameFormIdOf(int doorFormId)
	{
		int cellFormID = esmManager.getCellIdOfPersistentTarget(doorFormId);
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
						if (subrec.getSubrecordType().equals("FULL"))
							return new LString(subrec.getSubrecordData()).str;
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
	public boolean changeToCellOfTarget(int targetFormId)
	{
		int cellFormID = esmManager.getCellIdOfPersistentTarget(targetFormId);
		System.out.println("cellFormID " + cellFormID);
		if (cellFormID > 0)
		{
			setCurrentCellFormId(cellFormID);
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
	public boolean changeToCell(String str)
	{
		if (str == null)
			setCurrentCellFormId(0);
		else
			setCurrentCellFormId(convertNameRefToId(str));
		return true;
	}

	public void setCurrentCellFormId(int newCellFormId)
	{
		System.out.println("Moving to cell " + newCellFormId);
		if (currentCellFormId != -1 && currentCellFormId != newCellFormId)
		{
			System.out.println("unloading...");
			// unload current
			if (currentBethWorldVisualBranch != null)
			{
				currentBethWorldVisualBranch.unload();
				currentBethWorldVisualBranch.detach();
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

		try
		{
			// now load new
			if (currentCellFormId != -1)
			{
				System.out.println("loading...");
				ScrollsExplorer.dashboard.setCellLoading(1);
				PluginRecord cell = esmManager.getWRLD(currentCellFormId);
				if (cell != null)
				{
					currentBethWorldVisualBranch = new BethWorldVisualBranch(currentCellFormId, j3dCellFactory);
					if (avatarLocation != null)
					{
						currentBethWorldVisualBranch.init(avatarLocation.getTransform());
						avatarLocation.addAvatarLocationListener(currentBethWorldVisualBranch);
					}
					// notice init before making live to speed it up
					simpleWalkSetup.addToVisualBranch(currentBethWorldVisualBranch);

					currentBethWorldPhysicalBranch = new BethWorldPhysicalBranch(simpleWalkSetup.getPhysicsSystem(), currentCellFormId,
							j3dCellFactory);
					if (avatarLocation != null)
					{
						currentBethWorldPhysicalBranch.init(avatarLocation.getTransform());
						avatarLocation.addAvatarLocationListener(currentBethWorldPhysicalBranch);
					}
					simpleWalkSetup.addToPhysicalBranch(currentBethWorldPhysicalBranch);
				}
				else
				{
					//must be interior?
					cell = esmManager.getInteriorCELL(currentCellFormId);
					if (cell != null)
					{
						currentBethInteriorVisualBranch = new BethInteriorVisualBranch(currentCellFormId, cell.getEditorID(),
								j3dCellFactory);
						simpleWalkSetup.addToVisualBranch(currentBethInteriorVisualBranch);

						currentBethInteriorPhysicalBranch = new BethInteriorPhysicalBranch(simpleWalkSetup.getPhysicsSystem(),
								currentCellFormId, j3dCellFactory);
						simpleWalkSetup.addToPhysicalBranch(currentBethInteriorPhysicalBranch);

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
	}

	/**
	 * for TES3 conversions
	 * @param str
	 * @return
	 */
	private int convertNameRefToId(String str)
	{
		if (esmManager instanceof esmLoader.tes3.ESMManagerTes3)
		{
			ESMManagerTes3 esmManagerTes3 = (ESMManagerTes3) esmManager;

			return esmManagerTes3.convertNameRefToId(str);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public void setAvatarLocationListener(AvatarLocation avatarLocation)
	{
		this.avatarLocation = avatarLocation;
	}

	/*
	  * Create some Background geometry to use as
	  * a backdrop for the application. Here we create
	  * a Sphere that will enclose the entire scene and
	  * apply a texture image onto the inside of the Sphere
	  * to serve as a graphical backdrop for the scene.
	  */
	public static BranchGroup createBackground(TextureSource textureSource)
	{

		/*Background background = new Background();
		background.setApplicationBounds(Utils3D.defaultBounds);
		//background.setColor(new Color3f(1.0f, 1.0f, 1.0f));

		BranchGroup bgNifbg = new BranchGroup();
		NifFile nifFile = NifToJ3d.loadNiObjects("meshes\\sky\\stars.nif", meshSource);
		NiToJ3dData niToJ3dData = new NiToJ3dData(nifFile.blocks);
		for (NiObject no : nifFile.blocks)
		{
			if (no instanceof NiTriShape)
			{
				NiTriShape niTriShape = (NiTriShape) no;
				//J3dNiTriShape jnts = new J3dNiTriShape(niTriShape, niToJ3dData, textureSource);
				//bgNifbg.addChild(jnts);
			}
		}

		background.setGeometry(bgNifbg);

		BranchGroup bgbg = new BranchGroup();
		bgbg.addChild(background);*/

		// create a parent BranchGroup for the Background
		BranchGroup backgroundGroup = new BranchGroup();

		// create a new Background node
		Background back = new Background();

		// set the range of influence of the background
		back.setApplicationBounds(Utils3D.defaultBounds);

		// create a BranchGroup that will hold
		// our Sphere geometry
		BranchGroup bgGeometry = new BranchGroup();

		// create an appearance for the Sphere
		Appearance app = new Appearance();

		Texture tex = null;
		// load a texture image 		
		if (textureSource.textureFileExists("textures\\sky\\skyrimcloudsupper04.dds"))
		{
			tex = textureSource.getTexture("textures\\sky\\skyrimcloudsupper04.dds");
		}
		else if (textureSource.textureFileExists("textures\\sky\\cloudsclear.dds"))
		{
			tex = textureSource.getTexture("textures\\sky\\cloudsclear.dds");
		}
		else if (textureSource.textureFileExists("textures\\sky\\urbancloudovercastlower01.dds"))
		{
			tex = textureSource.getTexture("textures\\sky\\urbancloudovercastlower01.dds");
		}
		else if (textureSource.textureFileExists("textures\\tx_sky_clear.dds"))
		{
			tex = textureSource.getTexture("textures\\tx_sky_clear.dds");
		}
		else
		{
			System.out.println("BUM, no tex fro sky");
		}

		// apply the texture to the Appearance
		app.setTexture(tex);

		// create the Sphere geometry with radius 1.0.
		// we tell the Sphere to generate texture coordinates
		// to enable the texture image to be rendered
		// and because we are *inside* the Sphere we have to generate 
		// Normal coordinates inwards or the Sphere will not be visible.
		Sphere sphere = new Sphere(1.0f, Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS_INWARD, app);

		// start wiring everything together,

		// add the Sphere to its parent BranchGroup.
		bgGeometry.addChild(sphere);

		// assign the BranchGroup to the Background as geometry.
		back.setGeometry(bgGeometry);

		// add the Background node to its parent BranchGroup.
		backgroundGroup.addChild(back);

		return backgroundGroup;

	}

	public void setLocation(float x, float y, float z, float rx, float ry, float rz)
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

		simpleWalkSetup.warp(new Vector3f(x * ESConfig.ES_TO_METERS_SCALE, z * ESConfig.ES_TO_METERS_SCALE, -y
				* ESConfig.ES_TO_METERS_SCALE));
		Quat4f q = new Quat4f();
		Utils3D.safeGetQuat(transform, q);
		simpleWalkSetup.getAvatarLocation().setRotation(q);
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

}
