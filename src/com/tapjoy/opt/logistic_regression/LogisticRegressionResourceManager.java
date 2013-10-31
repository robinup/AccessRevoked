package com.tapjoy.opt.logistic_regression;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.tapjoy.opt.ModelController;
import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.logistic_regression.config.Configuration;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.util.StringUtil;

public class LogisticRegressionResourceManager extends ResourceManager {
	private static Logger logger = Logger.getLogger(LogisticRegressionResourceManager.class);
	
	public LogisticRegressionResourceManager() {
		super();
		RELOAD_DELAY = 15 * 60;
		setupCronJob();
	}

	@Override
	protected void initialize() {
		logger.info("Initializing LR resource manager.");
		
		container = LogisticRegressionResourceDataContainer.getInstance();
		updateMgr = new LogisticRegressionResourceUpdateManager(container);
		computeEngine = new LogisticRegressionOfferListComputeEngine(container);
		
		backupRsrcMgr =  ModelController.getRegModel(ModelController.getAlgoIndex(Configuration.BACKUP_ALGO_ID));
	}

	@Override
	public String getOfferListKey(HashMap <String, String> specs, boolean asStaticEngine){
		StringBuffer buff = new StringBuffer();
		
		buff.append(specs.get("algorithm"));

		buff.append(".");
		if (specs.get("source") == null) {
			return null;
		} else {
			String source = specs.get("source");
			if ("offerwall".equals(source)) {
				buff.append("0");
			} else {
				buff.append("1");
			}
		}
		
		
		// os
		buff.append(".");
		buff.append(specs.get("platform"));
		
		// Country
		buff.append(".");
		String country = specs.get("primaryCountry");
		if (country != null) {
			buff.append(country);
		}
		
		// currency
		buff.append(".");
		String currency = specs.get("currency_id");
		if (currency != null) {
			buff.append(currency);
		}
		
		// device
		String device = specs.get("device_type");
		buff.append(".");
		buff.append(device);	
		
		// udid
		String appId = specs.get("app_id");
		if (appId != null) {
			buff.append(".");
			buff.append(appId);
		}
		
		return buff.toString();
	}
	
	@Override
	protected OfferList retrieveOrCreateOfferList(HashMap<String, String> specs) {
		String backupKey = backupRsrcMgr.getOfferListKey(specs, true);
		
		logger.info(String.format("Backup Key: %s", backupKey));
		
		OfferList staticOl = OfferListCache.getInstance().retrieve(backupKey, true);
		
		String key = getOfferListKey(specs, false);
		logger.info(String.format("Key: %s", key));
		return this.computeEngine.computeForDevice(key, specs, staticOl);
	}

	@Override
	public String getOfferListKey(String defaultKey,
			HashMap<String, String> specs) {
		// TODO Auto-generated method stub
		return null;
	}

}
