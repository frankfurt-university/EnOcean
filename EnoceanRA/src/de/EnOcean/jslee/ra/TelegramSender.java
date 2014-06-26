package de.EnOcean.jslee.ra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.slee.facilities.Tracer;

// openning port 2003 to make sure the gateway is ready

/**
 * Simple listener thread, listens on a TCP port and creates new {@link ServerConnectionHandler} 
 * objects when connections are accepted.
 */
class TelegramSender
    extends Thread
{
    

	private int listenPort;
	private String gatewayIP;

	TelegramSender( Tracer tracer, TcpResourceAdaptor ra, int myNodeID, int telegramSendingPort,String gatewayIP )
        throws Exception
    {
        super( "TCP RA Listener Thread" );
        this.tracer = tracer;
        this.ra = ra;
        this.myNodeID = myNodeID;
        this.TelegramMessageSendingSocket = new Socket( gatewayIP,telegramSendingPort );
    }

    public void run()
    {
    	
    	
        tracer.info( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>version 5, accepting listener port 2005<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        PrintWriter outSend = null;
        
		try
        {
      //  Socket echoSocket = null;
       
        
        tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>openning client socket on port 2005 in the EnOceanRA >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        
        //listenSocket = new Socket("gatewayIP", telegramSendingPort);
		
        outSend = new PrintWriter( TelegramMessageSendingSocket.getOutputStream(), true );
        
		//BufferedReader out = new BufferedReader(TelegramMessageSendingSocket.getOutputStream());
        tracer.info(">>>>>>>>>>>>>>>>>>openning PORT 2005,for sending telegram messages>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        
       
		
        }
		catch ( IOException e )
		{
			tracer.info(">>>>>>>>>>>>>>>>>>error while openning port 2005>>>>>>>>>>>>>>>>>>>>>");
            
        }
	    
    	    
        while ( running )
        {
            Socket sock;
            
            outSend.println("a55a6b0550000000fff7098030"); 
    		tracer.info(">>>>>>>>>>>>>>>>>>>sending telegram message a55a6b0550000000fff7098030 //// light on ");
    		outSend.flush();
    	    outSend.close();
            
            
            
            tracer.info(">>>>>>>>>>>>>>>>>>>>>>>>openning client socket on port 2005 in the EnOceanRA >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            break;
			

        }
    }
        
      
    
//    }
////    
//    
//    
//    
//    /**
//     * Close the listen socket and stop the thread, called when the RA is deactivated.
//     */
//    void close()
//    {
//        running = false;
//        shutdown();
//    }
//
//    
//    
//    
//    
//    private void shutdown()
//    {
//        try
//        {
//            if ( !listenSocket.isClosed() )
//            {
//                tracer.fine( "shutting down" );
//                listenSocket.close();
//            }
//        }
//        catch ( Exception e )
//        {
//            tracer.severe( "error closing listen socket", e );
//       }
//   }


    
    private volatile boolean running = true;

    private final TcpResourceAdaptor ra;

    private final int myNodeID;

    private int sequence = 0;
    
    private String NextLine;

    private Socket TelegramMessageSendingSocket;

    private static Tracer tracer;
}
