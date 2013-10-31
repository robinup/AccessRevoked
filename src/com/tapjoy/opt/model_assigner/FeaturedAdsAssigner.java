package com.tapjoy.opt.model_assigner;

import java.util.HashMap;

public class FeaturedAdsAssigner implements ModelAssigner {
	
	public String assign(HashMap<String, String> specs)
	{
    	specs.remove("ip_addr");
    	specs.put("algorithm", "210");
    	return "210";
	}
}
