package com.tapjoy.opt.cache;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tapjoy.opt.offerlist.OfferList;

/**
 * 
 * @author lding
 * 
 * In Memory Cache for the offer lists
 * 
 */
public class OfferListCache {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(OfferListCache.class);
	
	// The static cache. Entries in this cache should be removed explicitly when
	// not needed any more
	private HashMap<String, OfferList> sCache = new HashMap<String, OfferList>();
	
	// The volatile cache. When it reaches the size limit, the earliest inserted
	// entries will be cleaned
	private VolatileCache<String, OfferList> vCache = new VolatileCache<String, OfferList>(
			18000, 16, 0.75f, false);

	private static OfferListCache offerCache = new OfferListCache();

	public static OfferListCache getInstance() {
		return offerCache;
	}

	private OfferListCache() {
	}
	
	
	public Set<String> getKeySet(){
		return sCache.keySet();
	}

	/**
	 * @param permanent -- static or volatile
	 */
	public void store(String key, OfferList offerList, boolean permanent) {
		if (permanent) {
			sCache.put(key, offerList);
		} else {
			vCache.put(key, offerList);
		}
	}

	
	public OfferList retrieve(String key, boolean permanent) {
		if (permanent) {
			//if(!sCache.containsKey(key))
			//	System.out.printf("OfferListCache retrieval sCache returns a null --- key=%s", key);
			return sCache.get(key);
		} else {
			//if(!vCache.containsKey(key))
			//	System.out.printf("OfferListCache retrieval vCache returns a null --- key=%s", key);
			return vCache.get(key);
		}
	}


	public OfferList retrieve(String vKey, String sKey) {
		OfferList offerList = vCache.get(vKey);

		if (offerList == null) {
			offerList = sCache.get(sKey);
		}
		
		//if(offerList == null)
		//	System.out.printf("OfferListCache  null --- key=%s and %s\n", vKey, sKey);
		return offerList;
	}


	// Use this when you are not sure which retrieve to use
	public OfferList retrieve(String key) {
		OfferList offerList = vCache.get(key);

		if (offerList != null) {
			return offerList;
		}

		return sCache.get(key);
	}
	
	public void printKeys(boolean flag)
	{
		if(flag)
		{
			for(String key: sCache.keySet())
			{
				System.out.printf("%s, ", key);
			}
			System.out.println();
		}
		else
		{
			for(String key: vCache.keySet())
			{
				System.out.printf("%s, ", key);
			}
			System.out.println();
		}
	}
	
	public int size(boolean flag)
	{
		if(flag)
			return sCache.size();
		else
			return vCache.size();
	}
}
