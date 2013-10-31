package com.tapjoy.opt.vertica_score.etl.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.ListSQLExecutor;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.sql.OfferBase;

public class MysqlOffer implements ColumnDef {
	private static String ALL_OFFERS = "select a.id, a.name as offer_name, a.item_id, "
			+ "a.item_type, a.bid, a.price, a.payment, a.rewarded,a.reseller_id, a.pay_per_click, "
			+ "a.countries, a.device_types, a.user_enabled, a.tapjoy_enabled, "
			+ "a.self_promote_only, a.featured, a.daily_budget, a.overall_budget, "
			+ "a.approved_sources, b.balance as partner_balance, a.show_rate, a.rank_boost, "
			+ "a.audition_factor, a.publisher_app_whitelist "
			+ "from offers a join partners b on (a.partner_id=b.id) "
			+ "where a.user_enabled=true and a.tapjoy_enabled=true "
			+ "and a.rewarded=true and a.featured=false "
			+ "AND a.item_type != 'RatingOffer' "
			+ "AND a.item_type != 'ReengagementOffer' "
			+ "AND a.payment > 0 "
			+ "AND a.tracking_for_id IS NULL " + "AND b.balance >0";

	private static String SINGLE_OFFER = "select a.id, a.name as offer_name, a.item_id, "
			+ "a.item_type, a.bid, a.price, a.payment, a.rewarded,a.reseller_id, a.pay_per_click, "
			+ "a.countries, a.device_types, a.user_enabled, a.tapjoy_enabled, "
			+ "a.self_promote_only, a.featured, a.daily_budget, a.overall_budget, "
			+ "a.approved_sources, b.balance as partner_balance, a.show_rate, a.rank_boost, "
			+ "a.audition_factor, a.publisher_app_whitelist "
			+ "from offers a join partners b on (a.partner_id=b.id) "
			+ "where a.user_enabled=true and a.tapjoy_enabled=true "
			+ "and a.rewarded=true and a.featured=false "
			+ "AND a.item_type != 'RatingOffer' "
			+ "AND a.item_type != 'ReengagementOffer' "
			+ "AND a.payment > 0 "
			+ "AND a.tracking_for_id IS NULL " + "AND b.balance >0 AND a.id = ':OFFER_ID'";
	
	public static Map<String, Row> getAllOfferMap(Connection conn)
			throws SQLException {

		return OfferBase.getAllOfferMap(conn, ALL_OFFERS, false);
	}

	public static Map<String, Row> getOfferById(Connection conn, String offerId)
			throws SQLException {
		
		String replacedSQL = ListSQLExecutor.listSQLReplace(SINGLE_OFFER,
				":OFFER_ID", offerId);
	
		return OfferBase.getAllOfferMap(conn, replacedSQL, false);
	}
}
