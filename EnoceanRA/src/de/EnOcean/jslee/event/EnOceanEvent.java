package de.EnOcean.jslee.event;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.io.Serializable;

public final class EnOceanEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 100082601068192048L;
	
	//keys
	public static final int CONTENT = 0 ;
	public static final int GATEWAY_LIST = 1 ;
	public static final int ENOCEAN_TELEGRAM = 2 ;
	public static final int GATEWAY_ID = 3;
	public static final int READY_MESSAGE = 4;
	
	//content_values
	public static final int GATEWAY_LIST_EVENT = 0;
	public static final int GATEWAY_READY_EVENT = 1;
	public static final int TELEGRAM_RECEIVED_EVENT = 2;
	public static final int GATEWAY_CONNECTION_CLOSED_EVENT = 3;	
	
	
	Hashtable<Object, Object> payload;

	public EnOceanEvent() {
		id = new Random().nextLong() ^ System.currentTimeMillis();
		payload = new Hashtable<Object, Object>();
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		return (o instanceof EnOceanEvent) && ((EnOceanEvent)o).id == id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String toString() {
		return "EnOceanEvent[" + hashCode() + "]";
	}

	public void setPayload(Object key, Object value){

		payload.put(key, value);

	}
	
	public Enumeration<Object> getKeys(){
		return payload.keys();		
	}
	
	
	public Object getPayload(Object key){	
		Object value = payload.get(key);;	
		return value;
	}
	
	private final long id;
}
