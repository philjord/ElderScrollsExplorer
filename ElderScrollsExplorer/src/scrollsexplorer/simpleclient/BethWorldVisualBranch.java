package scrollsexplorer.simpleclient;


import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import scrollsexplorer.ScrollsExplorer;
import tools.QueuingThread;
import tools3d.utils.scenegraph.LocationUpdateListener;
import tools3d.utils.scenegraph.StructureUpdateBehavior;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.cell.Beth32LodManager;
import esmj3d.j3d.cell.Beth32_4LodManager;
import esmj3d.j3d.cell.BethLodManager;
import esmj3d.j3d.cell.BethNoLodManager;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.cell.J3dICELLPersistent;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

/**

 * 
 * @author phil
 *
 */
public class BethWorldVisualBranch extends BranchGroup implements LocationUpdateListener
{
	private int worldFormId;

	private J3dICELLPersistent j3dCELLPersistent;

	private boolean isWRLD = true; // false implies interior cell

	private Vector3f lastUpdatedTranslation = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

	private HashMap<Point, J3dCELLGeneral> loadedNears = new HashMap<Point, J3dCELLGeneral>();

	private HashMap<Point, J3dCELLGeneral> loadedFars = new HashMap<Point, J3dCELLGeneral>();

	private QueuingThread nearUpdateThread;

	private QueuingThread grossUpdateThread;

	private StructureUpdateBehavior structureUpdateBehavior;

	private J3dICellFactory j3dCellFactory;

	// TODO: on change don't dump gross until we forcable need a different one
	public static BethLodManager bethLodManager;

	private BethRenderSettings.UpdateListener listener = new BethRenderSettings.UpdateListener()
	{
		public void renderSettingsUpdated()
		{
			updateFromCurrent();
		}
	};

	public BethWorldVisualBranch(int worldFormId, J3dICellFactory j3dCellFactory)
	{
		this.setName("BethWorldVisualBranch" + worldFormId);
		this.worldFormId = worldFormId;
		this.j3dCellFactory = j3dCellFactory;

		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		ScrollsExplorer.dashboard.setLodLoading(1);
		//Expensive to load, so keep it around and only change when must
		if (bethLodManager == null)
		{
			//TODO: all these should be connected strongly to GameConfig
			if (j3dCellFactory.getMainESMFileName().indexOf("Morrowind") != -1)
			{
				bethLodManager = new BethNoLodManager(j3dCellFactory);
				J3dLAND.setTes3();
				BethRenderSettings.setTes3(true);
			}
			else if (j3dCellFactory.getMainESMFileName().indexOf("HunterSneaker") != -1)
			{
				esmj3dtes4.j3d.j3drecords.inst.J3dREFRFactory.NATURAL_ANIMALS_ONLY = true;
			}
			else if (j3dCellFactory.getMainESMFileName().indexOf("Oblivion") != -1)
			{
				bethLodManager = new Beth32LodManager(j3dCellFactory);
			}
			else
			{
				bethLodManager = new Beth32_4LodManager(j3dCellFactory);
			}
		}
		bethLodManager.detach();
		bethLodManager.setWorldFormId(worldFormId);
		addChild(bethLodManager);

		ScrollsExplorer.dashboard.setLodLoading(-1);

		// set up to listener for changes to teh static render settings
		BethRenderSettings.addUpdateListener(listener);

		isWRLD = j3dCellFactory.isWRLD(worldFormId);
		if (isWRLD)
		{
			//load the general children of this wrld space
			j3dCELLPersistent = j3dCellFactory.makeBGWRLDPersistent(worldFormId, false);
			addChild((J3dCELLGeneral) j3dCELLPersistent);

			QueuingThread.CallBack nearCallBack = new QueuingThread.CallBack()
			{
				public void run(Object parameter)
				{
					updateNear((Point3f) parameter);
				}
			};

			nearUpdateThread = new QueuingThread(nearCallBack);
			nearUpdateThread.setNewestOnly(true);
			nearUpdateThread.setName("Beth Vis near update thread");
			nearUpdateThread.setDaemon(true);
			nearUpdateThread.start();

			QueuingThread.CallBack grossCallBack = new QueuingThread.CallBack()
			{
				public void run(Object parameter)
				{
					updateGross((Point3f) parameter);
				}
			};

			grossUpdateThread = new QueuingThread(grossCallBack);
			grossUpdateThread.setNewestOnly(true);
			grossUpdateThread.setName("Beth Vis gross update thread");
			grossUpdateThread.setDaemon(true);
			grossUpdateThread.start();

			//NOTE! j3d does not allow multi threaded access to add and remove groups
			// It can cause deadlocks, betterdistanceLOD on teh behavior thread is
			// doing adds and removes, so these queueing thread need to be on a behavior as well.
			structureUpdateBehavior = new StructureUpdateBehavior();
			structureUpdateBehavior.setMaxElapsedTimeForCalls(20);
			addChild(structureUpdateBehavior);

		}
		else
		{
			System.out.println("WHATAA!!! why is this " + this + " being used for interior!");
		}

	}

	public void unload()
	{
		bethLodManager.detach();
	}

	/**
	 * Note this MUST be called wiht the visuals not yet attached, it does much structure change
	 * @param charLocation
	 */
	public void init(Transform3D charLocation)
	{
		ScrollsExplorer.dashboard.setNearLoading(1);
		Vector3f v = new Vector3f();
		charLocation.get(v);
		Point3f p = new Point3f(v);
		charLocation.get(newTranslation);
		lastUpdatedTranslation.set(newTranslation);

		//Note not on a seperate thread		
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().update(p.x, -p.z, bethLodManager);
		}
		Point3f updatePoint = new Point3f(lastUpdatedTranslation.x, 0, lastUpdatedTranslation.z);
		updateNear(updatePoint);
		updateGross(updatePoint);

		ScrollsExplorer.dashboard.setNearLoading(-1);

	}

	private void updateGross(Point3f p)
	{
		if (isWRLD)
		{
			ScrollsExplorer.dashboard.setLodLoading(1);
			updateGross(p.x, -p.z);
			ScrollsExplorer.dashboard.setLodLoading(-1);
		}
	}

	private void updateGross(float charX, float charY)
	{
		bethLodManager.updateGross(charX, charY);
	}

	private void updateNear(Point3f p)
	{
		//in case of warp fix up the old but ignore new?
		Point3f currentCharPoint = new Point3f(lastUpdatedTranslation.x, 0, lastUpdatedTranslation.z);
		if (currentCharPoint.distance(p) < BethRenderSettings.getFarLoadGridCount())
		{
			ScrollsExplorer.dashboard.setNearLoading(1);
			if (j3dCELLPersistent != null)
			{
				j3dCELLPersistent.getGridSpaces().update(p.x, -p.z, bethLodManager);
			}

			updateNear(p.x, -p.z);
			ScrollsExplorer.dashboard.setNearLoading(-1);
			ScrollsExplorer.dashboard.setFarLoading(1);
			updateFar(p.x, -p.z);
			ScrollsExplorer.dashboard.setFarLoading(-1);
		}
	}

	private void updateNear(float charX, float charY)
	{
		Rectangle bounds = bethLodManager.getGridBounds(charX, charY, BethRenderSettings.getNearLoadGridCount());

		long start = System.currentTimeMillis();

		// lets remove those loaded nears not in the range
		Iterator<Point> keys = loadedNears.keySet().iterator();
		ArrayList<Point> keysToRemove = new ArrayList<Point>();
		while (keys.hasNext())
		{
			Point key = keys.next();
			if (key.x < bounds.x || key.x > bounds.x + bounds.width || key.y < bounds.y || key.y > bounds.y + bounds.height)
			{
				keysToRemove.add(key);
			}
		}

		for (int i = 0; i < keysToRemove.size(); i++)
		{
			Point key = keysToRemove.get(i);
			BranchGroup bg = loadedNears.get(key);
			if (bg != null)
			{
				structureUpdateBehavior.remove(this, bg);
			}
			synchronized (loadedNears)
			{
				loadedNears.remove(key);
			}
		}

		for (int x = bounds.x; x <= bounds.x + bounds.width; x++)
		{
			for (int y = bounds.y; y <= bounds.y + bounds.height; y++)
			{
				//tester  
				//	if (x == -4 && y == 1)
				{
					Point key = new Point(x, y);

					if (!loadedNears.containsKey(key))
					{
						//Persistent are loaded in  the CELL that is makeBGWRLD all xy based persistents are empty

						J3dCELLGeneral bg = j3dCellFactory.makeBGWRLDTemporary(worldFormId, x, y, false);

						synchronized (loadedNears)
						{
							loadedNears.put(key, bg);
						}

						//NOTE nears own the detailed land					
						if (bg != null)
						{
							bg.compile();// better to be done not on the j3d thread?
							structureUpdateBehavior.add(this, bg);
						}

						// now get rid of any fars that have the same keys loaded in
						bg = loadedFars.get(key);
						if (bg != null)
						{
							structureUpdateBehavior.remove(this, bg);
							loadedFars.remove(key);
						}
					}
				}
			}
		}
		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("BethWorldVisualBranch.updateNear took " + (System.currentTimeMillis() - start) + "ms");

	}

	/**
	 * This only does things for Oblivion, tes5 doesn't use it
	 * @param charX
	 * @param charY
	 * @param isLive 
	 */
	private void updateFar(float charX, float charY)
	{
		long start = System.currentTimeMillis();

		// Note simple system used, as no lands invloved here
		Rectangle bounds = Beth32LodManager.getBounds(charX, charY, BethRenderSettings.getFarLoadGridCount());

		final int lowX = bounds.x;
		final int lowY = bounds.y;
		final int highX = bounds.x + +bounds.width;
		final int highY = bounds.y + bounds.height;

		// lets remove those loaded fars not in the range
		Iterator<Point> keys = loadedFars.keySet().iterator();
		ArrayList<Point> keysToRemove = new ArrayList<Point>();
		while (keys.hasNext())
		{
			Point key = keys.next();
			if (key.x < lowX || key.x > highX || key.y < lowY || key.y > highY)
			{
				keysToRemove.add(key);
			}
		}

		for (int i = 0; i < keysToRemove.size(); i++)
		{
			Point key = keysToRemove.get(i);
			BranchGroup bg = loadedFars.get(key);
			if (bg != null)
			{
				structureUpdateBehavior.remove(this, bg);
				loadedFars.remove(key);
			}
		}

		for (int x = lowX; x <= highX; x++)
		{
			for (int y = lowY; y <= highY; y++)
			{
				Point key = new Point(x, y);
				// don't load fars where a near is
				if (!loadedFars.containsKey(key) && !loadedNears.containsKey(key))
				{
					//long start = System.currentTimeMillis();

					J3dCELLGeneral bg = j3dCellFactory.makeBGWRLDDistant(worldFormId, x, y, false);
					loadedFars.put(key, bg);
					if (bg != null)
					{
						bg.compile();// better to be done not on the j3d thread?
						structureUpdateBehavior.add(this, bg);
						//System.out.println("updateFar3 " + key + " " + (System.currentTimeMillis() - start));
					}
				}
			}
		}
		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("BethWorldVisualBranch.updateFar took " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * Called when the static render settings are changed
	 *
	 */
	public void updateFromCurrent()
	{
		if (isWRLD)
		{
			Point3f updatePoint = new Point3f(lastUpdatedTranslation.x, 0, lastUpdatedTranslation.z);
			nearUpdateThread.addToQueue(updatePoint);
		}
	}

	//	deburner
	private Vector3f newTranslation = new Vector3f();

	private Point3f p1 = new Point3f();

	private Point3f p2 = new Point3f();

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		if (isWRLD)
		{
			newTranslation.set(trans);
			p1.set(newTranslation);
			p2.set(lastUpdatedTranslation);
			if (p1.distance(p2) > 2)
			{
				lastUpdatedTranslation.set(newTranslation);

				Point3f updatePoint = new Point3f(newTranslation.x, 0, newTranslation.z);
				nearUpdateThread.addToQueue(updatePoint);
				grossUpdateThread.addToQueue(updatePoint);
			}
		}
	}

	public void handleRecordCreate(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordCreate(record);
		}
	}

	public void handleRecordDelete(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordDelete(record);
		}
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.getGridSpaces().handleRecordUpdate(record, updatedSubrecord);
		}

	}

	public J3dRECOInst getJ3dInstRECO(int recoId)
	{

		synchronized (loadedNears)
		{
			for (J3dCELLGeneral cell : loadedNears.values())
			{
				if (cell != null && cell.getParent() != null)
				{
					J3dRECOInst jri = cell.getJ3dRECOs().get(recoId);
					if (jri != null)
					{
						return jri;
					}
				}
			}
		}
		//ok try the persistent cell as well, it'll return null if it's really not here
		if (j3dCELLPersistent.getGridSpaces() != null)
			return j3dCELLPersistent.getGridSpaces().getJ3dInstRECO(recoId);
		else
			return null;
	}

}