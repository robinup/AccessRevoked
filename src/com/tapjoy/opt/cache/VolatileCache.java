package com.tapjoy.opt.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * @author lei.ding@tapjoy.com
 *
 * @param <K>
 * @param <V>
 * 
 * Override the LinkedHashMap to make it the data storage engine for the volatile cache
 * 
 * Choose the maxEntries value carefully such that it will host enough 
 * entries without using too much memory
 * 
 */

@SuppressWarnings("serial")
public class VolatileCache<K, V> extends LinkedHashMap<K, V> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(VolatileCache.class);
	
	private double maxEntries;
	
	public VolatileCache(double maxEntries, int cap, float loadFactor, boolean policy){
		super(cap, loadFactor, policy);
		this.maxEntries = maxEntries;
	}

    @SuppressWarnings("rawtypes")
	protected boolean removeEldestEntry(Map.Entry eldest) {
       return size() > maxEntries;
    }

}
