package de.EnOcean.jslee.ra;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.slee.facilities.Tracer;

import de.EnOcean.jslee.ra.TcpResourceAdaptor;

public class EnOceanGatewayConnectionListener extends Thread{
	
	private volatile boolean running = true;
	private ServerSocket listenSocket;
	private static Tracer tracer;
	ArrayList<Socket> gatewayList;
	public 
	
	EnOceanGatewayConnectionListener( Tracer tracer, TcpResourceAdaptor ra, int myNodeID, int connectionPort, ArrayList<Socket> gatewayList )
			throws Exception
			{
				super( "EnOceanGatewayConnectionListener" );
				EnOceanGatewayConnectionListener.tracer = tracer;
				this.listenSocket = new ServerSocket( connectionPort );
				this.gatewayList=gatewayList;
			}

	public void run()
	{		
		while ( running )
		{
			Socket newConnectionSocket;
			try
			{       	 
				tracer.info( "> waiting for new gateway connections on port 2001" );
				newConnectionSocket = listenSocket.accept(); 
				tracer.info("> connection established between the gateway & the EnOceanRA on port 2001");
				handleConnection ( newConnectionSocket );
			}
			catch ( IOException e )
			{
				tracer.severe("> failed to establish connection between the gateway & the EnOceanRA on port 2001");          
			}
		}
	}
	private void handleConnection( Socket gatewaySocket ) throws IOException 
	{ 
		gatewayList.add(gatewaySocket);
	}
	
	public void closeSockets(){
		ListIterator<Socket> gatewayListIterator = gatewayList.listIterator();
		tracer.info( "Trying to close all open gateway connections!" );
		try {
			tracer.info( "Close gateway listening socket: " + listenSocket.getLocalSocketAddress() + ":" + listenSocket.getLocalPort() + "!" );
			running = false;
			listenSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (gatewayListIterator.hasNext()){
			Socket gatewaySocket = (Socket) gatewayListIterator.next();
			try {
				tracer.info( "Close gateway connection " + gatewaySocket.getRemoteSocketAddress() + ":" + gatewaySocket.getPort() + "!" );
				gatewaySocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}

	
}
