package de.EnOcean.jslee.ratype;

import java.util.List;

public interface GatewayConnectionActivity {
	  
    public EnOceanConnectionActivity connect2Gateway(String gatewayIP);
    
    public List getGatewayList();
    
    public GatewayConnectionActivity getGatewayActivity();
}
