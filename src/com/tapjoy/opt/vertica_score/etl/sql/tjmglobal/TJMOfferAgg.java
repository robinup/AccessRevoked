package com.tapjoy.opt.vertica_score.etl.sql.tjmglobal;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationTJMCountries;

public class TJMOfferAgg {
	private static Logger logger = Logger.getLogger(TJMOfferAgg.class);

	public static String statementList[] = {
			"truncate table tjm_global_views_norm_agg",

			"insert into tjm_global_views_norm_agg "
					+ "select os, offerwall_rank, sum(impressions)  "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='tj_games' "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2",

			"truncate table tjm_global_actions_norm_agg",

			"insert into tjm_global_actions_norm_agg "
					+ "select os, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='tj_games' "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2",

			"truncate table tjm_global_norm_ctr_cvr",

			"insert into tjm_global_norm_ctr_cvr "
					+ "select a.os, a.offerwall_rank, sum(impressions), 0, sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from tjm_global_views_norm_agg a "
					+ "join tjm_global_actions_norm_agg c "
					+ "on (a.os=c.os and a.offerwall_rank = c.offerwall_rank) "
					+ "group by 1,2 " + "having sum(actions)>=6",

			"truncate table tjm_global_views_offer_agg",

			"insert into tjm_global_views_offer_agg "
					+ "select offer_id, os, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='tj_games' "
					+ "group by 1,2,3",

			"truncate table tjm_global_actions_offer_agg",

			"insert into tjm_global_actions_offer_agg "
					+ "select offer_id, os, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='tj_games' "
					+ "group by 1,2,3",

			"truncate table tjm_global_offer_ctr_cvr",
			"insert into tjm_global_offer_ctr_cvr "
					+ "select a.os,a.offer_id, a.offerwall_rank, sum(impressions), 0, sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from tjm_global_views_offer_agg a "
					+ "left join tjm_global_actions_offer_agg c "
					+ "on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.offer_id=c.offer_id) "
					+ "group by 1,2,3 "
					+ "having sum(impressions)>=1000 or sum(actions)>=3 " };

	public static void execute(Connection conn, String startDate,
			String endDate, Integer rankThreshold) throws SQLException {

		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":OfferWallRankThreshold", rankThreshold.toString());
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":startDate",
				startDate);
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":endDate",
				endDate);

		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}

	public static void execute(Connection conn) throws SQLException {
		logger.debug("ETL for TJM global");

		// for testing database, using 2 month looking back
		if (OverallConfig.isProduction == false) {
			execute(conn, DateUtil.getDiffDateString(-Configuration.Etl.TEST_ETL_LOOKBACK + 1),
					DateUtil.getTodayDateString(),
					Configuration.Ranking.MIN_RANK_POSITION);
		} else {
			execute(conn,
					DateUtil.getDiffDateString(-ConfigurationTJMCountries.GLOBAL_LOOKBACK_DATE + 1),
					DateUtil.getTodayDateString(),
					Configuration.Ranking.MIN_RANK_POSITION);
		}
	}
}
