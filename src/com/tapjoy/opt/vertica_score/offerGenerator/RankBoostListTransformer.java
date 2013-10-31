package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class RankBoostListTransformer implements ColumnDef {
	private static Logger logger = Logger
			.getLogger(RankBoostListTransformer.class);

	public RankBoostListTransformer() {
	}

	public static double getRankBoost(Row row) {
		Double finalRankBoost = 0.0;

		String rankBoostStr = row.getColumn(RANK_BOOST);
		if ((rankBoostStr != null) && (rankBoostStr.length() > 0)) {
			Double rankBoost = Double.parseDouble(rankBoostStr);
			String whiteList = row.getColumn(PUBLISHER_APP_WHITELIST);

			if (rankBoostStr.endsWith("1")
					|| ((whiteList != null) && (whiteList.length() > 0) && (rankBoost < Configuration.RankBoost.RANKBOOST_THRESHOLD))
					|| (rankBoost < 0)) {
				finalRankBoost = rankBoost;
			}
		}

		return finalRankBoost;
	}

	/**
	 * RankBoosted the offer list 1. Rank boosted 2. filtering out row with
	 * lower than TAIL_THRESH boost adjusted score
	 */
	public List<Row> transform(List<Row> offerList) {
		List<Row> boostedList = new ArrayList<Row>();

		for (Row row : offerList) {
			if ((row.getColumn(CONVERT_SCORE) == null)
					|| (row.getColumn(CONVERT_SCORE).toString().length() == 0)) {
				logger.warn("converted score less than 0.0, removed:" + row);
				continue;
			} else if (Double.parseDouble(row.getColumn(CONVERT_SCORE)) <= 0.0) {
				logger.warn("converted score less than 0.0, removed:" + row);
				continue;
			} else if ((row.getColumn(BID) != null)
					&& (Double.parseDouble(row.getColumn(BID)) <= 0.0)) {
				logger.warn("bid less than 0.0, removed:" + row);
				continue;
			}

			Double boostedScore = Double.parseDouble(row
					.getColumn(CONVERT_SCORE));
			boostedScore = boostedScore + getRankBoost(row);

			row.setColumn(RANK_ADJUSTED_SCORE, boostedScore);
			if (boostedScore >= Configuration.Ranking.TAIL_THRESH) {
				OfferRowUtil.debugOffer(row, "Adding rank boosted offer");
				boostedList.add(row);
			} else {
				OfferRowUtil.debugOffer(row,
						"offer does not have enough boosted score");
			}
		}

		return boostedList;
	}
}
