package esmchangemodel;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;

public class ODMUpdateRequest
{

	// values used to serialize the subclasses
	public static final int ODM_UPDATE = 0;
	public static final int ODM_CREATE = 1;
	public static final int ODM_DELETE = 2;

	private int eventType = -1;

	private Record record = null;
	private int recordId;
	private int initiatorId = -1;
	private Subrecord subrecord = null;

	/**
	 * The constructor
	 */
	public ODMUpdateRequest(int eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * @return the initiator id (client or physics or admin etc)
	 */
	public int getInitiatorId()
	{
		return initiatorId;
	}

	/**
	 * @return the object id to which this event applies
	 */
	public int getRecordId()
	{
		return recordId;
	}
	/**
	 * @param i the intiator id
	 */
	public void setInitiatorId(int i)
	{
		initiatorId = i;
	}

	/**
	 * @param i the object id to which this event applies
	 */
	public void setRecordId(int i)
	{
		recordId = i;
	}

	public Record getRecord()
	{
		return record;
	}

	public void setRecord(Record record)
	{
		this.record = record;
	}

	public int getEventType()
	{
		return eventType;
	}

	public static ODMUpdateRequest createDeleteEvent(int recordId)
	{
		ODMUpdateRequest ret = new ODMUpdateRequest(ODM_DELETE);
		ret.setRecordId(recordId);
		return ret;
	}

	public Subrecord getSubrecord()
	{
		return subrecord;
	}

	public void setSubrecord(Subrecord subrecord)
	{
		this.subrecord = subrecord;
	}

}
