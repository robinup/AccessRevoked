package com.tapjoy.opt.vertica_score.offerGenerator.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppOfferNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGeneratorBase;

/**
 * Ranking offers based algorithm
 * 
 * @author lli
 * 
 */
public class AppOptOfferGenerator extends OptOfferGeneratorBase implements
		ColumnDef, OptOfferGenerator {
	private String app;

	@Override
	public String getSegment() {
		return ConfigurationSegment.OPT_GOW_APP;
	}

	@Override
	public RankedOfferKey getRankedOfferKey(String currentOS, String device, String algorithm) {
		RankedOfferKey rankedOfferKey = new RankedOfferKey();

		rankedOfferKey.algorithm = algorithm;
		rankedOfferKey.platform = Configuration.Platform.OFFERWALL;
		rankedOfferKey.os = currentOS;
		rankedOfferKey.currency_id = app;
		rankedOfferKey.country = null;
		rankedOfferKey.device = device;
		rankedOfferKey.segment = getSegment();

		return rankedOfferKey;
	}

	public AppOptOfferGenerator(String app) {
		this.app = app;
	}

	// Offer Aggregation
	public void executeOfferAgg(Connection conn) throws SQLException {
		AppOfferAgg.execute(conn);
	}

	// OS, CURRENCY_ID, OFFER_ID, OFFERWALL_RANK ==> ROW
	public Map<Row, Row> getOfferNormCVRMap(Connection conn)
			throws SQLException {
		return new AppOfferNormCVR().getNormCVRMap(conn);
	}
}
