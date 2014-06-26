package de.EnOcean.jslee.ra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.slee.facilities.FacilityException;
import javax.slee.facilities.Tracer;

public class ReadySocketHandler extends Thread{
	
	
	private Tracer tracer;
	private TcpResourceAdaptor ra;
	private EnOceanActivityHandler eoAH;
	private int readyPort;
	Socket readySocket;
	private String  gatewayIP;

	public ReadySocketHandler(Tracer tracer,EnOceanActivityHandler eoAH, TcpResourceAdaptor ra, String gatewayIP, int readyPort) throws IOException {
	    super( "IncomingEnOceanTelegramHandlerThread" );
	    this.tracer = tracer;
	    this.ra = ra;
	    this.eoAH = eoAH;
	    this.gatewayIP = gatewayIP;
	    this.readyPort = readyPort;
	}

	public void run(){
		tracer.info( "> initialising ready port " + readyPort );
		try {
			this.readySocket = new Socket( gatewayIP, readyPort);
		} catch (UnknownHostException e2) {
			tracer.severe("____----->>>> UnknownHostException! <<<<-----____");     
			e2.printStackTrace();
		} catch (IOException e2) {
			tracer.severe("____----->>>> IOException! <<<<-----____");
			e2.printStackTrace();
		}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(readySocket.getInputStream()));	
				ra.fireGATEWAY_READY_EVENT(eoAH.getEnOceanActivity(), readIncomingReadyMessage(in));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				in.close();
			} catch (NullPointerException e) {	
			} catch (IOException e) {
				e.printStackTrace();
			}	
		
			try {
				readySocket.close();
			} catch (NullPointerException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void close(){
		try {	
			readySocket.close();
		} catch (NullPointerException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String readIncomingReadyMessage(BufferedReader in) {
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
