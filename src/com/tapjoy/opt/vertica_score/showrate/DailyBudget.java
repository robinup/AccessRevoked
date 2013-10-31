package com.tapjoy.opt.vertica_score.showrate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class DailyBudget implements ColumnDef {
	public static String DAILY_BUDGET_SQL = "select a.offer_id as offer_id, offer_name, item_type, bid, show_rate, "
			+ "partner_balance, daily_budget, count(offer_id) as daily_conversion, max(time) as last_conversion_time "
			+ "from actions_accounting a right join offers b on (a.offer_id=b.id) "
			+ "where a.day=':DAY' " + "group by 1,2,3,4,5,6,7";

	public static String[] columnList = { OFFER_ID, OFFER_NAME, ITEM_TYPE, BID,
			SHOW_RATE, PARTNER_BALANCE, DAILY_BUDGET, DAILY_CONVERSION,
			LAST_CONVERSION_TIME };
	public static String[] keyColumns = { OFFER_ID };

	/**
	 * returning the map for OFFER_ID ==> OFFER_ID, OFFER_ID, OFFER_NAME,
	 * ITEM_TYPE, BID, SHOW_RATE, PARTNER_BALANCE, DAILY_BUDGET,
	 * DAILY_CONVERSION, LAST_CONVERSION_TIME
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<Row, Row> getDailyBudget(Connection conn) throws SQLException {
		Date targetDate = DateUtil.getDiffDateBySeconds(Configuration.ShowRate.ETL_DELAY);
		String adjustedSQL = DAILY_BUDGET_SQL.replaceFirst(":DAY", DateUtil.getTodayDateString(targetDate));
		Map<Row, Row> map = new GenericQuery().runQueryToKeyMap(conn,
				adjustedSQL, columnList, null, keyColumns, false);

		return map;
	}
}
