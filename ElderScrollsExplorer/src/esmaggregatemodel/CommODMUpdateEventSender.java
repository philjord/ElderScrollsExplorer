package esmaggregatemodel;

import esmchangemodel.ODMUpdateEvent;

public interface CommODMUpdateEventSender
{
	public void stopPublishing();
	public void setODMToPublish(MasterEsmAggModel oblivionDeltaModel, ODMUpdateEvent initEvent);

}
