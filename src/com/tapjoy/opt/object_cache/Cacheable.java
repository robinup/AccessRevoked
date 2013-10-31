package com.tapjoy.opt.object_cache;

import java.util.Date;

public interface Cacheable {
	/**
	 * Allowing the object having its own expiration policy to gain	flexibility
	 */
	public boolean isExpired();
	
	public boolean isExpired(Date date);	
	
	/**
	 * uniquely identifying cached object
	*/
	public Object getPrimaryKey();
	
	public Object getValue();
}
