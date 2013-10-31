package com.tapjoy.opt.vertica_score.config;

import java.util.HashSet;
import java.util.Set;

public class ConfigurationTargetApp {
	public static Set<String> offerSet = new HashSet<String>();
	public static ConfigurationTargetApp myConfiguration = new ConfigurationTargetApp();

	static {
		offerSet.add("ce3c2387-e126-4e7e-98e4-031ea05142c0");
		offerSet.add("e861df43-4cdb-404a-ad70-102b618e50fe");
		offerSet.add("e25ef7b1-6dab-4981-be28-7a1a5f9de609");
	}
}
