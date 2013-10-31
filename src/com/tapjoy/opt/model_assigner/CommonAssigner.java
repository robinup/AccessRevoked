package com.tapjoy.opt.model_assigner;

import java.util.HashMap;

public class CommonAssigner implements ModelAssigner {
	
	public String assign(HashMap<String, String> specs)
	{
		String algo = specs.get("algorithm");
		if(algo == null)
		{
			specs.put("algorithm", "324");
			return "324";
		}
	    if(algo.equals("330"))
	    {
	    	specs.put("control_test", "control");
	    	specs.put("exp_label", "topk");
	    	return "324";
	    }
	    else if(algo.equals("350"))
	    {
	    	specs.put("algorithm", "680");
	    	specs.put("control_test", "test");
	    	specs.put("exp_label", "topk");
	    	return "680";
	    }
	    else if(algo.equals("999"))
	    	return "324";
	    else
	    	return algo;
	}
}
