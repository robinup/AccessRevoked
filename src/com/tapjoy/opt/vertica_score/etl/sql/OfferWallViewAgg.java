package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;

/**
 * From offerwall_views ==> offerwall_views_agg Parameter :startDate
 * 
 * @author lli
 * 
 */
public class OfferWallViewAgg {
	public static String statementList[] = {
			"select drop_partition('offerwall_views_agg',':startDate');",
			"insert into offerwall_views_agg "
					+ "select currency_id, country, offer_id, device_type_canon, platform, "
					+ "source, offerwall_rank, day, count(day) "
					+ "from offerwall_views " + "where day=':startDate' "
					+ "group by 1,2,3,4,5,6,7,8;" };

	public static void execute(Connection conn, String startDate)
			throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":startDate", startDate);
		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}
}
