package com.tapjoy.opt.vertica_score.target.app;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.target.app.OfferPublisherRerun;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OfferPublisherRerunTest implements ColumnDef {
	private static Map<Row, Row> offerPublisherMap = new HashMap<Row, Row>();

	@Before
	public void setUp() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		RowFactory rowFactory = new RowFactory(OfferPublisherRerun.columnList,
				OfferPublisherRerun.derivedColumnList);
		RowFactory keyrowFactory = new RowFactory(
				OfferPublisherRerun.keyColumns);

		Row row = rowFactory.newRow();
		Row keyrow = keyrowFactory.newRow();

		keyrow.setColumn(OFFER_ID, "OFFER_ID_A");
		keyrow.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_A");

		row.setColumn(OFFER_ID, "OFFER_ID_A");
		row.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_A");

		row.setColumn(INSTALLS, "1000");
		row.setColumn(RETURNERS, "100");

		offerPublisherMap.put(keyrow, row);

		row = rowFactory.newRow();
		keyrow = keyrowFactory.newRow();

		keyrow.setColumn(OFFER_ID, "OFFER_ID_A");
		keyrow.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_B");

		row.setColumn(OFFER_ID, "OFFER_ID_A");
		row.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_B");

		row.setColumn(INSTALLS, "2000");
		row.setColumn(RETURNERS, "400");

		offerPublisherMap.put(keyrow, row);

		row = rowFactory.newRow();
		keyrow = keyrowFactory.newRow();

		keyrow.setColumn(OFFER_ID, "OFFER_ID_B");
		keyrow.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_B");

		row.setColumn(OFFER_ID, "OFFER_ID_B");
		row.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_B");

		row.setColumn(INSTALLS, "2000");
		row.setColumn(RETURNERS, "100");

		offerPublisherMap.put(keyrow, row);

		conn.close();
	}

	@Test
	public void getRerun() {
		OfferPublisherRerun.GroupByResult groupByResult = new OfferPublisherRerun()
				.publiserhGroupBy(offerPublisherMap);

		Row globalRerunResult = groupByResult.globalResult;
		assertEquals("5000", globalRerunResult.getColumn(TOTAL_INSTALLS_SUM));
		assertEquals("600", globalRerunResult.getColumn(TOTAL_RETURNERS_SUM));

		Map<Row, Row> rerunMap = groupByResult.groupByResult;
		assertEquals(2, rerunMap.size());

		RowFactory publisherGroupByKeyrowFactory = new RowFactory(
				OfferPublisherRerun.publisherGroupByColumns);

		Row keyrow = publisherGroupByKeyrowFactory.newRow();
		keyrow.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_A");

		Row row = rerunMap.get(keyrow);

		assertEquals("PUBLISHER_ID_A", row.getColumn(PUBLISHER_APP_ID));
		assertEquals("1000", row.getColumn(INSTALLS_SUM));
		assertEquals("100", row.getColumn(RETURNERS_SUM));
		assertEquals("0.2", row.getColumn(ESTIMATE_REACH_RATE));

		keyrow = publisherGroupByKeyrowFactory.newRow();
		keyrow.setColumn(PUBLISHER_APP_ID, "PUBLISHER_ID_B");

		row = rerunMap.get(keyrow);
		assertEquals("PUBLISHER_ID_B", row.getColumn(PUBLISHER_APP_ID));
		assertEquals("4000", row.getColumn(INSTALLS_SUM));
		assertEquals("500", row.getColumn(RETURNERS_SUM));
		assertEquals("0.8", row.getColumn(ESTIMATE_REACH_RATE));
	}

	@Test
	public void processInstallReturnMapTest() {
		Map<String, List<Row>> rerunResultListMap = new OfferPublisherRerun()
				.processInstallReturnMap(offerPublisherMap);

		for (String offerId : rerunResultListMap.keySet()) {
			List<Row> rerunResultList = rerunResultListMap.get(offerId);

			for (Row row : rerunResultList) {
				System.out.println(row);
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void toJson() {
		Map<String, List<Row>> rerunResultListMap = new OfferPublisherRerun()
				.processInstallReturnMap(offerPublisherMap);

		JSONObject appTargetObj = new JSONObject();
		JSONArray offerListObj = new JSONArray();
		
		appTargetObj.put("showWeight", "1.00");
		appTargetObj.put("defaultShowWeight", "0.05");
		appTargetObj.put("offerList", offerListObj);
		
		for (String offerId : rerunResultListMap.keySet()) {
			List<Row> rerunResultList = rerunResultListMap.get(offerId);
			
			JSONObject publisherListObj = new JSONObject();
			JSONArray publisherObj = new JSONArray();

			publisherListObj.put(OFFER_ID, offerId);
			publisherListObj.put("publisherList", publisherObj);
			for (Row row : rerunResultList) {
				JSONObject obj = new JSONObject();
				obj.put(PUBLISHER_APP_ID, row.getColumn(PUBLISHER_APP_ID));
				obj.put(RERUN_RATE, row.getColumn(RERUN_RATE));
				obj.put(ESTIMATE_REACH_RATE, row.getColumn(ESTIMATE_REACH_RATE));
				obj.put(AGG_REACH_RATE, row.getColumn(AGG_REACH_RATE));
				obj.put(ESTIMATE_REACH_RATE, row.getColumn(ESTIMATE_REACH_RATE));
				obj.put(AGG_RERUN_RATE, row.getColumn(AGG_RERUN_RATE));

				publisherObj.add(obj);
			}
			
			offerListObj.add(publisherListObj);
		}

		System.out.println(appTargetObj);
	}
}
