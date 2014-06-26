package de.fhffm.service.enocean_service;




public class MSMLCreator {

	/**
	 * creates an Audio/Video conference room MSML
	 * 
	 * @param confID
	 * @return data
	 */
	
	

	
	/**
	 * adds an user to a conference room with audio and video
	 * 
	 * @param uniqueID
	 * @param toTag
	 * @param position
	 * @return msml
	 */
	public String addUser(String uniqueID, String toTag, int position){	
	    String msml = 
	    "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
		"<msml version=\"1.1\">\n" +
			"<join id1=\"conn:" + toTag + "\" id2=\"conf:" + uniqueID + "\">\n" +
					"<stream media=\"audio\"/>\n" +
					"<stream media=\"video\" dir=\"to-id1\"/>\n" +
					"<stream media=\"video\" dir=\"from-id1\" display=\"" + position + "\"/>\n" +
			"</join>\n" +
		"</msml>";
	    return msml;
	}
	
	/**
	 * adds an user to a conference room with audio
	 * 
	 * @param uniqueID
	 * @param toTag
	 * @return msml
	 */

	public String addUserAudio(String uniqueID, String toTag){	
	    String msml = 
    	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
 		"<msml version=\"1.1\">\n" +
     		"<join id1=\"conn:" + toTag + "\" id2=\"conf:" + uniqueID + "\">\n" +
         			"<stream media=\"audio\"/>\n" +
     		"</join>\n" +
 		"</msml>";    
	    return msml;
	}
	
	/**
	 * removes a user from the conference room
	 * 
	 * @param uniqueID
	 * @param toTag
	 * @return msml
	 */
	public String removeUserFromConf(String uniqueID, String toTag){
	    String msml = 
	    	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 		"<msml version=\"1.1\">\n" +
	     		"<unjoin id1=\"conn:" + toTag + "\" id2=\"conf:" + uniqueID + "\"/>\n" +
	 		"</msml>";    
		    return msml;
	}

	/**
	 * destroys the conference room
	 * 
	 * @param uniqueID
	 * @return msml
	 */
	public String destroyConference(String uniqueID){
	    String msml = 
	    	"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" +
	 		"<msml version=\"1.1\">\n" +
	     		"<destroyconference id=\"conf:" + uniqueID + "\"/>\n" +
	 		"</msml>";    
		    return msml;
	}

}