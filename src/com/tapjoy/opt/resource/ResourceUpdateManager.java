package com.tapjoy.opt.resource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.spy.memcached.MemcachedClient;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListFactory;
import com.tapjoy.opt.offerlist.OfferListRowMC;
import com.tapjoy.opt.util.MClient;

/**
 * 
 * @author lding
 *
 * Each DataResource need to get updated frequently. The UpdateManager is responsible for:
 *   1) Decide when to update the resource
 *   2) Do the update job
 *   3) Synchronize with other servers during the update process 
 */
public abstract class ResourceUpdateManager {
	protected ResourceDataContainer dataContainer;
	
	public ResourceUpdateManager(ResourceDataContainer dataContainer){
		this.dataContainer = dataContainer;
	}
	
	public abstract void reloadDataResource();
	
	protected abstract HashSet<String> getStaticOfferListKeys();
	
	protected abstract void setOfferListKeys(HashSet<String> offerListKeys);
	
	protected abstract String getIDKey();
	
	protected abstract Logger getLogger();
	

	
	public boolean exportDataResource(MemcachedClient mcClt) {
		String key_base = getIDKey();
		Logger logger = getLogger();
		try{
			// allOfferMap
			logger.info("Start exportDataResource");
			

			// Step 1 - Breakdown allOfferMap and write it to MC
		    // 
			// this.exportAllOfferMapToMC(allOfferMap, mcClt);
			
			
			// Step 2 - write all static offer lists to MC
			HashSet<String> offerListKeys = getStaticOfferListKeys();
			if (! MClient.executeCmd(mcClt, "set", key_base + ":OLKeys", 7200, offerListKeys, 1000)){
				return false;
			}
			logger.info("ExportDataResource - written OfferKeys : " + offerListKeys.size());

			
			for (String key : offerListKeys) {
				OfferList ol = OfferListCache.getInstance().retrieve(key, true);
				//System.out.println("LeiTest - exportDataResource - about to write OfferList: " + key);
				if (ol != null) {
					LinkedList<OfferListRowMC> ofmc = ol.toMcFormat();
					if (ofmc == null) {
						continue; 
					}
					if (! MClient.executeCmd(mcClt, "set", key_base + ":OL:" + key, 7200, ol.toMcFormat(), 1000)){
						return false;
					}
					// mcClt.set(MCKEY_BASE + key, 7200, ol.toMcFormat());
					//System.out.println("LeiTest - exportDataResource - written list:" + key);
				}
			}
			
			logger.info("ExportDataResource - written Offers (MC extrac)");
		} catch (Exception e) {
			logger.error("Error Happened during exportDataResource; " + e.getMessage());
			return false;
		}
		
		return true;
	}



	public boolean importDataResource(MemcachedClient mcClt)  { 
		String key_base = getIDKey();
		Logger logger = getLogger();
		
		logger.info("Start importDataResource");
		
		Map<String, Row> allOfferMap = OfferCache.getInstance().get(); 
		
		// Restore the List keys
		@SuppressWarnings("unchecked")
		HashSet<String> offerListKeys = (HashSet<String>)(mcClt.get(key_base + ":OLKeys"));
		if (offerListKeys == null) {
			return false;
		}
		logger.info("ImportDataResource - restored OfferKeys: " + offerListKeys.size());
		
		// Restore Each list
		for (String key : offerListKeys) {
			//System.out.println("LeiTest - importDataResource - restoring list: " + ":OL:" + key);
			@SuppressWarnings("unchecked")
			List<OfferListRowMC> offerlistMc = (List<OfferListRowMC>)(mcClt.get(key_base + ":OL:" + key));
			
			if (offerlistMc != null){
				OfferList ol = OfferListFactory.restoreFromMcObject(key, true, offerlistMc, allOfferMap);
				OfferListCache.getInstance().store(key, ol, true);
				//System.out.println("LeiTest - importDataResource - restored list: " + key);
			} else {
				logger.error("ImportDataResource - restored list: " + key + " failed");
				return false;
			}
		}
		
		logger.info("ImportDataResource - restored individual offer lists");
		setOfferListKeys(offerListKeys);
		
		return true;
	}
}
