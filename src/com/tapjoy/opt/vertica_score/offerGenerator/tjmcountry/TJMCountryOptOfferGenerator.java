package com.tapjoy.opt.vertica_score.offerGenerator.tjmcountry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryOfferNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGeneratorBase;

/**
 * Ranking offers based algorithm
 * 
 * @author lli
 * 
 */
public class TJMCountryOptOfferGenerator extends OptOfferGeneratorBase
		implements ColumnDef, OptOfferGenerator {
	private String country;

	public TJMCountryOptOfferGenerator(String country) {
		this.country = country;
	}

	@Override
	public String getSegment() {
		return ConfigurationSegment.OPT_TJM_COUNTRY;
	}

	@Override
	public RankedOfferKey getRankedOfferKey(String currentOS, String device, String algorithm) {
		RankedOfferKey rankedOfferKey = new RankedOfferKey();

		rankedOfferKey.algorithm = algorithm;
		rankedOfferKey.platform = Configuration.Platform.TJM;
		rankedOfferKey.os = currentOS;
		rankedOfferKey.currency_id = null;
		rankedOfferKey.country = country;
		rankedOfferKey.device = device;
		rankedOfferKey.segment = getSegment();

		return rankedOfferKey;
	}

	// Offer Aggregation
	public void executeOfferAgg(Connection conn) throws SQLException {
		TJMCountryOfferAgg.execute(conn);
	}

	// CVR, OS_OFFERID_RANK ==> ROW
	public Map<Row, Row> getOfferNormCVRMap(Connection conn)
			throws SQLException {
		return new TJMCountryOfferNormCVR().getNormCVRMap(conn);
	}
}
