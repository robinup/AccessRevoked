package com.tapjoy.opt.model_assigner;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class LogisticRegressionAssigner implements ModelAssigner {
	
	private Logger logger = Logger.getLogger(LogisticRegressionAssigner.class);
	
	public LogisticRegressionAssigner()
	{
		logger.info("Logistic Regression assigner created.");
	}

	@Override
	public String assign(HashMap<String, String> specs) {
		// TODO Auto-generated method stub
		
		logger.info("Assigner called.");
		
		String algo = specs.get("algorithm");
		
		if (algo.equals("999") || algo.equals("880")) {
			specs.put("algorithm", "880");
			return "880";
		}
		
		specs.put("algorithm", "324");
		return "324";
	}

}
