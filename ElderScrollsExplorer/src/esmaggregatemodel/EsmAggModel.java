package esmaggregatemodel;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import tools.WeakListenerList;
import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;
import esmchangemodel.ODMEventListener;
import esmchangemodel.ODMUpdateEvent;

/**
 * Cut from desing doc on 21/8/07
 
 The oblivion delta model

 The models loaded by the client form the esm files are as they stand, with no changes to position or other subrecords
 When a object in the current esm is determined to need to be removed it is marked as deleted and an entirely new object is set up.

 New objects are put into a group and moved from group to group, hence group lists from the esm are extended by new objects, not never reduced (removed objects are just marked deleted)

 Any new instance type record has a pointer to a cell, and is implicitly put into the cells persistent children, not temp or dist

 New objects are only ever REFR ANPC ACRE nothing else.

 New objects can have any subrecords altered, position contents etc.

 All new objects are deleted by removal from the model and db.

 The model must organize new objects by group for ease of access

 Data:
 Instance record with parent cell id
 Subrecords inside instance record
 Delete instance record just formId in hashtable

 Events:
 Send delete record
 Send add record
 Send update record
 Send remove record


 Flows of events
 Server starts up
 The model is loaded from the DB
 The physics and AI are loaded form the model, all new objects are handed to these sub systems to create entities. 
 AI only applies to new ANPC and ACRE
 Physics needs to see news and deletes 
 Then things are now running, AI is able to ask for changes to the model which get sent out to physics for update, and any subsequently connected clients


 Client connect

 When the client connects the model is sent out same as now. An exact paralelle system
 Client then creates world from esm with deletes removed and those Cells? Listen for updates?
 Client creates vis and phys for any newed items and those newed items listen to the model object for updates
 Client players now send updates to server


 Client asks to pick up an item in the world
 Client sends pick up request to server
 Server decides it is ok (close enough, cartable etc)
 Server makes delete event for original and applies to server model
 Delete event goes to all clients
 Client apply delete event to model
 Something?? Cell?? On client gets given delete event and removes the original from the vis and phys?
 Server makes change to sub records of player to include reference to item picked up
 Server applies change event
 Change event is sent out to the clients
 Client applies change to model
 A listener (the vis player) get change event and applies it to itself

 AI moves an ANPC
 AI asks for an ANPC subrecord detail to be updated by creating a update record event
 Update record event is given to the server model
 Server model oks it and applies it
 Server model send to all clients
 Client model applies update
 Something??? Is listening to this record update, because it was attached when the record was “newed” and therefore updates the visual and physical on the client


 Player drops something
 Client sends drop item request to server (or record update and new item events???) 
 Server makes 2 events if it oks drop
 Events are applied to server model
 Events are sent to physics (new)  and AI
 Events are sent to clients
 Client alter model,
 listener to update on records updates
 New makes new vis and phys object and listner to model changes



 Next cuts:
 First cut does not load sub parts of the model the entire models goes to the client machine, sub parts will be done later, and all changes to each model are reflected in the client delta models.
 First cut only allow 3 of the instance records but type records and other instance could be done later
 Instance records could be put into non persistent cell groups
 New cells could be added, with full structure


 * 
 * There are various listeners able to be added
 * Finally remeber DO NOT alter objects given back by this class, ever.
 *  
 * @author pj
 */
public abstract class EsmAggModel
{
	private WeakListenerList<ODMEventListener> eventListeners = new WeakListenerList<ODMEventListener>();

	protected Hashtable<Integer, Record> recordsById = new Hashtable<Integer, Record>();

	//record all deletes of actual records so we can easily remove from DB on save
	private Hashtable<Integer, Integer> deletedRecordsById = new Hashtable<Integer, Integer>();

	//record all ESM files that should be ignored henceforth
	protected Hashtable<Integer, Integer> esmDeletedRecordsById = new Hashtable<Integer, Integer>();

	private boolean locked = false;

	/**
	 * The constructor
	 */
	public EsmAggModel()
	{
	}

	/**
	 * @param listener a listener interested in events happening on the statemodel  
	 */
	public void addODMEventListener(ODMEventListener listener)
	{
		eventListeners.add(listener);
	}

	/**
	 * @param listener a previously added listener
	 */
	public void removeStateModelEventListener(ODMEventListener listener)
	{
		eventListeners.remove(listener);
	}

	public void initialize(ArrayList<Record> records, ArrayList<Integer> deletes)
	{
		clear();
		for (int i = 0; i < records.size(); i++)
		{
			Record record = records.get(i);
			recordsById.put(new Integer(record.getFormID()), record);
		}
		for (int i = 0; i < deletes.size(); i++)
		{
			Integer id = deletes.get(i);
			esmDeletedRecordsById.put(id, id);
		}
	}

	/**
	 * A locked statemodel will queue up events until it is unlocked
	 * for use with persisting the model
	 */
	public void lock()
	{
		locked = true;
	}

	/**
	 * A locked statemodel will queue up events until it is unlocked
	 * for use with persisting the model
	 */
	public void unlock()
	{
		locked = false;
	}

	/**
	 * Clears the model completely and tells population listeners
	 */
	public void clear()
	{
		recordsById.clear();
		esmDeletedRecordsById.clear();
		deletedRecordsById.clear();
	}

	/**
	 * This is the method by which all model changes occur, this will trigger 
	 * the same event to all listeners of the model
	 * @param event the event to happen
	 */
	public synchronized void alterModel(ODMUpdateEvent event)
	{
		//TODO: should perhaps this be on a single thread state updater?

		//infinite loop for locking (perfect for deadlocks)
		while (locked)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
			}
		}
		// tell the listeners about it "pre"
		for (int i = 0; i < eventListeners.size(); i++)
		{
			ODMEventListener element = eventListeners.get(i);
			element.preEventTriggered(event);
		}

		// now just get the static handler to distinguish it
		applyEvent(event);

		// tell the listeners about it "post"
		for (int i = 0; i < eventListeners.size(); i++)
		{
			ODMEventListener element = eventListeners.get(i);
			element.postEventTriggered(event);
		}

	}

	private synchronized void applyEvent(ODMUpdateEvent event)
	{
		if (event.getEventType() == ODMUpdateEvent.ODM_CREATE)
		{
			addRecord(event.getRecord());
		}
		else if (event.getEventType() == ODMUpdateEvent.ODM_UPDATE)
		{
			updateRecord(event.getRecord());
		}
		else if (event.getEventType() == ODMUpdateEvent.ODM_DELETE)
		{
			deleteRecord(event.getRecordId());
		}
		else if (event.getEventType() == ODMUpdateEvent.ODM_INTITIALIZE)
		{
			initialize(event.getRecords(), event.getEsmDeletes());
		}
		else if (event.getEventType() == ODMUpdateEvent.ODM_UPDATE_SUBRECORD)
		{
			updateSubrecord(event.getRecordId(), event.getSubrecord());
		}
		else
		{
			new Exception("Unknown ODMEvent type to handle " + event.getEventType()).printStackTrace();
		}

	}

	/**
	 * NOTE do not alter this object or chaos will ensue
	 * @param ID the id of the requested object
	 * @return the object for the id
	 */
	public Record getRecord(int ID)
	{
		return recordsById.get(new Integer(ID));
	}

	public ArrayList<Record> getUpdatedRecords()
	{
		ArrayList<Record> ret = new ArrayList<Record>();
		Enumeration<Record> recs = recordsById.elements();
		while (recs.hasMoreElements())
		{
			Record record = recs.nextElement();
			if (record.isUpdated())
			{
				ret.add(record);
			}
		}
		return ret;
	}

	public Enumeration<Integer> getRecordIds()
	{
		return recordsById.keys();
	}

	public Enumeration<Integer> getDeletedRecordIds()
	{
		return deletedRecordsById.keys();
	}

	public Enumeration<Integer> getEsmDeleteRecordIds()
	{
		return esmDeletedRecordsById.keys();
	}

	public boolean isEsmRecordDeleted(int ID)
	{
		return esmDeletedRecordsById.containsKey(new Integer(ID));
	}

	/**
	 * Note package level visibility for use by StateModelEventHandler only
	 * The STATE will have had it's id set at the return of this call
	 * @param STATE a prepared object to be added, the id should be -1 unless the db gives it in
	 */
	protected void addRecord(Record record)
	{
		Integer newRecordid = new Integer(record.getFormID());
		if (!recordsById.contains(newRecordid))
		{
			recordsById.put(newRecordid, record);
		}
		else
		{
			new Exception("Attempt to add object with duplicate id " + newRecordid).printStackTrace();
		}
	}

	/**
	 * Note package level visibility for use by StateModelEventHandler only
	 * The STATE will have had it's id set at the return of this call
	 * @param STATE a prepared object to be added, the id should be -1 unless the db gives it in
	 */
	private void updateRecord(Record record)
	{
		Record currentRecord = recordsById.get(new Integer(record.getFormID()));
		if (currentRecord != null)
		{
			currentRecord.updateFrom(record);
		}
		else
		{
			new Exception("Attempt to update with id " + record.getFormID() + " but record does not exist").printStackTrace();
		}
	}

	/**
	 * updates one subrecord within a record
	 * @param formId
	 * @param subrecord
	 */
	private void updateSubrecord(int formId, Subrecord subrecord)
	{
		Record currentRecord = recordsById.get(new Integer(formId));
		if (currentRecord != null)
		{
			currentRecord.updateSubrecord(subrecord);
		}
		else
		{
			new Exception("Attempt to update subrecord with id " + formId + " but record does not exist").printStackTrace();
		}

	}

	/**
	 * Note package level visibility for use by StateModelEventHandler only
	 * @param id the id of the object to be removed
	 */
	private void deleteRecord(int id)
	{
		// shall we record it for esm delete or remove the delta record?		
		Record record = recordsById.remove(new Integer(id));

		if (record == null)
		{
			// we obviously need to record this delete event
			esmDeletedRecordsById.put(new Integer(id), new Integer(id));
		}
		else
		{
			// record the delete so it can be removed from DB on save
			deletedRecordsById.put(new Integer(id), new Integer(id));
		}
	}

	public String stringModelDump()
	{
		String modelDumpString = "Current Model Contents: \n";

		Enumeration<Record> recs = recordsById.elements();
		while (recs.hasMoreElements())
		{
			Record record = recs.nextElement();
			modelDumpString += "rec:" + record;
			modelDumpString += "\n";
		}

		Enumeration<Integer> dels = esmDeletedRecordsById.keys();
		while (dels.hasMoreElements())
		{
			Integer delId = dels.nextElement();
			modelDumpString += "del:" + delId;
			modelDumpString += "\n";
		}

		return modelDumpString;
	}

}
