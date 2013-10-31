package com.tapjoy.opt.vertica_score.offerGenerator.tjmcountry;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferFileWriter;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.country.CountryOptOfferGeneratorTest;
import com.tapjoy.opt.vertica_score.offerGenerator.tjmcountry.TJMCountryOptOfferGenerator;

public class TJMCountryOptOfferGeneratorTest {
	private static Logger logger = Logger
			.getLogger(CountryOptOfferGeneratorTest.class);
	private HashMap<RankedOfferKey, List<Row>> rankedOfferMap = new HashMap<RankedOfferKey, List<Row>>();

	@Ignore
	public void globalOptOffer() throws SQLException, ClassNotFoundException,
			IOException, EmptyRankedScoreException, ParseException, java.text.ParseException {
		Connection conn = VerticaConn.getTestConnection();
		Map<String, Row> allOfferMap = OfferCache.getInstance().get(conn);

		String country = "JP";
		logger.info("generating optimization list for country::" + country);

		// do global optimization for generic offerwall
		if (Configuration.Ranking.OPT_TJM && Configuration.Ranking.OPT_COUNTRY) {
			OptOfferGenerator optOfferGenerator = new TJMCountryOptOfferGenerator(country);

			// updating rankedOfferMap
			optOfferGenerator.updateMap(conn, rankedOfferMap);

			// output the ranking to file
			OptOfferFileWriter.outputFile(conn, rankedOfferMap,
					allOfferMap, new Date(), new HashSet<String>());
		}
	}
}
