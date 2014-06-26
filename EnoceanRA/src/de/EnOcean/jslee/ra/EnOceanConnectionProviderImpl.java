package de.EnOcean.jslee.ra;

import de.EnOcean.jslee.ratype.*;
//
public class EnOceanConnectionProviderImpl implements EnOceanConnectionProvider {
//
	protected Identification id = null;
	protected Listener listener= null;
//	
//	
	public EnOceanConnectionProviderImpl(Identification  id, Listener listener)
	{
		super();
		
		this.id =id;
		this.listener=listener;
	}
//
//	
//public SessionIDActivity getNewSessionID(Address from, Address to)
//	throws Exception {
//			if (from == null) {
//				throw new IllegalArgumentException("From address cant be null");
//			}
//			if (to == null) {
//				throw new IllegalArgumentException("To address cant be null");
//			}
//			return this.id;
//		}
//	
//	
	@Override
	public Listener getListener() {
		// TODO Auto-generated method stub
		return this.listener;
	}
}
