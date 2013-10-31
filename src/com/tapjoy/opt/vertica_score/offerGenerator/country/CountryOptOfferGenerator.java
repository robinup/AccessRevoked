package com.tapjoy.opt.vertica_score.offerGenerator.country;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferAgg;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferNormCVR;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGeneratorBase;

/**
 * Ranking offers based algorithm
 * 
 * @author lli
 * 
 */
public class CountryOptOfferGenerator extends OptOfferGeneratorBase implements
		ColumnDef, OptOfferGenerator {
	private String country;

	public CountryOptOfferGenerator(String country) {
		this.country = country;
	}

	@Override
	public String getSegment() {
		return ConfigurationSegment.OPT_GOW_COUNTRY;
	}

	@Override
	public RankedOfferKey getRankedOfferKey(String currentOS, String device, String algorithm) {
		RankedOfferKey rankedOfferKey = new RankedOfferKey();

		rankedOfferKey.algorithm = algorithm;
		rankedOfferKey.platform = Configuration.Platform.OFFERWALL;
		rankedOfferKey.os = currentOS;
		rankedOfferKey.currency_id = null;
		rankedOfferKey.country = country;
		rankedOfferKey.device = device;
		rankedOfferKey.segment = getSegment();

		return rankedOfferKey;
	}

	// Offer Aggregation
	public void executeOfferAgg(Connection conn) throws SQLException {
		CountryOfferAgg.execute(conn);
	}

	// CVR, OS_OFFERID_RANK ==> ROW
	public Map<Row, Row> getOfferNormCVRMap(Connection conn)
			throws SQLException {
		return new CountryOfferNormCVR().getNormCVRMap(conn);
	}
}
