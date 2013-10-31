package com.tapjoy.opt.vertica_score.target.app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.GroupBy;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowDoubleFieldComparator;
import com.tapjoy.opt.common.RowFactory;
import com.tapjoy.opt.common.Sum;
import com.tapjoy.opt.common.GroupBy.CallBackPair;

public class OfferPublisherRerun implements ColumnDef {
	public static class GroupByResult {
		public Map<Row, Row> groupByResult;
		public Row globalResult;
	}

	private static Logger logger = Logger.getLogger(OfferPublisherRerun.class);

	public static String RERUN_SQL = "select q.offer_id, q.publisher_app_id, q.installs, r.returners "
			+ "from "
			+ "( "
			+ "select offer_id, publisher_app_id, sum(total_install) as installs "
			+ "from app_install "
			+ "where install_day between date(now()) - 28 and date(now()) - 2 "
			+ "group by 1, 2 "
			+ ") q "
			+ "left join "
			+ "( "
			+ "select offer_id, publisher_app_id, sum(total_rerun) as returners "
			+ "from app_dayone_rerun "
			+ "where install_day between date(now()) - 28 and date(now()) - 2 "
			+ "group by 1, 2 "
			+ ") r "
			+ "on q.offer_id = r.offer_id and q.publisher_app_id = r.publisher_app_id "
			+ "where r.returners > 0";

	public static String[] columnList = { OFFER_ID, PUBLISHER_APP_ID, INSTALLS,
			RETURNERS };
	public static String[] derivedColumnList = { RERUN_RATE,
			ESTIMATE_REACH_RATE, AGG_REACH_RATE, AGG_RERUN_RATE };
	public static String[] keyColumns = { OFFER_ID, PUBLISHER_APP_ID };

	public static String[] publisherGroupByColumns = { PUBLISHER_APP_ID };

	/**
	 * returning the map for OFFER_ID, PUBLISHER_APP_ID ==> OFFER_ID,
	 * PUBLISHER_APP_ID, INSTALLS, RETURNERS, RERUN_RATE, REACH_RATE,
	 * AGG_REACH_RATE, AGG_RERUN_RATE
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<Row, Row> getInstallRetunerFromDB(Connection conn)
			throws SQLException {
		List<Row> returners = new GenericQuery().runQuery(conn, RERUN_SQL,
				columnList, derivedColumnList, false);
		Map<Row, Row> map = Row.toKeyMap(returners, keyColumns);

		return map;
	}

	private List<Row> getSubListRow(Map<Row, Row> subMap) {
		// sorted by rerun-rate
		List<Row> returnerList = new ArrayList<Row>(subMap.values());

		for (Row row : returnerList) {
			row.setColumn(RERUN_RATE, "0.0");

			if (row.getColumn(INSTALLS) != null
					&& row.getColumn(RETURNERS) != null) {
				int installs = Integer.parseInt(row.getColumn(INSTALLS));
				int returners = Integer.parseInt(row.getColumn(RETURNERS));

				if (installs > 0) {
					Double rerunRate = new Double(returners)
							/ new Double(installs);
					row.setColumn(RERUN_RATE, rerunRate.toString());
				}
			}
		}

		Collections
				.sort(returnerList, new RowDoubleFieldComparator(RERUN_RATE));

		// retrieving global reach_rate of the publisher
		Map<Row, Row> publisherMap = publiserhGroupBy(subMap).groupByResult;
		RowFactory publisherGroupByKeyrowFactory = new RowFactory(
				OfferPublisherRerun.publisherGroupByColumns);
		for (Row row : returnerList) {
			Row keyrow = publisherGroupByKeyrowFactory.newRow();
			keyrow.setColumn(PUBLISHER_APP_ID, row.getColumn(PUBLISHER_APP_ID));

			Row publisherRow = publisherMap.get(keyrow);
			row.setColumn(ESTIMATE_REACH_RATE,
					publisherRow.getColumn(ESTIMATE_REACH_RATE));
		}

		double aggReachRate = 0.0;
		double aggRerunRate = 0.0;
		// calculating aggregate rerun-rate
		for (Row row : returnerList) {
			double rerunRate = 0.0;
			if (row.getColumn(RERUN_RATE) != null) {
				rerunRate = new Double(row.getColumn(RERUN_RATE));
			}

			double reachRate = 0.0;
			if (row.getColumn(ESTIMATE_REACH_RATE) != null) {
				reachRate = new Double(row.getColumn(ESTIMATE_REACH_RATE));
			}

			aggReachRate += reachRate;
			aggRerunRate += reachRate * rerunRate;

			row.setColumn(AGG_REACH_RATE, aggReachRate);
			row.setColumn(AGG_RERUN_RATE, aggRerunRate / aggReachRate);
		}

		return returnerList;
	}

	/**
	 * OFFER_ID ==> sorted list of ROW
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, List<Row>> processInstallReturnMap(Map<Row, Row> map) {
		// map of sorted list
		Map<String, List<Row>> offerMap = new HashMap<String, List<Row>>();

		// map of offerid ==> map
		Map<String, Map<Row, Row>> offerPublisherMap = new HashMap<String, Map<Row, Row>>();
		for (Row keyRow : map.keySet()) {
			String offerId = keyRow.getColumn(OFFER_ID);

			Map<Row, Row> subMap = offerPublisherMap.get(offerId);
			if (subMap == null) {
				subMap = new HashMap<Row, Row>();
				offerPublisherMap.put(offerId, subMap);
			}

			subMap.put(keyRow, map.get(keyRow));
		}

		for (String offerId : offerPublisherMap.keySet()) {
			Map<Row, Row> subMap = offerPublisherMap.get(offerId);
			List<Row> sublistRow = getSubListRow(subMap);
			offerMap.put(offerId, sublistRow);
		}

		return offerMap;
	}

	/**
	 * returning the map for OFFER_ID, PUBLISHER_APP_ID ==> INSTALLS, RETURNERS
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<String, List<Row>> getInstallRetuner(Connection conn)
			throws SQLException {
		Map<Row, Row> map = getInstallRetunerFromDB(conn);

		return processInstallReturnMap(map);
	}

	/**
	 * group by PUBLISHER_APP_ID
	 * 
	 * @param rowMap
	 * @return
	 */
	public GroupByResult publiserhGroupBy(Map<Row, Row> rowMap) {

		CallBackPair[] callbackpairs = new CallBackPair[3];
		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = INSTALLS_SUM;
		callbackpairs[0].callBack = new Sum(INSTALLS);

		callbackpairs[1] = new CallBackPair();
		callbackpairs[1].columnName = RETURNERS_SUM;
		callbackpairs[1].callBack = new Sum(RETURNERS);

		callbackpairs[2] = new CallBackPair();
		callbackpairs[2].columnName = ESTIMATE_REACH_RATE;

		CallBackPair[] globalCallbackpairs = new CallBackPair[2];
		globalCallbackpairs[0] = new CallBackPair();
		globalCallbackpairs[0].columnName = TOTAL_INSTALLS_SUM;
		globalCallbackpairs[0].callBack = new Sum(INSTALLS);

		globalCallbackpairs[1] = new CallBackPair();
		globalCallbackpairs[1].columnName = TOTAL_RETURNERS_SUM;
		globalCallbackpairs[1].callBack = new Sum(RETURNERS);

		GroupBy groupBy = new GroupBy(publisherGroupByColumns, callbackpairs,
				globalCallbackpairs);

		Iterator<Entry<Row, Row>> iter = rowMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			Row row = entry.getValue();

			groupBy.proceess(row);
		}

		Map<Row, Row> results = groupBy.getGroupByResult();

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(results,
					"after group by publisher_app_id");
		}

		Row globalResultRow = groupBy.getGlobGroupByResult();
		if (logger.isDebugEnabled()) {
			logger.debug("global result:" + globalResultRow);
		}

		double totalInstalls = 0.0;
		if (globalResultRow.getColumn(TOTAL_INSTALLS_SUM) != null) {
			totalInstalls = new Double(
					globalResultRow.getColumn(TOTAL_INSTALLS_SUM));
		}

		if (totalInstalls > 0) {
			// calculating estimate reach rate
			iter = results.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Row, Row> entry = iter.next();
				Row row = entry.getValue();

				double installs = 0.0;
				if (row.getColumn(INSTALLS_SUM) != null) {
					installs = new Double(row.getColumn(INSTALLS_SUM));
				}

				row.setColumn(ESTIMATE_REACH_RATE, installs / totalInstalls);
			}
		}

		GroupByResult groupByResult = new GroupByResult();
		groupByResult.groupByResult = results;
		groupByResult.globalResult = globalResultRow;

		return groupByResult;
	}
}
