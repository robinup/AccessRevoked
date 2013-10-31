package com.tapjoy.opt.vertica_score.showrate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.util.DateUtil;

public class HourlyConversion implements ColumnDef {
	public static String OFFERS_HOURLY_SQL = "select a.offer_id as offer_id, count(offer_id) as hourly_conversion "
			+ "from actions_accounting a right join offers b on (a.offer_id=b.id) "
			+ "where a.day=':DAY' and hour(time)=:HOUR " + "group by 1";

	public static String[] columnList = { OFFER_ID, HOURLY_CONVERSION };
	public static String[] keyColumns = { OFFER_ID };

	/**
	 * returning the map for OFFER_ID ==> OFFER_ID, OFFER_ID, OFFER_NAME, ITEM_TYPE, BID,
			SHOW_RATE, PARTNER_BALANCE, DAILY_BUDGET, DAILY_CONVERSION,
			LAST_CONVERSION_TIME 
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<Row, Row> getHourlyConversion(Connection conn, int prevHourDelay) throws SQLException {
		Date targetDate = DateUtil.getDiffDateBySeconds(prevHourDelay);
		String adjustedSQL = OFFERS_HOURLY_SQL.replaceFirst(":HOUR", DateUtil.getHH(targetDate));
		adjustedSQL = adjustedSQL.replaceFirst(":DAY", DateUtil.getTodayDateString(targetDate));
		

		
		Map<Row, Row> map = new GenericQuery().runQueryToKeyMap(conn,
				adjustedSQL, columnList, null, keyColumns, false);

		return map;
	}
}
