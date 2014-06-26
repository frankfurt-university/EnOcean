
package de.EnOcean.jslee.ra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

import javax.slee.Address;
import javax.slee.EventTypeID;
import javax.slee.SLEEException;
import javax.slee.UnrecognizedEventException;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ActivityIsEndingException;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.EventFlags;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireEventException;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.IllegalEventException;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;
import javax.slee.resource.StartActivityException;
import javax.slee.resource.UnrecognizedActivityHandleException;

import de.EnOcean.jslee.event.*;
import de.EnOcean.jslee.ratype.*;
import de.EnOcean.jslee.ra.*;


public class TcpResourceAdaptor implements ResourceAdaptor, ConnectionProvider, EnOceanProvider {
    // --- ResourceAdaptor implementation ---

    private ActivityHandle connectionId;
    private ArrayList<Socket> availableGatewayList = new ArrayList<Socket>();
    private ArrayList <Integer> availableEnOceanPortList;
    //EnOceanGatewayConnectionListener newGatewayConnectionListener;
    GatewayConnectionHandler gatewayConnectionHandler;
    
	public void setResourceAdaptorContext(ResourceAdaptorContext context) {
        this.context = context;
        this.tracer = context.getTracer(context.getEntityName());
        this.endpoint = context.getSleeEndpoint();
        
    }

    public void unsetResourceAdaptorContext() {
        this.context = null;
        this.tracer = null;
        this.endpoint = null;
    }

    public void raVerifyConfiguration( ConfigProperties properties )
    throws InvalidConfigurationException
{
//        int listenPort = ((Integer)properties.getProperty("ConnectionPort").getValue()).intValue();// port 2001 !!!!!!
//          if (listenPort <= 1024 || listenPort > 65535)
//            throw new InvalidConfigurationException("ListenPort must be between 1025 .. 65535");
//       tracer.info(">>>>>>>>>>>>>>>>check config IP>>>>>>>>>>>>>>" + ((Integer)properties.getProperty("ConnectionPort").getValue()).intValue());
          
//        int connectionPort= ((Integer)properties.getProperty("ConnectionHandlerPort").getValue()).intValue(); // port 2100!!!!!
//        tracer.info(">>>>>>>>>>>>>>>>>>>>check connection port config>>>>>>>>>>>>>" + ((Integer)properties.getProperty("ConnectionHandlerPort").getValue()).intValue());

        
   //          if (listenPort <= 1024 || listenPort > 65535)
//              throw new InvalidConfigurationException("ListenPort must be between 1025 .. 65535");
        
//        int connectionHandlerPort= ((Integer)properties.getProperty("ListenPort").getValue()).intValue(); // port 2003!!!!!
//        tracer.info(">>>>>>>>>>>>>>>>>>>>check connection port config>>>>>>>>>>>>>" + ((Integer)properties.getProperty("ListenPort").getValue()).intValue());
        
        //String gatewayIP = ((String)properties.getProperty("GatewayIP").getValue()).toString();
//        tracer.info("Gateway IP================>>>>>>>>"+    (String)properties.getProperty("GatewayIP").getValue().toString());
        
//        int telegramSendingPort= ((Integer)properties.getProperty("TelegramSendingPort").getValue()).intValue(); // port 2003!!!!!
//        tracer.info(">>>>>>>>>>>>>>>>>>>>check connection port config to send telegram message>>>>>>>>>>>>>" + ((Integer)properties.getProperty("TelegramSendingPort").getValue()).intValue());
        
        
        
    }

    public void raConfigure( ConfigProperties properties )
    {
        marshaler = new TcpMarshaler();
//        connectionMap = new HashMap<ConnectionID,GatewayConnectionHandler>();
//        enOceanActivityMap = new HashMap<ConnectionID,EnOceanActivityHandler>();
        connectionMap = new HashMap<ConnectionID,ActivityHandler>();
        

        // Get configuration property
        listenPort = ((Integer)properties.getProperty("ListenPort").getValue()).intValue();
        connectionPort = ((Integer)properties.getProperty("ConnectionPort").getValue()).intValue();
        //connectionHandlerPort = ((Integer)properties.getProperty("ConnectionHandlerPort").getValue()).intValue();
        telegramSendingPort = ((Integer)properties.getProperty("ListenPort").getValue()).intValue();
        
        gatewayIP = ((String)properties.getProperty("GatewayIP").getValue()).toString();
      //   Get Rhino-specific configuration property
        ConfigProperties.Property nodeIDProp = properties.getProperty("slee-vendor:com.opencloud.rhino_node_id");
        nodeID = nodeIDProp != null ? (Integer)nodeIDProp.getValue() : 0;

        EventTypeID messageRequestEventType = new EventTypeID( "MessageEvent_REQUEST", "EnOceanFH", "1.0" );
        EventTypeID messageResponseEventType = new EventTypeID( "MessageEvent_RESPONSE", "EnOceanFH", "1.0" );
        EventTypeID enOceanEventType = new EventTypeID( "EnOceanEvent", "de.EnOcean.jslee.event", "1.0" );
        
        try {
           
            enOceanEventID = context.getEventLookupFacility().getFireableEventType( enOceanEventType);
            
//            tracer.info(">>>>>>>>>>>>>>>>>>>EVENT ID, request==========>>>>>>>>>>>>>>>>>>>>>>>>"+ context.getEventLookupFacility().getFireableEventType( messageRequestEventType));
//            tracer.info(">>>>>>>>>>>>>>>>>>>EVENT ID, response==========>>>>>>>>>>>>>>>>>>>>>>>>"+ context.getEventLookupFacility().getFireableEventType( messageResponseEventType));
            
            tracer.info("created");   
            setState(STATE_INACTIVE);
        }
        catch (UnrecognizedEventException uee) {
            tracer.severe("No event ID found for " + messageRequestEventType + " or " + messageResponseEventType + ", cannot initialise");
        }
    }
    
    public void raConfigurationUpdate( ConfigProperties properties )
    {
        // Update configuration property
        listenPort = ((Integer)properties.getProperty("listenPort").getValue()).intValue();
        connectionPort = ((Integer)properties.getProperty("connectionPort").getValue()).intValue();
        //connectionHandlerPort= ((Integer)properties.getProperty("connectionHandlerPort").getValue()).intValue();
        gatewayIP = ((String)properties.getProperty("GatewayIP").getValue()).toString();
    }

    public void raUnconfigure() {
        tracer.info("removed");
        setState(STATE_UNCONFIGURED);
    }

    public void raActive() {
    	initEnOceanPortMap();
        if (getState() != STATE_INACTIVE) {
            tracer.warning("Initialisation failed, not starting");
            return;
        }

        try {
            //this.connector = new Connector(tracer, this, nodeID);
            
            ConnectionID id = new ConnectionID(ConnectionID.GATEWAY_AC, nodeID,0);
            gatewayConnectionHandler= new GatewayConnectionHandler(tracer, this, id, connectionPort, availableGatewayList);
            gatewayConnectionHandler.start();
       
            tracer.info(">>>>>>>>>>>>>> EnOceanRA v1.15 ready <<<<<<<<<<<<<<<< ");
            //Thread connectionlistener = new Thread (new ConnectionListener(tracer,this, nodeID, connectionPort));
            
            
//            //Warte auf Verbindung von Gateways auf Port 2001
//            newGatewayConnectionListener= new EnOceanGatewayConnectionListener(tracer, this, nodeID, connectionPort, gatewayList);//Thread (new EnOceanGatewayConnectionListener(tracer, this, nodeID, connectionPort, gatewayList));
//            newGatewayConnectionListener.start();

            
//            tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>openning handler port>>>>>>>>>>>>>>>>>>>>>");
//            Thread connectionhandlerlistener =  new Thread(new ConnectionHandlerListener(tracer,this,nodeID,connectionHandlerPort));
//            
//            tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>openning listener port >>>>>>>>>>>>>>>>>>>>>>>>");
//       
//            Thread listener = new Thread (new Listener(tracer, this, nodeID, listenPort,gatewayIP, listenSocket));
//         
//            BufferedReader in = new BufferedReader(
//            new InputStreamReader(listenSocket.getInputStream()));
//            tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>opened client socket on port 2003 in the EnOceanRA >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>Message as a string from the gateway to the EnOceanRA on port 2003::" + in.readLine());
//            
//           Thread telegramsender = new Thread (new TelegramSender(tracer, this, nodeID, telegramSendingPort,gatewayIP));
            
//            
//            this.multiserver = new MultiServer(tracer, this, nodeID, connectionPort, listenPort, connectionHandlerPort );
//            
        
            
//            connectionlistener.start(); //port 2001
//            connectionhandlerlistener.start();  // port 2100
//           listener.start();  //port 2003
//            telegramsender.start();//port 2005
//            multiserver.start();
           
            
//            try {
//            connectionlistener.join(); //port 2001
//            connectionhandlerlistener.join();  // port 2100
//            listener.join();  //port 2003
//            telegramsender.join();//port 2005
//            } catch (InterruptedException ex){
//            	ex.printStackTrace();
//            	
//            	tracer.info(">>>>>>>>>>>>>>>>>>>><________________done_______________>>>>>>>>>>>>>>>>>>>>");
//            }
            
            setState(STATE_ACTIVE);
        }
        catch (Exception e) {
            tracer.warning("Unable to activate RA entity", e);
        }
        tracer.info("started");
    }

	public void raStopping() {
    	tracer.info("stopping - going down");
         //gatewayConnectionHandler.closeSockets();
         //enOceanActivity.closeSockets();         
         Iterator keysIT = connectionMap.keySet().iterator();
         while(keysIT.hasNext()){
        	 ConnectionID connectionID = (ConnectionID) keysIT.next();
        	 ActivityHandler ac = connectionMap.get(connectionID);
        	 if (ac instanceof GatewayConnectionHandler){
        		 ((GatewayConnectionHandler)ac).close();       		 
        	 }
        	 if (ac instanceof EnOceanActivityHandler){
        		 ((EnOceanActivityHandler)ac).close();       		 
        	 }
         }
         
         
         if (getState() != STATE_ACTIVE) return;

        // Shutdown the listener so that no new connections can be created
        tracer.info("stopping - no new activities will be created");
    }

    public void raInactive() {
        if (getState() != STATE_ACTIVE) return;

        connectionMap.clear();
        //enOceanActivityMap.clear();
        tracer.info("stopped");
        setState(STATE_INACTIVE);
    }

    // optional call back method
    public void eventProcessingSuccessful( ActivityHandle handle, FireableEventType eventType, Object event, Address address,
                                           ReceivableService service, int flags )
    {
        tracer.fine("eventProcessingSuccessful: ah=" + handle + ", eventType=" + eventType + ", event=" + event + ", flags=" + flags);
        if ((flags & EventFlags.SBB_PROCESSED_EVENT) == 0) {
            ConnectionHandler connection = (ConnectionHandler) getActivity(handle);
            try {
                connection.sendMessage("RA: No SBBs active!");
                
            } catch (Exception e) {
                tracer.warning("error sending message", e);
            }
        }
    }

   // optional call back method
    public void eventProcessingFailed( ActivityHandle handle, FireableEventType eventType, Object event, Address address,
                                       ReceivableService service, int flags, FailureReason reason )
    {
        tracer.fine("eventProcessingFailed: ah=" + handle + ", eventType=" + eventType + ", event=" + event + ", flags=" + flags + ", reason=" + reason);
        ConnectionHandler connection = (ConnectionHandler) getActivity(handle);
        try {
            connection.sendMessage("RA: Event failed!");
        } catch (Exception e) {
            tracer.warning("error sending message", e);
        }
    }        

    public void activityEnded(ActivityHandle handle) {
        synchronized (connectionMap) {
            connectionMap.remove(handle);         
        }
//        synchronized (enOceanActivityMap) {
//        	enOceanActivityMap.remove(handle);         
//        }enOceanActivityMap
        
    }

    public Marshaler getMarshaler() {
        return marshaler;
    }

    public void serviceActive( ReceivableService serviceInfo )
    {
        // Do nothing...
    }

    public void serviceInactive( ReceivableService serviceInfo )
    {
        // Do nothing...
    }

    public void serviceStopping( ReceivableService serviceInfo )
    {
        // Do nothing...
    }

    
    // mandatory call back method
    public void queryLiveness(ActivityHandle handle) {
        if (handle instanceof ConnectionID) {
            ConnectionHandler connection = (ConnectionHandler)getActivity(handle);
            if (connection == null || !connection.isOpen()) {
                connectionClosed((ConnectionID)handle);
            }
        }
        else {
            tracer.warning("unrecognized activity handle: " + handle);
        }
    }

    
    // mandatory call back method
    public Object getActivity(ActivityHandle handle) {
        synchronized (connectionMap) {
            return connectionMap.get(handle);
        }
    }
    
//    // mandatory call back method
//    public ActivityHandle getActivityHandle(Object activity) {
//        synchronized (connectionMap) {
//            return ((ConnectionHandler)activity).getConnectionID();
//        }
//    }
    
    // mandatory call back method
    public ActivityHandle getActivityHandle(Object activity) {
    	try{
    		synchronized (connectionMap) {
    			return ((GatewayConnectionHandler) activity).getActivityID();  
    		}
    	}catch(ClassCastException cce){
    		synchronized (connectionMap) {
    			return ((EnOceanActivityHandler) activity).getActivityID();  
    		}
    	}
    }
   
    // mandatory call back method
    public Object getResourceAdaptorInterface(String className) {
        return this;
    }

    public void administrativeRemove(ActivityHandle handle) {
        ConnectionHandler handler = (ConnectionHandler)getActivity(handle);
        if (handler != null) handler.close();
    }

    public void eventUnreferenced( ActivityHandle handle, FireableEventType eventType, Object event, Address address,
                                   ReceivableService service, int flags )
    {
    }
    public void activityUnreferenced(ActivityHandle handle) {}

    // --- End of ResourceAdaptor implementation ---

    // --- Local utility methods ---

//    /**
//     * Callback from {@link ConnectionHandler}. A new connection has been opened
//     * @param newConnection the new connection
//     */
    void connectionOpened(ConnectionHandler newConnection) {
//        synchronized (connectionMap) {
//            connectionMap.put(newConnection.getConnectionID(), newConnection);
//            
//        }
//        tracer.fine("connection opened, starting activity");
//        // Tell the SLEE about the new activity
//        try
//        {
//            endpoint.startActivity(newConnection.getConnectionID(),newConnection);
//        }
//        catch ( StartActivityException e )
//        {
//            tracer.warning( "Failed to start the activity: " + e.getMessage(), e);
//        }
    	tracer.info("ERROR connectionOpened is old implementation!");
    }
//    /**
//     * Callback from {@link ConnectionHandler}. A new connection has been opened
//     * @param newConnection the new connection
//     */
//    
//    void connectionOpened(GatewayConnectionHandler newConnection) {
//        synchronized (connectionMap) {
//            connectionMap.put(newConnection.getConnectionID(), newConnection);
//            
//        }
//        tracer.fine("connection opened, starting activity");
//        // Tell the SLEE about the new activity
//        try
//        {
//            endpoint.startActivity(newConnection.getConnectionID(),newConnection);
//        }
//        catch ( StartActivityException e )
//        {
//            tracer.warning( "Failed to start the activity: " + e.getMessage(), e);
//        }
//    }
  /**
  * Callback from {@link ConnectionHandler}. A new connection has been opened
  * @param newConnection the new connection
  */
 
 void activityCreated(ActivityHandler newConnection) {
     synchronized (connectionMap) {
         connectionMap.put(newConnection.getActivityID(), newConnection);
         
     }
     tracer.fine("connection opened, starting activity");
     // Tell the SLEE about the new activity
     try
     {
         endpoint.startActivity(newConnection.getActivityID() ,newConnection);
     }
     catch ( StartActivityException e )
     {
         tracer.warning( "Failed to start the activity: " + e.getMessage(), e);
     }
 }    
//    /**
//     * Callback from {@link ConnectionHandler}. A new connection has been opened
//     * @param newConnection the new connection
//     */
//    void enOceanActivityOpened(EnOceanActivityHandler enOceanActivity) {
//        synchronized (enOceanActivityMap) {
//        	enOceanActivityMap.put(enOceanActivity.getConnectionID(), enOceanActivity);
//            
//        }
//        tracer.fine("connection opened, starting activity");
//        // Tell the SLEE about the new activity
//        try
//        {
//            endpoint.startActivity(enOceanActivity.getConnectionID(),enOceanActivity);
//        }
//        catch ( StartActivityException e )
//        {
//            tracer.warning( "Failed to start the activity: " + e.getMessage(), e);
//        }
//    }

    /**
     * Callback from {@link ConnectionHandler}. The connection has been closed, end the activity
     * @param closedConnection the ID of the {@link ConnectionHandler} that was closed
     */
    void connectionClosed(ConnectionID closedConnection) {
        tracer.fine("connection closed, ending activity");
        // Submit an activity end event - but only if we are not already deactivated
        if (getState() == STATE_ACTIVE) {
            try {
                endpoint.endActivity(closedConnection);
                // The SLEE will clean up and then call activityEnded() - we clean up our connection map then
            }
            catch (UnrecognizedActivityHandleException uahe) {
                // ignore - the activity may have already ended via an administrative remove
            }
        }
    }

    /**
     * Callback from {@link ServerConnectionHandler}, some data was received. Create the
     * {@link MessageEventImpl} object for the data and submit the event to the SLEE.
     * @param connection the {@link ServerConnectionHandler} that received the data
     * @param message the data received
     */
    void connectionRequestEvent(ConnectionHandler connection, String message) {
        fireEvent( connection, message, messageRequestEventID );
    }
    
  
//    void GatewayConnectionEstablished(ConnectionListener connection, String message) {
//        fireEvent( connection, message, messageRequestEventID );
//    }
//    
//    void GatewayConnectionHandler(ConnectionHandlerListener connectionhandler, String message) {
//        fireEvent( connectionhandler, message, messageRequestEventID );
//    }
//    
//    void GatewayReadyEvent(Listener connection, String message) {
//        fireEvent( connection, message, messageRequestEventID );
//    }
//    
//    void GatewayStop(ConnectionHandler connection, String message) {
//        fireEvent( connection, message, messageRequestEventID );
//    }
    
    
    /**
     * Callback from {@link ClientConnectionHandler}, some data was received. Create the
     * {@link MessageEventImpl} object for the data and submit the event to the SLEE.
     * @param connection the {@link ClientConnectionHandler} that received the data
     * @param message the data received
     */
    
    // "EnOcean message event to be fired..."
    void connectionResponseEvent(ConnectionHandler connection, String message) {
        fireEvent( connection, message, messageResponseEventID );
    }
    

    
//    void OnReadyEvent(TelegramSender sendTelegramMessage, String message){
//    	fireEvent(sendTelegramMessage, message, messageReadyEventID);
//         sendTelegramMessage.start();    	
//    	
//    	
//    	 }

    
    
    
//    private void fireEvent(TelegramSender sendTelegramMessage,ConnectionHandler connection, String message, FireableEventType eventType) {
//        try {
//            tracer.fine("data received, firing message event");
//            MessageEvent event = new MessageEventImpl(message);
//            
//            int flags = EventFlags.REQUEST_PROCESSING_SUCCESSFUL_CALLBACK | EventFlags.REQUEST_PROCESSING_FAILED_CALLBACK;
//            endpoint.fireEvent( connection.getConnectionID(), eventType, event, null, null, flags );
//        }
//        catch (Exception e) {
//            tracer.warning("Error firing event to SLEE", e);
//        }
//    }
    
    

    
    void fireGATEWAY_LIST_EVENT(GatewayConnectionHandler activityHandler){
    	tracer.info("new gateway connection, firing EnOceanEvent");    	   	
    	EnOceanEvent enOceanEvent = new EnOceanEvent();
    	Iterator<Socket> gatewayListIterator = availableGatewayList.iterator();
    	ArrayList<Socket> gatewayIPList = new ArrayList<Socket>();
    	while (gatewayListIterator.hasNext()){
    		Socket gatewaySocket = gatewayListIterator.next();
    		gatewayIPList.add(gatewaySocket);
    	}
    	enOceanEvent.setPayload(EnOceanEvent.CONTENT, EnOceanEvent.GATEWAY_LIST_EVENT);
    	enOceanEvent.setPayload(EnOceanEvent.GATEWAY_LIST, gatewayIPList);
    	fireEnOceanEvent(activityHandler, enOceanEvent);
    }
    
    void fireTELEGRAM_RECEIVED_EVENT(EnOceanActivityHandler activityHandler, String incomingEnOceanMessage){
    	tracer.info("EnOceanMessage received -> firing EnOceanEvent");
    	EnOceanEvent enOceanEvent = new EnOceanEvent();    	
    	enOceanEvent.setPayload(EnOceanEvent.CONTENT, EnOceanEvent.TELEGRAM_RECEIVED_EVENT);
    	enOceanEvent.setPayload(EnOceanEvent.ENOCEAN_TELEGRAM, incomingEnOceanMessage);
    	fireEnOceanEvent(activityHandler, enOceanEvent);
    }
    
    void fireEnOceanEvent(Object activityHandler, EnOceanEvent enOceanEvent){
    	try {
//			endpoint.fireEvent( connection.getConnectionID(), enOceanEventID, enOceanEvent, null, null );
			endpoint.fireEvent( ((ActivityHandler) activityHandler).getActivityID(), enOceanEventID, enOceanEvent, null, null );
		} catch (UnrecognizedActivityHandleException e) {
			tracer.severe("UnrecognizedActivityHandleException");
			e.printStackTrace();
		} catch (IllegalEventException e) {
			tracer.severe("IllegalEventException");
			e.printStackTrace();
		} catch (ActivityIsEndingException e) {
			tracer.severe("ActivityIsEndingException");
			e.printStackTrace();
		} catch (NullPointerException e) {
			tracer.severe("NullPointerException");
			e.printStackTrace();
		} catch (SLEEException e) {
			tracer.severe("SLEEException");
			e.printStackTrace();
		} catch (FireEventException e) {
			tracer.severe("FireEventException");
			e.printStackTrace();
		}
    }
    
	public void fireGATEWAY_READY_EVENT( EnOceanConnectionActivity enOceanActivity, String readyMessage) {
    	tracer.info("Ready received -> firing EnOceanEvent");
    	EnOceanEvent enOceanEvent = new EnOceanEvent();    	
    	enOceanEvent.setPayload(EnOceanEvent.CONTENT, EnOceanEvent.GATEWAY_READY_EVENT);
    	enOceanEvent.setPayload(EnOceanEvent.READY_MESSAGE, readyMessage);
    	fireEnOceanEvent(enOceanActivity, enOceanEvent);
		
	}
    
    // "EnOcean"
    private void fireEvent(ConnectionHandler connection, String message, FireableEventType eventType) {
        try {
            tracer.fine("data received, firing message event");
            MessageEvent event = new MessageEventImpl(message);
            
            int flags = EventFlags.REQUEST_PROCESSING_SUCCESSFUL_CALLBACK | EventFlags.REQUEST_PROCESSING_FAILED_CALLBACK;
            endpoint.fireEvent( connection.getConnectionID(), eventType, event, null, null, flags );
        }
        catch (Exception e) {
            tracer.warning("Error firing event to SLEE", e);
        }
    }
    
   
    public void DisconnectFromGateway(){
    	try {
    		tracer.fine("Connection is disconnected between the GW & AS"); 
    	}
    	catch (Exception e){
    		tracer.warning("error while disconnecting b/w the GW & AS");
    	}
    	
    }
    
    
    
    
    

    private synchronized int getState() {
        return state;
    }

    private synchronized void setState(int newState) {
        state = newState;
    }

    // --- Implementation of the ConnectionProvider ---
    
    public ClientConnectionActivity createClientConnection(InetSocketAddress address) throws IOException {
        //ConnectionID connectionId = connector.connectClient( address );
        
       
        
       
        return (ClientConnectionActivity) getActivity(connectionId);
    }
    
    public void setAll(String allString){
    	tracer.info("<<<***setAll called from SBB***>>>");
    }
    
	@Override
	public GatewayConnectionActivity getGatewayConnection() throws IOException {
		return (GatewayConnectionActivity) this.gatewayConnectionHandler;
	}
    
    public SendTelegramMessageActivity sendTelegramMessage( TelegramMessage telegram) throws IOException {
  
    	Thread telegramsender;
		try {
			telegramsender = new Thread (new TelegramSender(tracer, this, nodeID, telegramSendingPort,gatewayIP));
			telegramsender.start();
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	    TelegramMessage telegram1;
	    
	    
	    
	    
	    return (SendTelegramMessageActivity)   getActivity(connectionId) ;
    }
    

    
    
    
    // --- Fields ---

    // Config Property values
    
    
    
    
    
    
    

    /**
     * Rhino node ID (if running in Rhino).
     * The node ID is used here to generate activity handles that are unique across a cluster.
     */
    private int nodeID;

    /** Config property to specify the TCP port that the RA will listen on. */
    private int listenPort;
    
    private String gatewayIP;
    
    private int connectionPort;
    
    private int readyPort = 2003;
    private int sendTelegramPort = 2005;
    
   //private int connectionHandlerPort;
   
   private int telegramSendingPort;
    // Other fields
    private ResourceAdaptorContext context;

    // The SLEE endpoint that we submit events to
    private SleeEndpoint endpoint;

    // The SLEE-allocated event ID for our event, this must be obtained
    // using the event lookup facility, see entityCreated().
    private FireableEventType messageRequestEventID;
    private FireableEventType messageResponseEventID;
    private FireableEventType messageReadyEventID;
    private FireableEventType enOceanEventID;
    
    
 

    // Marshaler for efficiently (de)serializing activity handles and events
    private Marshaler marshaler;

    // The listener thread, waits for new connections
   
    
    private Socket listenSocket;
    
    
    //private MultiServer multiserver;
    
    
    private ConnectionListener connectionlistener;
    
    
    private ConnectionHandlerListener connectionhandlerlistener;
    
   private TelegramSender telegramsender;
    
    
    // Connector, creates new client connections
    //private Connector connector;

    // Map of "live" activities, maps ConnectionID -> ConnectionHandler
//    private Map<ConnectionID,GatewayConnectionHandler> connectionMap;
//    private Map<ConnectionID,EnOceanActivityHandler> enOceanActivityMap;
    private Map<ConnectionID,ActivityHandler> connectionMap;

    // The internal state of the resource adaptor
    private static final int STATE_UNCONFIGURED = 0;
    private static final int STATE_INACTIVE = 1;
    private static final int STATE_ACTIVE = 2;
    private int state = STATE_UNCONFIGURED;

    private Tracer tracer;
	private static int enOceanActivityID;

	public int getandRemoveFreePort() {
		if(availableEnOceanPortList.isEmpty()){
			return 0;
		}
		int freePort;
		freePort = availableEnOceanPortList.get(0);
		availableEnOceanPortList.remove(0);
		return freePort;
	}
	
	public void addFreePort(Integer port){
		availableEnOceanPortList.add(port);
	}
	
    private void initEnOceanPortMap() {
    	int i=2100;
    	availableEnOceanPortList = new ArrayList<Integer>();
    	while (i<2200)
    	{
    		availableEnOceanPortList.add(i);
    		i++;
    	}
	}

	public EnOceanConnectionActivity createEnOceanActivityHandler(String gatewayIP, int messageReceivePort) {
		Iterator<Socket> gwlIT = availableGatewayList.iterator();
		while(gwlIT.hasNext()){
			Socket gwSocket = (Socket) gwlIT.next();
			if(gwSocket.getInetAddress().getHostAddress().equalsIgnoreCase(gatewayIP)){
				tracer.severe("Socket gefunden!: " + gatewayIP + ":" + messageReceivePort);
				return createEnOceanActivityHandler(gwSocket, messageReceivePort);
			}
		}
		tracer.severe( "TODO -> createEnOceanActivityHandler failed! -> Gateway socket not found!");
		return null;
	}

	private EnOceanConnectionActivity createEnOceanActivityHandler(Socket gwSocket, int messageReceivePort) {
        ConnectionID id = new ConnectionID(ConnectionID.ENOCEAN_AC, nodeID,enOceanActivityID++);
		EnOceanActivityHandler eoACHandler = new EnOceanActivityHandler(tracer, this, id, gwSocket, messageReceivePort, readyPort, sendTelegramPort);
		eoACHandler.start();	
		return eoACHandler;
		
	}



}
