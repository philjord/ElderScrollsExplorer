package scrollsexplorer.simpleclient.scenegraph;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import nif.NifJ3dVisRoot;
import nif.NifToJ3d;
import nif.j3d.J3dNiAVObject;
import utils.source.MediaSources;

public class LoadScreen extends BranchGroup
{
	private MediaSources mediaSources;

	private BranchGroup currentLoadScreenBG;

	private TransformGroup currentLoadScreenTG;

	public LoadScreen(MediaSources mediaSources)
	{
		this.mediaSources = mediaSources;
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	}

	public void setShowLoadScreen(boolean show, Transform3D transform3d)
	{
		if (!show)
		{
			if (currentLoadScreenBG != null)
			{
				currentLoadScreenBG.detach();
				currentLoadScreenBG = null;
			}
		}
		else
		{
			currentLoadScreenBG = new BranchGroup();
			currentLoadScreenBG.setCapability(BranchGroup.ALLOW_DETACH);

			currentLoadScreenTG = new TransformGroup();
			currentLoadScreenTG.setTransform(transform3d);

			currentLoadScreenBG.addChild(currentLoadScreenTG);

			if (!loadScene("meshes\\i\\in_v_arena_01.nif"))
				if (!loadScene("meshes\\i\\in_de_shipwreckul_lg.nif"))
					if (!loadScene("meshes\\i\\in_dae_hall_l_stair_curve_01.nif"))
						if (!loadScene("meshes\\dungeons\\chargen\\prisonhall04.nif"))
							if (!loadScene("meshes\\dungeons\\cloudrulertemple\\testcloudrulerint.nif"))
								if (!loadScene("meshes\\dungeons\\cathedral\\cathedralstenintback01.nif"))
									if (!loadScene("meshes\\interface\\loading\\loadinganim01.nif"))
										loadScene("meshes\\loadscreenart\\loadscreenadventure01.nif");

			//morrowind
			//meshes\i\in_v_arena.nif
			//meshes\i\in_de_shipwreckul_lg.nif
			//meshes\i\in_dae_hall_l_stair_curve_01.nif

			// obliv
			//meshes\dungeons\chargen\prisonhall04.nif
			//meshes\dungeons\cloudrulertemple\testcloudrulerint.nif
			//meshes\dungeons\cathedral\cathedralstenintback01.nif

			//fallout + NV
			//meshes\interface\loading\loadinganim01.nif

			//skyrim
			// all from meshes\loadscreenart  with black background

			//TODO: note not on strucutre behavior, trouble?
			addChild(currentLoadScreenBG);
		}

	}

	private boolean loadScene(String nifFile)
	{
		if (mediaSources.getMeshSource().nifFileExists(nifFile))
		{
			NifJ3dVisRoot nvr = NifToJ3d.loadShapes(nifFile, mediaSources.getMeshSource(), mediaSources.getTextureSource());
			if (nvr != null)
			{
				J3dNiAVObject j3dNiAVObject = nvr.getVisualRoot();

				if (j3dNiAVObject != null)
				{
					currentLoadScreenTG.addChild(j3dNiAVObject);
					return true;
				}
			}
		}

		return false;
	}
}
