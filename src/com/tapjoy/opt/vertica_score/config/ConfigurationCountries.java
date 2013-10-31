package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationCountries extends ConfigurationLookback {
	public static Map<String, Integer> lookbackDates = new HashMap<String, Integer>();
	public static ConfigurationCountries myConfiguration = new ConfigurationCountries();

	public static Integer GLOBAL_LOOKBACK_DATE = 14;   //suggested to use original int for dynamic configurtation, but ok - LJ
	
	static {
		lookbackDates.put("US", 28);
		lookbackDates.put("GB", 28);
		lookbackDates.put("UK", 28);
		lookbackDates.put("KR", 28);
		lookbackDates.put("CA", 28);
		lookbackDates.put("DE", 28);
		lookbackDates.put("FR", 28);
		lookbackDates.put("FX", 28);
		lookbackDates.put("CN", 28);
		lookbackDates.put("AU", 28);
		lookbackDates.put("HK", 28);
		lookbackDates.put("JP", 28);
		lookbackDates.put("JA", 28);
		
		/*
		lookbackDates.put("KR", 28);
		lookbackDates.put("CN", 28);
		lookbackDates.put("GB", 28);
		lookbackDates.put("FR", 28);
		lookbackDates.put("DE", 28);
		lookbackDates.put("CA", 28);
		lookbackDates.put("HK", 28);
		lookbackDates.put("JP", 28);
		lookbackDates.put("TW", 28);
		*/
	}

	public ConfigurationCountries() {
		super.init(lookbackDates);
	}
	
	public static boolean isValidCountry(String country){
		if (country == null || lookbackDates.get(country) == null) {
			return false;
		}
		
		return true;
	}
}
