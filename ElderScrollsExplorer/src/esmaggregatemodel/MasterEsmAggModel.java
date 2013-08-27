package esmaggregatemodel;

import java.util.Iterator;

import esmLoader.common.data.record.Record;
import esmchangemodel.ODMUpdateEvent;

/**
 * see statemodel this master calss allows non id'd object to be added (frmo teh DB or 
 * model editor and it applies rule systems.
 *  
 * @author pj
 */
public class MasterEsmAggModel extends EsmAggModel
{
	private IdSequenceSource idSequenceSource;

	/**
	 * The constructor
	 */
	public MasterEsmAggModel(IdSequenceSource idSequenceSource)
	{
		this.idSequenceSource = idSequenceSource;
	}

	/**
	 * Note package level visibility for use by StateModelEventHandler only
	 * The STATE will have had it's id set at the return of this call
	 * @param STATE a prepared object to be added, the id should be -1 unless the db gives it in
	 */
	protected void addRecord(Record record)
	{
		if (record.getFormID() == -1)
		{
			record.setFormID(idSequenceSource.getNextId());
		}

		super.addRecord(record);
	}

	public synchronized void attachPublisher(CommODMUpdateEventSender commODMUpdateEventSender)
	{
		// stop it publishing out what ever it may be currently attached to
		commODMUpdateEventSender.stopPublishing();

		// now tell the client to re-intialize its model
		ODMUpdateEvent initEvent = new ODMUpdateEvent(ODMUpdateEvent.ODM_INTITIALIZE);

		Iterator<Record> records = recordsById.values().iterator();
		while (records.hasNext())
		{
			Record record = records.next();
			initEvent.getRecords().add(record);
		}

		Iterator<Integer> esmDeletes = esmDeletedRecordsById.values().iterator();
		while (esmDeletes.hasNext())
		{
			Integer delete = esmDeletes.next();
			initEvent.getEsmDeletes().add(delete);
		}

		// tell it to start publishing, which will begin with teh full evnet sent
		commODMUpdateEventSender.setODMToPublish(this, initEvent);
	}

}
