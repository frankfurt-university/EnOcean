package de.EnOcean.jslee.ra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.slee.facilities.FacilityException;
import javax.slee.facilities.Tracer;

public class IncomingEnOceanMessageHandler extends Thread{

	private Tracer tracer;
	private TcpResourceAdaptor ra;
	private ServerSocket listenSocket;
	private int messageReceivePort;
	private EnOceanActivityHandler eoAH;
	private boolean running = true;

	public IncomingEnOceanMessageHandler(Tracer tracer,EnOceanActivityHandler eoAH, TcpResourceAdaptor ra,
			int messageReceivePort) throws IOException {
	    super( "IncomingEnOceanTelegramHandlerThread" );
	    this.tracer = tracer;
	    this.ra = ra;
	    this.messageReceivePort = messageReceivePort;
	    this.eoAH = eoAH;
	}
	
	public void run(){	
		
		
	    try {
			this.listenSocket = new ServerSocket( messageReceivePort );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    tracer.info( "EnOceanActivityHandler-> receive EnoceanMessages on Socket: " + listenSocket.getInetAddress().getHostAddress() + ":" + messageReceivePort);
		while ( running )
		{
			BufferedReader in = null;
			Socket cSocket = null;
			try
			{
				tracer.info("____----->>>> Waiting for EnOcean telegram <<<<-----____");
				eoAH.setReceiveSocketReady();
				cSocket = listenSocket.accept();	
				in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));	
				fireEnOceanEvent(readIncomingEnoceanMessage(in));
//				in.close();
//				cSocket.close();
			}
			catch ( IOException e )
			{
				// socket closed - shutdown     
			}
			
			try {
				in.close();
			} catch (NullPointerException e) {
				//			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				cSocket.close();
			} catch (NullPointerException e) {
				//
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}
	

	public void close(){
		try {
			running = false;
			listenSocket.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void fireEnOceanEvent(String incomingEnOceanMessage) {
	    ra.fireTELEGRAM_RECEIVED_EVENT(eoAH , incomingEnOceanMessage);
	}

	private String readIncomingEnoceanMessage(BufferedReader in) {
		String incomingEnOceanMessage = null;
		try {
			incomingEnOceanMessage = in.readLine();
			tracer.info("Incoming EnOcean Message: " + incomingEnOceanMessage);
		} catch (FacilityException e) {
			tracer.severe("____----->>>> FacilityException! <<<<-----____");     
			e.printStackTrace();
		} catch (NullPointerException e) {
			tracer.severe("____----->>>> NullPointerException! <<<<-----____");
			e.printStackTrace();
		} catch (IOException e) {
			tracer.severe("____----->>>> IOException! <<<<-----____");
			e.printStackTrace();
		}
		return incomingEnOceanMessage;
	}
}
