package com.tapjoy.opt.vertica_score.etl.sql.countryapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationApp;
import com.tapjoy.opt.vertica_score.config.ConfigurationCountries;

public class CountryAppOfferAgg {
	private static Logger logger = Logger.getLogger(CountryAppOfferAgg.class);

	public static String initStatementList[] = {
			"truncate table gen_country_app_views_norm_agg",
			"truncate table gen_country_app_actions_norm_agg",
			"truncate table gen_country_app_norm_ctr_cvr",
			"truncate table gen_country_app_views_offer_agg",
			"truncate table gen_country_app_actions_offer_agg",
			"truncate table gen_country_app_offer_ctr_cvr" };

	public static String statementList[] = {
			"insert into gen_country_app_views_norm_agg "
					+ "select os, country, currency_id, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and country in (:countryList) and currency_id in (:currencyList) "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2,3,4",

			"insert into gen_country_app_actions_norm_agg "
					+ "select os, country, currency_id, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and country in (:countryList) and currency_id in (:currencyList) "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2,3,4",

			"insert into gen_country_app_views_offer_agg "
					+ "select offer_id, os, country, currency_id, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and country in (:countryList) and currency_id in (:currencyList) "
					+ "group by 1,2,3,4,5",

			"insert into gen_country_app_actions_offer_agg "
					+ "select offer_id, os, country, currency_id, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' and country in (:countryList) and currency_id in (:currencyList) "
					+ "group by 1,2,3,4,5" };

	public static String cvrStatementList[] = {
			"insert into gen_country_app_norm_ctr_cvr "
					+ "select a.os, a.country, a.currency_id, a.offerwall_rank, sum(impressions), 0, sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from gen_country_app_views_norm_agg a "
					+ "join gen_country_app_actions_norm_agg c "
					+ "on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.currency_id=c.currency_id and a.country=c.country) "
					+ "group by 1,2,3,4 " + "having sum(actions)>=6",

			"insert into gen_country_app_offer_ctr_cvr "
					+ "select a.os, a.country, a.currency_id, a.offer_id, a.offerwall_rank, sum(impressions), 0,  "
					+ "sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from gen_country_app_views_offer_agg a "
					+ "left join gen_country_app_actions_offer_agg c "
					+ "on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.offer_id=c.offer_id and a.currency_id=c.currency_id and a.country=c.country) "
					+ "group by 1,2,3,4,5 "
					+ "having sum(impressions)>=2000 or sum(actions)>=3" };

	public static void execute(Connection conn, String startDate,
			String endDate, Integer rankThreshold, String appList,
			String countryList) throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":OfferWallRankThreshold", rankThreshold.toString());
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":startDate",
				startDate);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":endDate",
				endDate);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL,
				":currencyList", appList);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL,
				":countryList", countryList);

		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}

	public static void execute(Connection conn) throws SQLException {
		logger.debug("ETL for country application");

		ListSQLExecutor.listSqlExecute(conn, initStatementList);

		String allAppIdStirng = ConfigurationApp.myConfiguration
				.getAllIdString();
		Map<Integer, Set<String>> countriesLookbackMap = ConfigurationCountries.myConfiguration
				.getLookBackIds();
		for (Integer lookback : countriesLookbackMap.keySet()) {
			String countryIdListStr = ConfigurationCountries.myConfiguration
					.getIdString(lookback);
			execute(conn, DateUtil.getDiffDateString(-lookback + 1),
					DateUtil.getTodayDateString(),
					Configuration.Ranking.MIN_RANK_POSITION, allAppIdStirng,
					countryIdListStr);
		}

		ListSQLExecutor.listSqlExecute(conn, cvrStatementList);
	}
}
