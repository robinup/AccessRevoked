package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.objectCache.NewOfferCache;
import com.tapjoy.opt.vertica_score.offerGenerator.NewOfferGenerator;

public class NewOfferGeneratorTest {
	private static Logger logger = Logger
			.getLogger(NewOfferGeneratorTest.class);

	@Test
	public void getNewOffer() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		Map<String, Row> newOfferMap = NewOfferCache.getInstance().get(conn,
				ConfigurationSegment.OPT_GOW_GLOBAL);

		int i = 0;
		for (String offerId : newOfferMap.keySet()) {
			Row row = newOfferMap.get(offerId);
			logger.debug(row);

			if (i++ > 10) {
				break;
			}
		}

		Set<String> idSet = new HashSet<String>();
		for (String offerId : newOfferMap.keySet()) {
			idSet.add(offerId);
		}

		assertTrue(idSet.contains("9d3aa7be-5ec9-4eba-ab53-f73a1d975483"));
		assertEquals(1366, newOfferMap.size());
		conn.close();
	}

	@Test
	public void getSortedOffer() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		List<Row> sortedRow = NewOfferGenerator.getSortedNewOffers(conn,
				Configuration.OS.IOS, Configuration.Platform.OFFERWALL,
				ConfigurationSegment.OPT_GOW_GLOBAL);

		int i = 0;
		for (Row row : sortedRow) {
			logger.debug(row.getColumn(ColumnDef.ID) + ", "
					+ row.getColumn(ColumnDef.PREDITION_SCORE));

			if (i == 0) {
				assertEquals("d63e5e20-3fb1-4692-b646-928143f4c087",
						row.getColumn(ColumnDef.ID));
				assertEquals("92772.596",
						row.getColumn(ColumnDef.PREDITION_SCORE));
			}

			if (i++ > 10) {
				break;
			}
		}

		assertTrue(sortedRow.size() > 200);
		conn.close();
	}
}
