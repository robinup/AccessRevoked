package com.tapjoy.opt.conversion_matrix.config;

public class Configuration {
	
	public static final String ALGO_ID = "680";
	public static final String BACKUP_ALGO_ID = "324";
	public static final String IDKEY = "ConversionMatrix";
	
	// Strategies
	public static int FRONT_APPEND = 1;
	public static int WEIGHT_BOOST = 2;
	
	public static int RELOAD_DELAY = 60000;
	public static int MODEL_RELOAD_DELAY = 300; 
	
	public static int ACTIVE_STRATEGY = FRONT_APPEND;
	
	public static int HIST_THRES = 10; //added by LJ 2013-08-29 the max number of history records retrieved for one udid
	public static int OFFERWALL_CF_THRES = 100;
	
	public static long HBASE_TIMEOUT_THRES = 100;
	public static long HBASE_TRAFFIC_TIME = 30;  //this time, in milliseconds, force the main thread to sleep before attempting to retrieve HBase result
	
	public static String RT_TABLE_NAME = "conversion_history_1month";  //one real-time HBase table for now
	public static String[] AUX_TABLE_NAMES = {"conversion_history_1week_I", "conversion_history_1week_II"};
}
