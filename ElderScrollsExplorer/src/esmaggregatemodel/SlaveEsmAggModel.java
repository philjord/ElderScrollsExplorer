/*
 * Created on 15/05/2005
 */
package esmaggregatemodel;

import esmLoader.common.data.record.Record;

/**
 * At this stage the slave state model does little more than the abstract super class, the name basically implys usage
 * @author pj
 *
 */
public class SlaveEsmAggModel extends EsmAggModel
{
	protected void addRecord(Record record)
	{
		if (record.getFormID() == -1)
		{
			new Exception("Slave state model handed object with id -1");
		}

		super.addRecord(record);
	}
}
