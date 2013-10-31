package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;

/**
 * 
 * @author lli
 * 
 */
public class OfferWallAction {
	public static String statementList[] = {
			"select drop_partition('offerwall_actions',':startDate');",
			"insert into offerwall_actions "
					+ "select udid, currency_id, country, viewed_at, time, offer_id, source, type, "
					+ "currency_reward, advertiser_amount, publisher_amount, "
					+ "displayer_amount, tapjoy_amount, offerwall_rank, 1.0, day,device_type_canon, "
					+ "platform " + "from actions "
					+ "where day=':startDate'; " };

	public static void execute(Connection conn, String startDate) throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":startDate", startDate);
		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}
}
