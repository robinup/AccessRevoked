package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Ignore;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.util.DateUtil;

/**
 * 
 * @author lli
 * 
 */
public class Opt2Test {
	public static String statementList[] = {
			"select drop_partition('offerwall_actions',':startDate');",
			"insert into offerwall_actions "
					+ "select udid, currency_id, country, viewed_at, time, offer_id, source, type, "
					+ "currency_reward, advertiser_amount, publisher_amount, "
					+ "displayer_amount, tapjoy_amount, offerwall_rank, 1.0, day,device_type_canon, "
					+ "platform " + "from optimization.actions "
					+ "where day=':startDate'; ",
			"select drop_partition('offerwall_views_agg',':startDate');",
			"insert into offerwall_views_agg "
					+ "select currency_id, country, offer_id, device_type_canon, platform, "
					+ "source, offerwall_rank, day, count(day) "
					+ "from optimization.offerwall_views " + "where day=':startDate' "
					+ "group by 1,2,3,4,5,6,7,8;" };

	@Ignore
	public static void execute(Connection conn, String startDate)
			throws SQLException {
		String[] replacedSQL = ListSQLExecutor.listSQLReplace(statementList,
				":startDate", startDate);
		ListSQLExecutor.listSqlExecute(conn, replacedSQL);
	}
	
	@Ignore
	public static void execute(Connection conn, int daysLookBack)
			throws SQLException {

		for (int lookBack = -daysLookBack; lookBack <= 0; lookBack++) {
			String lookBackDate = DateUtil.getDiffDateString(lookBack);
			execute(conn, lookBackDate);
		}
	}
	
}
