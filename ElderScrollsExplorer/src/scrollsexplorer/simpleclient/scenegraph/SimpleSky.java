package scrollsexplorer.simpleclient.scenegraph;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Texture;

import scrollsexplorer.GameConfig;
import tools3d.utils.Utils3D;
import utils.source.TextureSource;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

public class SimpleSky extends BranchGroup
{
	private GameConfig gameConfig;

	private BranchGroup backSkyBG = new BranchGroup();

	/**
	  * Create some Background geometry to use as
	  * a backdrop for the application. Here we create
	  * a Sphere that will enclose the entire scene and
	  * apply a texture image onto the inside of the Sphere
	  * to serve as a graphical backdrop for the scene.
	  */
	public SimpleSky(GameConfig gameConfig1, TextureSource textureSource)
	{
		this.gameConfig = gameConfig1;

		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		backSkyBG.setCapability(BranchGroup.ALLOW_DETACH);

		// create a new Background node
		Background backSky = new Background();

		// set the range of influence of the background
		backSky.setApplicationBounds(Utils3D.defaultBounds);

		// create a BranchGroup that will hold
		// our Sphere geometry
		BranchGroup bgGeometry = new BranchGroup();

		// create an appearance for the Sphere
		Appearance app = new Appearance();

		Texture tex = null;
		// load a texture image 		
		tex = textureSource.getTexture(gameConfig.skyTexture);

		// apply the texture to the Appearance
		app.setTexture(tex);

		app.setRenderingAttributes(new RenderingAttributes());

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
		backSky.setGeometry(bgGeometry);

		// add the Background node to its parent BranchGroup.
		backSkyBG.addChild(backSky);

		addChild(backSkyBG);

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

	}

	public void setShowSky(boolean showSky)
	{
		//TODO: note ! not on structure behavior, trouble?
		if (showSky)
		{
			if (!backSkyBG.isLive())
			{
				addChild(backSkyBG);
			}
		}
		else
		{
			if (backSkyBG.isLive())
			{
				backSkyBG.detach();
			}
		}

	}
}