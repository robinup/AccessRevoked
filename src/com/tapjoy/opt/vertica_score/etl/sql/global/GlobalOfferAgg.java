package com.tapjoy.opt.vertica_score.etl.sql.global;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationCountries;

public class GlobalOfferAgg {
	private static Logger logger = Logger.getLogger(GlobalOfferAgg.class);

	public static String statementList[] = {
			"truncate table gen_global_views_norm_agg;",
			"insert into gen_global_views_norm_agg "
					+ "select os, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2",

			"truncate table gen_global_actions_norm_agg;",
			"insert into gen_global_actions_norm_agg "
					+ "select os, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' "
					+ "and offerwall_rank<=:OfferWallRankThreshold "
					+ "group by 1,2",

			"truncate table gen_global_norm_ctr_cvr; ",
			"insert into gen_global_norm_ctr_cvr "
					+ "select a.os, a.offerwall_rank, sum(impressions), 0, sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from gen_global_views_norm_agg a "
					+ "join gen_global_actions_norm_agg c on (a.os=c.os and a.offerwall_rank = c.offerwall_rank) "
					+ "group by 1,2 " + "having sum(actions)>=6",

			"truncate table gen_global_views_offer_agg; ",
			"insert into gen_global_views_offer_agg "
					+ "select offer_id, os, offerwall_rank, sum(impressions) "
					+ "from offerwall_views_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' "
					+ "group by 1,2,3",

			"truncate table gen_global_actions_offer_agg; ",
			"insert into gen_global_actions_offer_agg "
					+ "select offer_id, os, offerwall_rank, sum(actions) "
					+ "from offerwall_actions_agg "
					+ "where day between ':startDate' and ':endDate' and source='offerwall' "
					+ "group by 1,2,3",

			"truncate table gen_global_offer_ctr_cvr; ",
			"insert into gen_global_offer_ctr_cvr "
					+ "select a.os,a.offer_id, a.offerwall_rank, sum(impressions), 0, sum(actions), 0, cast(sum(actions) as float)/cast(sum(impressions) as float) "
					+ "from gen_global_views_offer_agg a "
					+ "left join gen_global_actions_offer_agg c on (a.os=c.os and a.offerwall_rank = c.offerwall_rank and a.offer_id=c.offer_id) "
					+ "group by 1,2,3 "
					+ "having sum(impressions)>=2000 or sum(actions)>=3" };

	public static void execute(Connection conn, String startDate,
			String endDate, Integer rankThreshold) throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":OfferWallRankThreshold", rankThreshold.toString());
		replacedSQL = ListSQLExecutor.listSQLReplace(replacedSQL, ":startDate",
				startDate);
		String[] finalSQL = ListSQLExecutor.listSQLReplace(replacedSQL,
				":endDate", endDate);

		ListSQLExecutor.listSqlExecute(conn, finalSQL);
	}

	public static void execute(Connection conn) throws SQLException {
		logger.debug("ETL for global");

		// for testing database, using 2 month looking back
		if (OverallConfig.isProduction == false) {
			GlobalOfferAgg.execute(conn, DateUtil.getDiffDateString(-Configuration.Etl.TEST_ETL_LOOKBACK + 1),
					DateUtil.getTodayDateString(),
					Configuration.Ranking.MIN_RANK_POSITION);
		} else {
			GlobalOfferAgg
					.execute(
							conn,
							DateUtil.getDiffDateString(-ConfigurationCountries.GLOBAL_LOOKBACK_DATE + 1),
							DateUtil.getTodayDateString(),
							Configuration.Ranking.MIN_RANK_POSITION);
		}
	}
}
