package com.tapjoy.opt.vertica_score.objectCache;

public class OfferTest {

	/*
	 * 
	 * system("mysql -h$DB_HOST -u$DB_USER -p$DB_PWD tapjoy_db -e\"select a.id,
	 * a.name,a.item_id,a.item_type,
	 * a.bid,a.price,a.payment,a.rewarded,a.reseller_id, a.pay_per_click,
	 * a.countries,a.device_types, a.user_enabled, a.tapjoy_enabled,
	 * a.self_promote_only, a.featured, a.daily_budget, a.overall_budget,
	 * a.approved_sources, b.balance, a.show_rate, a.rank_boost,
	 * a.audition_factor, a.publisher_app_whitelist from offers a join partners
	 * b on (a.partner_id=b.id) where a.user_enabled=true and
	 * a.tapjoy_enabled=true and a.rewarded=true and a.featured=false AND
	 * a.item_type != 'RatingOffer' AND a.item_type != 'ReengagementOffer' AND
	 * a.payment > 0 AND a.tracking_for_id IS NULL and b.balance
	 * >0;\" >$DATA_DIR/offers.txt");
	 */
}
