package de.EnOcean.jslee.ra;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.slee.facilities.Tracer;

public class SendTelegramHandler extends Thread{

	private Tracer tracer;
	@SuppressWarnings("unused")
	private TcpResourceAdaptor ra;
	@SuppressWarnings("unused")
	private EnOceanActivityHandler eoAH;
	private String gatewayIP;
	private int sendTelegramPort;
	private Socket runSocket;
	private String enOceanTelegram;

	public SendTelegramHandler(Tracer tracer,
			EnOceanActivityHandler enOceanActivityHandler,
			TcpResourceAdaptor ra, String gatewayIP, int sendTelegramPort) {
	    super( "sendEnOceanTelegramHandlerThread" );
	    this.tracer = tracer;
	    this.ra = ra;
	    this.eoAH = enOceanActivityHandler;
	    this.gatewayIP = gatewayIP;
	    this.sendTelegramPort = sendTelegramPort;
	}

	public void setEnOceanTelegram(String enOceanTelegram){
		this.enOceanTelegram = enOceanTelegram;
	}
	
	public void run(){
		tracer.info( "> initialising telegram send port " + sendTelegramPort + " telegram message: " + this.enOceanTelegram);
		try {
			this.runSocket = new Socket( gatewayIP, sendTelegramPort);
		} catch (UnknownHostException e2) {
			tracer.severe("____----->>>> UnknownHostException! <<<<-----____");     
			e2.printStackTrace();
		} catch (IOException e2) {
			tracer.severe("____----->>>> IOException! <<<<-----____");
			e2.printStackTrace();
		}
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(runSocket.getOutputStream()));	
				out.write(enOceanTelegram);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				out.close();
			} catch (NullPointerException e) {	
			} catch (IOException e) {
				e.printStackTrace();
			}	
		
			try {
				runSocket.close();
			} catch (NullPointerException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void close(){
		try {	
			runSocket.close();
		} catch (NullPointerException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
