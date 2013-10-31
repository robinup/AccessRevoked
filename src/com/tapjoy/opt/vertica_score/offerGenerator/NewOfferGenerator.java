package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowDoubleFieldComparator;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryNewOffer;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMNewOffer;
import com.tapjoy.opt.vertica_score.objectCache.AuditionCache;
import com.tapjoy.opt.vertica_score.objectCache.NewOfferCache;

public class NewOfferGenerator implements ColumnDef {
	private static Logger logger = Logger.getLogger(NewOfferGenerator.class);

	/**
	 * getting new offers. new offers are defined those offers not having
	 * optimization information
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Row> getNewOffers(Connection conn, String segment)
			throws SQLException {
		logger.debug("getNewOffers<");

		Map<String, Row> newOfferMap = new HashMap<String, Row>();

		if (segment.equals(ConfigurationSegment.OPT_GOW_GLOBAL)) {
			newOfferMap = GlobalNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY)) {
			newOfferMap = CountryNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_APP)) {
			newOfferMap = AppNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY_APP)) {
			newOfferMap = CountryAppNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM)) {
			newOfferMap = TJMNewOffer.getOfferMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM_COUNTRY)) {
			newOfferMap = TJMCountryNewOffer.getOfferMap(conn);
		}

		logger.debug("getNewOffers>");
		return newOfferMap;
	}

	/**
	 * get new offers that we don't have enough statistics data sorted them by
	 * predicted score on descending order
	 * 
	 * Bid_Disount change - Now with the new fields in the new offer list, we have the choice
	 * to get new offers ranked based on different algorithms
	 * 
	 * @throws SQLException
	 */
	public static List<Row> getSortedNewOffers(Connection conn, String os,
			Integer platform, String segment) throws SQLException {
		logger.debug("getSortedNewOffers<");
		List<Row> newOfferList = new ArrayList<Row>();
		System.out.printf("now in getSortedNewOffers from NewOfferGenerator -- LJ\n");
		try {
			Map<String, Row> auditionOfferMap = AuditionCache.getInstance()
					.getAuditionMap(os, platform);
			
			System.out.printf("got auditionOfferMap size=%d from NewOfferGenerator -- LJ\n", auditionOfferMap.size());

			logger.debug("getting new offer for segment::" + segment);
			Map<String, Row> newOfferMap = NewOfferCache.getInstance().get(
					conn, segment);
			logger.debug("total number of offers for segment::" + segment
					+ " is::" + newOfferMap.size());

			for (String offerId : newOfferMap.keySet()) {
				if ((offerId != null)
						&& (auditionOfferMap.get(offerId) != null)) {
					Row row = newOfferMap.get(offerId);

					if (row == null) {
						logger.error("row is null for offerId:" + offerId);
					} else {
						row.setColumn(
								PREDITION_SCORE,
								auditionOfferMap.get(offerId).getColumn(
										PREDITION_SCORE));
						newOfferList.add(row);
					}
				}
			}

			OfferRowUtil.debugOfferRow(newOfferList,
					"new offer list with prediction score");
			Collections.sort(newOfferList, new RowDoubleFieldComparator(
					PREDITION_SCORE));   //commented by LJ 08-15
			//System.out.printf("auditionOffer sort skipped -- LJ\n");
			OfferRowUtil.debugOfferRow(newOfferList,
					"after sorting, new offer list with prediction score");
		} catch (Throwable e) {
			String fatalMsg = "fatal excpetion caught!!!" + os + "||"
					+ platform + "||" + segment;
			logger.fatal(fatalMsg, e);
			throw new SQLException(fatalMsg, e);
		}

		logger.debug("getSortedNewOffers>");
		return newOfferList;
	}
	
	
	
	public static List<Row> getSortedNewOffersWithDiscount(Connection conn, String os,
			Integer platform, String segment) throws SQLException {
		logger.debug("getSortedNewOffers<");
		List<Row> newOfferList = new ArrayList<Row>();

		try {
			Map<String, Row> auditionOfferMap = AuditionCache.getInstance()
					.getAuditionMap(os, platform);

			logger.debug("getting new offer for segment::" + segment);
			Map<String, Row> newOfferMap = NewOfferCache.getInstance().get(
					conn, segment);
			logger.debug("total number of offers for segment::" + segment
					+ " is::" + newOfferMap.size());

			for (String offerId : newOfferMap.keySet()) {
				if ((offerId != null)
						&& (auditionOfferMap.get(offerId) != null)) {
					Row row = newOfferMap.get(offerId);

					if (row == null) {
						logger.error("row is null for offerId:" + offerId);
					} else {
						row.setColumn(
								PREDITION_SCORE,
								auditionOfferMap.get(offerId).getColumn(
										PREDICTION_SCORE_DISCOUNT));
						newOfferList.add(row);
					}
				}
			}

			OfferRowUtil.debugOfferRow(newOfferList,
					"new offer list with prediction score");
			Collections.sort(newOfferList, new RowDoubleFieldComparator(
					PREDITION_SCORE));
			OfferRowUtil.debugOfferRow(newOfferList,
					"after sorting, new offer list with prediction score");
		} catch (Throwable e) {
			logger.fatal("fatal excpetion caught!!!", e);
			throw new SQLException("", e);
		}

		logger.debug("getSortedNewOffers>");
		return newOfferList;
	}
}

