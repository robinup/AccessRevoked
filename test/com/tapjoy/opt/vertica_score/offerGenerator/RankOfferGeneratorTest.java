package com.tapjoy.opt.vertica_score.offerGenerator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.util.StringUtil;

public class RankOfferGeneratorTest {
	private static Logger logger = Logger
			.getLogger(RankOfferGeneratorTest.class);
	private static Map<String, Row> offerMap;

	@BeforeClass
	public static void setUp() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();
		offerMap = OfferCache.getInstance().get(conn);
		conn.close();
	}

	@Test
	public void testMatch() {
		assertTrue("itouch".matches("^i.*"));
		assertFalse("android".matches("^i.*"));
	}

	@Test
	public void checkIphone() {
		Row row = offerMap.get("405b6e88-2c34-4f8b-a2b7-c699d8ca823e");

		String os = "iOS";
		String deviceTypes = row.getColumn(ColumnDef.DEVICE_TYPES);
		List<String> devices = OfferRowUtil.getDevicesFromOffer(row, os);
		logger.debug(" device types==>" + deviceTypes + "devices" + devices);

		Set<String> deviceSet = StringUtil.listToSet(devices);
		assertTrue(deviceSet.contains("iphone"));
		assertFalse(deviceSet.contains("itouch"));
		assertTrue(deviceSet.contains("ipad"));
		assertFalse(deviceSet.contains("android"));
		assertFalse(deviceSet.contains("windows"));
	}

	@Test
	public void checkAndoid() {
		Row row = offerMap.get("405b6e88-2c34-4f8b-a2b7-c699d8ca823e");

		String os = "Android";
		String deviceTypes = row.getColumn(ColumnDef.DEVICE_TYPES);
		List<String> devices = OfferRowUtil.getDevicesFromOffer(row, os);
		logger.debug(" device types==>" + deviceTypes + "devices" + devices);

		Set<String> deviceSet = StringUtil.listToSet(devices);
		assertFalse(deviceSet.contains("iphone"));
		assertFalse(deviceSet.contains("itouch"));
		assertFalse(deviceSet.contains("ipad"));
		assertTrue(deviceSet.contains("android"));
		assertFalse(deviceSet.contains("windows"));
	}

	@Test
	public void checkWindows() {
		Row row = offerMap.get("405b6e88-2c34-4f8b-a2b7-c699d8ca823e");

		String os = "Windows";
		String deviceTypes = row.getColumn(ColumnDef.DEVICE_TYPES);
		List<String> devices = OfferRowUtil.getDevicesFromOffer(row, os);
		logger.debug(" device types==>" + deviceTypes + "devices" + devices);

		Set<String> deviceSet = StringUtil.listToSet(devices);
		assertFalse(deviceSet.contains("iphone"));
		assertFalse(deviceSet.contains("itouch"));
		assertFalse(deviceSet.contains("ipad"));
		assertFalse(deviceSet.contains("android"));
		assertFalse(deviceSet.contains("windows"));
	}

	/*
	@Test
	public void testOfferGenerator() throws SQLException, ClassNotFoundException {
		RankOfferGenerator generator = new RankOfferGenerator(
				Configuration.Ranking.CURR_ALGORITHM_ID,
				Configuration.Platform.OFFERWALL, null, null);

		Map<Row, Row> scoredOfferMap = new HashMap<Row, Row>();

		RowFactory keyFactory = new RowFactory(OfferGroupBy.groupByColumns);
		Row keyRow = keyFactory.newRow();
		keyRow.setColumn(ColumnDef.OS, "iOS");
		keyRow.setColumn(ColumnDef.OFFER_ID,
				"0012a9e9-d468-4087-aaad-91f65a92ec23");

		RowFactory offerFactory = new RowFactory(Offer.columnList);
		Row offerRow = offerFactory.newRow();
		offerRow.setColumn(ColumnDef.ID, "0012a9e9-d468-4087-aaad-91f65a92ec23");
		offerRow.setColumn(ColumnDef.DEVICE_TYPES,
				"[\"iphone\",\"itouch\",\"ipad\",\"android\",\"windows\"]");
		offerRow.setColumn(ColumnDef.COUNTRIES, "");

		scoredOfferMap.put(keyRow, offerRow);

		Connection conn = VerticaConn.getTestConnection();
		Map<RankedOfferKey, List<Row>> optimizedOfferMap = generator
				.transform(conn, scoredOfferMap);
		conn.close();
		assertEquals(0, optimizedOfferMap.values().size());
	}

	@Test
	public void testOfferGeneratorOver80() throws SQLException, ClassNotFoundException {
		RankOfferGenerator generator = new RankOfferGenerator(
				Configuration.Ranking.CURR_ALGORITHM_ID,
				Configuration.Platform.OFFERWALL, null, null);

		Map<Row, Row> scoredOfferMap = new HashMap<Row, Row>();

		for (int i = 0; i < 100; i++) {
			RowFactory keyFactory = new RowFactory(OfferGroupBy.groupByColumns);
			Row keyRow = keyFactory.newRow();
			keyRow.setColumn(ColumnDef.OS, "iOS");
			keyRow.setColumn(ColumnDef.OFFER_ID,
					"A"+i);

			RowFactory offerFactory = new RowFactory(Offer.columnList);
			Row offerRow = offerFactory.newRow();
			offerRow.setColumn(ColumnDef.ID,
					"A"+i);
			offerRow.setColumn(ColumnDef.DEVICE_TYPES,
					"[\"iphone\",\"itouch\",\"ipad\",\"android\",\"windows\"]");
			offerRow.setColumn(ColumnDef.COUNTRIES, "");

			scoredOfferMap.put(keyRow, offerRow);
		}

		Connection conn = VerticaConn.getTestConnection();
		Map<RankedOfferKey, List<Row>> optimizedOfferMap = generator
				.transform(conn, scoredOfferMap);
		assertEquals(3, optimizedOfferMap.values().size());
		
		for(RankedOfferKey offerKey:optimizedOfferMap.keySet()) {
			List<Row> offerList = optimizedOfferMap.get(offerKey);
			assertEquals(100, offerList.size());			
		}
		
		conn.close();
	}
	*/
}
