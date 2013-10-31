package com.tapjoy.opt.featured_logit;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.featured_logit.config.Configuration;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.resource.ResourceManager;


public class FeaturedLogisticRegressionResourceManager extends ResourceManager {

	private static Logger logger = Logger.getLogger(ResourceManager.class);
	
	public FeaturedLogisticRegressionResourceManager() {
		super();
		RELOAD_DELAY = 900;
		MCKEY_BASE = "FeaturedLogisticRegression";
		setupCronJob();
	}
	
	protected void initialize() {
		// TODO Auto-generated method stub
		container = FeaturedLogisticRegressionResourceDataContainer.getInstance();
		updateMgr = new FeaturedLogisticRegressionResourceUpdateManager(container);
		computeEngine = new FeaturedLogisticRegressionOfferListComputeEngine(container);
	}
	
	// Override the default version
	@SuppressWarnings("unchecked")
	protected OfferList retrieveOrCreateOfferList(HashMap <String, String> specs) {
		// get the static key
		String key = getOfferListKey(specs, false);
		System.out.println(key);
//		if (key == null ) {
//			logger.error("Requests mapped to empty key");
//			return null;
//		}
		
		// Check if the static version is in cache
		OfferList staticOl = OfferListCache.getInstance().retrieve(key, true);
		if (staticOl == null) {
			key = getGlobalOfferListKey(specs);
			//System.out.println(key);
			staticOl = OfferListCache.getInstance().retrieve(key, true);
			if (staticOl == null) {
				key = getGeneralOfferListKey(specs);
				staticOl = OfferListCache.getInstance().retrieve(key, true);
				if(staticOl == null)
				{
					logger.error("Requests mapped to empty key");
					return null;
				}
			}
		}	
		
		// get the customized key
		String customizedKey = getOfferListKey(key, specs);
		
		// check if the customized offer list is in cache. if so. serve it
		OfferList ol = OfferListCache.getInstance().retrieve(customizedKey, false);
		if (ol != null) {
			System.out.println("Customized key was found. retrieveOrCreateOfferList() --- end");
			return ol;
		}

		return this.computeEngine.computeForDevice(customizedKey, specs, new OfferListWithref(customizedKey, staticOl.getOffers(), false));	
	}

	@Override
	public String getOfferListKey(HashMap<String, String> specs, boolean asStaticEngine) {
		StringBuffer buff = new StringBuffer();
		
		
		// Algo
		String algo = specs.get("algorithm");
		if (algo != null) {
			buff.append(algo);
		} else if (asStaticEngine) {
			algo = Configuration.DEFAULT_ALGO;
			buff.append(Configuration.DEFAULT_ALGO);
		} else {
			return null;
		}
		

		// Country
		buff.append(".");
		String country = specs.get("primaryCountry");
		if(country == null) {
			country = "US";  //fallback routine added
		}
		buff.append(country);
		
		
		//App ID
		buff.append(".");
		String app_id = specs.get("app_id");
		if(app_id == null) {
			return null;
		}
		buff.append(app_id);
		
		
		// os
		buff.append(".");
		String os = specs.get("platform");
		if ( os == null ) {
			return null;
		} else {
			buff.append(os);
		}
		
		return buff.toString();
	}

	@Override
	public String getOfferListKey(String defaultKey,HashMap<String, String> specs) {
		return defaultKey + "." + specs.get("udid");
	}
	
	private String getGlobalOfferListKey(HashMap<String, String> specs) {
		StringBuffer buff = new StringBuffer();
		// Algo
		String algo = specs.get("algorithm");
		if (algo != null) {
			buff.append(algo);
		} else {
			return null;
		}
		
		buff.append(".GLOBAL.");
		
		// Country
		String country = specs.get("primaryCountry");
		if(country == null) {
			country = "US";
		}
		buff.append(country);
		
		buff.append(".");
		String os = specs.get("platform");
		if (os == null ) {
			return null;
		} else {
			buff.append(os);
		}
		
		return buff.toString();
	}
	
	private String getGeneralOfferListKey(HashMap<String, String> specs) {
		StringBuffer buff = new StringBuffer();
		// Algo
		String algo = specs.get("algorithm");
		if (algo != null) {
			buff.append(algo);
		} else {
			return null;
		}
		
		buff.append(".GLOBAL.");
		
		// Country
		buff.append("US");
		
		buff.append(".");
		String os = specs.get("platform");
		if (os == null ) {
			return null;
		} else {
			buff.append(os);
		}
		
		return buff.toString();
	}
}

