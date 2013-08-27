package esmaggregatemodel;

import esmLoader.common.data.plugin.Master;
import esmLoader.common.data.record.Record;
import esmLoader.loader.ESMManager;

public class EAMMasterRecordStore extends ESMManager
{
	private EsmAggModel oblivionDeltaModel;

	public EAMMasterRecordStore(Master master, EsmAggModel oblivionDeltaModel)
	{
		super();
		addMaster(master);
		this.oblivionDeltaModel = oblivionDeltaModel;
	}

	@Override
	public Record getRecord(int formID)
	{
		Record record = oblivionDeltaModel.getRecord(formID);
		if (record != null)
		{
			return record;
		}
		else if (!oblivionDeltaModel.isEsmRecordDeleted(formID))
		{
			return super.getRecord(formID);
		}

		return null;
	}

}
