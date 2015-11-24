package test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import utils.ESMUtils;
import utils.source.EsmSoundKeyToName;
import utils.source.MediaSources;
import bsa.BSAFileSet;
import bsa.source.BsaMeshSource;
import bsa.source.BsaSoundSource;
import bsa.source.BsaTextureSource;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.universe.SimpleUniverse;

import esmj3dfo3.data.RecordToRECO;
import esmmanager.EsmFileLocations;
import esmmanager.common.PluginException;
import esmmanager.common.data.plugin.FormInfo;
import esmmanager.common.data.plugin.PluginGroup;
import esmmanager.common.data.plugin.PluginRecord;
import esmmanager.common.data.record.Record;
import esmmanager.loader.CELLPointer;
import esmmanager.loader.ESMManager;
import esmmanager.loader.InteriorCELLTopGroup;
import esmmanager.loader.WRLDTopGroup;

public class ESMTest
{
	private static SimpleUniverse universe;

	private static TransformGroup transformGroup = new TransformGroup();

	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Pick cell to load, 0 == all cells, < 0 load full esm to RECO stage");

		frame.setSize(400, 400);
		final JSpinner numberSpinner = new JSpinner();

		frame.getContentPane().setLayout(new GridLayout(-1, 1));
		frame.getContentPane().add(numberSpinner);
		JButton okButton = new JButton("Select");
		frame.getContentPane().add(okButton);
		frame.setVisible(true);

		setUpUniverseAndCanvas();

		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int cellformid = (Integer) numberSpinner.getValue();
				if (cellformid < 0)
				{
					loadAllRECO();
				}
				else
				{
					loadCell(cellformid);
				}
			}
		});

	}

	private static void loadAllRECO()
	{
		long start = System.currentTimeMillis();

		String esmFile = EsmFileLocations.getGeneralEsmFile();
		ESMManager esmManager = (ESMManager) ESMManager.getESMManager(esmFile);

		try
		{
			for (FormInfo formInfo : esmManager.getFormMap().values())
			{
				//	System.out.println("stat " + formInfo.getSource());
				Record rec = new Record(formInfo.getPluginRecord());
				//TODO: non cell reco converstion
				RecordToRECO.makeRECO(rec);
			}

			for (InteriorCELLTopGroup interiorCELLTopGroup : esmManager.getInteriorCELLTopGroups())
			{
				for (CELLPointer cp : interiorCELLTopGroup.interiorCELLByFormId.values())
				{
					PluginRecord record = esmManager.getInteriorCELL(cp.formId);
					if (record != null)
					{
						PluginGroup cellChildren = esmManager.getInteriorCELLChildren(cp.formId);
						if (cellChildren != null)
						{
							RecordToRECO.makeRECOsForCELL(esmManager, new Record(record),
									ESMUtils.getChildren(cellChildren, PluginGroup.CELL_TEMPORARY));
						}
					}

				}
			}
			for (WRLDTopGroup wRLDTopGroup : esmManager.getWRLDTopGroups())
			{
				for (CELLPointer cp : wRLDTopGroup.WRLDExtBlockCELLByFormId.values())
				{
					PluginRecord record = esmManager.getWRLDExtBlockCELL(cp.formId);
					if (record != null)
					{
						PluginGroup cellChildren = esmManager.getWRLDExtBlockCELLChildren(cp.formId);
						if (cellChildren != null)
						{
							RecordToRECO.makeRECOsForCELL(esmManager, new Record(record),
									ESMUtils.getChildren(cellChildren, PluginGroup.CELL_TEMPORARY));
						}
					}
				}
			}
		}
		catch (PluginException e)
		{
			e.printStackTrace();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Done in " + (System.currentTimeMillis() - start));

	}

	private static void loadCell(int cellformid)
	{
		String esmFile = EsmFileLocations.getGeneralEsmFile();

		ESMManager esmManager = (ESMManager) ESMManager.getESMManager(esmFile);
		BSAFileSet bsaFileSet = new BSAFileSet(esmFile, false, false);

		BsaMeshSource ms = new BsaMeshSource(bsaFileSet.getMeshArchives());
		BsaTextureSource ts = new BsaTextureSource(bsaFileSet.getTextureArchives());
		BsaSoundSource ss = new BsaSoundSource(bsaFileSet.getSoundArchives(), new EsmSoundKeyToName(esmManager));

		MediaSources mediaSources = new MediaSources(ms, ts, ss);

		if (cellformid == 0)
		{
			loadAllFullJ3d();
		}
		else
		{
			System.out.println("I am gonna load " + cellformid);

			for (InteriorCELLTopGroup interiorCELLTopGroup : esmManager.getInteriorCELLTopGroups())
			{
				if (interiorCELLTopGroup.interiorCELLByFormId.containsKey(cellformid))
				{
					System.out.println("loading " + cellformid);
					BranchGroup bg = ESMToJ3d.makeBGCELL(esmManager, PluginGroup.CELL_TEMPORARY, cellformid, false, mediaSources);
					transformGroup.removeAllChildren();
					Runtime.getRuntime().gc();
					transformGroup.addChild(bg);
					System.out.println(cellformid + " done");
					return;
				}
			}
			for (WRLDTopGroup wRLDTopGroup : esmManager.getWRLDTopGroups())
			{
				if (wRLDTopGroup.WRLDExtBlockCELLByFormId.containsKey(cellformid))
				{
					CELLPointer cp = wRLDTopGroup.WRLDExtBlockCELLByFormId.get(cellformid);
					System.out.println("loading " + cp.formId);
					BranchGroup bg = ESMToJ3d.makeBGCELL(esmManager, PluginGroup.CELL_TEMPORARY, cp.formId, false, mediaSources);
					transformGroup.removeAllChildren();
					Runtime.getRuntime().gc();
					transformGroup.addChild(bg);
					System.out.println(cp.formId + " done");

					return;
				}
			}
			System.out.println("cell id " + cellformid + " is not in the loaded esm :(");

		}

	}

	private static void loadAllFullJ3d()
	{
		String esmFile = EsmFileLocations.getGeneralEsmFile();
		ESMManager esmManager = (ESMManager) ESMManager.getESMManager(esmFile);
		BSAFileSet bsaFileSet = new BSAFileSet(esmFile, false, false);
		BsaSoundSource ss = new BsaSoundSource(bsaFileSet.getSoundArchives(), new EsmSoundKeyToName(esmManager));
		BsaTextureSource ts = new BsaTextureSource(bsaFileSet.getTextureArchives());
		BsaMeshSource ms = new BsaMeshSource(bsaFileSet.getMeshArchives());

		MediaSources mediaSources = new MediaSources(ms, ts, ss);
		//01002FCE is the bridge from the zeta esm 16789454

		// 00024512 Vault101a
		for (InteriorCELLTopGroup interiorCELLTopGroup : esmManager.getInteriorCELLTopGroups())
		{
			for (Integer formId : interiorCELLTopGroup.interiorCELLByFormId.keySet())
			{
				System.out.println("loading " + formId);
				BranchGroup bg = ESMToJ3d.makeBGCELL(esmManager, PluginGroup.CELL_TEMPORARY, formId, false, mediaSources);
				transformGroup.removeAllChildren();
				transformGroup.addChild(bg);

				System.out.println(formId + " done");
			}
		}
		for (WRLDTopGroup wRLDTopGroup : esmManager.getWRLDTopGroups())
		{
			for (CELLPointer cp : wRLDTopGroup.WRLDExtBlockCELLByFormId.values())
			{
				System.out.println("loading " + cp.formId);
				BranchGroup bg = ESMToJ3d.makeBGCELL(esmManager, PluginGroup.CELL_TEMPORARY, cp.formId, false, mediaSources);
				transformGroup.removeAllChildren();
				Runtime.getRuntime().gc();
				transformGroup.addChild(bg);
				System.out.println(cp.formId + " done");
			}
		}

	}

	private static void setUpUniverseAndCanvas()
	{
		universe = new SimpleUniverse();

		SwingUtilities.getWindowAncestor(universe.getCanvas()).setSize(600, 600);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		transformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		transformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

		// Create ambient light	and add it
		Color3f alColor = new Color3f(1f, 1f, 1f);
		AmbientLight ambLight = new AmbientLight(true, alColor);
		ambLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		ambLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));

		BranchGroup bg = new BranchGroup();
		bg.addChild(transformGroup);
		bg.addChild(ambLight);

		MouseRotate mr = new MouseRotate(transformGroup);
		mr.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		mr.setEnable(true);
		bg.addChild(mr);

		universe.addBranchGraph(bg);

		setEye();

		universe.getViewer().getView().setBackClipDistance(5000);

		universe.getCanvas().addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getWheelRotation() < 0)
				{
					zoomIn();
				}
				else
				{
					zoomOut();
				}
			}
		});

		universe.getCanvas().addKeyListener(new KeyAdapter()
		{

			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_W)
				{
					eye.z = eye.z - 10;
					center.z = center.z - 10;
				}
				else if (e.getKeyCode() == KeyEvent.VK_S)
				{
					eye.z = eye.z + 10;
					center.z = center.z + 10;
				}
				else if (e.getKeyCode() == KeyEvent.VK_A)
				{
					eye.x = eye.x - 10;
					center.x = center.x - 10;
				}
				else if (e.getKeyCode() == KeyEvent.VK_D)
				{
					eye.x = eye.x + 10;
					center.x = center.x + 10;
				}
				else if (e.getKeyCode() == KeyEvent.VK_E)
				{
					center.z = center.z - 3;
					center.y = -center.z / 20;
				}
				else if (e.getKeyCode() == KeyEvent.VK_C)
				{
					center.z = center.z + 3;
					center.y = -center.z / 20;
				}

				setEye();
			}

		});

	}

	static Point3d eye = new Point3d(0, 1000, 0);

	static Point3d center = new Point3d(0, 0, 0);

	private static void zoomOut()
	{
		eye.y = eye.y * 1.1d;
		setEye();
	}

	private static void zoomIn()
	{
		eye.y = eye.y * 0.9d;
		setEye();

	}

	private static void setEye()
	{
		TransformGroup tg = universe.getViewingPlatform().getViewPlatformTransform();
		Transform3D t = new Transform3D();
		t.lookAt(eye, center, new Vector3d(0, 0, -1));
		t.invert();
		tg.setTransform(t);
	}

}