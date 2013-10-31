package com.tapjoy.opt.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.StringUtil;

public class OfferRowUtil implements ColumnDef {
	private static Logger logger = Logger.getLogger(OfferRowUtil.class);

	/**
	 * creating offerId/Row mapping out of row list
	 */
	public static Map<String, Row> offerListToMap(List<Row> rowList) {
		Map<String, Row> rowMap = new HashMap<String, Row>();

		for (Row row : rowList) {
			String offerId = row.getColumn(ID).toString();

			if (offerId != null) {
				rowMap.put(offerId, row);
			}
		}

		return rowMap;
	}

	/**
	 * String list to String array
	 */
	public static String[] toStringArray(List<String> strList) {
		String[] strArray = new String[strList.size()];

		int i = 0;
		for (String str : strList) {
			strArray[i++] = str;
		}

		return strArray;
	}

	/**
	 * appending offerList to the Map
	 */
	public static Map<String, Row> appendOfferListToMap(
			Map<String, Row> rowMap, List<Row> rowList) {

		for (Row row : rowList) {
			String offerId = row.getColumn(ID).toString();

			if (offerId != null) {
				rowMap.put(offerId, row);
			}
		}

		return rowMap;
	}

	public static String[] getKeyColumns(Map<Row, Row> rowMap) {
		String[] keyColumns = null;

		Iterator<Entry<Row, Row>> iter = rowMap.entrySet().iterator();

		if (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			keyColumns = entry.getKey().getColumns();
		}

		return keyColumns;
	}

	public static String[] getValueColumns(Map<Row, Row> rowMap) {
		String[] valueColumns = null;

		Iterator<Entry<Row, Row>> iter = rowMap.entrySet().iterator();

		if (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			valueColumns = entry.getValue().getColumns();
		}

		return valueColumns;
	}

	public static void debugGroupByMap(Map<Row, Row> groupByMap,
			String debugInfo) {
		if (logger.isDebugEnabled()) {
			for (String offerId : OverallConfig.OfferDebug.tackingOffers) {
				for (Row keyRow : groupByMap.keySet()) {
					Row row = groupByMap.get(keyRow);
					if (row != null) {
						if ((row.getColumnIndex(OFFER_ID) != null)
								&& (row.getColumn(OFFER_ID).equals(offerId))) {
							logger.debug(debugInfo + ", debugged row is::"
									+ keyRow + "==>" + row);
						} else if ((row.getColumnIndex(ID) != null)
								&& (row.getColumn(ID).equals(offerId))) {
							logger.debug(debugInfo + ", debugged row is::"
									+ keyRow + "==>" + row);
						}
					}
				}
			}
		}
	}

	public static void debugOfferRow(Collection<Row> offerSet, String debugInfo) {
		if (logger.isDebugEnabled()) {
			for (String offerId : OverallConfig.OfferDebug.tackingOffers) {
				for (Row row : offerSet) {
					if (row != null) {
						if ((row.getColumnIndex(OFFER_ID) != null)
								&& (row.getColumn(OFFER_ID).equals(offerId))) {
							logger.debug(debugInfo + " " + row);
						} else if ((row.getColumnIndex(ID) != null)
								&& (row.getColumn(ID).equals(offerId))) {
							logger.debug(debugInfo + " " + row);
						}
					}
				}
			}
		}
	}

	public static void debugOffer(Row row, String debugInfo) {
		if (logger.isDebugEnabled()) {
			for (String offerId : OverallConfig.OfferDebug.tackingOffers) {
				if (row != null) {
					if ((row.getColumnIndex(OFFER_ID) != null)
							&& (row.getColumn(OFFER_ID).equals(offerId))) {
						logger.debug(debugInfo + " " + row);
					} else if ((row.getColumnIndex(ID) != null)
							&& (row.getColumn(ID).equals(offerId))) {
						logger.debug(debugInfo + " " + row);
					}
				}
			}
		}
	}

	/**
	 * retrieving device types from the offer
	 * 
	 * @param row
	 * @return
	 */
	public static Double getLeastBoostedScore(List<Row> rowList, Double leastScore) {
		Double leastBoostedScore = leastScore;

		for (Row row : rowList) {
			Double boostedScore = new Double(
					row.getColumn(ColumnDef.RANK_ADJUSTED_SCORE));
			if (boostedScore < leastBoostedScore) {
				leastBoostedScore = boostedScore;
			}
		}

		return leastBoostedScore;
	}

	/**
	 * Minus offerMap
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Row> minusOfferMap(Map<String, Row> allOfferMap,
			Map<String, Row> offerMap) {
		Map<String, Row> newOfferMap = new HashMap<String, Row>();

		for (String offerId : allOfferMap.keySet()) {
			if (offerMap.containsKey(offerId) == false) {
				newOfferMap.put(offerId, allOfferMap.get(offerId));
			}
		}

		return newOfferMap;
	}

	/**
	 * deDup the offerList by id
	 * 
	 * @param row
	 * @return
	 */
	public static void deDupOfferListById(List<Row> rowList) {
		Set<String> offerIdSet = new HashSet<String>();

		Iterator<Row> iter = rowList.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			String id = row.getColumn(ColumnDef.ID);
			if (offerIdSet.contains(id)) {
				iter.remove();
			} else {
				offerIdSet.add(id);
			}
		}
	}

	/**
	 * clone the list of Row
	 * 
	 * @param row
	 * @return
	 */
	public static List<Row> cloneList(List<Row> rowList) {
		if (rowList == null) {
			return null;
		}

		List<Row> cloneRowList = new ArrayList<Row>();

		Iterator<Row> iter = rowList.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();
			cloneRowList.add(row.clone());
		}

		return cloneRowList;
	}

	public static Map<String, Row> cloneRowMap(Map<String, Row> map) {
		Map<String, Row> cloneRowMap = new HashMap<String, Row>();

		Iterator<Entry<String, Row>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Row> mapEntry = iter.next();
			cloneRowMap.put(mapEntry.getKey(), mapEntry.getValue());
		}

		return cloneRowMap;
	}

	/**
	 * retrieving device types from the offer
	 * 
	 * @param row
	 * @return
	 */
	public static List<String> getDevicesFromOffer(Row row, String os) {
		String deviceTypesString = row.getColumn(ColumnDef.DEVICE_TYPES);
		List<String> qualifiedDevices = new ArrayList<String>();

		// check null or empty string
		if ((deviceTypesString == null) || (deviceTypesString.length() == 0)) {
			return qualifiedDevices;
		}

		// check null or empty string
		if ((os == null) || (os.length() == 0)) {
			logger.error("os is null");
			return qualifiedDevices;
		}

		List<String> deviceTypes = StringUtil
				.formattedStringToList(deviceTypesString);
		deviceTypes = StringUtil.toLowerCase(deviceTypes);

		for (String device : deviceTypes) {
			device = device.toLowerCase();
			if ((os.equals("iOS") && device.matches("android"))
					|| (os.equals("iOS") && device.matches("windows"))
					|| (os.equals("Android") && device.matches("^i.*"))
					|| (os.equals("Android") && device.matches("windows"))
					|| (os.equals("Windows") && device.matches("^i.*"))
					|| (os.equals("Windows") && device.matches("android"))) {
				continue;
			}

			qualifiedDevices.add(device);
		}

		return qualifiedDevices;
	}
	
	
	public static HashMap<String, String []> getMCExtraction(Map<String, Row> offerMap){
		HashMap<String, String []> mcExtraction = new HashMap<String, String []>();
		Set<String> keys = offerMap.keySet();
		for(String key : keys){
			Row row = offerMap.get(key);
			if (row != null) {
				mcExtraction.put(key, row.getRowdata());
			}
		}
		
		return mcExtraction;
	}
	
	public static HashMap<String, Row> restoreFromMcExtraction(HashMap<String, String []> mcExtract, Map<String, Integer> colMap,
			      Map<String, Integer> derivedColMap,  Map<Integer, String> idxMap){
		HashMap<String, Row> offerMap = new HashMap<String, Row>();
		Set<String> keys = mcExtract.keySet();
		for (String key : keys) {
			String [] rowVals = mcExtract.get(key);
			if (rowVals != null) {
				offerMap.put(key, new Row(colMap, derivedColMap, idxMap, rowVals));
			}
		}
		return offerMap;
	}

}
