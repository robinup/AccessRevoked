package com.tapjoy.opt.vertica_score.target.app.etl;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;

/**
 * offerwall_actions ==> offerwall_actions_agg
 * 
 * @author lli
 * 
 */
@Deprecated
public class OfferPublisherAgg {
	public static String statementList[] = {
			"select drop_partition('offerwall_actions_agg',':startDate');",
			"insert into offerwall_actions_agg "
					+ "select q.offer_id, q.publisher_app_id, q.installs, r.returners " +
					"from " +
					"(" +
					" select offer_id, publisher_app_id, count(*) as installs " +
					"from analytics.actions " +
					"where " +
					"type in ('install', 'tjm_install', 'featured_install') " +
					"and day between date(now()) - 28 and date(now()) - 2 " +
					"group by 1, 2" +
					") q " +
					"left join " +
					"( " +
					"select offer_id, a.publisher_app_id, count(distinct(b.udid)) as returners " +
					"from analytics.actions a, analytics.connects_bi b " +
					"where a.advertiser_app_id = b.app_id " +
					"and a.type in ('install', 'tjm_install', 'featured_install') " +
					"and a.day between date(now()) - 28 and date(now()) - 2 " +
					"and b.day - a.day = 1 and a.udid = b.udid " +
					"group by 1, 2" +
					") r " +
					"on q.offer_id = r.offer_id " +
					"and q.publisher_app_id = r.publisher_app_id"
	};
	
	public static void execute(Connection conn, String startDate)
			throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":startDate", startDate);
		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}
}
