package com.tapjoy.opt.linear_regression.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is the configuration file for the VerticaScore Engine!!!
 * @author lding
 *
 */

public class Configuration {
	// The identifier for this OfferList Engine
	public static final String ALGO_ID = "324";
	public static final String IDKEY = "LinearRegression";

	// The dir for S3 offer list files
	public static String S3LocalCopyDir = "/tmp/LeiTest1";

	public static String[] algorithms = {"324", "330", "350", "999"};
	
	// For situation when this offerlist engine is used as the static offerlist engine by another offerlist engine
	public static final String DEFAULT_ALGO = "324";
	
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
	
	public static class Ranking {
		public static boolean OPT_GLOBAL = true;

		public static boolean OPT_COUNTRY = true;
		
		public static boolean OPT_APP = false;
	}
	
	private static HashSet<String> valideCountries = new HashSet<String>();

	static {
		valideCountries.add("350US");
		valideCountries.add("350AT");
		valideCountries.add("350AU");
		valideCountries.add("350CA");
		valideCountries.add("350CH");
		valideCountries.add("350CN");
		valideCountries.add("350DE");
		valideCountries.add("350DK");
		valideCountries.add("350ES");
		valideCountries.add("350FI");
		valideCountries.add("350FR");
		valideCountries.add("350FX");
		valideCountries.add("350GB");
		valideCountries.add("350GR");
		valideCountries.add("350HK");
		valideCountries.add("350HU");
		valideCountries.add("350IL");
		valideCountries.add("350IN");
		valideCountries.add("350KR");
		valideCountries.add("350MO");
		valideCountries.add("350MY");
		valideCountries.add("350NL");
		valideCountries.add("350NO");
		valideCountries.add("350NZ");
		valideCountries.add("350PL");
		valideCountries.add("350PT");
		valideCountries.add("350RU");
		valideCountries.add("350SE");
		valideCountries.add("350SG");
		valideCountries.add("350TW");
		valideCountries.add("350UK");
		valideCountries.add("999US");
	}
	
	public static boolean isValidCountry(String algo, String country){
		if (algo == null || country == null || ! valideCountries.contains(algo + country)) {
			return false;
		}
		
		return true;
	}
	
	
	private static HashSet<String> valideCurrencies = new HashSet<String>();

	static {
		//valideCurrencies.add("");
	}
	
	public static boolean isValidCurrency(String algo, String currency){
		if (algo == null || currency == null || ! valideCurrencies.contains(algo + currency)) {
			return false;
		}
		
		return true;
	}

}
