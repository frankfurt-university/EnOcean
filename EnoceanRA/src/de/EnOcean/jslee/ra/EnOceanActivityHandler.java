  package de.EnOcean.jslee.ra;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.slee.facilities.Tracer;

import de.EnOcean.jslee.ratype.EnOceanConnectionActivity;



public class EnOceanActivityHandler extends Thread implements EnOceanConnectionActivity, ActivityHandler{
	
	private Tracer tracer;
	private TcpResourceAdaptor ra;
	private Socket gwSocket;
	private int messageReceivePort;
	ConnectionID id;
	private boolean isReceiveSocketReady = false;
	Thread incomingEnOceanMessageHandlerThread = null;
	IncomingEnOceanMessageHandler incomingEnOceanMessageHandler = null;
	ReadySocketHandler rsHandler = null;
	SendTelegramHandler sendTelegramHandler = null;
	private int readyPort;
	private int sendTelegramPort;
	
	public EnOceanActivityHandler(Tracer tracer,
			TcpResourceAdaptor ra, ConnectionID id,
			Socket gwSocket, int messageReceivePort, int readyPort, int sendTelegramPort) {
		this.tracer = tracer;
		this.ra = ra;
		this.gwSocket = gwSocket;
		this.messageReceivePort = messageReceivePort;
		this.id = id;
		this.readyPort = readyPort;
		this.sendTelegramPort = sendTelegramPort;
		ra.activityCreated( this );
	}
	
	public void run(){	

		try {
			incomingEnOceanMessageHandler = new IncomingEnOceanMessageHandler(tracer,this,this.ra,this.messageReceivePort);
			incomingEnOceanMessageHandlerThread = new Thread(incomingEnOceanMessageHandler);
		} catch (IOException e1) {
			tracer.severe( "Not able to create thread for incoming EnOcean Telegram Message: " + gwSocket.getInetAddress().getHostAddress() + ":" + messageReceivePort);
			e1.printStackTrace();
		}
		incomingEnOceanMessageHandlerThread.start();
				
		try {
			rsHandler = new ReadySocketHandler(tracer, this, ra, gwSocket.getInetAddress().getHostAddress(), readyPort);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		sendTelegramHandler = new SendTelegramHandler(tracer, this, ra, gwSocket.getInetAddress().getHostAddress(), sendTelegramPort);

		//waiting for server socket listen on messageReceivePort
		while(!isReceiveSocketReady){
			try {
				sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	    Scanner in = null;
		try {
			in = new Scanner( gwSocket.getInputStream() );
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    PrintWriter out = null;
		try {
			out = new PrintWriter( gwSocket.getOutputStream(), true );
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    
	    out.println("accept#"+System.nanoTime()+"*"+messageReceivePort);
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

	@Override
	public ConnectionID getActivityID() {
		return id;
	}

	@Override
	public EnOceanConnectionActivity getEnOceanActivity() {
		return this;
	}
	
	public void setReceiveSocketReady(){
		isReceiveSocketReady = true;
	}

	public synchronized void closeSockets(){
		tracer.info( "Trying to close all open EnOceanActivity connections!" );
		try {
			gwSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
				
	}
	
	
	
	public void close() {
		closeSockets();
		incomingEnOceanMessageHandler.close();
		rsHandler.close();
		
	}

	@Override
	public void checkReadyStatus() {
		Thread rsThread = new Thread(rsHandler);
		rsThread.start();		
	}

	@Override
	public void sendEnOceanTelegram(String telegram) {
		sendTelegramHandler.setEnOceanTelegram(telegram);
		Thread sendTelegramThread = new Thread(sendTelegramHandler);
		sendTelegramThread.start();
	}

}
