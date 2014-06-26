package de.EnOcean.jslee.ra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.slee.facilities.Tracer;


///////////////////////////////////////////////////
// ////...........opening port 2001 for establishing connection ///////////
// ////..........handles connection on port 2001////////////////////////
///////////////$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

class ConnectionListener extends Thread
{



	private int connectionPort;

	ConnectionListener( Tracer tracer, TcpResourceAdaptor ra, int myNodeID, int connectionPort )
    throws Exception
{
    super( "TCP RA Listener Thread" );
    this.tracer = tracer;
    this.ra = ra;
    this.myNodeID = myNodeID;
    this.listenSocket = new ServerSocket( connectionPort );
}
	
	public void run()
    {
    	
    	
        
		tracer.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>version 6, connection listener port 2001 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        
		
		
	
	 while ( running )
     {
//         Socket sock;
//        ServerSocket serverS = null;
		Socket client;
         
         try
         {
        	 
        	 tracer.info(">>>>>>>>>>>openning server socket on port 2001/ establishing connection b/w the gateway and the EnOceanRA >>>>>>>>");
        	 
//             sock = listenSocket.accept();
        //	 listenSocket = new ServerSocket( connectionPort );
              client = listenSocket.accept(); 
 	          handleConnection ( client );
 	         tracer.info(">>>>>>>>>>connection established between the gateway & the EnOceanRA on port 2001>>>>>>>>>>>>>>>");
             
         }
         catch ( IOException e )
         {
        	 tracer.info(">>>>>>>>>>failed to establish connection between the gateway & the EnOceanRA on port 2001>>>>>>>>>>>>>>>");
          
         }
	
       }
   
 }


//	   void close()
//	    {
//	        running = false;
//	        shutdown();
//	    }

	    
	   
	    
//	    private void shutdown()
//	    {
//	        try
//	        {
//	            if ( !listenSocket.isClosed() )
//	            {
//	                tracer.fine( "shutting down" );
//	                listenSocket.close();
//	            }
//	        }
//	        catch ( Exception e )
//	        {
//	            tracer.severe( "error closing listen socket", e );
//	       }
//	   }
	
	
		private static void handleConnection( Socket client ) throws IOException 
    	{ 
    		tracer.info("BSC-TX DEBUGPOINT 4" + " BSC-BAP-TX versucht eine Verbindung aufzubauen!");
    	    Scanner     in  = new Scanner( client.getInputStream() ); 
    	    PrintWriter out = new PrintWriter( client.getOutputStream(), true ); 
    	    
    	    out.println("accept#"+System.nanoTime()+"*2100");
    	    int i=0;
    	    String bscIdentityString, baptxID, connection, version;
    	    while (in.hasNext()){
    	    	bscIdentityString = in.nextLine();
    	    	tracer.info(i + " bscIdentityString: " +bscIdentityString);
    	    	
    	    	baptxID = bscIdentityString.substring(0, bscIdentityString.indexOf("#"));
    	    	tracer.info(i + " baptxID: " +baptxID);
    	    	
    	    	connection = bscIdentityString.substring(bscIdentityString.indexOf("#")+1,bscIdentityString.lastIndexOf("#"));
    	    	tracer.info(i + " connection: " +connection);
    	    	
    	    	version = bscIdentityString.substring(bscIdentityString.lastIndexOf("#")+1);
    	    	tracer.info(i + " version: " +version);
    	    	i++;
    	    }
    	}
		
		
		
		
		
	    private volatile boolean running = true;

	    private final TcpResourceAdaptor ra;

	    private final int myNodeID;

	    private int sequence = 0;

	    private ServerSocket listenSocket;

	    private static Tracer tracer;
		
		
		
		
	
}
