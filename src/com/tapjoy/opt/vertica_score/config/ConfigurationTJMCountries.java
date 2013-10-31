package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationTJMCountries extends ConfigurationLookback {
	public static Map<String, Integer> lookbackDates = new HashMap<String, Integer>();
	public static ConfigurationTJMCountries myConfiguration = new ConfigurationTJMCountries();

	public static Integer GLOBAL_LOOKBACK_DATE = 14;  //suggested to use original int for dynamic configuration, but ok -- LJ
	
	static {
		lookbackDates.put("US", 14);
		lookbackDates.put("KR", 28);
		lookbackDates.put("CA", 28);
		lookbackDates.put("FR", 28);
		lookbackDates.put("GB", 28);
		lookbackDates.put("CN", 28);
	}

	public ConfigurationTJMCountries() {
		super.init(lookbackDates);
	}
}
