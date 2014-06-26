package de.EnOcean.jslee.ratype;

import java.io.IOException;


//import de.EnOcean.jslee.ra.Listener;
import de.EnOcean.jslee.ra.*;



public interface EnOceanConnectionProvider {
	
	
	// create sesionID
//    public SessionIDActivity createSessionIDActivity( SendTelegramMessageActivity sendTelegramMessage )
//    throws IOException;

    
    
    // To listen for ready status of the gateway
    public Listener getListener();   
    
}
