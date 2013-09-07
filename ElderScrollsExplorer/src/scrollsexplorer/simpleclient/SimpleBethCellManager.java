package scrollsexplorer.simpleclient;

import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import tools3d.navigation.AvatarLocation;
import tools3d.utils.Utils3D;
import utils.ESConfig;
import utils.source.MeshSource;
import utils.source.SoundSource;
import utils.source.TextureSource;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import esmLoader.common.PluginException;
import esmLoader.common.data.plugin.PluginRecord;
import esmLoader.loader.ESMManager;
import esmj3d.j3d.cell.J3dICellFactory;

public class SimpleBethCellManager
{
	//TODO: bad form only for ActionableMouseOverHandler
	public static BethWorldVisualBranch currentBethWorldVisualBranch;

	//TODO: more bad form only for ActionableMouseOverHandler
	public static SimpleBethCellManager simpleBethCellManager;

	private SimpleWalkSetup simpleWalkSetup;

	private AvatarLocation avatarLocation;

	private int currentCellFormId = -1;

	private BethWorldPhysicalBranch currentBethWorldPhysicalBranch;

	private BethInteriorVisualBranch currentBethInteriorVisualBranch;

	private BethInteriorPhysicalBranch currentBethInteriorPhysicalBranch;

	private J3dICellFactory j3dCellFactory;

	private ESMManager esmManager;

	private MeshSource meshSource;

	private TextureSource textureSource;

	private SoundSource soundSource;

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
	public void setSources(ESMManager esmManager, MeshSource meshSource, TextureSource textureSource, SoundSource soundSource)
	{
		this.esmManager = esmManager;
		this.meshSource = meshSource;
		this.textureSource = textureSource;
		this.soundSource = soundSource;

		float version = esmManager.getVersion();

		if (version == 0.94f)
		{
			if (esmManager.getName().equals("Skyrim.esm"))
			{
				j3dCellFactory = new esmj3dtes5.j3d.cell.J3dCellFactory(esmManager, esmManager, meshSource, textureSource, soundSource);
			}
			else
			{

				j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager, esmManager, meshSource, textureSource, soundSource);
			}
		}
		else if (version == 1.32f)
		{
			j3dCellFactory = new esmj3dfo3.j3d.cell.J3dCellFactory(esmManager, esmManager, meshSource, textureSource, soundSource);
		}
		else if (version == 1.0f || version == 0.8f)
		{
			j3dCellFactory = new esmj3dtes4.j3d.cell.J3dCellFactory(esmManager, esmManager, meshSource, textureSource, soundSource);
		}
		else
		{
			System.out.println("Bad esm version! " + version + " in " + esmManager.getName());
		}

		System.out.println("j3dCellFactory = " + j3dCellFactory);
	}

	/**
	 * ONLY for teleport, so MUST be persistent so only need check wrld and cell pers children
	 * @param formInNewCellId
	 */
	public void setCurrentCellFormIdOf(int formInNewCellId)
	{
		int cellFormID = esmManager.getCellFormIdForPersistenetFormID(formInNewCellId);
		if (cellFormID != -1)
		{
			setCurrentCellFormId(cellFormID);
		}
	}

	public void setCurrentCellFormId(int newCellFormId)
	{

		//tODO: On change persistent cells seem to have bad gridspaces?
		System.out.println("Moving to cell " + newCellFormId);
		if (currentCellFormId != -1 && currentCellFormId != newCellFormId)
		{
			System.out.println("unloading...");
			// unload current
			if (currentBethWorldVisualBranch != null)
			{
				currentBethWorldVisualBranch.detach();
				if (avatarLocation != null)
				{
					avatarLocation.removeAvatarLocationListener(currentBethWorldVisualBranch);
				}
			}
			if (currentBethWorldPhysicalBranch != null)
			{
				currentBethWorldPhysicalBranch.detach();
				if (avatarLocation != null)
				{
					avatarLocation.removeAvatarLocationListener(currentBethWorldPhysicalBranch);
				}
			}
			if (currentBethInteriorVisualBranch != null)
			{
				currentBethInteriorVisualBranch.detach();
			}
			if (currentBethInteriorPhysicalBranch != null)
			{
				currentBethInteriorPhysicalBranch.detach();
			}
		}
		currentCellFormId = newCellFormId;

		try
		{

			// now load new
			if (currentCellFormId != -1)
			{
				System.out.println("loading...");
				PluginRecord cell = esmManager.getWRLD(currentCellFormId);
				if (cell != null)
				{
					//add skynow
					simpleWalkSetup.addToVisualBranch(createBackground());

					currentBethWorldVisualBranch = new BethWorldVisualBranch(currentCellFormId, cell.getEditorID(), j3dCellFactory);
					simpleWalkSetup.addToVisualBranch(currentBethWorldVisualBranch);
					if (avatarLocation != null)
					{
						currentBethWorldVisualBranch.init(avatarLocation.getTransform());
						avatarLocation.addAvatarLocationListener(currentBethWorldVisualBranch);
					}

					currentBethWorldPhysicalBranch = new BethWorldPhysicalBranch(simpleWalkSetup.getClientPhysicsSystem(),
							currentCellFormId, j3dCellFactory);
					simpleWalkSetup.addToPhysicalBranch(currentBethWorldPhysicalBranch);
					if (avatarLocation != null)
					{
						currentBethWorldPhysicalBranch.init(avatarLocation.getTransform());
						avatarLocation.addAvatarLocationListener(currentBethWorldPhysicalBranch);
					}

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

						currentBethInteriorPhysicalBranch = new BethInteriorPhysicalBranch(simpleWalkSetup.getClientPhysicsSystem(),
								currentCellFormId, j3dCellFactory);
						simpleWalkSetup.addToPhysicalBranch(currentBethInteriorPhysicalBranch);
					}
					else
					{
						System.out.println("unknown cell id " + currentCellFormId);

					}

				}

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
	public BranchGroup createBackground()
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
		transform.get(q);
		simpleWalkSetup.getAvatarLocation().setRotation(q);
	}
}
