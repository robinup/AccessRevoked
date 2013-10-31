package com.tapjoy.opt.vertica_score.target.app;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.VerticaConn;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.CountryFilterTest;
import com.tapjoy.opt.vertica_score.target.app.OfferPublisherRerun;
import com.tapjoy.opt.vertica_score.target.app.etl.AppInstallAgg;
import com.tapjoy.opt.vertica_score.target.app.etl.AppRerunAgg;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OfferPublisherRerunDBTest implements ColumnDef {
	private static Logger logger = Logger.getLogger(CountryFilterTest.class);
	private static Map<Row, Row> offerPublisherMap = new HashMap<Row, Row>();

	@Before
	public void setUp() throws SQLException, ClassNotFoundException {
		Connection conn = VerticaConn.getTestConnection();

		AppInstallAgg.execute(conn,
				Configuration.Targeting.App.ETL_LOOKBACK);

		AppRerunAgg.execute(conn);

		offerPublisherMap = new OfferPublisherRerun()
				.getInstallRetunerFromDB(conn);

		conn.close();
	}

	@Test
	public void getRerun() {
		OfferPublisherRerun.GroupByResult groupByResult = new OfferPublisherRerun()
				.publiserhGroupBy(offerPublisherMap);

		Row globalRerunResult = groupByResult.globalResult;
		assertEquals("2386062", globalRerunResult.getColumn(TOTAL_INSTALLS_SUM));
		assertEquals("212053", globalRerunResult.getColumn(TOTAL_RETURNERS_SUM));

		Map<Row, Row> rerunMap = groupByResult.groupByResult;
		assertEquals(2281, rerunMap.size());

		RowFactory publisherGroupByKeyrowFactory = new RowFactory(
				OfferPublisherRerun.publisherGroupByColumns);

		Row keyrow = publisherGroupByKeyrowFactory.newRow();
		keyrow.setColumn(PUBLISHER_APP_ID,
				"fad830be-9f54-4e97-8b9b-74b6da8e6787");

		Row row = rerunMap.get(keyrow);

		assertEquals("fad830be-9f54-4e97-8b9b-74b6da8e6787",
				row.getColumn(PUBLISHER_APP_ID));
		assertEquals("266", row.getColumn(INSTALLS_SUM));
		assertEquals("80", row.getColumn(RETURNERS_SUM));
		assertEquals("9.507538119509039E-5", row.getColumn(ESTIMATE_REACH_RATE));

		keyrow = publisherGroupByKeyrowFactory.newRow();
		keyrow.setColumn(PUBLISHER_APP_ID,
				"2a9fc4fa-e75f-4e74-a6f7-b6c1ce945360");

		row = rerunMap.get(keyrow);
		assertEquals("2a9fc4fa-e75f-4e74-a6f7-b6c1ce945360",
				row.getColumn(PUBLISHER_APP_ID));
		assertEquals("21869", row.getColumn(INSTALLS_SUM));
		assertEquals("4148", row.getColumn(RETURNERS_SUM));
		assertEquals("0.007816554553967788", row.getColumn(ESTIMATE_REACH_RATE));
	}

	@Test
	public void processInstallReturnMapTest() {
		Map<String, List<Row>> rerunResultListMap = new OfferPublisherRerun()
				.processInstallReturnMap(offerPublisherMap);

		for (String offerId : rerunResultListMap.keySet()) {
			logger.debug("checking rerun for offer id:" + offerId);
			List<Row> rerunResultList = rerunResultListMap.get(offerId);

			for (Row row : rerunResultList) {
				logger.debug(row);
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
		
		String jsonString = appTargetObj.toString();
		jsonString = jsonString.replaceAll("\\},", "},\n");	
		System.out.println(jsonString);
	}
}
