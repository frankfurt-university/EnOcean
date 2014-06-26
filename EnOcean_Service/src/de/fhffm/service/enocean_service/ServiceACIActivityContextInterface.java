package de.fhffm.service.enocean_service;

import gov.nist.javax.sip.header.From;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ToHeader;
import javax.slee.ActivityContextInterface;

import de.EnOcean.jslee.ra.ActivityHandler;
import de.EnOcean.jslee.ra.EnOceanActivityHandler;
import de.EnOcean.jslee.ratype.EnOceanConnectionActivity;
import de.EnOcean.jslee.ratype.GatewayConnectionActivity;


public interface ServiceACIActivityContextInterface extends ActivityContextInterface{

	/**
	 * Returns an ArrayList of Conference IDs
	 * 
	 * @return ArrayList<String>
	 */
  
    
    /**
     * Sets an ArrayList of Conference IDs
     * 
     * @param ArrayList<String>
     */

    
    /**
     * Returns a HashMap with linked ControlChannel dialogs and ConfIDs
     * 
     * @return HashMap<String, Dialog>
     */
    public HashMap<String, Dialog> getControlChannelDialog();
    
 
//    
    /**
     * Sets the ControlChannel dialog for a ConfID
     * 
     * @param HashMap<String, Dialog>
     */
    public void setControlChannelDialog(HashMap<String, Dialog> dialogs);
    
    
    public void setEnOceanActivity(HashMap<String,EnOceanConnectionActivity> activity);
    
    public HashMap<String,EnOceanConnectionActivity> getEnOceanActivity();

    
//    public ArrayList<EnOceanConnectionActivity> getEnOceanActivity();
    
    /**
     * Returns an ArrayList of dialogs for UAs
     * 
     * @return ArrayList<Dialog>
     */
    public ArrayList<Dialog> getSubscriberDialogs();
    
    /**
     * Sets an ArrayList of dialogs for UAs
     * 
     * @param ArrayList<Dialog>
     */
    public void setSubscriberDialogs(ArrayList<Dialog> dialogs);
    
    /**
     * Returns an ArrayList of dialogs for UAs to MS
     * 
     * @return ArrayList<Dialog>
     */
    public ArrayList<Dialog> getMSSubscriberDialogs();
    
    /**
     * Sets an ArrayList of dialogs for UAs to MS
     * 
     * @param ArrayList<Dialog>
     */
    public void setMSSubscriberDialogs(ArrayList<Dialog> dialogs);
    
    /**
     * Returns a HashMap with linked dialogs
     * 
     * @return HashMap<Dialog, Dialog>
     */
    public HashMap<Dialog, Dialog> getDialogs();
    
    /**
     * Sets a HashMap with linked dialogs
     * 
     * @param dialogs
     */
    public void setDialogs(HashMap<Dialog, Dialog> dialogs);
    
    /**
     * Returns a HashMap with Dialogs and their linked ServerTransactions
     * 
     * @return HashMap<Dialog, ServerTransaction>
     */
    public HashMap<Dialog, ServerTransaction> getDialogServerTransaction();
    
    /**
     * Sets a HashMap with Dialogs and their linked ServerTransactions
     * 
     * @param transactions
     */
    public void setDialogServerTransaction(HashMap<Dialog, ServerTransaction> transactions);
    
    /**
     * Returns the position in quad-splitted video
     * 
     * @return HashMap<String, Integer>
     */
    public HashMap<String, Integer> getUserPosition();
    
    /**
     * Sets the position in quad-splitted video
     * 
     * @param position
     */

    /**
     * Returns a HashMap with Dialogs and their linked ToHeaders
     * 
     * @return HashMap<Dialog, ToHeader>
     */
    public HashMap<Dialog, ToHeader> getDialogToHeader();
    
    /**
     * Sets a HashMap with Dialogs and their linked ToHeader
     * 
     * @param toHeaders
     */
    public void setDialogToHeader(HashMap<Dialog, ToHeader> toHeaders);
    
    /**
     * Sets a HashMap with Dialogs and their linked  address
     * 
     * @param address
     */
    
    public void setDialogFromAtControlChannel(HashMap<javax.sip.address.Address, javax.sip.Dialog> address);
    
    /**
    * Returns a HashMap with Dialogs and their linked address
    * 
    * @return HashMap<Address, Dialog>
    */
   
    public HashMap<javax.sip.address.Address, javax.sip.Dialog> getDialogFromAtControlChannel();
    
    /**
     * Returns a HashMap with Dialogs and their linked CSeqNumber
     * 
     * @return HashMap<Dialog, Long>
     */
    
    public HashMap<Dialog, Long> getDialogCSeq();
    
    /**
     * Sets a HashMap with Dialogs and their linked CSeqNumber
     * 
     * @param cSeqNumber
     */
    public void setDialogCSeq(HashMap<Dialog, Long> cSeqNumber);
    
    /**
     * Returns a HashMap with CallID and ConfID
     * 
     * @return HashMap<CallIdHeader, String>
     */
   
   
}
