package com.tapjoy.opt.logistic_regression.config;

import java.util.ArrayList;

public class Configuration {
	public static final String ALGO_ID = "880";
	public static final String BACKUP_ALGO_ID = "324";
	public static final String IDKEY = "LogisticRegression";

	public static final String MODEL_DATA_PATH = "/ebs/data/cvm_prod/data/logit_deploy/latest";
	
	public static final int OFFERWALL_RANK_END = 10;
	
	public static final ArrayList<Double> RANK_WEIGHTS = new ArrayList<Double>(10);
	
	public static final ArrayList<Double> RANK_WEIGHTS_CUMSUM = new ArrayList<Double>(10);
	
	static {
		RANK_WEIGHTS.add(1.00);
		RANK_WEIGHTS.add(0.39);
		RANK_WEIGHTS.add(0.24);
		RANK_WEIGHTS.add(0.16);
		RANK_WEIGHTS.add(0.12);
		RANK_WEIGHTS.add(0.09);
		RANK_WEIGHTS.add(0.07);
		RANK_WEIGHTS.add(0.06);
		RANK_WEIGHTS.add(0.05);
		RANK_WEIGHTS.add(0.04);
		RANK_WEIGHTS.add(0.04);
		RANK_WEIGHTS.add(0.04);
		RANK_WEIGHTS.add(0.03);
		RANK_WEIGHTS.add(0.03);
		RANK_WEIGHTS.add(0.03);
		RANK_WEIGHTS.add(0.02);
		RANK_WEIGHTS.add(0.02);
		RANK_WEIGHTS.add(0.02);
		RANK_WEIGHTS.add(0.02);
		RANK_WEIGHTS.add(0.02);
		RANK_WEIGHTS.add(0.01);
		RANK_WEIGHTS.add(0.01);
		RANK_WEIGHTS.add(0.01);
		RANK_WEIGHTS.add(0.01);
		RANK_WEIGHTS.add(0.01);
		
		RANK_WEIGHTS_CUMSUM.add(1.00);
		RANK_WEIGHTS_CUMSUM.add(1.39);
		RANK_WEIGHTS_CUMSUM.add(1.63);
		RANK_WEIGHTS_CUMSUM.add(1.79);
		RANK_WEIGHTS_CUMSUM.add(1.91);
		RANK_WEIGHTS_CUMSUM.add(2.00);
		RANK_WEIGHTS_CUMSUM.add(2.07);
		RANK_WEIGHTS_CUMSUM.add(2.13);
		RANK_WEIGHTS_CUMSUM.add(2.18);
		RANK_WEIGHTS_CUMSUM.add(2.22);
		RANK_WEIGHTS_CUMSUM.add(2.26);
		RANK_WEIGHTS_CUMSUM.add(2.30);
		RANK_WEIGHTS_CUMSUM.add(2.33);
		RANK_WEIGHTS_CUMSUM.add(2.36);
		RANK_WEIGHTS_CUMSUM.add(2.39);
		RANK_WEIGHTS_CUMSUM.add(2.41);
		RANK_WEIGHTS_CUMSUM.add(2.43);
		RANK_WEIGHTS_CUMSUM.add(2.45);
		RANK_WEIGHTS_CUMSUM.add(2.47);
		RANK_WEIGHTS_CUMSUM.add(2.49);
		RANK_WEIGHTS_CUMSUM.add(2.50);
		RANK_WEIGHTS_CUMSUM.add(2.51);
		RANK_WEIGHTS_CUMSUM.add(2.52);
		RANK_WEIGHTS_CUMSUM.add(2.53);
		RANK_WEIGHTS_CUMSUM.add(2.54);
	}
}
