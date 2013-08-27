package esmchangemodel;



/**
 * For things interested in evetn on the statemodel
 * @author pj
 */
public interface ODMEventListener
{
	public void preEventTriggered(ODMUpdateEvent event);
	public void postEventTriggered(ODMUpdateEvent event);
}
