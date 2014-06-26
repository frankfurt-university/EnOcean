
package de.EnOcean.jslee.ratype;

import javax.slee.ActivityContextInterface;
import javax.slee.UnrecognizedActivityException;
import javax.slee.FactoryException;

/**
 * Factory to return the ACI associated with an Activity
 * <i>This interface is implemented by the SLEE</i>
 */
public interface TcpActivityContextInterfaceFactory
{
   
  
    public ActivityContextInterface getActivityContextInterface( GatewayConnectionActivity activity )
        throws UnrecognizedActivityException, FactoryException;
      

    public ActivityContextInterface getActivityContextInterface( EnOceanConnectionActivity activity )
        throws UnrecognizedActivityException, FactoryException;
    
 
    
    
//    public ActivityContextInterface getActivityContextInterface( GatewayConnectionActivity activity )
//    throws UnrecognizedActivityException, FactoryException;
//    
//    
//    public ActivityContextInterface getActivityContextInterface( SendTelegramMessageActivity activity )
//    throws UnrecognizedActivityException, FactoryException;
    
    public ActivityContextInterface getActivityContextInterface( SessionIDActivity activity )
    throws NullPointerException, UnrecognizedActivityException, FactoryException;
}
