package com.tapjoy.opt.object_cache;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class CachedEntry implements Cacheable , Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// time the entry would be expired
	private Date expirationDate = null;
	private static Random random = new Random();

	private Object primaryKey = null;
	public Object value = null;
	private int reuseCount = 0;

	public CachedEntry(Object primaryKey, Object value, int ttl) {
		double effectiveTTL = ttl;
		
		this.value = value;
		this.primaryKey = primaryKey;

		// allowing 25% even distribution of ttl to spread out expiration
		int id = random.nextInt();
		id = id > 0 ? id : -id;
		id = id % 100;
		effectiveTTL = effectiveTTL - effectiveTTL*0.25*((double)id/100.0);
	
		if (ttl != 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.SECOND, (int)effectiveTTL);
			expirationDate = cal.getTime();
		}
	}

	/**
	 * check if the object is expired
	 */
	public boolean isExpired() {
		return isExpired(new Date());
	}

	/**
	 * check if the object is expired
	 */
	public boolean isExpired(Date date) {
		boolean isExpired = false;
		Date expiredDate = (date==null? new Date(): date);

		if (expirationDate != null) {
			if (expirationDate.before(expiredDate)) {
				return true;
			}
		}

		return isExpired;
	}

	/**
	 * returning the primary key of the object
	 */
	public Object getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * returning the cache object
	 */
	public Object getValue() {
		return value;
	}

	public int getReuseCount() {
		return reuseCount;
	}

	public void incReuseCount() {
		reuseCount++;
	}
}
