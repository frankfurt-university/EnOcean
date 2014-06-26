package de.fhffm.service.enocean_service;



import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sdp.BandWidth;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.OptionTag;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.ActivityEndEvent;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbID;
import javax.slee.SbbLocalObject;
import javax.slee.UnrecognizedActivityException;
import javax.slee.facilities.ActivityContextNamingFacility;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.Tracer;
import javax.slee.nullactivity.NullActivity;
import javax.slee.nullactivity.NullActivityContextInterfaceFactory;
import javax.slee.nullactivity.NullActivityFactory;
import javax.slee.serviceactivity.ServiceActivity;
import javax.slee.serviceactivity.ServiceActivityContextInterfaceFactory;
import javax.slee.serviceactivity.ServiceActivityFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.example.first.MsmlFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import de.fhffm.service.play_record.ParseExeption;


import de.EnOcean.jslee.event.EnOceanEvent;
import de.EnOcean.jslee.ra.ActivityHandler;
import de.EnOcean.jslee.ra.EnOceanActivityHandler;
import de.EnOcean.jslee.ratype.EnOceanConnectionActivity;
import de.EnOcean.jslee.ratype.EnOceanProvider;
import de.EnOcean.jslee.ratype.GatewayConnectionActivity;
import de.EnOcean.jslee.ratype.TcpActivityContextInterfaceFactory;
import de.fhffm.service.enocean_service.ServiceACIActivityContextInterface;

import net.java.slee.resource.sip.CancelRequestEvent;
import net.java.slee.resource.sip.DialogActivity;
import net.java.slee.resource.sip.SipActivityContextInterfaceFactory;
import net.java.slee.resource.sip.SleeSipProvider;

// Invite from MS

public abstract class EnOcean_ServiceSbb  implements Sbb{
	

	ToHeader toHeader = null;
	boolean flag = false;
	
	
	private EnOceanProvider enOceanProvider;
	
	
	// the Sbb's context
	private SbbContext sbbContext;

	// cached objects in Sbb's environment, lookups are expensive
	private ServiceActivityFactory serviceActivityFactory;
	private ServiceActivityContextInterfaceFactory serviceACIFactory;	
    @SuppressWarnings("unused")
	private ActivityContextNamingFacility namingFacility;

	// SIP	
    private SleeSipProvider sipProvider;
    private SipActivityContextInterfaceFactory sipAcif;
	private TcpActivityContextInterfaceFactory connectionACIFactory;
	private ServiceActivityContextInterfaceFactory serviceActivityContextInterfaceFactory;


    // HTTP
    @SuppressWarnings("unused")

     // SLEE Facilities
    private TimerFacility timerFacility = null;
	private ActivityContextNamingFacility aciNamingFacility;
	private EnOceanConnectionActivity enOceanConnectionActivity;
    
    
    
    
    // Variables
	private static String msIp = "192.168.1.186";		// The IP of the Media Server
	private static String csIp = "192.168.0.12";		// The IP of the Call Server
	
	private static Tracer tracer; 
	
	private final String enOceanActivityHashMapKey =  "EnOceanActivity";
	
	private int sendTelegramPort = 2005;
	
    /**
     * The SbbActivityContextInterface
     * 
     * @param aci
     * @return ServiceACIActivityContextInterface
     */
    public abstract ServiceACIActivityContextInterface asSbbActivityContextInterface(ActivityContextInterface aci);
	
	// SbbObject LIFECYCLE METHODS

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#setSbbContext(javax.slee.SbbContext)
	 */
	@SuppressWarnings("static-access")
	public void setSbbContext(SbbContext sbbContext) {
        this.sbbContext = sbbContext;
        this.tracer = sbbContext.getTracer(EnOcean_ServiceSbb.class.getSimpleName());
        try {
            
            Context myEnv = (Context) new InitialContext().lookup("java:comp/env");
    
            
            //initialize SACI
            serviceACIFactory = 
            	(ServiceActivityContextInterfaceFactory)myEnv.lookup("slee/serviceactivity/activitycontextinterfacefactory");
            serviceActivityFactory = 
            	(ServiceActivityFactory)myEnv.lookup("slee/serviceactivity/factory");  
            
            namingFacility = 
            	(ActivityContextNamingFacility) myEnv.lookup("slee/facilities/activitycontextnaming");
            
//            this.serviceActivityFactory = (ServiceActivityFactory) 
//			                              myEnv.lookup(ServiceActivityFactory.JNDI_NAME);
//            this.serviceActivityContextInterfaceFactory = (ServiceActivityContextInterfaceFactory) 
//			                                              myEnv.lookup(ServiceActivityContextInterfaceFactory.JNDI_NAME);
//            
//            this.aciNamingFacility = (ActivityContextNamingFacility) 
//			                                myEnv.lookup(ActivityContextNamingFacility.JNDI_NAME);
                        
            
            
           
            
            
          //initialize SIP RA 
			sipProvider = 
				(SleeSipProvider) myEnv.lookup("slee/resources/jainsip/1.2/provider");
            sipAcif = 
            	(SipActivityContextInterfaceFactory) myEnv.lookup("slee/resources/jainsip/1.2/acifactory");
            
           //initialize EnOcean RA
            
            
            enOceanProvider = (EnOceanProvider) myEnv.lookup( "slee/resources/EnOceanRA/provider" );
            
            connectionACIFactory = (TcpActivityContextInterfaceFactory) myEnv.lookup( "slee/resources/EnOceanRA/acifactory" );
   
    	} catch (javax.naming.NamingException e) {
    		tracer.severe("Could not set SBB context:", e);
    	}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#unsetSbbContext()
	 */
	public void unsetSbbContext() {
		this.sbbContext = null;
		this.serviceACIFactory = null;
		this.serviceActivityFactory = null;
		this.aciNamingFacility = null;		
		this.sipProvider = null;		
		this.sipAcif = null;

		// SLEE facilities
//		this.timerFacility = null;
//		this.aciNamingFacility = null;
//		this.nullACIFactory = null;
//		this.nullActivityFactory = null;
//		this.serviceActivityFactory = null;
//		this.serviceActivityContextInterfaceFactory = null;
		
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbActivate()
	 */
	public void sbbActivate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbCreate()
	 */
	public void sbbCreate() throws CreateException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbExceptionThrown(java.lang.Exception,
	 * java.lang.Object, javax.slee.ActivityContextInterface)
	 */
	public void sbbExceptionThrown(Exception exception, Object object,
			ActivityContextInterface activityContextInterface) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbLoad()
	 */
	public void sbbLoad() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbPassivate()
	 */
	public void sbbPassivate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbPostCreate()
	 */
	public void sbbPostCreate() throws CreateException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbRemove()
	 */
	public void sbbRemove() {
		
		
//		Dialog controlDialog = getControlChannelDialog("controller");
//		
//
//        removeControlChannelDialog("Controller");
//        removeDialogCSeq(controlDialog);
//		
	
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbRolledBack(javax.slee.RolledBackContext)
	 */
	public void sbbRolledBack(RolledBackContext rolledBackContext) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.Sbb#sbbStore()
	 */
	public void sbbStore() {
	}
	
	// EVENT HANDLERS

	/**
	 * Event handler for the StartServiceEvent
	 * 
	 * @param event
	 * @param aci
	 */
	public void onStartService( javax.slee.serviceactivity.ServiceStartedEvent event,ActivityContextInterface aci) {		
		tracer.info("#####  onStartServiceEvent of EnOcean_ServiceSbb  #####");
		
	
		
		GatewayConnectionActivity gca = null;
        try {
			//connectionProvider.setAll("set all to no thing");
        	gca = enOceanProvider.getGatewayConnection();
        	ArrayList<Socket> gatewayList = (ArrayList) gca.getGatewayList();
        	if(gatewayList.size()!=0){        		
        		EnOceanConnectionActivity eoa = gca.connect2Gateway(gatewayList.get(0).getInetAddress().getHostAddress());//.getGatewayList();
//        		tracer.info( "-> gateway available -> connect to: " + gatewayList.get(0).getInetAddress().getHostAddress() );	
        		
        		ActivityContextInterface eoACI = connectionACIFactory.getActivityContextInterface(eoa.getEnOceanActivity());
        		eoACI.attach(sbbContext.getSbbLocalObject());
//        		tracer.info(">>>>>>>>>>>>>>>>>>checkpoint for connnection activity-------------------------->>>>>>>>>>>>>" +eoACI );

    	      	
        	}else{
        		ActivityContextInterface gwACI = connectionACIFactory.getActivityContextInterface(gca.getGatewayActivity());
        		gwACI.attach(sbbContext.getSbbLocalObject());
        		tracer.info( "-> No available gateway found! Waiting for new gateway." );
        	}
        }catch (Exception e) {
    	    	tracer.severe(">>>>>>>>>>>>>>>EnOcean gateway connection failed>>>>>>>>>>> ", e);
    	    }
		
		
		
		try{
			HeaderFactory headerFactory = sipProvider.getHeaderFactory();
			AddressFactory addressFactory = sipProvider.getAddressFactory();
			URI requestURI = addressFactory.createURI("sip:msml@192.168.1.186");
			CallIdHeader callId = sipProvider.getNewCallId();
			List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>(1);
			ListeningPoint listeningPoint = sipProvider.getListeningPoints()[0];
			ViaHeader viaHeader = sipProvider.getHeaderFactory().createViaHeader(listeningPoint.getIPAddress(),
			listeningPoint.getPort(),
			listeningPoint.getTransport(), null);
			viaHeaders.add(viaHeader);
			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,Request.INVITE);
			MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			Address fromAddress = addressFactory.createAddress("sip:as@192.168.0.65");
			FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "MobicentsASTag");
			Address toAddress = addressFactory.createAddress("sip:msml@192.168.1.186");
			ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
			
			
   // Create SDP without m-parameters
    		String randomMediaSessionNumber = String.valueOf(System.currentTimeMillis());
    		String sdp = 	"v=0\n"+
    						"o=MobicentsAS " + randomMediaSessionNumber + " 0 IN IP4 "
    							+ sipProvider.getListeningPoint("udp").getIPAddress() + "\n" +
    						"s=EnOcean_Service Control Channel " + "\n"+
    						"c=IN IP4 "+sipProvider.getListeningPoint("udp").getIPAddress()+"\n"+
    						"t=0 0\n";
        
	        Request ControlChannelRequest = sipProvider.getMessageFactory().createRequest(
	        		requestURI,
	        		Request.INVITE,
	        		callId,
	        		cSeqHeader,
	        		fromHeader,
	        		toHeader,
	        		viaHeaders,
	        		maxForwardsHeader,
	        		contentTypeHeader,
	        		sdp
	        );
	        
	       
	        ContactHeader contact = this.createContactHeaderForMS();
	        ControlChannelRequest.addHeader(contact);
	        
	        ClientTransaction ct = sipProvider.getNewClientTransaction(ControlChannelRequest);
	        ct.sendRequest();
	        ActivityContextInterface ctAci = sipAcif.getActivityContextInterface(ct);
	      	ctAci.attach(sbbContext.getSbbLocalObject());
	      	Dialog dialog = sipProvider.getNewDialog(ct);
	      	setControlChannelDialog(dialog);
	      	
	      	
	      	
	  
	      	
	      	
	      	DialogActivity dialogAci = (DialogActivity)dialog;
	      	ActivityContextInterface inDialogAci = sipAcif.getActivityContextInterface(dialogAci);	      	
	        inDialogAci.attach(sbbContext.getSbbLocalObject());	  
	    
	    } catch (Exception e) {
	    	tracer.severe("Can not create Control Channel for MS: ", e);
	    }
      
	}
	
	/**
	 * Returns the ContactHeader from this AS for MS
	 * 
	 * @return ContactHeader
	 * @throws ParseExeption
	 */

	
	
	public void onSuccess(ResponseEvent event, ActivityContextInterface aci){
	// sends and ack after after receiving 200 OK from the MS	
		tracer.info("#####  onSuccessServiceEvent of EnOcean_ServiceSbb  #####");
	    CSeqHeader cseq = (CSeqHeader)event.getResponse().getHeader(CSeqHeader.NAME);
	    String responsedMethod = cseq.getMethod();
	
	      if(responsedMethod.equals(Request.INFO)){
		     tracer.info("####### Received 200 OK from MS for an INFO #######");
						
	      }
	      
	      
	      else if(responsedMethod.equals(Request.BYE)){
						tracer.info("####### Received 200 OK from MS for an BYE #######");
//						Dialog controlDialog = getControlChannelDialog("controller");
						
						
//						tracer.info("################ Delete controldialog ####################");
						
						
//					    EnOceanConnectionActivity activity  =  getEnOceanActivity();
//				            
//				        tracer.info("Send out telegram message to turn light off---------------------->>>11111");
//				        activity.sendEnOceanTelegram("a55a6b0500000000fff7098020");
						
//						if(controlDialog != null){
						// detach the ControlDialog from A
//						Dialog dialog = event.getClientTransaction().getDialog();
//			        	DialogActivity dialogAci = (DialogActivity)dialog;
//				        ActivityContextInterface inDialogAci = sipAcif.getActivityContextInterface(dialogAci);	
//			            inDialogAci.detach(sbbContext.getSbbLocalObject());	
			            // remove from all Lists
//			            removeControlChannelDialog("Controller");
//			            removeDialogCSeq(controlDialog);
						
			      
			            
			            
			            
//						}
	      }
				
	      else  {
		
		AddressFactory addressFactory = sipProvider.getAddressFactory();
		Dialog dialog = event.getClientTransaction().getDialog();
        ToHeader toHeader = (ToHeader) event.getResponse().getHeader(ToHeader.NAME);
		//sending ACK to establish Control Channel
        String inimynimo = ((FromHeader)event.getResponse().getHeader(FromHeader.NAME)).getAddress().getURI().toString();
				if(inimynimo.startsWith("sip:as@")) {
			try{
				
				Dialog controlDialog = getControlChannelDialog("Controller");
				
				tracer.info("------------------------------------control dialog------------------------>>>" + controlDialog);
				
				
				URI tempAddress =  ((FromHeader)event.getResponse().getHeader(FromHeader.NAME)).getAddress().getURI();
				System.out.println(tempAddress.toString());

				Address fromAddress = addressFactory.createAddress("sip:as@192.168.0.65");
				
				//set control Channel dialog
				this.setDialogFromAtControlChannel(controlDialog, fromAddress);
				if(controlDialog != null){
					setDialogToHeader(controlDialog, toHeader);
					long cseqNumber = cseq.getSeqNumber();
			        setDialogCSeq(controlDialog, cseqNumber);
				}
				
			HeaderFactory headerFactory = sipProvider.getHeaderFactory();
		    CSeqHeader cseqAck = headerFactory.createCSeqHeader(1L,Request.ACK);
		 
			long cseqNumber = cseqAck.getSeqNumber();
			Request ack;
		
			    ack = dialog.createAck(cseqNumber);
				ack.setHeader(toHeader);
				if(flag == false){
					this.toHeader = toHeader;
					flag = true;
				}
				dialog.sendAck(ack);
	           } catch (Exception e) {
			    	tracer.severe("Can not send ack after 200 O.K. from MS ", e);
			    }
		}
	
			// send the 200 O.K. to the UA  after receiving 200 O.K. from the MS with the SDP
	           Dialog msSubscriberDialog = getMediaServerSubscriberDialog(dialog);
	      
				if(msSubscriberDialog != null){
					if(msSubscriberDialog.getCallId().equals(dialog.getCallId())){
						// store the ToHeader in link to the Dialog
			          	setDialogToHeader(dialog, toHeader);
						// send Response to the Subscriber and wait for ACK
						Dialog inDialog = getLinkedInDialog(msSubscriberDialog);					
						Request invite = getDialogServerTransaction(inDialog).getRequest();
						try {
 							Response response = sipProvider.getMessageFactory().createResponse(Response.OK, invite);
							if (event.getResponse().getRawContent()!= null)
							{
								ContentTypeHeader contentTypeHeader = (ContentTypeHeader)event.getResponse().getHeader(ContentTypeHeader.NAME);
								// add Content(SDP)
								byte[] data = event.getResponse().getRawContent();		
								response.setContent(data, contentTypeHeader);
								// add Contact-Header							
								response.setHeader(createContactHeader(getConfID(event.getResponse())));
					    		// send Response
								ServerTransaction st = (ServerTransaction) getDialogServerTransaction(inDialog);
								// wait for ACK store callID to CMP
								setAckExpectedforCall(inDialog.getCallId().getCallId());

								st.sendResponse(response);
							}
						} catch (ParseException e) {
							tracer.severe("Could not create new Response: ", e);
						} catch (SipException e) {
							tracer.severe("Could not send new Response: ", e);
						} catch (InvalidArgumentException e) {
							tracer.severe("Could not send new Response: ", e);
						}
					}
				}
	         }		
	}
		
	
	
	public void onInvite(RequestEvent event, ActivityContextInterface aci) {
		sendResponse(event, Response.TRYING);
		Request invite = event.getRequest();
		tracer.info("#####  Incoming call from: " + event.getRequest().getHeader(FromHeader.NAME) + "  #####");
		ViaHeader via = (ViaHeader) event.getRequest().getHeader(ViaHeader.NAME);
	
//		if(via.getHost().equals(csIp))
		{
			Dialog inDialog = null;
	        ToHeader toHeader = (ToHeader) invite.getHeader(ToHeader.NAME);	        
	        String to = toHeader.getAddress().getURI().toString();
	        int startHostname = to.indexOf(":");
	        int endHostname = to.indexOf("@");
	        String hostName = to.substring(startHostname + 1, endHostname);
	     
	        // is the syntax of the host name correct ?
	        int signEqual = hostName.indexOf("=");
	        String conf = hostName.substring(0, signEqual + 1);
	        if(!conf.equalsIgnoreCase("enocean=")){
	        	// reject call
	        	sendResponse(event,Response.NOT_FOUND);
	        	// set CMP for expecting ACK
	        	CallIdHeader callID = (CallIdHeader) invite.getHeader(CallIdHeader.NAME);
	        	setAckExpectedforRejection(callID.getCallId());
	        }
	        String confID = hostName.substring(signEqual + 1, conf.length() + 1);
	        try{
	        	@SuppressWarnings("unused")
				int i = Integer.parseInt(hostName.substring(signEqual + 1, conf.length() + 1));
	        }catch(NumberFormatException e){
	        	// reject call
	        	sendResponse(event,Response.NOT_FOUND);
	        	// set CMP for expecting ACK
	        	CallIdHeader callID = (CallIdHeader) invite.getHeader(CallIdHeader.NAME);
	        	setAckExpectedforRejection(callID.getCallId());
	        }		        

	        ServerTransaction st = event.getServerTransaction();
	        
	        try {
	        	Dialog dialog = sipProvider.getNewDialog(st);
	        	DialogActivity dialogAci = (DialogActivity)dialog;
	        	
	        	
	        	
	            ActivityContextInterface inDialogAci = sipAcif.getActivityContextInterface(dialogAci);	
	            inDialogAci.attach(sbbContext.getSbbLocalObject());	
	            // save Subscribers dialog in SACI
	            setSubscriberDialog(dialog);
	            inDialog = dialog;
	            setDialogServerTransaction(inDialog, st);
	        } catch (Exception e) {
	        	tracer.severe("Could not get Dialog: ", e);
	        }

	        // create new SIP INVITE
	        HeaderFactory headerFactory = sipProvider.getHeaderFactory();
            CallIdHeader callIdHeader = sipProvider.getNewCallId();     
            try {
				CSeqHeader cseqHeader = headerFactory.createCSeqHeader(1l, Request.INVITE);
	            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp"); 
	            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
	            ArrayList list = new ArrayList();
	            // get the SDP of the received INVITE
		        byte[] data = invite.getRawContent();
		        String sdp = new String(data);
		        // if contains Video part, then add Bandwidth parameter
		        if (sdp.contains("m=video")){
					javax.sdp.SdpFactory sdpFactory = javax.sdp.SdpFactory.getInstance();
					javax.sdp.SessionDescription sdpDescription = sdpFactory.createSessionDescription(sdp);
					java.util.Vector <javax.sdp.MediaDescription> mediaDescriptions = sdpDescription.getMediaDescriptions(true);
					for (int index = 0; index < mediaDescriptions.size(); index++){
						javax.sdp.MediaDescription mediaDescription = mediaDescriptions.get(index);
						javax.sdp.Media media = mediaDescription.getMedia();
						if(media.getMediaType().equalsIgnoreCase("video")){
							//TODO: Bandwidth variable 
							mediaDescription.setBandwidth(BandWidth.AS, 768);
						}
					}
					sdpDescription.setMediaDescriptions(mediaDescriptions);
					sdp = sdpDescription.toString();
					data = sdp.getBytes();
		        }
		        
		        MessageFactory messageFactory = sipProvider.getMessageFactory();
		        Request inviteRequest = messageFactory.createRequest(
		        		createMSRequestURI(),		//URI
						Request.INVITE,				//Method
						callIdHeader,				//CallIdHeader
						cseqHeader,					//CSeqHeader
						createFromHeader(confID),	//FromHeader
						createToHeader(confID),		//ToHeader
						list,						//List
						maxForwardsHeader,			//MaxForwardsHeader
						contentTypeHeader,			//ContenTypeHeader
						data);						//byte[] Content
		        
		        inviteRequest.addHeader(createContactHeaderForMS());		        

       			// save callID and ID in SACI
       			
       			
	       	    ClientTransaction ct = sipProvider.getNewClientTransaction(inviteRequest);
	 	        ct.sendRequest();
		        ActivityContextInterface ctAci = sipAcif.getActivityContextInterface(ct);
	          	ctAci.attach(sbbContext.getSbbLocalObject());
	          	Dialog dialog = sipProvider.getNewDialog(ct);
	          	DialogActivity dialogAci = (DialogActivity)dialog;
	          	ActivityContextInterface inDialogAci = sipAcif.getActivityContextInterface(dialogAci);	
	          	inDialogAci.attach(sbbContext.getSbbLocalObject());	          	
	          	// save dialog in SACI
	          	// dialog between AS and MS for Subscribers
	          	setMediaServerSubscriberDialog(dialog);
	         	setLinkedDialogs(inDialog, dialog);
	         	setDialogFromAtControlChannel(dialog, null);
	         	
	         	
			} catch (ParseException e) {
				tracer.severe("Could not create new SIP INVITE headers: ", e);
				sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			} catch (InvalidArgumentException e) {
				tracer.severe("Could not create new SIP INVITE headers: ", e);
				sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			} catch (SdpParseException e) {
				tracer.severe("Could not create new SIP INVITE SDP part: ", e);
				sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			} catch (SdpException e) {
				tracer.severe("Could not create new SIP INVITE SDP part: ", e);
				sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			} catch (TransactionUnavailableException e) {
				tracer.severe("Could not create new Client Transaction for INVITE Request to MS: ", e);
				sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			} catch (SipException e) {
				tracer.severe("Could not send new INVITE Request to MS: ", e);
				//sendResponse(event, Response.SERVER_INTERNAL_ERROR);
			}	        
		}
		
	}

	     

	public void onAck(RequestEvent event, ActivityContextInterface aci){
		tracer.info("#####  onAckServiceEvent of EnOcean_ServiceSbb  #####");
		
		AddressFactory addressFactory = sipProvider.getAddressFactory();;
		try{
		
			Dialog dialog = event.getServerTransaction().getDialog();
		    Address fromAddress = addressFactory.createAddress("sip:as@192.168.0.65");
		    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
//		    tracer.info("-------------for control channel onACK--------------------------"+ forControlChannel);
     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//     	    tracer.info("------------subscriber dialog----------------------------" + msSubscriberDialog );
		    ToHeader to = getDialogToHeader(msSubscriberDialog);
//		    tracer.info("to---------------- onAck---------------------" + to);
		    long cSeq = this.getDialogCSeq(forControlChannel);
		    cSeq = cSeq+1;
		    this.setDialogCSeq(forControlChannel, cSeq);
		
//		    "<dtmf fdt=\"\" idt=\"30s\" edt=\"30s\">\n" +
	//	    fdt=\"40s\" idt=\"40s\" edt=\"40s\
			Request info = null;   
	        CSeqHeader cseqHeader;
		    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
	        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
	        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
	        ArrayList list = new ArrayList();
	        
	         String data =  "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
					       "<msml version=\"1.1\">\n" + 
							  "<dialogstart target=\"conn:" + to.getTag() + 
							  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
							  "<play cvd:barge=\"true\" cvd:cleardb=\"false\">\n" +
							  "<audio uri=\"file://provisioned/EnOceanServiceAnnoucementCall.wav\"/>\n" + 
							  "</play>\n"+ 
							  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
							  "<dtmf>\n" +
							  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
							  "<noinput/>\n"+ 
							  "<nomatch/>\n" + 
							  "</dtmf>\n"+ 
							  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
							  "</dialogstart>\n"+
							  "</msml>";
	        	
	         info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		   
	
	
				
	         
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				
		
				
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}		
		        
		        
	        	
	   
	         
	         
	         
	            
			        
			   
			        
			        
			        
	        	
	      /*  	
	        	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
            "<msml version=\"1.1\">\n" + 
            "<dialogstart target=\"conn:" + to.getTag() + 
	           "\" type=\"application/moml+xml\" name=\"dialog1\">\n" + 
            "<play id=\"play1\" cvd:barge=\"true\" cvd:cleardb=\"false\">\n" +
            "<audio uri=\"2\"/>\n" + 
            "</play>\n"+
            "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.end play.amt\"/>\n"+
            "<record dest=\"file://transient/username1.wav\" format=\"audio/wav\" maxtime=\"10s\"/>\n"+
            "<send target=\"source\" event=\"app.recorddone\" namelist=\"record.end record.len\"/>\n"+
            "<play id=\"play2\" cvd:barge=\"true\" cvd:cleardb=\"false\" <audio uri=\"file://transient/username1.wav\"/>\n"+
            "</play>\n"+ 
            "<exit namelist=\"play.end play.amt\"/>\n"+
            "</dialogstart>\n"+
            "</msml>";
	        */
	   
	    
	 	
	      /*  
	       String data2 = 	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
						      "<msml version=\"1.1\">\n" + 
							  "<dialogstart target=\"conn:" + to.getTag() + 
							  "\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
							  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
							  "<audio uri=\"file://provisioned/thanks.wav\"/>\n" + 
							  "</play>\n"+ 
							  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
							  "<dtmf fdt=\"3s\" idt=\"5s\" edt=\"2s\">\n" +
							  "<pattern digits=\"min=2;max=6;rtk=#\" format=\"moml+digits\"/>\n"+
							  "<noinput/>\n"+ 
							  "<nomatch/>\n" + 
							  "</dtmf>\n"+ 
							  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
							  "</dialogstart>\n"+
							  "</msml>";
			*/	        
	      
	        
	
	        
	        
	        
	        
	        
	        
	        
	        	/*
	                    "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
			               "<msml version=\"1.1\">\n" + 
			               "<dialogstart target=\"conn:" + to.getTag() + 
			  	           "\" type=\"application/moml+xml\" name=\"dialog1\">\n" + 
	                       "<play id=\"play1\" cvd:barge=\"true\" cvd:cleardb=\"false\">\n" +
	                       "<audio uri=\"2\"/>\n" + 
	                       "</play>\n"+
	                       "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.end play.amt\"/>\n"+
	                       "<record dest=\"file://transient/11.wav\" format=\"audio/wav\"maxtime=\"10s\">\n"+
	                       "<send target=\"source\" event=\"app.recorddone\" namelist=\"record.end record.len\"/>\n"+
	                       "</record>\n"+
	                       "<play id=\"play2\" cvd:barge=\"true\" cvd:cleardb=\"false\" <audio uri=\"file://transient/11.wav\"/>\n"+
	                       "</play>\n"+ 
	                       "<exit namelist=\"play.end play.amt\"/>\n"+
	                       "</dialogstart>\n"+
			               "</msml>";
	        */
	        	
	        	
	        	
	        	
	       /* 	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 					  "<msml version=\"1.1\">\n" + 
	 					  "<dialogstart target=\"conn:" + to.getTag() + 
	 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
	 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
	 					  "<audio uri=\"file://provisioned/thanks.wav\"/>\n" + 
	 					  "</play>\n"+ 
	 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
	 					  "<dtmf fdt=\"3s\" idt=\"5s\" edt=\"2s\">\n" +
	 					  "<pattern digits=\"min=2;max=6;rtk=#\" format=\"moml+digits\"/>\n"+
	 					  "<noinput/>\n"+ 
	 					  "<nomatch/>\n" + 
	 					  "</dtmf>\n"+ 
	 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
	 					  "</dialogstart>\n"+
	 					  "</msml>";
	 		*/
	    
        
        	
			
 @SuppressWarnings("unchecked")
	
 
 
 public void onEnOceanEvent(EnOceanEvent event, ActivityContextInterface aci ){

		
	 

		
	 
	 
		if(((Integer) event.getPayload(EnOceanEvent.CONTENT))==
			EnOceanEvent.GATEWAY_LIST_EVENT){
		
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			tracer.info( "!!!!!       GATEWAY_LIST_EVENT        !!!!!" );
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			chooseGateway((ArrayList<Socket>)
					event.getPayload(EnOceanEvent.GATEWAY_LIST),aci);
		}else		
		if(((Integer) event.getPayload(EnOceanEvent.CONTENT))==
			EnOceanEvent.TELEGRAM_RECEIVED_EVENT){
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			tracer.info( "!!!!!     TELEGRAM_RECEIVED_EVENT     !!!!!" );
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			initialiseReadyChecking(aci);
			analyseTelegramm(event,aci);
			
			 setEnOceanActivity((EnOceanConnectionActivity)aci.getActivity(), enOceanActivityHashMapKey);
			
			
		}else
		if(((Integer) event.getPayload(EnOceanEvent.CONTENT))==
			EnOceanEvent.GATEWAY_READY_EVENT){
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			tracer.info( "!!!!!       GATEWAY_READY_EVENT       !!!!!" );
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			tracer.info( "ready string: " + 
					event.getPayload(EnOceanEvent.READY_MESSAGE) );
			//turnLightOn(aci);				
		}else{
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
			tracer.info( "!!!!!      unknown onEnOceanEvent     !!!!!" );
			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
		}
		
		
		


		
//		if(gatewayList!=null){
//			GatewayConnectionActivity ac = (GatewayConnectionActivity)aci.getActivity();
//			ac.connect2Gateway(gatewayList.get(0));
//			aci.detach(sbbContext.getSbbLocalObject());
//		}else{
//			aci.attach(sbbContext.getSbbLocalObject());
//		}
		
	}
    
private void analyseTelegramm(EnOceanEvent event,
			ActivityContextInterface activity) {
		String telegramMessage = (String) event.getPayload(EnOceanEvent.ENOCEAN_TELEGRAM);
		
		telegramMessage = (String) telegramMessage.subSequence(telegramMessage.indexOf("#")+1, telegramMessage.lastIndexOf("#"));
		
		tracer.info(telegramMessage);
		

		
		

		
		setEnOceanActivity((EnOceanConnectionActivity)activity.getActivity(), enOceanActivityHashMapKey);
		
		
		//4294379904#a55a0b0570000000001e64533085#989615631289
		
		
		//button 1:			4294379904#a55a0b0550000000001e64533065#226904323098
		//button 1 losgel.:	4294379904#a55a0b0500000000001e64532005#226904365887
		
		//button 2:			4294379904#a55a0b0570000000001e64533085#226904416281
		//button 2 losgel.:	4294379904#a55a0b0500000000001e64532005#226904437212
		
		//button 3:			4294379904#a55a0b0510000000001e64533025#226904458517
		//button 3 losgel.:	4294379904#a55a0b0500000000001e64532005#226904476266
		
		//button 4:			4294379904#a55a0b0530000000001e64533045#226904493821
		//button losgel.:	4294379904#a55a0b0500000000001e64532005#226904508406
		
		if(telegramMessage.equalsIgnoreCase("a55a0b0550000000001e64533065")){
			tracer.info("knopf 1 gedrückt");
			turnLightOn(activity);			
		}
		if(telegramMessage.equalsIgnoreCase("a55a0b0500000000001e64532005")){
			tracer.info("knopf 1,2,3 oder 4 losgelassen");
			turnLightOff(activity);			
		}
		
		if(telegramMessage.equalsIgnoreCase("a55a0b0570000000001e64533085")){
			tracer.info("knopf 2 gedrückt");
			turnLightOn(activity);			
		}
//		if(telegramMessage.equalsIgnoreCase("a55a0b0500000000001e64532005")){
//			tracer.info("knopf 2 losgelassen");
//			turnLightOff(aci);				
//		}
		
		if(telegramMessage.equalsIgnoreCase("a55a0b0510000000001e64533025")){
			tracer.info("knopf 3 gedrückt");
			turnLightOn(activity);			
		}
//		if(telegramMessage.equalsIgnoreCase("a55a0b0500000000001e64532005")){
//			tracer.info("knopf 3 losgelassen");
//			turnLightOff(aci);				
//		}
		
		if(telegramMessage.equalsIgnoreCase("a55a0b0530000000001e64533045")){
			tracer.info("knopf 4 gedrückt");
			initialiseReadyChecking(activity);			
		}
		
//		if(telegramMessage.equalsIgnoreCase("a55a0b0500000000001e64532005")){	
//			tracer.info("knopf 4 losgelassen");
//		}
		
//		turnLightOn(aci);	
//		turnLightOff(aci);
//		initialiseReadyChecking(aci);
	}

			private void turnLightOn(ActivityContextInterface enAci) {
					tracer.info( "-> Sending EnOcean telegram message!" );
					sendEnOceanTelegram(enAci,"a55a6b0550000000fff7098030");	
			}
			
			private void turnLightOff(ActivityContextInterface aci) {
				tracer.info( "-> Sending EnOcean telegram message!" );
				sendEnOceanTelegram(aci,"a55a6b0500000000fff7098020");	
			}
			
			private void sendEnOceanTelegram(ActivityContextInterface enAci, String enOceanTelegram) {
				 
				    EnOceanConnectionActivity eoCA;
					eoCA = (EnOceanConnectionActivity)enAci.getActivity();
					eoCA.sendEnOceanTelegram(enOceanTelegram);
				
					
					
				}
			
			private void initialiseReadyChecking(ActivityContextInterface aci) {	
					EnOceanConnectionActivity eoCA;
					eoCA = (EnOceanConnectionActivity)aci.getActivity();
					eoCA.checkReadyStatus();		
			}


	public void chooseGateway() {
	
	}
	
			public void onInfo(RequestEvent event, ActivityContextInterface activity){
				//TODO: can be used for further implementations//make events related to the msml created for the event and result.
				tracer.info("#####  onInfoEvent of EnOcean_ServiceSbb  #####");
				
				sendResponse(event, Response.OK);
				byte[] rawcontent =  event.getRequest().getRawContent();
//				tracer.info(">>>>>>>>>>>>>>>>>>>>the raw content>>>>>>>>>>>>>>"+ event.getRequest().getRawContent());
			
				String dtmfvalue = new String(rawcontent);
		        int dtmfDigit=0;
				
				try
			
				{	
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>DocumentBuilderFactory>>>>>>>>"+ DocumentBuilderFactory.newInstance());		
				DocumentBuilder db = dbf.newDocumentBuilder();
		        Document doc = db.parse(new InputSource(new StringReader(dtmfvalue)));
		        NodeList nodeList = doc.getElementsByTagName("*");
		        System.out.println(nodeList.getLength());

		           for (int s = 0; s < nodeList.getLength(); s++) {
		        	   
		        	   Node fstNode = nodeList.item(s);
		        	   
		        	   
		        	   if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

		        		   Element fstElmnt = (Element) fstNode;
		        		   
		        		   if(fstElmnt.getNodeName().equals("name") && fstElmnt.getTextContent().equals("dtmf.digits")) {
		        			   Node digitValueNode = nodeList.item(s+1);
		        			  
		        			   
		        			   String tempDigit = digitValueNode.getTextContent();
		        			   dtmfDigit = Integer.parseInt(tempDigit);
		        			   
		        			   
		        		   }
		        	   }
		           }
					}catch(Exception e) {
		        	   e.printStackTrace();
		           }
		           
					System.out.println("dtmf5===========>>>>>>>>>>" + dtmfDigit);
					
				
					
					
					
					
		           switch(dtmfDigit) {
		           
		           
		        		case 1:
		        			System.out.println("1 was pressed");
		        			
//		    		    tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
		 	               
		 				 EnOceanConnectionActivity activity1  =  getEnOceanActivity();
		 				 
		 				 
		 				 
		 			     tracer.info("#########################" +"send telegram message to turn light on#######################");
		 				 activity1.sendEnOceanTelegram("a55a6b0550000000fff7098030");
		        			
		 				 
//		 				"a55a6b0500000000fff7098020"	 				 
		 				 
		        			AddressFactory addressFactory = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		
		    				       
		    			         
		    					
				           			

		        		String data1="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 					  "<msml version=\"1.1\">\n" + 
	 					  "<dialogstart target=\"conn:" + to.getTag() + 
	 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
	 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
	 					  "<audio uri=\"file://provisioned/LightON.wav\"/>\n" + 
	 					  "</play>\n"+ 
	 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
	 					  "<dtmf>\n" +
	 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
	 					  "<noinput/>\n"+ 
	 					  "<nomatch/>\n" + 
	 					  "</dtmf>\n"+ 
	 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
	 					  "</dialogstart>\n"+
	 					  "</msml>";
       
		        		        
		        		        
		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				

		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
		         
		           			break;
		         	
		           		case 2:
		           			System.out.println("2 was pressed");
		           			
		           			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        					tracer.info( "!!!!!     Send Telegram     !!!!!" );
        					tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        					//initialiseReadyChecking(event,aci);
        					
        					
        				  tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity()); 
        				  
        				  
        				  
 		 	               
   		 				 EnOceanConnectionActivity activity2  =  getEnOceanActivity();
   		 			     tracer.info("#########################send " +
   		 			     		"telegram message to turn light off#######################"); activity2.sendEnOceanTelegram("a55a6b0500000000fff7098020");
        					
        					
        					
        					
        					AddressFactory addressFactory1 = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory1.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		 tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
		    				       
		    			         
		    					
				           			
		        			
		        		        String data1 = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
			 					  "<msml version=\"1.1\">\n" + 
			 					  "<dialogstart target=\"conn:" + to.getTag() + 
			 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
			 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
			 					  "<audio uri=\"file://provisioned/LightOFF.wav\"/>\n" + 
			 					  "</play>\n"+ 
			 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
			 					  "<dtmf>\n" +
			 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
			 					  "<noinput/>\n"+ 
			 					  "<nomatch/>\n" + 
			 					  "</dtmf>\n"+ 
			 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
			 					  "</dialogstart>\n"+
			 					  "</msml>";

		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				
  		
		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        					
    

		           			break;
		           			
		           		case 3:
		           			System.out.println("3 was pressed");
		           			
		           			
			 	       
			 			    AddressFactory addressFactory3 = sipProvider.getAddressFactory();;
			        			try{
			        			
			        				Dialog dialog = event.getServerTransaction().getDialog();
			        			    Address fromAddress = addressFactory3.createAddress("sip:as@192.168.0.65");
			        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
			        			    tracer.info("control channel----------------->>>" + forControlChannel);
//			        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//			        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
			        	     	   
			        	     	    ToHeader to = getDialogToHeader(forControlChannel);
			        	     	    tracer.info("");
			        	     	   
//			        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
			        			    tracer.info("control channel for to header----------------->>>" + to);
			        			    long cSeq = this.getDialogCSeq(forControlChannel);
			        			    cSeq = cSeq+1;
			        			    this.setDialogCSeq(forControlChannel, cSeq);
			        			
			        			    Request info = null;   
			        		        CSeqHeader cseqHeader;
			        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
			        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
			        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
			        		        ArrayList list = new ArrayList();
			        			
			        	    		 tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
			    				       
			    			         
		
			        		String data1="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
		 					  "<msml version=\"1.1\">\n" + 
		 					  "<dialogstart target=\"conn:" + to.getTag() + 
		 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
		 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
		 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
		 					  "</play>\n"+ 
		 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
		 					  "<dtmf>\n" +
		 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
		 					  "<noinput/>\n"+ 
		 					  "<nomatch/>\n" + 
		 					  "</dtmf>\n"+ 
		 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
		 					  "</dialogstart>\n"+
		 					  "</msml>";
//			        		            
			        		        
			        		        
			        		        
			        info = sipProvider.getMessageFactory().createRequest(
				            this.createMSRequestURI(),				//URI
							Request.INFO,						    //Method
							forControlChannel.
							getCallId(),	        //CallIdHeader
							cseqHeader,							    //CSeqHeader
							createFromHeader("Controller"),		    //FromHeader
							this.toHeader,                          //ToHeader
							list,								//List
							maxForwardsHeader,					//MaxForwardsHeader
							contentTypeHeader,					//ContenTypeHeader
							data1);								//byte[] Content
		        
			        info.addHeader(createContactHeaderForMS());
			        
			        
			        
			        ClientTransaction cti;
					cti = sipProvider.getNewClientTransaction(info);
					cti.sendRequest();
					
		        
					
					} catch (TransactionUnavailableException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (SipException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		           			
		           			
		           			
		           		
  				      	           			
		           		
					
		           		case 4:
		           			System.out.println("4 was pressed");
					
		           			tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        					tracer.info( "!!!!!     Send Telegram     !!!!!" );
        					tracer.info( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        					//initialiseReadyChecking(event,aci);
   
        					
        					AddressFactory addressFactory4 = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory4.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		 tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
		    				       
		    			         
		    					
				           			
		        			
		        		        String data1 = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
			 					  "<msml version=\"1.1\">\n" + 
			 					  "<dialogstart target=\"conn:" + to.getTag() + 
			 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
			 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
			 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
			 					  "</play>\n"+ 
			 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
			 					  "<dtmf>\n" +
			 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
			 					  "<noinput/>\n"+ 
			 					  "<nomatch/>\n" + 
			 					  "</dtmf>\n"+ 
			 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
			 					  "</dialogstart>\n"+
			 					  "</msml>";

		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				
  		
		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        					
        					
        					
        					
        					
                          break;
        					
        				       
		           			
		           		case 5:
		           			System.out.println("5 was pressed");
		           			
		       
		           		
			 				AddressFactory addressFactory5 = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory5.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		
		    				       
		    			         
		    					
				           			

		        		String data1="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 					  "<msml version=\"1.1\">\n" + 
	 					  "<dialogstart target=\"conn:" + to.getTag() + 
	 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
	 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
	 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
	 					  "</play>\n"+ 
	 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
	 					  "<dtmf>\n" +
	 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
	 					  "<noinput/>\n"+ 
	 					  "<nomatch/>\n" + 
	 					  "</dtmf>\n"+ 
	 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
	 					  "</dialogstart>\n"+
	 					  "</msml>";
       
		        		        
		        		        
		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				

		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 				 
			 				 
			 				 
			 				 
			 				 
			 				 
			 				 
			 				 
			 			   break;
		           		case 6:
		           			System.out.println("6 was pressed");
		           			
		           		
	   		 			     	AddressFactory addressFactory6 = sipProvider.getAddressFactory();;
			        			try{
			        			
			        				Dialog dialog = event.getServerTransaction().getDialog();
			        			    Address fromAddress = addressFactory6.createAddress("sip:as@192.168.0.65");
			        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
			        			    tracer.info("control channel----------------->>>" + forControlChannel);
//			        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//			        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
			        	     	   
			        	     	    ToHeader to = getDialogToHeader(forControlChannel);
			        	     	    tracer.info("");
			        	     	   
//			        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
			        			    tracer.info("control channel for to header----------------->>>" + to);
			        			    long cSeq = this.getDialogCSeq(forControlChannel);
			        			    cSeq = cSeq+1;
			        			    this.setDialogCSeq(forControlChannel, cSeq);
			        			
			        			    Request info = null;   
			        		        CSeqHeader cseqHeader;
			        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
			        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
			        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
			        		        ArrayList list = new ArrayList();
			        			
			        	    		 tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
			    				       
			    			         
			    					
					           			
			        			
			        		        String data1 = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
				 					  "<msml version=\"1.1\">\n" + 
				 					  "<dialogstart target=\"conn:" + to.getTag() + 
				 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
				 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
				 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
				 					  "</play>\n"+ 
				 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
				 					  "<dtmf>\n" +
				 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
				 					  "<noinput/>\n"+ 
				 					  "<nomatch/>\n" + 
				 					  "</dtmf>\n"+ 
				 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
				 					  "</dialogstart>\n"+
				 					  "</msml>";

			        info = sipProvider.getMessageFactory().createRequest(
				            this.createMSRequestURI(),				//URI
							Request.INFO,						    //Method
							forControlChannel.
							getCallId(),	        //CallIdHeader
							cseqHeader,							    //CSeqHeader
							createFromHeader("Controller"),		    //FromHeader
							this.toHeader,                          //ToHeader
							list,								//List
							maxForwardsHeader,					//MaxForwardsHeader
							contentTypeHeader,					//ContenTypeHeader
							data1);								//byte[] Content
		        
			        info.addHeader(createContactHeaderForMS());
			        
			        
			        
			        ClientTransaction cti;
					cti = sipProvider.getNewClientTransaction(info);
					cti.sendRequest();
					
	  		
			        
					
					} catch (TransactionUnavailableException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (SipException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     break;
		           		
		           		
		           		case 7:
		           			System.out.println("7 was pressed");
		              
		 	                AddressFactory addressFactory7 = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory7.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		
		    				       
		    			         
		    					
				           			

		        		String data1="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 					  "<msml version=\"1.1\">\n" + 
	 					  "<dialogstart target=\"conn:" + to.getTag() + 
	 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
	 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
	 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
	 					  "</play>\n"+ 
	 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
	 					  "<dtmf>\n" +
	 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
	 					  "<noinput/>\n"+ 
	 					  "<nomatch/>\n" + 
	 					  "</dtmf>\n"+ 
	 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
	 					  "</dialogstart>\n"+
	 					  "</msml>";
      
		        		        
		        		        
		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				

		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 				    
		 				    
		 				    
		 				    
		 				    
		 				    
		 				   break;
		           			
		           		case 8:
		           			System.out.println("8 was pressed");
		  
	   		 			     	AddressFactory addressFactory8 = sipProvider.getAddressFactory();;
			        			try{
			        			
			        				Dialog dialog = event.getServerTransaction().getDialog();
			        			    Address fromAddress = addressFactory8.createAddress("sip:as@192.168.0.65");
			        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
			        			    tracer.info("control channel----------------->>>" + forControlChannel);
//			        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//			        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
			        	     	   
			        	     	    ToHeader to = getDialogToHeader(forControlChannel);
			        	     	    tracer.info("");
			        	     	   
//			        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
			        			    tracer.info("control channel for to header----------------->>>" + to);
			        			    long cSeq = this.getDialogCSeq(forControlChannel);
			        			    cSeq = cSeq+1;
			        			    this.setDialogCSeq(forControlChannel, cSeq);
			        			
			        			    Request info = null;   
			        		        CSeqHeader cseqHeader;
			        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
			        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
			        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
			        		        ArrayList list = new ArrayList();
			        			
			        	    		 tracer.info("set get activity in the onACK-----------------------------------" + activity.getActivity());       
			    				       
			    			         
			    					
					           			
			        			
			        		        String data1 = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
				 					  "<msml version=\"1.1\">\n" + 
				 					  "<dialogstart target=\"conn:" + to.getTag() + 
				 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
				 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
				 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
				 					  "</play>\n"+ 
				 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
				 					  "<dtmf>\n" +
				 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
				 					  "<noinput/>\n"+ 
				 					  "<nomatch/>\n" + 
				 					  "</dtmf>\n"+ 
				 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
				 					  "</dialogstart>\n"+
				 					  "</msml>";

			        info = sipProvider.getMessageFactory().createRequest(
				            this.createMSRequestURI(),				//URI
							Request.INFO,						    //Method
							forControlChannel.
							getCallId(),	        //CallIdHeader
							cseqHeader,							    //CSeqHeader
							createFromHeader("Controller"),		    //FromHeader
							this.toHeader,                          //ToHeader
							list,								//List
							maxForwardsHeader,					//MaxForwardsHeader
							contentTypeHeader,					//ContenTypeHeader
							data1);								//byte[] Content
		        
			        info.addHeader(createContactHeaderForMS());
			        
			        
			        
			        ClientTransaction cti;
					cti = sipProvider.getNewClientTransaction(info);
					cti.sendRequest();
					
	  		
			        
					
					} catch (TransactionUnavailableException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (SipException e) {
					tracer.severe("onAck --> Could not send Info ", e);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
	   		 			     		
		           		case 9:
		           			System.out.println("9 was pressed");
		                  	 				   
		 				   
		 				  AddressFactory addressFactory9 = sipProvider.getAddressFactory();;
		        			try{
		        			
		        				Dialog dialog = event.getServerTransaction().getDialog();
		        			    Address fromAddress = addressFactory9.createAddress("sip:as@192.168.0.65");
		        			    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
		        			    tracer.info("control channel----------------->>>" + forControlChannel);
//		        	     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
//		        	     	   tracer.info("control channel----------------->>>" + msSubscriberDialog);
		        	     	   
		        	     	    ToHeader to = getDialogToHeader(forControlChannel);
		        	     	    tracer.info("");
		        	     	   
//		        			    ToHeader to = getDialogToHeader(msSubscriberDialog);
		        			    tracer.info("control channel for to header----------------->>>" + to);
		        			    long cSeq = this.getDialogCSeq(forControlChannel);
		        			    cSeq = cSeq+1;
		        			    this.setDialogCSeq(forControlChannel, cSeq);
		        			
		        			    Request info = null;   
		        		        CSeqHeader cseqHeader;
		        			    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
		        		        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
		        		        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
		        		        ArrayList list = new ArrayList();
		        			
		        	    		
		    				       
		    			         
		    					
				           			

		        		String data1="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 					  "<msml version=\"1.1\">\n" + 
	 					  "<dialogstart target=\"conn:" + to.getTag() + 
	 					  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
	 					  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
	 					  "<audio uri=\"file://provisioned/bad_digit.wav\"/>\n" + 
	 					  "</play>\n"+ 
	 					  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
	 					  "<dtmf>\n" +
	 					  "<pattern digits=\"min=1;max=1\" format=\"moml+digits\"/>\n"+
	 					  "<noinput/>\n"+ 
	 					  "<nomatch/>\n" + 
	 					  "</dtmf>\n"+ 
	 					  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
	 					  "</dialogstart>\n"+
	 					  "</msml>";
     
		        		        
		        		        
		        info = sipProvider.getMessageFactory().createRequest(
			            this.createMSRequestURI(),				//URI
						Request.INFO,						    //Method
						forControlChannel.
						getCallId(),	        //CallIdHeader
						cseqHeader,							    //CSeqHeader
						createFromHeader("Controller"),		    //FromHeader
						this.toHeader,                          //ToHeader
						list,								//List
						maxForwardsHeader,					//MaxForwardsHeader
						contentTypeHeader,					//ContenTypeHeader
						data1);								//byte[] Content
	        
		        info.addHeader(createContactHeaderForMS());
		        
		        
		        
		        ClientTransaction cti;
				cti = sipProvider.getNewClientTransaction(info);
				cti.sendRequest();
				

		        
				
				} catch (TransactionUnavailableException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (SipException e) {
				tracer.severe("onAck --> Could not send Info ", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 				   
		 				   
		 				   
		 				   
		 				 
		 				    break;
		   
		           			
		           		default:
		           			System.out.println("Could not read any Digit");
		           }
		           
		           
		           
		           
		           
		         
			}

  
			
			
			
			
		
	/*	
		String info_media ="<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
        "<msml version=\"1.1\">\n" + 
		"<event name=\"msml.dialog.exit\" id=\"conn:192.168.1.1865060+1+13a0003+ee446c07/dialog:mydialogname\">\n"+
		"<name>dtmf.end</name>\n"+
		"<value>dtmf.match</value>\n"+
		"<name>dtmf.digits</name>\n"+
		"<value>2</value>\n"+
		"</event>\n"+
		"</msml>";
		
		
		
	if (info_media!= null) {
		
		AddressFactory addressFactory = sipProvider.getAddressFactory();;
		try{
		
			Dialog dialog = event.getServerTransaction().getDialog();
		    Address fromAddress = addressFactory.createAddress("sip:as@192.168.0.240");
		    Dialog forControlChannel = getDialogFromAtControlChannel(dialog,fromAddress);
     	    Dialog msSubscriberDialog = getLinkedMsDialog(dialog);
		    ToHeader to = getDialogToHeader(msSubscriberDialog);
		    long cSeq = this.getDialogCSeq(forControlChannel);
		    cSeq = cSeq+1;
		    this.setDialogCSeq(forControlChannel, cSeq);
		
		    Request info = null;   
	        CSeqHeader cseqHeader;
		    cseqHeader = sipProvider.getHeaderFactory().createCSeqHeader(cSeq, Request.INFO);
	        ContentTypeHeader contentTypeHeader = sipProvider.getHeaderFactory().createContentTypeHeader("application", "msml+xml"); 
	        MaxForwardsHeader maxForwardsHeader = sipProvider.getHeaderFactory().createMaxForwardsHeader(70);
	        ArrayList list = new ArrayList();
	       
	        
	        String data =  "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	                       "<msml version=\"1.1\">\n" + 
						  "<dialogstart target=\"conn:" + to.getTag() + 
						  	"\" type=\"application/moml+xml\" name=\"mydialogname\">\n" + 
						  "<play cvd:barge=\"true\" cvd:cleardb=\"true\">\n" +
						  "<audio uri=\"file://provisioned/thanks.wav\"/>\n" + 
						  "</play>\n"+ 
						  "<send target=\"source\" event=\"app.playcomplete\" namelist=\"play.amt\"/>\n"+ 
						  "<dtmf fdt=\"3s\" idt=\"5s\" edt=\"2s\">\n" +
						  "<pattern digits=\"min=2;max=6;rtk=#\" format=\"moml+digits\"/>\n"+
						  "<noinput/>\n"+ 
						  "<nomatch/>\n" + 
						  "</dtmf>\n"+ 
						  "<exit namelist=\"dtmf.digits dtmf.end\"/>\n"+ 
						  "</dialogstart>\n"+
						  "</msml>";

	        info = sipProvider.getMessageFactory().createRequest(
		            this.createMSRequestURI(),				//URI
					Request.INFO,						    //Method
					forControlChannel.getCallId(),	        //CallIdHeader
					cseqHeader,							    //CSeqHeader
					createFromHeader("Controller"),		    //FromHeader
					this.toHeader,                          //ToHeader
					list,								//List
					maxForwardsHeader,					//MaxForwardsHeader
					contentTypeHeader,					//ContenTypeHeader
					data);								//byte[] Content
        
	        info.addHeader(createContactHeaderForMS());
	        
	        
	        
	        ClientTransaction cti;
			cti = sipProvider.getNewClientTransaction(info);
			cti.sendRequest();
			} catch (TransactionUnavailableException e) {
			tracer.severe("onAck --> Could not send Info ", e);
			} catch (SipException e) {
			tracer.severe("onAck --> Could not send Info ", e);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	        
		*/
		
	
	
	
		
			

	
	

	public void onBye(RequestEvent event, ActivityContextInterface aci){
		 
		
		

         
//		 EnOceanConnectionActivity EnOceanaci1  =  getEnOceanActivity();
//
//		 EnOceanaci1.sendEnOceanTelegram("a55a6b0500000000fff7098020");
		
		tracer.info("###################### onBye #########################");
		AddressFactory addressFactory = sipProvider.getAddressFactory();;
			
			Dialog dialog = event.getServerTransaction().getDialog();
			String reqURI = event.getRequest().getRequestURI().toString();
	        int startHostname = reqURI.indexOf(":");
	        int endHostname = reqURI.indexOf("@");
	        String hostName = reqURI.substring(startHostname + 1, endHostname);
	        // is the syntax of the host name correct ?
	        int signEqual = hostName.indexOf("=");
	        String conf = hostName.substring(0, signEqual + 1);
	        if(conf.equalsIgnoreCase("enocean=")) {
		        	// if it comes from subscriber
					sendResponse(event, Response.OK);
			
					ClientTransaction cti;
				
					// Detach the SubscriberDialog from ACI
		        	DialogActivity dialogAci = (DialogActivity)dialog;
		            ActivityContextInterface inDialogAci = sipAcif.getActivityContextInterface(dialogAci);	
		            inDialogAci.detach(sbbContext.getSbbLocalObject());		
		            // remove the dialog from the lists
		            removeDialogServerTransaction(dialog);
		            removeSubscriberDialog(dialog);
		            
					// close the dialog between AS and MS for the subscriber
		            Dialog msDialog = getLinkedMsDialog(dialog);
		            tracer.info("-----------------check point for ms Dialog in onBYE----------------------" + msDialog);
		           
            
            //remove the dialog from the lists
            removeDialogToHeader(msDialog);
            removeLinkedDialogs(msDialog);
            removeMediaServerSubscriberDialog(msDialog);
            
            
          
			
            String confID = hostName.substring(signEqual + 1, conf.length() + 1);
            sendResponse(event, Response.OK);
            
        
       }
	}
				
		// TODO: implement methods to handle a BYE Request from the MS in a MSSubscriberDialog		
		// TODO: implement methods to handle a BYE Request from the MS in a ControlChannelDialog
        // for now we send an OK Response so there will be no errors on MS side
  
	

	

	private void removeDialogCSeq(Dialog dialog){
    	ServiceACIActivityContextInterface omniACI = getSACI();
    	if(omniACI.getDialogCSeq() == null || omniACI.getDialogCSeq().isEmpty()){
    		return;
    	}
    	HashMap<Dialog, Long> cSeqs = omniACI.getDialogCSeq();
    	cSeqs.remove(dialog);
    	omniACI.setDialogCSeq(cSeqs);
    }
	
	
	
	private ContactHeader createContactHeaderForMS() {
		ListeningPoint listeningPoint = sipProvider.getListeningPoint("udp");
		Address address;
		ContactHeader contactHeader = null;
		try {
			address = sipProvider.getAddressFactory().createAddress("Mobicents SIP AS <sip:as@" + listeningPoint.getIPAddress() + ">");
			((SipURI) address.getURI()).setPort(listeningPoint.getPort());
			contactHeader = sipProvider.getHeaderFactory().createContactHeader(address);
		} catch (ParseException e) {
			tracer.severe("Can not create Contact Header for MS: ", e);
		}
		return contactHeader;
	}
	 
	   private FromHeader createFromHeader(String PlayID) {
			ListeningPoint listeningPoint = sipProvider.getListeningPoint("udp");
			Address fromaddress;
			FromHeader fromHeader = null;
			try {
				fromaddress = sipProvider.getAddressFactory().createAddress(
						"<sip:as@" + listeningPoint.getIPAddress() + ">");
				fromHeader = sipProvider.getHeaderFactory().createFromHeader(fromaddress, "MobicentsASTag");
			} catch (ParseException e) {
				tracer.severe("Can not create From Header for MS: ", e);
			}
			return fromHeader;
			
		}
	 
	   private ToHeader createToHeader(String uniqueID) {
	      	Address toAddress;
	      	ToHeader toHeader = null;
			try {
				toAddress = sipProvider.getAddressFactory().createAddress("<sip:msml@"+msIp+">");
		        toHeader = sipProvider.getHeaderFactory().createToHeader(toAddress, null); 	    	
			} catch (ParseException e) {
				tracer.severe("Can not create To Header for MS: ", e);
			}
			return toHeader;
	    }
	   
	   
	   private void removeLinkedDialogs(Dialog msDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogs() != null && !omniACI.getDialogs().isEmpty()){
	    		Dialog inDialog = null;
	    		HashMap<Dialog, Dialog> dialogs = omniACI.getDialogs();
	        	// run through values to find the key
	        	for(Dialog linkedDialog:dialogs.keySet()){
	        		if(dialogs.get(linkedDialog).equals(msDialog)){
	        			inDialog = linkedDialog;
	        		}
	        	}
	        	dialogs.remove(inDialog);
	    	}
	    }
	   
	   
	   
	    private String getConfID(Request event){
	    	try{
	    		Request Ack = event;	
	    		ToHeader toHeader = (ToHeader) Ack.getHeader(ToHeader.NAME);
	    		String to = toHeader.getAddress().getURI().toString();
		       
		        int startHostname = to.indexOf(":");
		        int endHostname = to.indexOf("@");
		        String hostName = to.substring(startHostname + 1, endHostname);	        
		        int signEqual = hostName.indexOf("=");
		        String conf = hostName.substring(0, signEqual + 1);
		        String confID = hostName.substring(signEqual + 1, conf.length() + 1);
		        return confID;
	    	}catch (NullPointerException e) {
	    		tracer.severe("getConfID --> Unexpected error: ", e);
	    		return null;
	    	}catch (ArrayIndexOutOfBoundsException e){
	    		tracer.severe("getConfID --> Unexpected error: ", e);
	    		return null;
	    	}
	    }
	   
	   
	   private void removeDialogServerTransaction(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogServerTransaction() != null && !omniACI.getDialogServerTransaction().isEmpty()){
	    		HashMap<Dialog, ServerTransaction> transactions = omniACI.getDialogServerTransaction();
	    		transactions.remove(dialog);
	    	}
	    }
	   

	    
	   
	   private void removeDialogToHeader(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogToHeader() != null && !omniACI.getDialogToHeader().isEmpty()){
	        	HashMap<Dialog, ToHeader> toHeader = omniACI.getDialogToHeader();
	        	toHeader.remove(dialog);
	    	}
	    }
	   
	   
	   
	   
	   
	 
	 
	   private void sendResponse(RequestEvent evt, int cause) {
	        Request request = evt.getRequest();
	        ServerTransaction st = evt.getServerTransaction();
	        MessageFactory messageFactory = sipProvider.getMessageFactory();;
	        try {
	            Response response = messageFactory.createResponse(cause, request);
	            st.sendResponse(response);
	        } catch (ParseException e) {
	        	tracer.severe("Invalid request: ", e);
	        } catch (SipException e) {
	        	tracer.severe("Invalid request: ", e);
	        } catch (InvalidArgumentException e) {
	        	tracer.severe("Invalid request: ", e);
	        }
	    }
	 
	   private URI createMSRequestURI(){    	
	    	URI requestURI = null;
			try {			
				requestURI = sipProvider.getAddressFactory().createURI("sip:msml@"+msIp);
			} catch (ParseException e) {
				tracer.severe("Can not create MS Request URI: ", e);
			}
	    	return requestURI;	
	    }
	   
	   
	   private void setLinkedDialogs(Dialog inDialog, Dialog msDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	// if there does not exist a HashMap, create it
	    	if(omniACI.getDialogs() == null){
	    		HashMap<Dialog, Dialog> dialogs = new HashMap<Dialog, Dialog>();
	    		omniACI.setDialogs(dialogs);
	    	}
	    	// put the two dialogs to the map
	    	HashMap<Dialog, Dialog> dialogs = omniACI.getDialogs();
	    	dialogs.put(inDialog, msDialog);
	    	omniACI.setDialogs(dialogs);
	    }
	   
	   
	   
	   
	   private boolean setSubscriberDialog(Dialog subscriberDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	// if there does not exist an ArrayList for Subscribers Dialogs
	    	// create one
	    	if(omniACI.getSubscriberDialogs() == null){
	    		ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
	    		omniACI.setSubscriberDialogs(dialogs);
	    	}
	    	// if Dialog does not exist, add it
	    	if(!omniACI.getSubscriberDialogs().contains(subscriberDialog)){
	    		ArrayList<Dialog> dialogs = omniACI.getSubscriberDialogs();
	    		dialogs.add(subscriberDialog);
	    		omniACI.setSubscriberDialogs(dialogs);
	    		return false;
	    	}else{
	    		return true;
	    	}
	    }
	   

	 
	   
	   private boolean setMediaServerSubscriberDialog(Dialog MSSubscriberDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	// if there does not exist an ArrayList for MS Subscribers Dialogs
	    	// create one
	    	if(omniACI.getMSSubscriberDialogs() == null){
	    		ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
	    		omniACI.setMSSubscriberDialogs(dialogs);
	    	}
	    	// if Dialog does not exist, add it
	    	if(!omniACI.getMSSubscriberDialogs().contains(MSSubscriberDialog)){
	    		ArrayList<Dialog> dialogs = omniACI.getMSSubscriberDialogs();
	    		dialogs.add(MSSubscriberDialog);
	    		omniACI.setMSSubscriberDialogs(dialogs);
	    		return false;
	    	}else{
	    		return true;
	    	}
	   
	   }
	   
	   
	   private Dialog getMediaServerSubscriberDialog(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	// if there does not exist any dialog for MS Subscribers
	    	if(omniACI.getMSSubscriberDialogs() == null || omniACI.getMSSubscriberDialogs().isEmpty()){
	    		return null;
	    	}
	    	// if the dialog does not exist in list
	    	if(!omniACI.getMSSubscriberDialogs().contains(dialog)){
	    		return null;
	    	}
	    	ArrayList<Dialog> dialogs = omniACI.getMSSubscriberDialogs();
	    	int idx = dialogs.indexOf(dialog);
	    	return dialogs.get(idx);
	    }  
	   
	   
	   
	 
	    
	   
	   private void setDialogServerTransaction(Dialog dialog, ServerTransaction st){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	// if there does not exist a HashMap, create it
	    	if(omniACI.getDialogServerTransaction() == null){
	    		HashMap<Dialog, ServerTransaction> transactions = new HashMap<Dialog, ServerTransaction>();
	    		omniACI.setDialogServerTransaction(transactions);
	    	}
	    	// put the dialog to the map
	    	HashMap<Dialog, ServerTransaction> transactions = omniACI.getDialogServerTransaction();
	    	ServerTransaction st_copy =  st;
	    	transactions.put(dialog, st_copy);
	    	omniACI.setDialogServerTransaction(transactions);
	    }
	   
	  

	   
	 
	 private ServerTransaction getDialogServerTransaction(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogServerTransaction() == null || omniACI.getDialogServerTransaction().isEmpty()){
	    		return null;
	    	}
	    	HashMap<Dialog, ServerTransaction> transactions = omniACI.getDialogServerTransaction();
	    	return transactions.get(dialog);
	    }
	 
	    
	 
	 private void setControlChannelDialog(Dialog controlChannelDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	if(omniACI.getControlChannelDialog() != null && omniACI.getControlChannelDialog().containsKey("Controller")){
	    	    return;
	    	}
	   		if(omniACI.getControlChannelDialog() == null){
	   			HashMap<String, Dialog> dialogs = new HashMap<String, Dialog>();
	   			omniACI.setControlChannelDialog(dialogs);
	   		}
	   		HashMap<String, Dialog> dialogs = omniACI.getControlChannelDialog();
	   		dialogs.put("Controller", controlChannelDialog);
	   		omniACI.setControlChannelDialog(dialogs);	
	    }
	 
	 private Dialog getControlChannelDialog(String controller){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getControlChannelDialog() == null || omniACI.getControlChannelDialog().isEmpty()){
	    		return null;
	    	}
	    	HashMap<String, Dialog> dialogs = omniACI.getControlChannelDialog();
	    	return dialogs.get(controller);
	    }
	 
	    
	 private void setEnOceanActivity(EnOceanConnectionActivity  activity, String enOceanActivityHashMapKey){
		 ServiceACIActivityContextInterface omniACI = getSACI(); 
		 
//		 if(omniACI.getEnOceanActivity() == null || omniACI.getEnOceanActivity().isEmpty()){
//	    		return;
//	    	}
		 
         tracer.info("checkpoint of set EnOcean Activity omniACI--------->>" + omniACI);
         
         tracer.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<test the OMNIACI----------------------->>>>"+ omniACI.getEnOceanActivity());
      
        
	 
		 HashMap<String, EnOceanConnectionActivity> activityHashMap = new HashMap<String,EnOceanConnectionActivity>();
	   	
		 activityHashMap.put(enOceanActivityHashMapKey, activity);
		 omniACI.setEnOceanActivity(activityHashMap);
		
		
	  // 	activity.put("EnOceanActivity", Activity);
	    // omniACI.setEnOceanActivity(activity);  
	     tracer.info("checkpoint of set EnOceanActivity------value------------------------>>>>>"+ activityHashMap.get(enOceanActivityHashMapKey));
	     tracer.info("-------------------EnOceanActivity------test for ------------ key ------------"+ enOceanActivityHashMapKey);
	     tracer.info("checkpoint for the set EnOcean activity-------------------------->>>>"+ omniACI);

	 }
	 
	 
	 
	 private EnOceanConnectionActivity getEnOceanActivity(){
		     ServiceACIActivityContextInterface omniACI = getSACI();
	    
	    	
		     
//		     if(omniACI.getEnOceanActivity() == null || omniACI.getEnOceanActivity().isEmpty()){
//		    	tracer.info("test get EnOcean Activity------------------>>>>" +omniACI);	
//		    	 
//		    	 return null;
//		    		
//		    		
//		    	}
		     
		     HashMap<String,EnOceanConnectionActivity> activityHashMap= omniACI.getEnOceanActivity();
		    tracer.info("------------>>>>>>>>>>>>>>test to get EnOcean Activity------------>>>>>>>"+ omniACI.getEnOceanActivity());
		     tracer.info("test hash map activity--------->>>>>>>>>>" + activityHashMap);
//		     tracer.info("test the get EnOcean Activity   VALUE------------->>>>" + activityHashMap.get(enOceanActivity) );
//		     tracer.info("test the get EnOcean activity   KEY--------->>>>>>>>>  " + enOceanActivity);
		    
		     return activityHashMap.get(enOceanActivityHashMapKey);
		     
	    
	    }
	 
	 
//	 if(omniACI.getEnOceanActivity() == null || omniACI.getEnOceanActivity().isEmpty()){
// 		return null;

	 
	 

	 private Dialog getLinkedInDialog(Dialog msDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogs() == null || omniACI.getDialogs().isEmpty()){
	    		return null;
	    	}
	    	Dialog inDialog = null;
	    	HashMap<Dialog, Dialog> dialogs = omniACI.getDialogs(); 
	    	// run through values to find the key
	    	for(Dialog linkedDialog:dialogs.keySet()){
	    		if(dialogs.get(linkedDialog).equals(msDialog)){
	    			inDialog = linkedDialog;
	    		}
	    	}    	
			return inDialog;
	    }
	 
	 
	 
	 private ServiceACIActivityContextInterface getSACI(){
		 ServiceActivity serviceBus = this.serviceActivityFactory.getActivity();
			ActivityContextInterface serviceBusACI = null;
			try {
				serviceBusACI = this.serviceACIFactory.getActivityContextInterface(serviceBus);
			} catch (Exception e) {
				tracer.severe("Can not get SACI: ", e);
			}
			ServiceACIActivityContextInterface omniACI = asSbbActivityContextInterface(serviceBusACI);
	   return omniACI;
}
	 
	    /**
	     * Sets a ToHeader linked to dialog
	     * 
	     * @param dialog
	     * @param to
	     */
	    private void setDialogToHeader(Dialog dialog, ToHeader to){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	// if there does not exist a HashMap, create it
	    	if(omniACI.getDialogToHeader() == null){
	    		HashMap<Dialog, ToHeader> toHeader = new HashMap<Dialog, ToHeader>();
	    		omniACI.setDialogToHeader(toHeader);
	    	}
	    	// put the dialog to the map
	    	HashMap<Dialog, ToHeader> toHeader = omniACI.getDialogToHeader();
	    	toHeader.put(dialog, to);
	    	omniACI.setDialogToHeader(toHeader);
	    }
	 
	    
	    private void setDialogFromAtControlChannel(Dialog dialog, Address address){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	// if there does not exist a HashMap, create it
	    	if(omniACI.getDialogFromAtControlChannel() == null){
	    		HashMap<Address, Dialog> addressMap = new HashMap<Address, Dialog>();
	    		omniACI.setDialogFromAtControlChannel(addressMap);
	    	}
	    	// put the dialog to the map
	    	HashMap<Address, Dialog> addressMap = omniACI.getDialogFromAtControlChannel();
	    	System.out.println("SIZE of AddressMap BEFORE ADDING = " + addressMap.size());
	    	addressMap.put(address, dialog);
	    	System.out.println("SIZE of AddressMap AFTER ADDING = " + addressMap.size());
	    	omniACI.setDialogFromAtControlChannel(addressMap);
	    	
	    	//print out key value pairs in HashMap
	    	
	    	System.out.println(addressMap.get(address));
	    	
	    }
	    
	    private Dialog getDialogFromAtControlChannel(Dialog dialog, Address fromAddress){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogFromAtControlChannel() == null || omniACI.getDialogFromAtControlChannel().isEmpty()) {
	    		return null;
	    	}
	    	HashMap<Address, Dialog> dialogs = omniACI.getDialogFromAtControlChannel();
	    	//print out existing key value pairs in HashMap
	    	
	    	return dialogs.get(fromAddress);
	    }
	    
	    private Dialog getLinkedMsDialog(Dialog inDialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogs() == null || omniACI.getDialogs().isEmpty()){
	    		return null;
	    	}
	    	HashMap<Dialog, Dialog> dialogs = omniACI.getDialogs();
	    	return dialogs.get(inDialog);
	    }
	    
	    private void removeSubscriberDialog(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	// if there does not exist any dialog for Subscribers
	    	if(omniACI.getSubscriberDialogs() != null && !omniACI.getSubscriberDialogs().isEmpty()){
	    		ArrayList<Dialog> dialogs = omniACI.getSubscriberDialogs();
	    		dialogs.remove(dialog);
	    	}
	    }
	    
	    
	    private void removeMediaServerSubscriberDialog(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI(); 
	    	// if there does not exist any dialog for MS Subscribers
	    	if(omniACI.getMSSubscriberDialogs() != null && !omniACI.getMSSubscriberDialogs().isEmpty()){
	    		ArrayList<Dialog> dialogs = omniACI.getMSSubscriberDialogs();
	    		dialogs.remove(dialog);
	    	}
	    }
	    
	    
	    
	    private String getConfID(Response event){
	    	try{
	    		Response Ack = event;	
	    		FromHeader fromHeader = (FromHeader) Ack.getHeader(FromHeader.NAME);
	    		String from = fromHeader.getAddress().getURI().toString();
		       
		        int startHostname = from.indexOf(":");
		        int endHostname = from.indexOf("@");
		        String hostName = from.substring(startHostname + 1, endHostname);	        
		        int signEqual = hostName.indexOf("=");
		        String conf = hostName.substring(0, signEqual + 1);
		        String confID = hostName.substring(signEqual + 1, conf.length() + 1);
		        return confID;
	    	}catch (NullPointerException e) {
	    		tracer.severe("getConfID --> Unexpected error: ", e);
	    		return null;
	    	}catch (ArrayIndexOutOfBoundsException e){
	    		tracer.severe("getConfID --> Unexpected error: ", e);
	    		return null;
	    	}
	    }
	    
		/**
		 * Returns the ContactHeader from this AS for specific confID
		 * 
		 * @return ContactHeader
		 * @throws ParseExeption
		 */
	    private ContactHeader createContactHeader(String confID) {
			ListeningPoint listeningPoint = sipProvider.getListeningPoint("udp");
			Address address;
			ContactHeader contactHeader = null;
			try {
				address = sipProvider.getAddressFactory().createAddress("Mobicents SIP AS <sip:enocean=" + confID + "@" + listeningPoint.getIPAddress() + ">");
				((SipURI) address.getURI()).setPort(listeningPoint.getPort());
				contactHeader = sipProvider.getHeaderFactory().createContactHeader(address);
			} catch (ParseException e) {
				tracer.severe("Can not create Contact Header for MS: ", e);
			}
			return contactHeader;
		}
	    
	    /**
	     * Returns the  ID from ToHeader
	     *  
	     * @param event - Request
	     * @return - conference ID
	     */

	    
	    /**
	     * Returns a CSeqNumber linked to dialog
	     * 
	     * @param dialog
	     * @return CSeqNumber
	     */
	    private void removeControlChannelDialog(String controller){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getControlChannelDialog() == null || omniACI.getControlChannelDialog().isEmpty()){
	    		return;
	    	}
	    	HashMap<String, Dialog> dialogs = omniACI.getControlChannelDialog();
	    	dialogs.remove(controller);
	    }
	    
	    private void chooseGateway(ArrayList<Socket> gatewayList, ActivityContextInterface aci) {
	    	//ArrayList<Socket> gatewayList =(ArrayList<Socket>)event.getPayload(EnOceanEvent.GATEWAY_LIST);
	    	GatewayConnectionActivity gca = null;
	    	if(gatewayList.size()!=0){       
	    		gca = (GatewayConnectionActivity)aci.getActivity();
	    		EnOceanConnectionActivity eoa= gca.connect2Gateway(gatewayList.get(0).getInetAddress().getHostAddress());			
	    		tracer.info( "-> gateway available -> connect to: " + gatewayList.get(0).getInetAddress().getHostAddress() );    		
	    		aci.detach(sbbContext.getSbbLocalObject());
	    		ActivityContextInterface eoACI = connectionACIFactory.getActivityContextInterface(eoa.getEnOceanActivity());
	    		eoACI.attach(sbbContext.getSbbLocalObject());
	    	}else{
//	    		ActivityContextInterface gwACI = connectionACIFactory.getActivityContextInterface(gca.getGatewayActivity());
//	    		gwACI.attach(sbbContext.getSbbLocalObject());
	    		tracer.info( "-> No available gateway found! Waiting for new gateway." );
	    	}		
	    }
	    
	    
	    private Long getDialogCSeq(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogCSeq() == null || omniACI.getDialogCSeq().isEmpty()){
	    		return null;
	    	}
	    	HashMap<Dialog, Long> cSeqs = omniACI.getDialogCSeq();
	    	return cSeqs.get(dialog);
	    }
	    
	    /**
	     * Returns a ToHeader linked to dialog
	     * 
	     * @param dialog
	     * @return ToHeader
	     */
	    private ToHeader getDialogToHeader(Dialog dialog){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	if(omniACI.getDialogToHeader() == null || omniACI.getDialogToHeader().isEmpty()){
	    		return null;
	    	}
	    	HashMap<Dialog, ToHeader> toHeader = omniACI.getDialogToHeader();
	    	return toHeader.get(dialog);
	    }
	    
	    /**
	     * Sets a CSeqNumber linked to dialog
	     * 
	     * @param dialog
	     * @param cSeq
	     */
	    private void setDialogCSeq(Dialog dialog, long cSeq){
	    	ServiceACIActivityContextInterface omniACI = getSACI();
	    	// if there does not exist a HashMap, create it
	    	if(omniACI.getDialogCSeq() == null){
	    		HashMap<Dialog, Long> cSeqs = new HashMap<Dialog, Long>();
	    		omniACI.setDialogCSeq(cSeqs);
	    	}
	    	// put the dialog to the map
	    	HashMap<Dialog, Long> cSeqs = omniACI.getDialogCSeq();
	    	cSeqs.put(dialog, cSeq);
	    	omniACI.setDialogCSeq(cSeqs);
	    }
	 
	    
	   
	    protected final void attachServiceActivity() {
	        try {
	            ServiceActivity service = serviceActivityFactory.getActivity();
	            ActivityContextInterface aci = serviceActivityContextInterfaceFactory.getActivityContextInterface(service);
	            aci.attach(getSbbLocalObject());
	        } catch (UnrecognizedActivityException e) {
	            // should never happen
	            throw new RuntimeException(e);
	        }
	    }
	   
	    private SbbLocalObject getSbbLocalObject() {
			// TODO Auto-generated method stub
			return null;
		}

		protected final void detachServiceActivity() {
	        ActivityContextInterface[] acis = getSbbContext().getActivities();
	        for (int i = 0; i < acis.length; i++)
	        {
	            if (acis[i].getActivity() instanceof ServiceActivity)
	            {
	                acis[i].detach(getSbbLocalObject());
	            }
	        }
	    }
		
		
   	   private SbbContext getSbbContext() {
			// TODO Auto-generated method stub
			return null;
		}
	     
   	   
   	   
   	   
   	   
   	   
   	   
   	   
   	   
	    
	    // CMP fields
	    public abstract void setAckExpectedforRejection(String ackID);
	    public abstract String getAckExpectedforRejection();
	    public abstract void setAckExpectedforCall(String callID);
	    public abstract String getAckExpectedforCall();

//	    public abstract void setStoredEnOceanActivity(ActivityContextInterface eoACI);
//	    public abstract  ActivityContextInterface getStoredEnOceanActivity();
	    
//		public abstract void setEnOceanActivity(EnOceanEvent EnOevent, ActivityContextInterface activity);
//		public abstract EnOceanEvent getEnOceanActivity();
			
		// TODO Auto-generated method stub
			
		
//	    public abstract void setEnOceanActivity(activity);
//	    public abstract String getEnOceanActivity();
   
	   
	    
	
}
