package com.tapjoy.opt.segmentation.config;

public class Configuration {
    // The identifier for this OfferList Engine
	public static final String ALGO_ID = "681";
	public static final String BACKUP_ALGO_ID = "324";
    public static final String IDKEY = "Segmentation";

    // The dir for S3 offer list files
    public static String S3LocalCopyDir = "/tmp/LeiTest1";

    // For situation when this offerlist engine is used as the static offerlist
    // engine by another offerlist engine
    public static final String DEFAULT_ALGO = "681";

    public static String RT_TABLE_NAME = "user-big-table"; // one real-time
                                                           // HBase table for
                                                           // now
}
