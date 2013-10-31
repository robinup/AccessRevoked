package com.tapjoy.opt.publisher_opt.config;

/**
 * This is the configuration file for the VerticaScore Engine!!!
 * 
 * @author lding
 * 
 */

public class Configuration {
    // The identifier for this OfferList Engine
    public static final String ALGO_ID = "401";
    public static final String IDKEY = "PublisherOptimization";
    public static final String BACKUP_ALGO_ID = "324";
    // The dir for S3 offer list files
    public static String S3LocalCopyDir = "/tmp/pub_opt";

    public static String[] algorithms = { "324", "401" };

    // For situation when this offerlist engine is used as the static offerlist
    // engine by another offerlist engine
    public static final String DEFAULT_ALGO = "401";

    public static class OS {
        public static String IOS = "iOS";
        public static String ANDROID = "Android";
        public static String[] activeOs = { IOS, ANDROID };
    }

    public static class Platform {
        public static int OFFERWALL = 0;
        public static int TJM = 1;
        public static int[] activePlatforms = { OFFERWALL, TJM };
    }

}
