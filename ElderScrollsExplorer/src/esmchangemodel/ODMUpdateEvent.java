package esmchangemodel;

import java.util.ArrayList;

import esmLoader.common.data.record.Record;
import esmLoader.common.data.record.Subrecord;


public class ODMUpdateEvent
{
	// values used to serialize the subclasses
	public static final int ODM_UPDATE = 0;
	public static final int ODM_CREATE = 1;
	public static final int ODM_DELETE = 2;
	public static final int ODM_INTITIALIZE = 3;
	public static final int ODM_UPDATE_SUBRECORD = 4;

	private int eventType = -1;

	private Record record = null;
	private int recordId;
	private int initiatorId = -1;
	private Subrecord subrecord = null;

	private ArrayList<Record> records = new ArrayList<Record>();
	private ArrayList<Integer> esmDeletes = new ArrayList<Integer>();

	//TODO: I need to do the "partial" subrecord update, very important for npc movement etc

	/**
	 * The constructor
	 */
	public ODMUpdateEvent(int eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * Constructor for when a request has been ok'ed and is now in fact an event
	 * NOTE as the request and event diverge this MUST be updated
	 * 
	 * @return
	 */
	public ODMUpdateEvent(ODMUpdateRequest oDMUpdateRequest)
	{
		this.eventType = oDMUpdateRequest.getEventType();
		this.record = oDMUpdateRequest.getRecord();
		this.recordId = oDMUpdateRequest.getRecordId();
		this.initiatorId = oDMUpdateRequest.getInitiatorId();
		this.subrecord = oDMUpdateRequest.getSubrecord();
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

	public static ODMUpdateEvent createDeleteEvent(int recordId)
	{
		ODMUpdateEvent ret = new ODMUpdateEvent(ODM_DELETE);
		ret.setRecordId(recordId);
		return ret;
	}

	public ArrayList<Integer> getEsmDeletes()
	{
		return esmDeletes;
	}

	public ArrayList<Record> getRecords()
	{
		return records;
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
