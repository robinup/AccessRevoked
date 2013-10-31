package com.tapjoy.opt.common;

public interface ColumnDef {
	public static String OS = "os";
	public static String OFFERWALL_RANK = "offerwall_rank";
	public static String OFFER_ID = "offer_id";

	public static String IMPRESSIONS = "impressions";
	public static String CLICKS = "clicks";
	public static String ACTIONS = "actions";

	public static String CVR = "cvr";

	public static String ACTIONS_SUM = "actions_sum";
	public static String IMPRESSIONS_SUM = "impressions_sum";
	public static String EXPECTED_ACTIONS_SUM = "expected_action_sum";
	public static String EXPECTED_ACTIONS_VARIANCE_SUM = "expected_action_variance_sum";

	public static String TOTAL_ACTIONS_SUM = "total_actions_sum";
	public static String TOTAL_EXPECTED_ACTIONS_SUM = "total_expected_action_sum";
	public static String TOTAL_EXPECTED_ACTIONS_VARIANCE_SUM = "total_expected_actions_variance_sum";

	public static String PREDITION_SCORE = "pred_score";
	public static String CONVERT_SCORE = "convert_score";
	public static String CONVERT_SCORE_WITH_VARIANCE = "convert_score_with_variance";
	public static String RANK_ADJUSTED_SCORE = "rank_adjusted_score";

	// The following are offer related columns
	public static String ID = "id";
	public static String OFFER_NAME = "offer_name";
	public static String ITEM_TYPE = "item_type";
	public static String BID = "bid";
	public static String PAYMENT = "payment";
	public static String COUNTRIES = "countries";
	public static String SHOW_RATE = "show_rate";
	public static String SHOW_RATE_NEW = "show_rate_new";
	public static String RANK_BOOST = "rank_boost";
	public static String PUBLISHER_APP_WHITELIST = "publisher_app_whitelist";
	public static String APPROVED_SOURCES = "approved_sources";
	public static String PARTNER_BALANCE = "partner_balance";
	public static String DEVICE_TYPES = "device_types";
	public static String SELF_PROMOTE_ONLY = "self_promote_only";
	public static String USE_QUALITY_LIST = "use_quality_list";
	
	public static String BALANCE = "balance";  //added  by LJ for updated vertica server choice Oct 15
	


	public static String IS_AUDITION = "is_audition";
	public static String IS_HARD_PROMOTION = "is_hard_promotion";
	public static String RANK_INDEX = "rank_index";
	
	// optimization related columns
	public static String ALGORITHM_ID = "ALGO_ID";
	public static String PLATFORM = "PLATFORM";
	public static String DEVICE = "DEVICE";
	public static String COUNTRY = "COUNTRY";
	public static String CURRENCY_ID = "CURRENCY_ID";
	
	// optimization target
	public static String PUBLISHER_APP_ID = "publisher_app_id";
	public static String INSTALLS = "installs";
	public static String INSTALLS_SUM = "installs_sum";
	public static String RETURNERS_SUM = "returners_sum";
	public static String TOTAL_INSTALLS_SUM = "total_installs_sum";
	public static String TOTAL_RETURNERS_SUM = "total_returners_sum";
	public static String RETURNERS = "returners";
	public static String RERUN_RATE = "rerun_rate";
	public static String ESTIMATE_REACH_RATE = "estimate_reach_rate";
	public static String AGG_REACH_RATE = "agg_reach_rate";
	public static String AGG_RERUN_RATE = "agg_rerun_rate";

	// show rate calculation related
	public static String DAILY_BUDGET = "daily_budget";
	public static String DAILY_CONVERSION = "daily_conversion";
	public static String HOURLY_CONVERSION = "hourly_conversion";
	public static String LAST_CONVERSION_TIME = "last_conversion_time";
	
	// Discount Audition related columns
	public static String RREDICTIION_RANK = "prediction_rank";
	public static String RREDICTIION_RANK_DISCOUNT = "prediction_rank_star";
	public static String PREDICTION_SCORE_DISCOUNT = "prediction_score_star";
	public static String BID_DISCOUNT = "bid_discount";
	
	//// For Filters
	// for GeoIp filter
	public static String COUNTRIES_BLACKLIST = "countries_blacklist";   // from app_metadatas table
	public static String REGIONS = "regions";        // from offers table 
	public static String DMA_CODES = "dma_codes";    // from offers table
	public static String CITIES = "cities";          // from offers table
	
	public static String[] offer_raw = {ID, OFFER_NAME, ITEM_TYPE, BID, PAYMENT, COUNTRIES, SHOW_RATE,
		RANK_BOOST,PUBLISHER_APP_WHITELIST, APPROVED_SOURCES,
		DEVICE_TYPES, SELF_PROMOTE_ONLY, USE_QUALITY_LIST, REGIONS,
		DMA_CODES, CITIES};  //added by Justin for featured ads
}
