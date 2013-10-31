package com.tapjoy.opt.vertica_score.offerGenerator.country;

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
import com.tapjoy.opt.vertica_score.config.ConfigurationCountries;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryOfferAgg;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferFileWriter;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.country.CountryOptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.global.GlobalOptOfferGenerator;

public class CountryOptOfferGeneratorTest {
	private static Logger logger = Logger
			.getLogger(CountryOptOfferGeneratorTest.class);
	private HashMap<RankedOfferKey, List<Row>> rankedOfferMap = new HashMap<RankedOfferKey, List<Row>>();

	public void updateOfferAgg() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		CountryOfferAgg.executeInit(conn);

		CountryOfferAgg.execute(conn, "2012-11-06", "2019-11-06",
				Configuration.Ranking.MIN_RANK_POSITION, "'US'");

		CountryOfferAgg.execute(conn, "2012-11-06", "2019-11-06",
				Configuration.Ranking.MIN_RANK_POSITION, "'CN'");

		CountryOfferAgg.executeCVR(conn);

		conn.close();
	}

	@Test
	public void CNOptOffer() throws SQLException,
			ClassNotFoundException, IOException, EmptyRankedScoreException,
			ParseException, java.text.ParseException {

		updateOfferAgg();

		Connection conn = VerticaConn.getTestConnection();

		Map<String, Row> allOfferMap = OfferCache.getInstance().get(conn);

		GlobalOptOfferGenerator globalOptOfferGenerator = new GlobalOptOfferGenerator();

		// updating rankedOfferMap
		globalOptOfferGenerator.updateMap(conn, rankedOfferMap);

		OptOfferGenerator optOfferGenerator = new CountryOptOfferGenerator("CN");

		// updating rankedOfferMap
		optOfferGenerator.updateMap(conn, rankedOfferMap);

		// output the ranking to file
		OptOfferFileWriter.outputFile(conn, rankedOfferMap, allOfferMap,
				new Date(), new HashSet<String>());
	}
}
