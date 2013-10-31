package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.util.DateUtil;

/**
 * 
 * @author lli
 * 
 */
public class CloneActionView2Test {
	public static String statementList[] = {
			"select drop_partition('opt_test.actions',':startDate');",
			"insert into opt_test.actions "
					+ "select udid, publisher_user_id, currency_id, country, viewed_at, "
					+ "time, publisher_app_id, advertiser_app_id, offer_id, path, source, "
					+ "type, exp, currency_reward, advertiser_amount, publisher_amount, "
					+ "displayer_amount, tapjoy_amount, day, etl_day, mac_address, open_udid, "
					+ "click_key, library_version, offerwall_rank, device_type, device_type_canon, "
					+ "platform, store_name, cached_offer_list_id, cached_offer_list_type, auditioning, etl_time "
					+ "from optimization.actions " + "where day=':startDate' ",
			"select drop_partition('opt_test.offerwall_views',':startDate')",
			"insert into opt_test.offerwall_views "
					+ "select udid, publisher_user_id, currency_id, country, app_version, "
					+ "time, app_id, offer_id, device_type, geoip_country, path, user_agent, "
					+ "library_version, ip_address, source, exp, offerwall_start_index, "
					+ "offerwall_max_items, offerwall_rank, offerwall_rank_score, day, etl_day, "
					+ "device_type_canon, platform, mac_address, open_udid, store_name, "
					+ "cached_offer_list_id, cached_offer_list_type, auditioning "
					+ "from optimization.offerwall_views "
					+ "where day=':startDate' " };

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
