package com.tapjoy.opt.linear_regression;

import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.util.StringUtil;

import com.tapjoy.opt.linear_regression.config.Configuration;

public class LinearRegressionResourceManager extends ResourceManager {
	
	public LinearRegressionResourceManager() {
		super();
		RELOAD_DELAY = 900;
		MCKEY_BASE = "LinearRegression";
		setupCronJob();
	}
	
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		container = LinearRegressionResourceDataContainer.getInstance();
		updateMgr = new LinearRegressionResourceUpdateManager(container);
		computeEngine = new LinearRegressionOfferListComputeEngine(container);
	}
	
	public String getOfferListKey(HashMap <String, String> specs, boolean asStaticEngine){
		StringBuffer buff = new StringBuffer();
		
		// Algo
		String algo = specs.get("algorithm");
		if (StringUtil.findInArray(algo, Configuration.algorithms)) {
			buff.append(algo);
		} else if (asStaticEngine) {
			algo = Configuration.DEFAULT_ALGO;
			buff.append(Configuration.DEFAULT_ALGO);
		} else {
			return null;
		}
		
		// Platform
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
		String os = specs.get("platform");
		if ( ! StringUtil.findInArray(os, Configuration.OS.activeOs) ) {
			return null;
		} else {
			buff.append(os);
		}
		
		// Country
		buff.append(".");
		String country = specs.get("primaryCountry");
		if (Configuration.Ranking.OPT_COUNTRY && Configuration.isValidCountry(algo, country) ) {
			buff.append(country);
			// Since we have chosen the country specific offerlist, no need to apply filter on country
			specs.remove("primaryCountry");
		}
		
		// currency
		buff.append(".");
		String currency = specs.get("currency_id");
		if (Configuration.Ranking.OPT_APP && Configuration.isValidCurrency(algo, currency) ) {
			buff.append(specs.get("currency_id"));
			// Since we have choosen the country specific offerlist, no need to apply filter on country
			specs.remove("currency_id");
		}		
		
		// device
		String device = specs.get("device_type");
		if (device == null) {
			return null;
		}
		buff.append(".");
		buff.append(device);		
		
		return buff.toString();
	}


	@Override
	public String getOfferListKey(String defaultKey, HashMap<String, String> specs) {
		return null;
	}

}
