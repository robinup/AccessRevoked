package com.tapjoy.opt.vertica_score.config;


import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.DateUtil;

import org.apache.log4j.Logger;

public class Configuration {
	public static final String ALGO_ID = "101";
	
	public static final String IDKEY = "VerticaScore";
	
	// whether or not uploading to S3, leaving it as true. the testing results
	// would be loaded into S3 test folder
	public static boolean LOAD = true;
	
	// Output the offerlists as file
	public static boolean OUTPUT_AS_FILE = true;

	// Output the offerlists as cached item
	public static boolean OUTPUT_TO_CACHE = true;
	
	// The default number of Offers per serving
	public static int CACHE_SERVE_SIZE = 10;
	
	// whether enable app targeting
	public static boolean enableAppTargeting = false;

	// running ETL or not
	public static boolean enableETL = true;

	// should we copy from production database to test database?
	public static boolean FROM_OPT_TO_TEST = false; // whether copying today's
													// data from optimization
	
	// Run the bid discount test for 280
	public static boolean RUN_BID_DISCOUNT = false;
													// database to test database
	public static Logger operationsLogger = Logger
			.getLogger(LOGGER_DEFINITION.OPERATIONS);

	// default to algorithm 280 only
	public static String[] algorithms = { "280", "101"};
	
	// For situation when this offerlist engine is used as the static offerlist engine by another offerlist engine
	public static final String DEFAULT_ALGO = "101";

	public static void setAlgorithms(String[] algorithms) {
		Configuration.algorithms = algorithms;
	}

	public static class Workspace {
		public static String WorkSpace = "workspace";
		public static String TestWorkSpace = "workspace";
	}

	public static class LOGGER_DEFINITION {
		public static String OPERATIONS = "operations";
	}

	public static class SQL {
		public static int MIN_OPT_OFFER_SIZE = 50;
	}

	public static class Cache {
		public static int NewOfferTTL = 5 * 60;
		public static int S3AuditionTTL = 5 * 60;
	}

	public static class Audition {
		// # source for audition prediction files
		public static String AUD_BUCKET = "tj-optimization-audition";
		public static String AUD_BUCKET_TEST = "tj-optimization-test";

		public static String AUD_DIR = "";
		public static String AUD_DIR_TEST = "audition-unittest/";

		// whether or not loading from S3
		public static boolean LOAD_S3 = false;

		public static boolean AUDITION_MERGE = false;

		// how to converting prediction score to rank score
		public static double PRED_TO_SCORE = 0.5;

		// how to converting prediction score to rank score, this is the base
		// score
		public static double PRED_TO_SCORE_BASE = 5;
	}

	public static class Etl {
		// public static boolean RUN_IN_GLOBAL = true;
		public static boolean RUN_ETL = true;

		public static String ETL_BUCKET = "tj-vertica";
		public static String ETL_SUBFOLDER = "etl_main";
		public static int TEST_ETL_LOOKBACK = 90;
	}

	public static class RankBoost {
		// if rank boost ends with the number, it will bypass all pub app
		// whitelist
		public static String RANKBOOST_CODE = "1";

		// ignore all rank boost that's higher than the threshold
		public static Integer RANKBOOST_THRESHOLD = 1001;   //suggested use original int for dynamic configuration, but ok--by LJ
	}

	public static class OS {
		public static String IOS = "iOS";
		public static String ANDROID = "Android";
		public static String [] activeOs = {IOS, ANDROID};
	}

	public static class Platform {
		public static int OFFERWALL = 0;
		public static int TJM = 1;
		public static int [] activePlatforms = {OFFERWALL, TJM};
	}

	public static class Targeting {
		public static class App {
			public static double REACH = 0.25; // 50% reach
			public static int MIN_INSTALLS = 0;

			public static String SHOW_WEIGHT = "1.00";
			public static String DEFAULT_SHOW_WEIGHT = "0.05";

			public static boolean enableAppTarget = true;
			public static int ETL_LOOKBACK = 2;

			public static String SUB_PATH = "/app/";
			public static String DASHBOARD_FILE_NAME = "dashboard.json";
			public static String PLATFORM_FILE_NAME = "platform.json";

			public static int REFRESH_APP_COUNT = 6; // every 6 hours roughly,
														// refreshing
														// application targeting
														// matrix
		}
	}
	
	public static class ShowRate {
		public static int ETL_DELAY = -1800;
	}

	public static class Ranking {
		public static String ALGORITHM_101 = "101";

		public static String ALGORITHM_280 = "280";

		public static int REFRESH_OFFER_COUNT = 6;

		// public static int CURR_ALGORITHM_ID = 280;

		public static boolean ENABLE_SHOW_RATE = false;
		
		public static int MIN_RANK_POSITION = 25;

		// making sure the offer size at least 80
		public static int MIN_OFFER_SIZE = 80;

		public static int MAX_RANK_INDEX = 1500;

		// minimum show rate in order for the new offer to be shown
		public static Double MIN_SHOW_RATE = 0.1;

		public static int HARD_INSERT_POS = 15;

		// initial maximum least score
		public static double DEFAULT_LEAST_SCORE = 10000.0;
		
		// default sigma distance
		public static double SIGMA_DISTANCE = 1.0;

		// RANK_ADJUST_RATIO
		public static double RANK_ADJUST_RATIO = 0.25;

		// control what offers to move to the very tail of list, tail_thresh
		// applies to the rank score
		public static double TAIL_THRESH = 10.0;

		public static int LIST_SIZE = 100;

		public static boolean OPT_GLOBAL = true;

		public static boolean OPT_COUNTRY = false;

		public static boolean OPT_APP = false;

		// generic offer wall
		public static boolean OPT_GOW = true;

		public static boolean OPT_TJM = true;

		public static String getOptOutputDir() {
			String directory;
			if (OverallConfig.isProduction == false) {
				directory = Configuration.Workspace.TestWorkSpace + "/"
						+ ":DAY/" + "opt/:HH:mm";
			} else {
				directory = Configuration.Workspace.WorkSpace + "/" + ":DAY/"
						+ "opt/:HH:mm";
			}

			return directory;
		}

		public static String getTargetOutputDir() {
			String directory;
			if (OverallConfig.isProduction == false) {
				directory = Configuration.Workspace.TestWorkSpace + "/"
						+ ":DAY/" + "target/:HH:mm";
			} else {
				directory = Configuration.Workspace.WorkSpace + "/" + ":DAY/"
						+ "target/:HH:mm";
			}

			return directory;
		}

		public static String getAuditionOutputDir() {
			String directory;
			if (OverallConfig.isProduction == false) {
				directory = Configuration.Workspace.TestWorkSpace + "/"
						+ ":DAY/" + "audition/:HH:mm";
			} else {
				directory = Configuration.Workspace.WorkSpace + "/" + ":DAY/"
						+ "audition/:HH:mm";
			}

			return directory;
		}
	}

	public static String getCurrentWorkSpace() {
		String directory;
		if (OverallConfig.isProduction == false) {
			directory = Configuration.Workspace.TestWorkSpace + "/"
					+ DateUtil.getTodayDateString() + "/";
		} else {
			directory = Configuration.Workspace.WorkSpace + "/"
					+ DateUtil.getTodayDateString() + "/";
		}

		return directory;
	}

	public static String getAuditionDir() {
		return getCurrentWorkSpace() + "audition";
	}

	public static String getAuditionDirPrefix() {
		String dirPrefix = null;
		if (OverallConfig.isProduction == false) {
			dirPrefix = Configuration.Audition.AUD_DIR_TEST;
		} else {
			dirPrefix = Configuration.Audition.AUD_DIR;
		}
		return dirPrefix;
	}

	public static String getAuditionS3Bucket() {
		String S3bucket = null;
		if (OverallConfig.isProduction == false) {
			S3bucket = Configuration.Audition.AUD_BUCKET_TEST;
		} else {
			S3bucket = Configuration.Audition.AUD_BUCKET;
		}
		return S3bucket;
	}
}
