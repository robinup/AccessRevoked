package com.tapjoy.opt.vertica_score;

import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import com.tapjoy.opt.resource.ResourceManager;
import com.tapjoy.opt.util.StringUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationApp;
import com.tapjoy.opt.vertica_score.config.ConfigurationCountries;

public class VerticaScoreResourceManager extends ResourceManager {
	
	private static Logger logger = Logger.getLogger(VerticaScoreResourceManager.class);

	public VerticaScoreResourceManager() {
		super();
		MCKEY_BASE = Configuration.IDKEY;
		setupCronJob();
	}

	@Override
	protected void initialize() {	
		container = VerticaScoreResourceDataContainer.getInstance();
		updateMgr = new VerticaScoreResourceUpdateManager(container);
		computeEngine = new VerticaScoreOfferListComuteEngine(container);
	}
	
	public String getOfferListKey(HashMap <String, String> specs, boolean asStaticEngine){
		StringBuffer buff = new StringBuffer();
		
		String algo = specs.get("algorithm");
		if (StringUtil.findInArray(algo, Configuration.algorithms)) {
			buff.append(algo);
		} else if (asStaticEngine) {
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
		if (Configuration.Ranking.OPT_COUNTRY && ConfigurationCountries.isValidCountry(country) ) {
			buff.append(country);
			// Since we have chosen the country specific offerlist, no need to apply filter on country
			specs.remove("primaryCountry");
		}
		
		// currency
		buff.append(".");
		String currency = specs.get("currency_id");
		if (Configuration.Ranking.OPT_APP && ConfigurationApp.isValidCurrency(currency) ) {
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
