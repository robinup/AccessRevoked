package com.tapjoy.opt.vertica_score.offerGenerator.app;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferFileWriter;
import com.tapjoy.opt.vertica_score.offerGenerator.OptOfferGenerator;
import com.tapjoy.opt.vertica_score.offerGenerator.app.AppOptOfferGenerator;

public class AppOptOfferGeneratorTest {
	private HashMap<RankedOfferKey, List<Row>> rankedOfferMap = new HashMap<RankedOfferKey, List<Row>>();

	@Test
	public void appOptOffer() throws SQLException, ClassNotFoundException,
			IOException, EmptyRankedScoreException, ParseException, java.text.ParseException {
		Connection conn = VerticaConn.getTestConnection();
		Map<String, Row> allOfferMap = OfferCache.getInstance().get(conn);

		OptOfferGenerator offerGenerator = new AppOptOfferGenerator(
				"7a0dfb05-2f64-4cbe-97bf-2d4cbee6ff20");

		// updating rankedOfferMap
		offerGenerator.updateMap(conn, rankedOfferMap);
		
		RankedOfferKey rankedOfferKey = offerGenerator.getRankedOfferKey("iOS", "itouch", "280");
		
		assertEquals(334, rankedOfferMap.get(rankedOfferKey).size());

		// output the ranking to file
		OptOfferFileWriter.outputFile(conn, rankedOfferMap, allOfferMap,
				new Date(), new HashSet<String>());
	}
}
