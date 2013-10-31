package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmailLogger;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.etl.sql.app.AppNormCVR;
import com.tapjoy.opt.vertica_score.etl.sql.country.CountryNormCVR;
import com.tapjoy.opt.vertica_score.etl.sql.countryapp.CountryAppNormCVR;
import com.tapjoy.opt.vertica_score.etl.sql.global.GlobalNormCVR;
import com.tapjoy.opt.vertica_score.etl.sql.tjmcountry.TJMCountryNormCVR;
import com.tapjoy.opt.vertica_score.etl.sql.tjmglobal.TJMNormCVR;
import com.tapjoy.opt.vertica_score.objectCache.OptOfferCache;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.ConvertScore;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.CountryFilter;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.ExpectedActionSum;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.Transformer;

public abstract class OptOfferGeneratorBase implements ColumnDef,
		OptOfferGenerator {
	private static Logger logger = Logger
			.getLogger(OptOfferGeneratorBase.class);

	// Retrieving opt offer map
	@Override
	public Map<String, Row> getOptOfferMap(Connection conn) throws SQLException {
		return OptOfferCache.getInstance().get(conn, getSegment());
	}

	@Override
	public void updateMap(Connection conn,
			Map<RankedOfferKey, List<Row>> rankedOfferMap) throws SQLException {

		Map<String, Row> allOfferMap = OfferCache.getInstance().get();  //commented by LJ 08-16
		//Map<String, Row> allOfferMap = OfferCache.getInstance().get(GlobalDataManager.conn); //added by LJ 08-16
		logger.info("All offer size is: " + allOfferMap.size());

		Map<Row, Row> groupByMap = getOfferList(conn, allOfferMap);

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(groupByMap,
					"after getting group by map in updateMap::");
		}

		// { OS, OFFER_ID, OFFERWALL_RANK }
		for (Row valueRow : groupByMap.values()) {
			String currentOS = valueRow.getColumn(OS);

			String offerId = valueRow.getColumn(OFFER_ID);

			if (allOfferMap.get(offerId) == null) {
				OfferRowUtil.debugOffer(valueRow,
						"offer id not found in the all offer map::" + offerId);
				continue;
			}

			Object covertScore = valueRow.getColumn(CONVERT_SCORE);
			if ((covertScore == null) || (covertScore.toString().length() == 0)) {
				logger.warn("removing row for results. covertScore is empty for row:"
						+ valueRow);
				continue;
			} else if (new Double(covertScore.toString()) <= 0.0) {
				logger.warn("removing row for results. covertScore is less or equal 0 for row:"
						+ valueRow);
				continue;
			}

			Object covertScoreWithVariant = valueRow
					.getColumn(CONVERT_SCORE_WITH_VARIANCE);
			if ((covertScoreWithVariant == null)
					|| (covertScoreWithVariant.toString().length() == 0)) {
				logger.warn("removing row for results. covertScore with variant is empty for row:"
						+ valueRow);
				continue;
			} else if (new Double(covertScoreWithVariant.toString()) <= 0.0) {
				logger.warn("removing row for results. covertScore with varaint is less or equal 0 for row:"
						+ valueRow);
				continue;
			}
			OfferRowUtil.debugOffer(valueRow, "covertScore::" + covertScore
					+ " covertScoreWithVariant::" + covertScoreWithVariant);

			List<String> devices = OfferRowUtil.getDevicesFromOffer(allOfferMap.get(offerId),
					currentOS);

			for (String device : devices) {
				for (String algorithm : Configuration.algorithms) {
					RankedOfferKey rankedOfferKey = getRankedOfferKey(
							currentOS, device, algorithm);

					Row offerRow = allOfferMap.get(offerId).clone();		
					offerRow.setColumn(CONVERT_SCORE_WITH_VARIANCE,
							covertScoreWithVariant);

					// For algorithm 280, introducing convert score with
					if (rankedOfferKey.algorithm
							.equals(Configuration.Ranking.ALGORITHM_280)) {
						offerRow.setColumn(CONVERT_SCORE,
								covertScoreWithVariant);
					} else {
						offerRow.setColumn(CONVERT_SCORE, covertScore);
					}
					OfferRowUtil.debugOffer(offerRow,
							"rankedOfferKey.algorithm::"
									+ rankedOfferKey.algorithm
									+ " covertScore::" + offerRow.getColumn(CONVERT_SCORE));

					// check country filter
					if ((rankedOfferKey.country != null)
							&& (rankedOfferKey.country.length() != 0)) {
						if (CountryFilter.isValidRow(offerRow,
								rankedOfferKey.country) == false) {
							logger.debug("Ignore the offer:" + offerId
									+ " as it is eligible for country:"
									+ rankedOfferKey.country);
							continue;
						}
					}

					List<Row> offerList = rankedOfferMap.get(rankedOfferKey);
					if (offerList == null) {
						offerList = new ArrayList<Row>();
						rankedOfferMap.put(rankedOfferKey, offerList);
					}

					if (allOfferMap.get(offerId) == null) {
						logger.debug("Ignore the offer:" + offerId
								+ " as it is not in the allOfferMap");
						continue;
					}

					OfferRowUtil.debugOffer(offerRow,
							"adding the offer into ranked offer with id::"
									+ offerId + " with device:" + device);
					offerList.add(offerRow);

					rankedOfferMap.put(rankedOfferKey, offerList);
				}
			}
		}

		/**
		 *  We don't want remove any list as we might be able to back fill those
		 * 
		 */
		/*
		// filtering out those rank with less than 80 offers, hack
		Iterator<Entry<RankedOfferKey, List<Row>>> offerListIter = rankedOfferMap
				.entrySet().iterator();

		while (offerListIter.hasNext()) {
			Entry<RankedOfferKey, List<Row>> offerRowsEntry = offerListIter
					.next();

			if (logger.isDebugEnabled()) {
				OfferRowUtil
						.debugOfferRow(
								offerRowsEntry.getValue(),
								"Offer List with ranked key:"
										+ offerRowsEntry.getKey());
			}

			if (offerRowsEntry.getValue().size() < Configuration.Ranking.MIN_OFFER_SIZE) {
				logger.warn("offerRows does not have enough offer. removed:"
						+ offerRowsEntry);
				// ignore if there is not enough offers
				offerListIter.remove();
			}
		}
		 */
	}

	private Map<Row, Row> getGlobalCVR(Connection conn, String segment)
			throws SQLException {
		Map<Row, Row> globalCVRMap = null;

		if (segment.equals(ConfigurationSegment.OPT_GOW_GLOBAL)) {
			globalCVRMap = new GlobalNormCVR().getNormCVRMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY)) {
			globalCVRMap = new CountryNormCVR().getNormCVRMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_APP)) {
			globalCVRMap = new AppNormCVR().getNormCVRMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY_APP)) {
			globalCVRMap = new CountryAppNormCVR().getNormCVRMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM)) {
			globalCVRMap = new TJMNormCVR().getNormCVRMap(conn);
		} else if (segment.equals(ConfigurationSegment.OPT_TJM_COUNTRY)) {
			globalCVRMap = new TJMCountryNormCVR().getNormCVRMap(conn);
		}

		if (globalCVRMap == null) {
			logger.fatal("Did not find global CVR for segment:" + segment);
		} else {
			logger.debug("found global CVR for segment:" + segment
					+ " with size:" + globalCVRMap.size());
		}

		return globalCVRMap;
	}

	@Override
	public Map<Row, Row> getOfferList(Connection conn,
			Map<String, Row> allOfferMap) throws SQLException {
		logger.info("Starting optimization for " + getSegment());

		Map<String, Row> optOfferMap = getOptOfferMap(conn);
		EmailLogger.getInstance().info(
				logger,
				"Optimizable " + getSegment() + " offer size is: "
						+ optOfferMap.size());

		// OS, CURRENCY_ID?, OFFER_ID, OFFERWALL_RANK ==> ROW
		Map<Row, Row> offerRankCVRMap = getOfferNormCVRMap(conn);

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(offerRankCVRMap,
					"after getOfferNormCVRMap in getOfferList::");
		}

		// { OS, CURRENCY_ID?, OFFER_ID, OFFERWALL_RANK ==> ROW ==> VIEWS_SUM,
		// ACTIONS_SUM }
		Map<Row, Row> groupOfferByMap = new OfferRankGroupBy()
				.groupBy(offerRankCVRMap);

		// OS, CURRENCY_ID, OFFERWALL_RANK ==> ROW
		Map<Row, Row> globalCVRMap = getGlobalCVR(conn, getSegment());

		// calculating expected action sum
		groupOfferByMap = new ExpectedActionSum(globalCVRMap)
				.transform(groupOfferByMap);

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(groupOfferByMap,
					"after ExpectedActionSum transform in getOfferList::");
		}

		// { OS, OFFER_ID ==> score }
		Map<Row, Row> groupByMap = new OfferGroupBy().groupBy(groupOfferByMap);

		// calculating score
		Transformer convertScoreTransformer = new ConvertScore(allOfferMap);
		groupByMap = convertScoreTransformer.transform(groupByMap);

		return groupByMap;
	}
}
