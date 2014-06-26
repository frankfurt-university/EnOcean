package de.EnOcean.jslee.ratype;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface EnOceanProvider {
	
	public GatewayConnectionActivity getGatewayConnection()
		    throws IOException;
}
