package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;

/**
 * Appending an additon column called EXPECTED_ACTIONS_SUM and SCORE
 * 
 * @author lli
 * 
 */
public class ConvertScore implements Transformer, ColumnDef {
	private Map<String, Row> allOfferMap = null;
	private static Logger logger = Logger.getLogger(ConvertScore.class);

	public ConvertScore(Map<String, Row> offerMap) {
		this.allOfferMap = offerMap;
	}

	/*
	 * Adding new expected conversion sum
	 */
	@Override
	public Map<Row, Row> transform(Map<Row, Row> rowMap) {
		Iterator<Row> iter = rowMap.values().iterator();
		while (iter.hasNext()) {
			Row row = iter.next();

			if ((row.getColumn(TOTAL_ACTIONS_SUM) == null)
					|| (row.getColumn(TOTAL_EXPECTED_ACTIONS_SUM) == null)
					|| (row.getColumn(TOTAL_EXPECTED_ACTIONS_VARIANCE_SUM) == null)) {
				logger.warn("action sum or expected action sum is null for row:"
						+ row);
				iter.remove();
				continue;
			}

			double total_actions = Double.parseDouble(row.getColumn(
					TOTAL_ACTIONS_SUM).toString());
			double total_expectedActions = Double.parseDouble(row.getColumn(
					TOTAL_EXPECTED_ACTIONS_SUM).toString());
			if (total_expectedActions == 0) {
				logger.trace("expected total action sum is 0 for row:" + row);
				iter.remove();
				continue;
			}

			double total_expected_variance_sum = Double.parseDouble(row
					.getColumn(TOTAL_EXPECTED_ACTIONS_VARIANCE_SUM).toString());
			if (total_expected_variance_sum == 0) {
				logger.trace("expected total action sum is 0 for row:" + row);
				iter.remove();
				continue;
			}

			double sigma = Math.sqrt(total_expected_variance_sum);
			double sigmaDistance = Configuration.Ranking.SIGMA_DISTANCE;

			double convertScore = (total_actions / total_expectedActions);
			if (convertScore == 0) {
				logger.trace("convertScore is 0 for row:" + row);
				iter.remove();
				continue;
			}

			double convertScoreWithVariance = (total_actions / (total_expectedActions + sigmaDistance
					* sigma));

			String offerId = row.getColumn(OFFER_ID).toString();
			Row offerRow = allOfferMap.get(offerId);

			if (offerRow != null) {
				double bid = new Double(offerRow.getColumn(BID).toString());
				row.setColumn(CONVERT_SCORE, convertScore * bid);
				row.setColumn(CONVERT_SCORE_WITH_VARIANCE,
						convertScoreWithVariance * bid);

				OfferRowUtil.debugOffer(row, "total_actions::" + total_actions
						+ " total_expectedActions::" + total_expectedActions
						+ " total_expected_variance_sum::"
						+ total_expected_variance_sum + " sigma::" + sigma
						+ " sigmaDistance::" + sigmaDistance
						+ " convertScore::" + convertScore
						+ " convertScoreWithVariance::"
						+ convertScoreWithVariance + " bid::" + bid
						+ " offerId::" + offerId);
			} else {
				OfferRowUtil.debugOffer(row,
						"offer row not available in the all offer map for offer id::"
								+ offerId);
			}
		}

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(rowMap,
					"after ConvertScore transform in getOfferList::");
		}

		return rowMap;
	}
}
