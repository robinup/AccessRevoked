package com.tapjoy.opt.model_assigner;

import java.util.HashMap;
import java.util.Random;

public class InitialTestOct21Assigner implements ModelAssigner {
	
	public String assign(HashMap<String, String> specs)
	{
		 //"platform" is a must-have field in url 
		Random generator = new Random();
		 if(AssignerManager.applist.containsKey(specs.get("app_id")) && specs.get("platform").equals(AssignerManager.applist.get(specs.get("app_id"))) && generator.nextFloat() <= 0.40)
		 {
			 
			 if(generator.nextFloat() >= 0.50)
			 {
				 specs.put("algorithm", "401");
				 specs.put("control_test", "1");
			     specs.put("exp_label", "pubopt");
				 return "401";
			 }
			 else
			 {
				 specs.put("algorithm", "324");
				 specs.put("control_test", "0");
			     specs.put("exp_label", "pubopt");
				 return "324";
			 }
		 }
		 else
		 { 
			 float rf = generator.nextFloat(); 
			 if(rf < 0.40)
			 {
			    	specs.put("algorithm", "324");
			    	specs.put("control_test", "0");
			    	specs.put("exp_label", "topk");
			    	return "324";
			 }
			 else if(rf >= 0.40 && rf < 0.80)
			 {
		    	specs.put("algorithm", "680");
		    	specs.put("control_test", "1");
		    	specs.put("exp_label", "topk");
		    	return "680";
		     }
			 else
			 {
				 	String algo = specs.get("algorithm");
					if(algo == null)
					{
						specs.put("algorithm", "324");
						return "324";
					}
				    if(algo.equals("330") || algo.equals("350") || algo.equals("999"))
				    {
				    	return "324";
				    }
				    else
				    	return algo;
			 }
		   
		 }
	}

}
