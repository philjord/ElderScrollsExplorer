package scrollsexplorer.simpleclient;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.QueuingThread;
import tools3d.utils.scenegraph.LocationUpdateListener;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmj3d.j3d.BethRenderSettings;
import esmj3d.j3d.cell.GridSpace;
import esmj3d.j3d.cell.J3dCELLGeneral;
import esmj3d.j3d.cell.J3dICELLPersistent;
import esmj3d.j3d.cell.J3dICellFactory;
import esmj3d.j3d.j3drecords.inst.J3dLAND;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

public class BethWorldPhysicalBranch extends BranchGroup implements LocationUpdateListener
{
	private int worldFormId;

	private boolean isWRLD = true; // false implies interior cell

	private J3dICELLPersistent j3dCELLPersistent;

	private J3dCELLGeneral j3dCELLTemporary;

	private Vector3f lastUpdatedTranslation = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

	private HashMap<Point, J3dCELLGeneral> loadedNears = new HashMap<Point, J3dCELLGeneral>();

	private HashMap<Point, J3dCELLGeneral> loadedFars = new HashMap<Point, J3dCELLGeneral>();

	private QueuingThread updateThread;

	private J3dICellFactory j3dCellFactory;

	private PhysicsSystem clientPhysicsSystem;

	private BethRenderSettings.UpdateListener listener = new BethRenderSettings.UpdateListener()
	{
		public void renderSettingsUpdated()
		{
			updateFromCurrent();
		}
	};

	public BethWorldPhysicalBranch(PhysicsSystem clientPhysicsSystem, int worldFormId, J3dICellFactory j3dCellFactory)
	{
		this.clientPhysicsSystem = clientPhysicsSystem;
		this.worldFormId = worldFormId;
		this.j3dCellFactory = j3dCellFactory;

		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(Group.ALLOW_CHILDREN_WRITE);
		this.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		// set up to listen for changes to the static render settings
		BethRenderSettings.addUpdateListener(listener);

		isWRLD = j3dCellFactory.isWRLD(worldFormId);
		if (isWRLD)
		{
			QueuingThread.CallBack callBack = new QueuingThread.CallBack()
			{
				public void run(Object parameter)
				{
					//enusre we are nearby (character hasn't warped)
					Point3f currentCharPoint = new Point3f(lastUpdatedTranslation.x, 0, lastUpdatedTranslation.z);
					Point3f p = (Point3f) parameter;
					if (currentCharPoint.distance(p) < BethRenderSettings.getFarLoadGridCount())
					{
						update(p.x, -p.z);

					}
				}
			};

			updateThread = new QueuingThread(callBack);
			updateThread.setName("Obliv Phys update thread");
			updateThread.setDaemon(true);
			updateThread.start();

			// load the general children of this wrld space

			j3dCELLPersistent = j3dCellFactory.makeBGWRLDPersistent(worldFormId, true);
			addChild((J3dCELLGeneral) j3dCELLPersistent);
			clientPhysicsSystem.cellChanged(worldFormId, (J3dCELLGeneral) j3dCELLPersistent);
			//this persistent is just a super cluster of vague things, not related to position at all
			// real data is just in temps below

		}
		else
		{
			j3dCELLPersistent = j3dCellFactory.makeBGInteriorCELLPersistent(worldFormId, true);
			addChild((J3dCELLGeneral) j3dCELLPersistent);
			clientPhysicsSystem.cellChanged(worldFormId, (J3dCELLGeneral) j3dCELLPersistent);

			j3dCELLTemporary = j3dCellFactory.makeBGInteriorCELLTemporary(worldFormId, true);
			addChild(j3dCELLTemporary);
			clientPhysicsSystem.loadJ3dCELL(j3dCELLTemporary);

			addChild(j3dCellFactory.makeBGInteriorCELLDistant(worldFormId, true));
			//not added to physics

		}
	}

	public void init(Transform3D charLocation)
	{

		if (isWRLD)
		{
			Vector3f v = new Vector3f();
			charLocation.get(v);
			Point3f p = new Point3f(v);
			lastUpdatedTranslation.set(newTranslation);

			//Note not on a seperate thread
			update(p.x, -p.z);
			if (j3dCELLPersistent != null)
			{
				j3dCELLPersistent.update(p.x, -p.z, J3dLAND.LAND_SIZE * BethRenderSettings.getNearLoadGridCount());
			}
		}
	}

	/**
	 *  Note the phys loads nears and fars using only one near dist as far phys is not important
	 * @param charX
	 * @param charY
	 */
	private void update(float charX, float charY)
	{
		long start = System.currentTimeMillis();

		if (j3dCELLPersistent != null)
		{
			float loadDist = J3dLAND.LAND_SIZE * BethRenderSettings.getNearLoadGridCount();

			// because j3dcellpersistent is in a lower project I have to do this here, bum			
			List<GridSpace> gridsToRemove = j3dCELLPersistent.getGridSpacesToRemove(charX, charY, loadDist);
			List<GridSpace> gridsToAdd = j3dCELLPersistent.getGridSpacesToAdd(charX, charY, loadDist);

			//done after gathering the lists above so we now do the grid changes
			j3dCELLPersistent.update(charX, charY, loadDist);

			for (GridSpace gridSpace : gridsToRemove)
			{
				clientPhysicsSystem.unloadJ3dGridSpace(gridSpace);
			}

			for (GridSpace gridSpace : gridsToAdd)
			{
				clientPhysicsSystem.loadJ3dGridSpace(gridSpace);
			}

		}

		/*int lowX = (int) Math.floor((charX - (J3dLAND.LAND_SIZE * 0.5)) / J3dLAND.LAND_SIZE);
		int lowY = (int) Math.floor((charY - (J3dLAND.LAND_SIZE * 0.5)) / J3dLAND.LAND_SIZE);
		int highX = (int) Math.ceil((charX + (J3dLAND.LAND_SIZE * 0.5)) / J3dLAND.LAND_SIZE);
		int highY = (int) Math.ceil((charY + (J3dLAND.LAND_SIZE * 0.5)) / J3dLAND.LAND_SIZE);*/

		int lowX = (int) Math.floor(charX / J3dLAND.LAND_SIZE) - 1;
		int lowY = (int) Math.floor(charY / J3dLAND.LAND_SIZE) - 1;
		int highX = (int) Math.floor(charX / J3dLAND.LAND_SIZE) + 1;//grids load out toward positive
		int highY = (int) Math.floor(charY / J3dLAND.LAND_SIZE) + 1;

		// lets remove those loaded nears not in the range
		Iterator<Point> keys = loadedNears.keySet().iterator();
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
			J3dCELLGeneral cell = loadedNears.remove(key);
			if (cell != null)
			{
				removeChild(cell);
				clientPhysicsSystem.unloadJ3dCELL(cell);
			}

		}

		// lets remove those loaded fars not in the range
		keys = loadedFars.keySet().iterator();
		keysToRemove = new ArrayList<Point>();
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
			J3dCELLGeneral cell = loadedFars.remove(key);
			if (cell != null)
			{
				removeChild(cell);
			}

		}

		for (int x = lowX; x <= highX; x++)
		{
			for (int y = lowY; y <= highY; y++)
			{
				//if(x==1&&y==1)
				{
					load(x, y);
				}
			}
		}

		if ((System.currentTimeMillis() - start) > 50)
			System.out.println("BethWorldPhysicalBranch.update took " + (System.currentTimeMillis() - start) + "ms");
	}

	private void load(int x, int y)
	{
		Point key = new Point(x, y);
		if (!loadedNears.containsKey(key))
		{
			//Persistent are loaded in  the CELL that is makeBGWRLD all xy based persistents are empty
			j3dCELLTemporary = j3dCellFactory.makeBGWRLDTemporary(worldFormId, x, y, true);
			loadedNears.put(key, j3dCELLTemporary);
			if (j3dCELLTemporary != null)
			{
				addChild(j3dCELLTemporary);
				clientPhysicsSystem.loadJ3dCELL(j3dCELLTemporary);
			}
		}

		if (!loadedFars.containsKey(key))
		{
			J3dCELLGeneral cell = j3dCellFactory.makeBGWRLDDistant(worldFormId, x, y, true);
			loadedFars.put(key, cell);
			if (cell != null)
			{
				addChild(cell);
				//not added to physics only added to the view for rendering
			}
		}
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
			updateThread.addToQueue(updatePoint);
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
			if (p1.distance(p2) > BethRenderSettings.getCHAR_MOVE_UPDATE_DIST())
			{
				lastUpdatedTranslation.set(newTranslation);
				Point3f updatePoint = new Point3f(newTranslation.x, 0, newTranslation.z);
				updateThread.addToQueue(updatePoint);
			}
		}

	}

	public void handleRecordCreate(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordCreate(record);
		}
	}

	public void handleRecordDelete(Record record)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordDelete(record);
		}
	}

	public void handleRecordUpdate(Record record, Subrecord updatedSubrecord)
	{
		if (j3dCELLPersistent != null)
		{
			j3dCELLPersistent.handleRecordUpdate(record, updatedSubrecord);
		}

	}

	public J3dRECOInst getJ3dInstRECO(int recordId)
	{
		for (J3dCELLGeneral cell : loadedNears.values())
		{
			if (cell != null)
			{
				J3dRECOInst jri = cell.getJ3dRECOs().get(recordId);
				if (jri != null)
				{
					return jri;
				}
			}
		}

		//ok try the persistent cell as well, it'll return null if it's really not here
		return j3dCELLPersistent.getJ3dInstRECO(recordId);
	}

}
