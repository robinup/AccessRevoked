package com.tapjoy.opt.sql;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GenericQuery;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.config.OverallConfig;

public class OfferBase implements ColumnDef {
	private static Logger logger = Logger.getLogger(OfferBase.class);

	public static String[] columnList =  { ID, OFFER_NAME, ITEM_TYPE, BID,
			PAYMENT, COUNTRIES, SHOW_RATE, RANK_BOOST, PUBLISHER_APP_WHITELIST,
			APPROVED_SOURCES, PARTNER_BALANCE, DEVICE_TYPES, SELF_PROMOTE_ONLY, USE_QUALITY_LIST,
			COUNTRIES_BLACKLIST, REGIONS,DMA_CODES, CITIES};
	
	/*{ ID, OFFER_NAME, ITEM_TYPE, BID,
		PAYMENT, COUNTRIES, SHOW_RATE, RANK_BOOST, PUBLISHER_APP_WHITELIST,
		APPROVED_SOURCES, BALANCE, DEVICE_TYPES, SELF_PROMOTE_ONLY, USE_QUALITY_LIST,
		REGIONS,DMA_CODES, CITIES};*/   //for tables in product vertica, some fields like PARTNER_BALANCE doesn't exist: we rely on backup vertica right now. 10-15-2013 LJ
	
	public static String[] fColumnList = { ID, OFFER_NAME, ITEM_TYPE, BID,
        PAYMENT, COUNTRIES, SHOW_RATE, RANK_BOOST, PUBLISHER_APP_WHITELIST,
        APPROVED_SOURCES, DEVICE_TYPES, SELF_PROMOTE_ONLY, USE_QUALITY_LIST,
        COUNTRIES_BLACKLIST, REGIONS,DMA_CODES, CITIES};  //for featured ads. by Justin

	public static String[] derivedColumnList = { OS, OFFERWALL_RANK,
		IMPRESSIONS_SUM, ACTIONS_SUM, EXPECTED_ACTIONS_SUM, 
		EXPECTED_ACTIONS_VARIANCE_SUM, CONVERT_SCORE, CONVERT_SCORE_WITH_VARIANCE,
		PREDITION_SCORE, RANK_ADJUSTED_SCORE, IS_AUDITION,
		IS_HARD_PROMOTION, RANK_INDEX, SHOW_RATE_NEW };

	public static String[] keyColumnList = { ID };

	public static Map<String, Row> getAllOfferMap(Connection conn,
			String sqlStatement, boolean immediate, boolean featureflag) throws SQLException {
		Map<String, Row> allOfferMap = new HashMap<String, Row>();
		List<Row> allOfferRows = null;

		if (immediate) {
			if(!featureflag)
				allOfferRows = new GenericQuery().runQuery(conn, sqlStatement,
					columnList, derivedColumnList, featureflag);
			else
				allOfferRows = new GenericQuery().runQuery(conn, sqlStatement,
						fColumnList, derivedColumnList, featureflag);
		} else {
			if(!featureflag)
				allOfferRows = new GenericQuery().runQuery(conn, sqlStatement,
					columnList, derivedColumnList,
					OverallConfig.SQL.MIN_ALL_OFFER_SIZE,
					OverallConfig.SQL.SLEEP_IN_SECONDS, featureflag);
			else
				allOfferRows = new GenericQuery().runQuery(conn, sqlStatement,
						fColumnList, derivedColumnList,
						OverallConfig.SQL.MIN_ALL_OFFER_SIZE,
						OverallConfig.SQL.SLEEP_IN_SECONDS, featureflag);
		}

		for (Row row : allOfferRows) {
			allOfferMap.put(row.getColumn(ID).toString(), row);
		}

		if (logger.isDebugEnabled()) {
			for (String offerId : OverallConfig.OfferDebug.tackingOffers) {
				Row row = allOfferMap.get(offerId);
				if (row != null) {
					logger.debug("tracked offers for all offer map::" + row);
				}
			}
		}

		return allOfferMap;
	}

	public static Map<String, Row> getAllOfferMap(Connection conn,
			String sqlStatement, boolean featureflag) throws SQLException {
		Map<String, Row> allOfferMap = getAllOfferMap(conn, sqlStatement, true, featureflag);
		return allOfferMap;
	}
}
