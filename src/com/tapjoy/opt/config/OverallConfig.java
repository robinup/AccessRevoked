package com.tapjoy.opt.config;

import java.util.HashSet;
import java.util.Set;


public class OverallConfig {
	// using testing database or not, always false unless testing against
	// production database
	public static boolean isProduction = false;

	// whether uploading to production s3 or testing s3
	public static boolean productionS3 = false;
	
	// Production OR Dev ?
	public static void setProduction() {
		isProduction = true;
	}

	// Production OR Dev ?
	public static void setProductionS3() {
		productionS3 = true;
	}
	
	public static String MODEL_OVERRIDE = "";   //by LJ --- algo id in url request can be overriden if this setting is not empty 
	public static String FEATURED_MODEL_OVERRIDE = ""; 

	public static final int MAX_RELOAD_INTERVAL = 86400;
	
	public static final long LOG_CYCLE_COUNT = 1000000;
	
	public static int RDBS_CONN_CYCLE = 600;  //moved from GlobalDataManager 10-16

	// The memcached server used for cross-server synchronization
	public static final String MEMCACHE_ADDR = "localhost";
	public static final int MEMCACHE_PORT = 11211;

	public static final class UserRequestSpec{
		public static final String COUNTRY = "country";
	}

	// Where to get the GeoIp data file
	public static final String GEOIP_DATA_FILE = "/usr/local/share/GeoIP/GeoIPCity.dat";
	
	public static int OPTSOA_PORT = 8888;
	public static long OPTSOA_TIMEOUT_THRES = 150;  //in milliseconds
	
	public static String OPTSOA_HOME_DIR = OverallConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("TapjoyOptService")[0]+"../bin/";
	
	public static String DYN_CONF_DIR = "conf";
	
	public static String MODEL_REG_FILE = "modelconfig.dat";
	public static String ASSIGNER_REG_FILE = "assignerconfig.dat";
	public static String PUBOPT_APPLIST_FILE = "pub_app_id_50new";
	public static String MONITOR_PATH_DIR = "/home/tjopt/GIT_opt/tapjoyoptimization/opt_server/watchdog_logs/";
	
	// The actual request will be used for health check ("http://soa:8888/health")
	public static String healthCheck = "/?command=offerwall&algorithm=324&source=offerwall&platform=iOS&device_type=iphone&udid=12345&offer_count=10&ip_addr=38.104.224.62";	
	
	// For MySQL connections
	public static final class MySQLConf{
		public static final String host = "replica.tapjoy.com";
		public static final int port = 3306;
		public static final String user = "tapjoy";
		public static final String password = "xatrugAxu6";
		public static final String database = "tapjoy_db";
	}

	////// For S3 Connetion ///////
	public static class S3Cred {
		public static String aws_access_key_id = "AKIAJ46RRH6TIM5BJ44A";
		public static String aws_secret_access_key = "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";
	}

	public static class S3 {
		public static class Production {
			public static String S3_BUCKET_NAME = "tj-optimization";
			public static String S3_BUCKET_KEY = "";
		}

		public static class Test {
			public static String S3_BUCKET_NAME = "tj-optimization-test";
			public static String S3_BUCKET_KEY = "unittest" + "/";
		}
	}

	public static String getS3BucketName() {
		String s3bucketName;
		if ((isProduction == false) || (productionS3 == false)) {
			s3bucketName = S3.Test.S3_BUCKET_NAME;
		} else {
			s3bucketName = S3.Production.S3_BUCKET_NAME;
		}

		return s3bucketName;
	}

	public static String getS3BucketKey() {
		String s3bucketKey;
		if ((isProduction == false) || (productionS3 == false)) {
			s3bucketKey = S3.Test.S3_BUCKET_KEY;
		} else {
			s3bucketKey = S3.Production.S3_BUCKET_KEY;
		}

		return s3bucketKey;
	}
    ////// End of S3 Connetion ///////
	
	
	// General pqrameters regarding SQL queries
 	public static class SQL {
		public static int MIN_ALL_OFFER_SIZE = 500;
		public static int SLEEP_IN_SECONDS = 10;
	}

 	// General parameters regarding Object Cache behavior
 	public static class Cache {
		// Make this long so it won't auto refresh
		public static int AllOfferTTL = 100 * 60;
 	}

	public static class OFFER {
		public static String DEEP_LINK_OFFER = "DeeplinkOffer";
	}

	// Info for Vertica Servers
	
	public static class DatabaseServer {
		public static class main {
			public static String name = "main";
			public static String hostIp = "10.94.101.41"; // internal ip
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
	
		public static class backup {
			public static String name = "backup";
			public static String hostIp = "50.19.61.148";
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
	
		public static class localmain {
			public static String name = "localmain";
			public static String hostIp = "127.0.0.1"; // internal ip
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
	
		public static class localbackup {
			public static String name = "localbackup";
			public static String hostIp = "127.0.0.1";
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
		
		public static class verticaprod {
			public static String name = "verticaprod";
			public static String hostIp = "vertica.tapjoy.com";
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
		
		public static class verticaprod2 {
			public static String name = "verticaprod2";
			public static String hostIp = "bi.tapjoy.com";
			public static String databaseName = "tapjoy";
			public static String userName = "dbadmin";
			public static String passWord = "TJ4ever!";
			public static String port = "5433";
		}
	}
	
	// database server, default to backup server
	public static String databaseServer =  DatabaseServer.verticaprod.name;  //DatabaseServer.verticaprod.name;



	public static void setDatabaseServer(String databaseServer) {
		OverallConfig.databaseServer = databaseServer;
	}

	// Generating extra debug information for the offer_ids listed here
	public static class OfferDebug {
		public static Set<String> tackingOffers = new HashSet<String>();
	
		static {
			//tackingOffers.add("b73306bb-1b00-44c3-8f14-9b7bfe278e7e");
		}
	}

	// Sending alert emails
	public static class Email {
		public static String HOST = "smtp.gmail.com";
		public static String USER = "opt_root@tapjoy.com";
		public static String PASSWORD = "<aymT2HZ";
		public static String PORT = "465";
		public static String FROM = "opt_root@tapjoy.com";
		public static String TO = "opt@tapjoy.com";
		public static String STARTTLS = "true";
		public static String AUTH = "true";
		public static String DEBUG = "true";
		public static String SOCKET_FACTORY = "javax.net.ssl.SSLSocketFactory";
		public static String SUBJECT = "Optimization Status Update";
		public static String TEXT = "This is a test message from my java application. Just ignore it";
	}


}
