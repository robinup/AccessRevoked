package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;

/**
 * offerwall_actions ==> offerwall_actions_agg
 * 
 * @author lli
 * 
 */
public class OfferWallActionAgg {
	public static String statementList[] = {
			"select drop_partition('offerwall_actions_agg',':startDate');",
			"insert into offerwall_actions_agg "
					+ "select currency_id, country, offer_id, device_type, os, source, "
					+ "offerwall_rank, day, count(day) "
					+ "from offerwall_actions " + "where day=':startDate' "
					+ "group by 1,2,3,4,5,6,7,8;" };

	public static void execute(Connection conn, String startDate)
			throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":startDate", startDate);
		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}
}
