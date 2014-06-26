package de.EnOcean.jslee.ra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.slee.facilities.Tracer;


// //////////////////////$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//////////////////////opening port 2100 to handle connection B/W gateway and the server
/////////////////////// connection handler for port 2100
////////////////////


 class ConnectionHandlerListener extends Thread{


	 private int connectionhandlerPort;

	ConnectionHandlerListener( Tracer tracer, TcpResourceAdaptor ra, int myNodeID, int connectionhandlerPort )
	    throws Exception
	{
	    super( "TCP RA Listener Thread" );
	    this.tracer = tracer;
	    this.ra = ra;
	    this.myNodeID = myNodeID;
	    this.listenSocket = new ServerSocket( connectionhandlerPort );
	}

	 
	 public void run()
	    {
	    	
	 
		 tracer.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>version 4, accepting connection handler listener port 2100<<<<<<<<<<<<<<<" );
	     tracer.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>accepting connection handler listener port 2100<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
	     tracer.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>accepting connection handler listener port 2100<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
	 
	        ServerSocket transferServer = null;
            Socket transferClient = null; 
			try {
				
				tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> openning port 2100 to handle connection>>>>>>>>>>>>>>>>>>>>");
				
				
				
				transferServer = new ServerSocket( connectionhandlerPort );
				
//				transferClient = transferServer.accept(); 
//		        handleTransferConnection ( transferClient ); 
				
				
				
				tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> port 2100 is opened to handle connection>>>>>>>>>>>>>>>>>>");
				tracer.info("====================================check point/PORT 2100=============================");
			} catch (IOException e) {
				tracer.info("==============================failed to handle connection on port 2100>>>>>>>>1111111>>>>>>>====================");
			} 
	       
			
			
//			     // ServerSocket transferServer = null;
//			      try 
//			      { 
//			    	  
//			    	tracer.info(">>>>>>>>>>>>>>>>>openning handling connnection to recieve messages from the GW>>>>>>>>>>>>>");
//			    	transferClient = transferServer.accept(); 
//			        handleTransferConnection ( transferClient ); 
//			        
//			       tracer.info(">>>>>>>>>>>>>openned handling connection to recieve messages>>>>>>>>>>>>");
//			      } 
//			      catch ( IOException e ) { 
//			    	tracer.info("==============================failed to handle connection on port 2100>>>>>>>>222222>>>>>>>>====================");
//			      }
//	     
 
	 
	 
	 while ( running )
     {
         Socket sock;
         try
         {
             sock = listenSocket.accept();
             
             tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> port 2100 is opened to handle connection>>>>>>>>>>>>>>>>>>");
             transferClient = transferServer.accept(); 
		     handleTransferConnection ( transferClient );
		     
		     tracer.info("====================================check point/PORT 2100=============================");
             
         }
         catch ( IOException e )
         {
             // socket closed - shutdown
          
         }
     }
       
	    }
 
        
			      
			      
			  private static void handleTransferConnection(Socket transferClient) throws IOException {
						
					    Scanner     in  = new Scanner( transferClient.getInputStream() ); 
					    PrintWriter out = new PrintWriter( transferClient.getOutputStream(), true ); 
					    String inData, inTelegramm = null, time;
					    String baptxID;
					    int i=0;
					    while (in.hasNext()){
					    	inData = in.nextLine();
					    	baptxID = inData.substring(0, inData.indexOf("#") );
					    	inTelegramm = inData.substring(inData.indexOf("#") + 1, inData.lastIndexOf("#") );
					    	time = inData.substring(inData.lastIndexOf("#") + 1);
//					    	System.out.println(i + " bscIdentityString: " +inData);
//					    	System.out.println(i + " baptxID: " +baptxID);
//					    	System.out.println(i + " inTelegramm: " +inTelegramm);
//					    	System.out.println(i + " time: " +time);
					    	i++;
					    }
					    if (inTelegramm.equals("a55a0b0530000000001e64533045")){
					    	tracer.fine("*************** Taste 1 gedrückt");
					    }else
					    if (inTelegramm.equals("a55a0b0500000000001e64532005")){
					    	tracer.info("*************** Taste losgelassen");
					    }else
					    if (inTelegramm.equals("a55a0b0510000000001e64533025")){
					    	tracer.info("*************** Taste 2 gedrückt");
					    }else
						if (inTelegramm.equals("a55a0b0570000000001e64533085")){
						    tracer.info("*************** Taste 3 gedrückt");
						}else
						if (inTelegramm.equals("a55a0b0550000000001e64533065")){
							tracer.info("*************** Taste 4 gedrückt");
						}else
						if (inTelegramm.equals("a55a0b0515000000001e6453302a")){
							tracer.info("*************** Taste 2 und 4 gedrückt");
						}else
						if (inTelegramm.equals("a55a0b0537000000001e6453304c")){
							tracer.info("*************** Taste 1 und 3 gedrückt");
						}else
						if (inTelegramm.equals("a55a0b0535000000001e6453304a")){
							tracer.info("*************** Taste 1 und 4 gedrückt");
						}else
						if (inTelegramm.equals("a55a0b0517000000001e6453302c")){
							tracer.info("*************** Taste 2 und 3 gedrückt");
						}else{
							tracer.info("*************** unbekannte Nachricht!!!");
						}	
					
					}  
			      
			      
			      
			
	 
	 
	   private volatile boolean running = true;

	    private final TcpResourceAdaptor ra;

	    private final int myNodeID;

	    private int sequence = 0;

	    private final ServerSocket listenSocket;

	    private static Tracer tracer;
	 
	 
}
