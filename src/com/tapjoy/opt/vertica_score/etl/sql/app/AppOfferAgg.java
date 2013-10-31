package com.tapjoy.opt.vertica_score.etl.sql.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationApp;

public class AppOfferAgg {
	private static Logger logger = Logger.getLogger(AppOfferAgg.class);

	public static String initStatementList[] = {
			"truncate table gen_app_views_norm_agg",
			"truncate table gen_app_actions_norm_agg",
			"truncate table gen_app_norm_ctr_cvr",
			"truncate table gen_app_views_offer_agg",
			"truncate table gen_app_actions_offer_agg",
			"truncate table gen_app_offer_ctr_cvr" };

	public static String statementList[] = {
			"insert into gen_app_views_norm_agg "
					+ "select os, currency_id, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and currency_id in (:currencyList) "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2,3",

			"insert into gen_app_actions_norm_agg "
					+ "select os, currency_id, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and currency_id in (:currencyList) "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2,3",

			"insert into gen_app_views_offer_agg "
					+ "select offer_id, os, currency_id, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and currency_id in (:currencyList) "
					+ "group by 1,2,3,4",

			"insert into gen_app_actions_offer_agg "
					+ "select offer_id, os, currency_id, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and currency_id in (:currencyList) "
					+ "group by 1,2,3,4" };

	public static String cvrStatementList[] = {
			"insert into gen_app_norm_ctr_cvr "
					+ "select a.os, a.currency_id, a.offerwall_rank, sum(impressions), 0, "
					+ "sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from gen_app_views_norm_agg a "
					+ "join gen_app_actions_norm_agg c "
					+ "on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.currency_id=c.currency_id) "
					+ "group by 1,2,3 " + "having sum(actions)>=6",

			"insert into gen_app_offer_ctr_cvr "
					+ "select a.os, a.currency_id, a.offer_id, a.offerwall_rank, sum(impressions), 0,  "
					+ "sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float)  "
					+ "from gen_app_views_offer_agg a "
					+ "left join gen_app_actions_offer_agg c "
					+ " on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.offer_id=c.offer_id and a.currency_id=c.currency_id) "
					+ "group by 1,2,3,4 "
					+ "having sum(impressions)>=2000 or sum(actions)>=3" };

	/**
	 * Init by purging older data
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void executeInit(Connection conn) throws SQLException {
		ListSQLExecutor.listSqlExecute(conn, initStatementList);
	}

	public static void executeCVR(Connection conn) throws SQLException {
		ListSQLExecutor.listSqlExecute(conn, cvrStatementList);
	}

	public static void execute(Connection conn, String startDate,
			String endDate, Integer rankThreshold, String currencyList)
			throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":OfferWallRankThreshold", rankThreshold.toString());
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":startDate",
				startDate);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":endDate",
				endDate);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL,
				":currencyList", currencyList);

		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}

	public static void execute(Connection conn) throws SQLException {
		logger.debug("ETL for app");
		
		executeInit(conn);

	    // go through the lbw and find out different lb for app's and group them based on lb length 
		Map<Integer, Set<String>> lookbackMap = ConfigurationApp.myConfiguration
				.getLookBackIds();
		for (Integer lookback : lookbackMap.keySet()) {
			String appIdListStr = ConfigurationApp.myConfiguration
					.getIdString(lookback);
			logger.info("Currency list : " + appIdListStr + " filtering");
			
			// for testing database, using 2 month looking back
			if (OverallConfig.isProduction == false) {
				lookback = Configuration.Etl.TEST_ETL_LOOKBACK;
			} 

			execute(conn, DateUtil.getDiffDateString(-lookback + 1),
					DateUtil.getTodayDateString(),
					Configuration.Ranking.MIN_RANK_POSITION, appIdListStr);
		}

		executeCVR(conn);
	}
}
