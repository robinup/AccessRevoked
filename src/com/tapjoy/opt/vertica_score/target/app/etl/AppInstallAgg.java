package com.tapjoy.opt.vertica_score.target.app.etl;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.util.DateUtil;

/**
 * offerwall_actions ==> offerwall_actions_agg
 * 
 * @author lli
 * 
 */
public class AppInstallAgg {
	public static String statementList[] = {
			"select drop_partition('app_install', ':startDate');",
			"insert into app_install "
					+ "select day, offer_id, publisher_app_id, count(*) as installs "
					+ "from actions "
					+ "where type in ('install', 'tjm_install', 'featured_install') "
					+ "and day = ':startDate' " + "group by 1, 2, 3" };

	public static void execute(Connection conn, int daysLookBack)
			throws SQLException {

		for (int lookBack = -daysLookBack; lookBack <= 0; lookBack++) {
			String lookBackDate = DateUtil.getDiffDateString(lookBack);

			String[] replacedSQL = ListSQLExecutor.listSQLReplace(
					statementList, ":startDate", lookBackDate);
			ListSQLExecutor.listSqlExecute(conn, replacedSQL);
		}
	}
}
