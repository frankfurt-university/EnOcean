package de.EnOcean.jslee.ra;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.slee.facilities.Tracer;

import de.EnOcean.jslee.ratype.EnOceanConnectionActivity;
import de.EnOcean.jslee.ratype.GatewayConnectionActivity;

public class GatewayConnectionHandler extends Thread implements GatewayConnectionActivity, ActivityHandler{
	
	private volatile boolean running = true;
	private ServerSocket listenSocket;
	private static Tracer tracer;
	private ArrayList<Socket> gatewayList;
	private TcpResourceAdaptor ra;
	private ConnectionID id;
	
	public GatewayConnectionHandler( Tracer tracer, TcpResourceAdaptor ra, ConnectionID id, int connectionPort, ArrayList<Socket> gatewayList )
			throws Exception
	{
		super( "GatewayConnectionHandler thread for " + id );
		GatewayConnectionHandler.tracer = tracer;
		this.listenSocket = new ServerSocket( connectionPort );
		this.gatewayList=gatewayList;
		this.ra = ra;
		this.id = id;
		
        // Inform the RA of this new connection
        //ra.connectionOpened( this );
        ra.activityCreated( this );
	}

	public void run()
	{		
		while ( running )
		{
			Socket newConnectionSocket = null;
			try
			{       	 
				tracer.info( "> waiting for new gateway connections on port 2001" );
				newConnectionSocket = listenSocket.accept(); 
			}
			catch ( IOException e )
			{
				tracer.info("listening port 2001 > " + e.getMessage());		
				break;
			}
			tracer.info("> connection established between the gateway & the EnOceanRA on port 2001");
			tracer.info("> remoteIP:" + newConnectionSocket.getInetAddress().getHostAddress());
			handleConnection ( newConnectionSocket );
		}
	}
	private void handleConnection( Socket gatewaySocket )  
	{ 
		gatewayList.add(gatewaySocket);
		fireEvent();
	}
	
	public synchronized void closeSockets(){
		ListIterator<Socket> gatewayListIterator = gatewayList.listIterator();
		tracer.info( "Trying to close all open gateway connections!" );
		tracer.info( "Close gateway listening socket: " + listenSocket.getLocalSocketAddress() + "!" );
		running = false;
		try {
			listenSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (gatewayListIterator.hasNext()){
			Socket gatewaySocket = (Socket) gatewayListIterator.next();
			try {
				tracer.info( "Close gateway connection " + gatewaySocket.getRemoteSocketAddress() + "!" );
				gatewaySocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}				
	}
	
    private void fireEvent (){
    	ra.fireGATEWAY_LIST_EVENT(this);
    }

	@Override
	public EnOceanConnectionActivity connect2Gateway(String gatewayIP) {
		tracer.info( " -> connect2Gateway " + gatewayIP);
		int messageReceivePort = ra.getandRemoveFreePort();
		if (messageReceivePort == 0){
			tracer.severe( "TODO -> Error: no free Port available on EnOceanRA");//TODO
		}
		return ra.createEnOceanActivityHandler(gatewayIP, messageReceivePort);		
	}

	@Override
	public ConnectionID getActivityID() {
		return id;
	}

	@Override
	public List<Socket> getGatewayList() {
		return gatewayList;
	}

	@Override
	public GatewayConnectionActivity getGatewayActivity() {
		return this;
	}

	@SuppressWarnings("deprecation")
	public void close() {
		this.closeSockets();
		running = false;
	}

}

	
