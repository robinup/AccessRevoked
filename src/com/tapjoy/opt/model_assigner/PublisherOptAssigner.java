package com.tapjoy.opt.model_assigner;

import java.util.HashMap;
import java.util.Random;
import com.tapjoy.opt.model_assigner.AssignerManager;

public class PublisherOptAssigner implements ModelAssigner {

	public String assign(HashMap<String, String> specs)
	{
		
	 String algo = specs.get("algorithm");
	 if(algo == null)
	 {
		 algo = "324";
		 specs.put("algorithm", "324");
	 }
	 
	 if(!specs.containsKey("app_id"))
		 return algo;
	 
	 //"platform" is a must-have field in url 
	 if(AssignerManager.applist.containsKey(specs.get("app_id")) && specs.get("platform").equals(AssignerManager.applist.get(specs.get("app_id"))))
	 {
		 Random generator = new Random();
		 if(generator.nextFloat() >= 0.50)
		 {
			 specs.put("algorithm", "401");
			 specs.put("control_test", "test");
		     specs.put("exp_label", "pubopt");
			 return "401";
		 }
		 else
		 {
			 specs.put("algorithm", "324");
			 specs.put("control_test", "control");
		     specs.put("exp_label", "pubopt");
			 return "324";
		 }
	 }
	 else
		 return algo;
	}
}