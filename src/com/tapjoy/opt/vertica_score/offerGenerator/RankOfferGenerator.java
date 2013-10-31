package com.tapjoy.opt.vertica_score.offerGenerator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowDoubleFieldComparator;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationHardPromotion;
import com.tapjoy.opt.vertica_score.config.ConfigurationSegment;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.CountryFilter;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.DeviceFilter;

/*
 * Let X be a discrete random variable with the binomial distribution with parameters n and p.

 Then the variance of X is given by:
 var(X)=np
 */
public class RankOfferGenerator implements ColumnDef {
	private RankedOfferKey rankedOfferKey;
	private Map<RankedOfferKey, List<Row>> scoredOfferMap;
	private Map<Integer, Double> auditionShowMap;
	private Map<String, Row> allOfferMap;
	@SuppressWarnings("unused")
	private boolean enabled;
	private String segment;

	private static Logger logger = Logger.getLogger(RankOfferGenerator.class);

	public RankOfferGenerator(RankedOfferKey rankedOfferKey,
			Map<RankedOfferKey, List<Row>> scoredOfferMap,
			Map<Integer, Double> auditionShowMap, Map<String, Row> allOfferMap,
			boolean enabled, String segment) {
		this.rankedOfferKey = rankedOfferKey;
		this.scoredOfferMap = scoredOfferMap;
		this.auditionShowMap = auditionShowMap;
		this.allOfferMap = allOfferMap;
		this.enabled = enabled;
		this.segment = segment;
	}

	private boolean insertAudition(int rankIndex, Double currentOfferScore,
			List<Row> newOfferList, List<Row> ohashList) {
		logger.debug("insertAudition<");

		boolean inserted = false;

		if (currentOfferScore <= 0.0) {
			if (logger.isDebugEnabled()) {
				OfferRowUtil
						.debugOfferRow(newOfferList,
								"insertAudition:: the current offer score is less or equal 0.0");
			}
		}

		Iterator<Row> iter = newOfferList.iterator();
		while (iter.hasNext()) {
			Row newOffer = iter.next().clone();
			logger.debug("Current new offer: " + newOffer);

			boolean isValid = CountryFilter.isValidRow(newOffer,
					rankedOfferKey.country);
			if (!isValid) {
				logger.debug("ignoring offer not fitting the country::"
						+ newOffer);
				iter.remove();
				continue;
			}

			isValid = DeviceFilter.isValidRow(newOffer, rankedOfferKey.os,
					rankedOfferKey.device);
			if (!isValid) {
				logger.debug("ignoring offer not fitting the device::"
						+ newOffer);
				iter.remove();
				continue;
			}

			Double showRate = new Double(newOffer.getColumn(SHOW_RATE));
			if (showRate < Configuration.Ranking.MIN_SHOW_RATE) {
				logger.debug("showing rate not meeting the requirement::"
						+ newOffer);
				iter.remove();
				continue;
			}

			Double rankScore = Math.round(new Double(currentOfferScore))
					- rankIndex / 1000.0;   //changed to - LJ 08-20

			logger.info(" Add Offer " + newOffer + " with score " + rankScore
					+ " to slot " + rankIndex);

			newOffer.setColumn(RANK_ADJUSTED_SCORE, rankScore);
			newOffer.setColumn(IS_AUDITION, "1");
			newOffer.setColumn(IS_HARD_PROMOTION, "0");
			newOffer.setColumn(RANK_INDEX, rankIndex);

			ohashList.add(newOffer);
			//System.out.println("audition item:"+newOffer.toString()); //LJ
			iter.remove();
			inserted = true;
			break;
		}

		logger.debug("insertAudition>" + inserted);
		return inserted;
	}

	/*
	 * creating offerlist to be sorted // { OS, OFFER_ID, OFFERWALL_RANK } ===>
	 * List of <Offer>
	 */
	public List<Row> ranking(Connection conn) throws SQLException,
			EmptyRankedScoreException, IOException, ParseException,
			java.text.ParseException {
		logger.debug("ranking<");
		
		System.out.printf("ranking in RankOfferGenerator called -- LJ\n");

		List<Row> offerList = OfferRowUtil.cloneList(scoredOfferMap
				.get(rankedOfferKey));
		
		System.out.printf("scoredOfferMap cloned - LJ\n");

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(offerList, "start ranking::");
		}

		// saying the final jason list
		List<Row> ohashList = new ArrayList<Row>();

		// applying discount filter for algorithm 280
		/*
		if ((rankedOfferKey.algorithm != null)
				&& rankedOfferKey.algorithm
						.equals(Configuration.Ranking.ALGORITHM_280)) {

			//DiscountScoreTransformer discountTransformer = new DiscountScoreTransformer(
			//		rankedOfferKey.os, rankedOfferKey.platform);
			//discountTransformer.transform(offerList);

            PaymentScoreTransformer paymentTransformer = new PaymentScoreTransformer(
                    rankedOfferKey.os, rankedOfferKey.platform);
            paymentTransformer.transform(offerList);
        }
		*/

		/**
		 * rank boost all the offers. discard those with lower than threshold
		 * score
		 */
		RankBoostListTransformer rankBoostTransformer = new RankBoostListTransformer();
		offerList = rankBoostTransformer.transform(offerList);
		System.out.printf("offerList rank boosted - LJ\n");
		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(offerList, "after rank boosting::");
		}

		Double lowestScore = OfferRowUtil.getLeastBoostedScore(offerList, Configuration.Ranking.DEFAULT_LEAST_SCORE);
		logger.info("Initial lowest score(" + lowestScore + ") for Algo: "
				+ rankedOfferKey);
		System.out.printf("lowestScore obtained - LJ\n");

		/**
		 * load all the rank boosted offers if they are not there already
		 */
		RankBoostOfferGenerator rankBoostGenertor = new RankBoostOfferGenerator(
				rankedOfferKey);
		List<Row> rankBoostOffers = rankBoostGenertor.generator(conn);
		System.out.printf("rankBoostOffers geneated - LJ\n");
		
		offerList.addAll(rankBoostOffers);
		System.out.printf("rankBoostOffers added to offerList - LJ\n");
		OfferRowUtil.deDupOfferListById(offerList);

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(offerList, "rankBoostGenertor::");
		}

		/**
		 * backfill the offers if possible
		 */
		// need backfill?
		if (offerList.size() < Configuration.Ranking.LIST_SIZE) {
			backfillOffers(offerList, lowestScore);
			System.out.printf("offerList backfilled - LJ\n");

			if (logger.isDebugEnabled()) {
				OfferRowUtil.debugOfferRow(offerList, "after backfillOffers::");
			}
		}
		else
			System.out.printf("no backfill needed - LJ\n");

		Collections.sort(offerList, new RowDoubleFieldComparator(
				ColumnDef.RANK_ADJUSTED_SCORE));   
		OfferRowUtil.deDupOfferListById(offerList);
		System.out.printf("offerList current size=%d\n", offerList.size());

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugOfferRow(offerList,
					"after sorting by ranked adjusted::");
		}

		Map<String, Row> offerMap = OfferRowUtil.offerListToMap(offerList);

		/**
		 * Audition handling
		 */
		int rankIndex = 0;
		List<Row> newOfferList;

		// applying discount filter for algorithm 280
		/*
		if ((rankedOfferKey.algorithm != null)
				&& rankedOfferKey.algorithm
						.equals(Configuration.Ranking.ALGORITHM_280)) {
			newOfferList = NewOfferGenerator.getSortedNewOffersWithDiscount(
					conn, rankedOfferKey.os, rankedOfferKey.platform, segment);
		} 
		*/
		
		newOfferList = NewOfferGenerator.getSortedNewOffers(conn,
					rankedOfferKey.os, rankedOfferKey.platform, segment);
		
		if(newOfferList.size() > 0)
		{
			System.out.printf("newOfferList: \n");
			System.out.println(newOfferList.get(0).toString());
			System.out.println(newOfferList.get(newOfferList.size()-1).toString());
		}
	
		logger.debug("found new sorted offer list is::" + newOfferList.size());

		OfferRowUtil.appendOfferListToMap(offerMap, newOfferList);

		Double currentOfferScore = 0.0;
		Double hardPromotionScore = 0.0;
		int audnum = 0;
		for (Row row : offerList) {
			rankIndex++;

			if (newOfferList.size() > 0) {
				Double threshold = auditionShowMap.get(rankIndex);
				Double randomValue = new Random().nextDouble();
				if (threshold == null || (randomValue >= threshold)) {
					logger.trace("There is not audition offer for position"
							+ rankIndex);
				} else {
					boolean inserted = insertAudition(rankIndex,
							currentOfferScore, newOfferList, ohashList);
					if (inserted) {
						rankIndex++;
						audnum++;
					}
				}
			}

			if (row.getColumn(ColumnDef.RANK_ADJUSTED_SCORE) != null) {
				currentOfferScore = new Double(row.getColumn(
						ColumnDef.RANK_ADJUSTED_SCORE).toString());

				// handling hard promotion offers
				if (rankIndex == Configuration.Ranking.HARD_INSERT_POS) {
					hardPromotionScore = currentOfferScore;
					rankIndex = handleHardPromotion(ohashList, offerMap,
							rankIndex, currentOfferScore);
				}

				row.setColumn(ColumnDef.IS_AUDITION, "0");
				row.setColumn(ColumnDef.IS_HARD_PROMOTION, "0");
				row.setColumn(ColumnDef.RANK_INDEX, rankIndex);

				ohashList.add(row);
			}
		}
		

		// if algorithm is 280, appending new offers as audition	
		if ((rankedOfferKey.algorithm != null)
				&& (segment.equals(ConfigurationSegment.OPT_GOW_GLOBAL)
						|| segment.equals(ConfigurationSegment.OPT_TJM)
						|| segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY) 
						|| segment.equals(ConfigurationSegment.OPT_GOW_COUNTRY_APP))) {
			System.out.printf("now we start to append audition in the end --- LJ\n"); 
			while (newOfferList.isEmpty() == false) {
				boolean inserted = insertAudition(rankIndex, currentOfferScore,
						newOfferList, ohashList);
				if (inserted) {
					rankIndex++;
				}
			}
		}
		
		System.out.printf("%d offers appended as audition tail.\n", audnum); //LJ

		// rerank the audition based on audition score
		if ((Configuration.Audition.AUDITION_MERGE)
				&& (rankedOfferKey.algorithm != null)) {
			// && rankedOfferKey.algorithm
			// .equals(Configuration.Ranking.ALGORITHM_280)) {
			// removing all the offers with audition flag
			Iterator<Row> offerIter = ohashList.iterator();
			double maxPreScore = 1.0;
			while (offerIter.hasNext()) {
				Row row = offerIter.next();
				String isAudition = row.getColumn(ColumnDef.IS_AUDITION);

				if ((isAudition != null) && (isAudition.equals("1"))) {
					Double predScore = new Double(
							row.getColumn(PREDITION_SCORE));
					// Maintaining the maximum predition score
					if (predScore > maxPreScore) {
						maxPreScore = predScore;
					}

					Double adjustedScore = hardPromotionScore
							* Configuration.Audition.PRED_TO_SCORE * predScore
							/ maxPreScore
							+ Configuration.Audition.PRED_TO_SCORE_BASE;
					row.setColumn(ColumnDef.RANK_ADJUSTED_SCORE, adjustedScore);
				}
			}
	
			
			// sort the offerList by score
			Collections.sort(ohashList, new RowDoubleFieldComparator(
				ColumnDef.RANK_ADJUSTED_SCORE));  
		}

		// deduping the offer list
		OfferRowUtil.deDupOfferListById(ohashList);

		// resetting the ranking after sort/dedup
		int rerankIndex = 1;
		for (Row row : ohashList) {
			row.setColumn(RANK_INDEX, rerankIndex);
			rerankIndex++;
		}

		// if algorithm 280 and TJM, checking for show rate
		// appending show rate
		/*
		if ((Configuration.Ranking.ENABLE_SHOW_RATE)
				&& (rankedOfferKey.algorithm != null)
				&& rankedOfferKey.algorithm
						.equals(Configuration.Ranking.ALGORITHM_280)) {

			ShowRateTransformer showRateTransformer = new ShowRateTransformer();
			ohashList = showRateTransformer.transform(ohashList, conn);
		}
		*/

		// truncating the extra offer exceeding the offerList maximum size
		if (ohashList.size() >= Configuration.Ranking.MAX_RANK_INDEX) {
			logger.info("ohash list size before truncation is::" + ohashList.size());
			ohashList = ohashList.subList(0, Configuration.Ranking.MAX_RANK_INDEX);
		}
		
		logger.info("ohash list size is::" + ohashList.size());
		logger.debug("ranking>");
		
		return ohashList;
	}

	private int handleHardPromotion(List<Row> ohashList,
			Map<String, Row> offerMap, int rankIndex, Double promotionRankScore) {
		String[] promotions = null;

		if ((rankedOfferKey.platform == Configuration.Platform.OFFERWALL)
				&& (rankedOfferKey.country == null)
				&& (rankedOfferKey.currency_id == null)
				&& (rankedOfferKey.os.equals(Configuration.OS.IOS))) {
			promotions = ConfigurationHardPromotion.hardPromotionMap
					.get(ConfigurationSegment.OPT_GOW_GLOBAL + "_"
							+ Configuration.OS.IOS);

		} else if ((rankedOfferKey.platform == Configuration.Platform.OFFERWALL)
				&& ((rankedOfferKey.country == null) || (rankedOfferKey.currency_id == null))
				&& (rankedOfferKey.os.equals(Configuration.OS.ANDROID))) {
			promotions = ConfigurationHardPromotion.hardPromotionMap
					.get(Configuration.OS.ANDROID);
		}

		if (promotions != null) {
			logger.debug("promotion found for rankedOfferKey:"
					+ this.rankedOfferKey);

			for (String offerId : promotions) {
				Row row = allOfferMap.get(offerId);

				if (row != null) {
					row = row.clone();
					row.setColumn(ColumnDef.RANK_ADJUSTED_SCORE,
							promotionRankScore);
					row.setColumn(ColumnDef.IS_AUDITION, "0");
					row.setColumn(ColumnDef.IS_HARD_PROMOTION, "1");
					row.setColumn(ColumnDef.RANK_INDEX, rankIndex);

					ohashList.add(row);

					rankIndex++;
				}
			}
		}

		return rankIndex;
	}

	private Double backfillOffers(List<Row> offerList, Double lowestScore) {
		if ((rankedOfferKey.country != null)
				&& (rankedOfferKey.country.length() != 0)) {
			RankedOfferKey globalOfferKey = rankedOfferKey.clone();
			globalOfferKey.country = null;

			if (rankedOfferKey.platform == Configuration.Platform.OFFERWALL) {
				globalOfferKey.segment = ConfigurationSegment.OPT_GOW_GLOBAL;
			} else if (rankedOfferKey.platform == Configuration.Platform.TJM) {
				globalOfferKey.segment = ConfigurationSegment.OPT_TJM;
			}

			List<Row> refillGlobalList = OfferRowUtil.cloneList(scoredOfferMap.get(globalOfferKey));
			if (refillGlobalList != null) {
				CountryFilter filter = new CountryFilter(rankedOfferKey.country);
				refillGlobalList = filter.transform(refillGlobalList);

				lowestScore = backfillOffers(offerList, refillGlobalList,
						rankedOfferKey, lowestScore);
			}
		}

		return lowestScore;
	}

	private Double backfillOffers(List<Row> offerList,
			List<Row> refillGlobalList_in, RankedOfferKey rankedOfferKey,
			Double currLowestScore) {
		if (refillGlobalList_in == null) {
			return currLowestScore;
		}

		Double newLowestScore = currLowestScore;

		List<Row> refillGlobalList = OfferRowUtil.cloneList(refillGlobalList_in);
		Iterator<Row> offerIter = refillGlobalList.iterator();
		while (offerIter.hasNext()) {
			Row offerRow = offerIter.next();
			
			// cloning before proceeding
			Row newofferRow = offerRow.clone();

			if ((newofferRow.getColumn(BID) == null)
					|| (Double.parseDouble(newofferRow.getColumn(BID)) <= 0.0)) {
				continue;
			}

			if ((newofferRow.getColumn(CONVERT_SCORE) == null)
					|| (Double.parseDouble(newofferRow.getColumn(CONVERT_SCORE)) <= Configuration.Ranking.TAIL_THRESH)) {
				continue;
			}

			if (CountryFilter.isValidRow(newofferRow, rankedOfferKey.country) == false) {
				continue;
			}

			logger.info("Current score for TJM_GLOBAL offer_name "
					+ newofferRow.getColumn(OFFER_NAME) + "(score: "
					+ newofferRow.getColumn(CONVERT_SCORE)
					+ "), initial lowest score: " + currLowestScore);

			Double score = new Double(newofferRow.getColumn(CONVERT_SCORE));
			score = score*Configuration.Ranking.RANK_ADJUST_RATIO;
			//score = currLowestScore * (score / (1.0 + score));
			newofferRow.setColumn(RANK_ADJUSTED_SCORE, score);
			offerList.add(newofferRow);
			
			if (score < newLowestScore) {
				newLowestScore = score;
			}

			logger.info("Current score for offer_name "
					+ newofferRow.getColumn(OFFER_NAME)
					+ ", current lowest score:" + newLowestScore);
		}

		// sort the offerList by score
		Collections.sort(offerList, new RowDoubleFieldComparator(
				ColumnDef.RANK_ADJUSTED_SCORE));
		
		// deduping offers
		OfferRowUtil.deDupOfferListById(offerList);

		
		/*if (offerList.size() >= Configuration.Ranking.MAX_RANK_INDEX) {
			offerList = offerList.subList(0,
					Configuration.Ranking.MAX_RANK_INDEX);
		} */  //uncommented by LJ 08-19
		
		return newLowestScore;
	}
}
