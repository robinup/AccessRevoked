package com.tapjoy.opt.featured_logit.config;

import java.util.HashSet;

public class Configuration {
	// The identifier for this OfferList Engine
	public static final String ALGO_ID = "210";
	public static final String IDKEY = "FeaturedLogisticRegression";

	// The dir for S3 offer list files
	public static final String S3LocalCopyDir = "/tmp/justin-test/";
	
	public static final String HBASE_FEEDBACK = "negative-feedback";
	
	public static int MAX_OFFERS = 100;
	
	// For situation when this offerlist engine is used as the static offerlist engine by another offerlist engine
	//TODO
	public static final String DEFAULT_ALGO = "210";

}
