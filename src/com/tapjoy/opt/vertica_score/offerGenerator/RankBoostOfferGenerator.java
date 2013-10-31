package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.object_cache.OfferCache;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.CountryFilter;
import com.tapjoy.opt.vertica_score.offerGenerator.transformer.DeviceFilter;

public class RankBoostOfferGenerator implements ColumnDef {
	private static Logger logger = Logger
			.getLogger(RankBoostOfferGenerator.class);
	private RankedOfferKey rankOfferKey;

	public RankBoostOfferGenerator(RankedOfferKey rankOfferKey) {
		this.rankOfferKey = rankOfferKey;
	}

	/**
	 * RankBoosted the offer list 1. Rank boosted 2. filtering out row with
	 * lower than TAIL_THRESH boost adjusted score
	 * 
	 * @throws SQLException
	 */
	public List<Row> generator(Connection conn) throws SQLException {
		List<Row> boostedList = new ArrayList<Row>();

		Map<String, Row> offerMap = OfferCache.getInstance().getRankBoostOfferMap(conn, false);
		for (String id : offerMap.keySet()) {
			boostedList.add(offerMap.get(id));
		}

		//RankBoostListTransformer transformer = new RankBoostListTransformer();
		//boostedList = transformer.transform(boostedList);

		// adding country filter
		if (rankOfferKey.country != null) {
			CountryFilter filter = new CountryFilter(rankOfferKey.country);
			boostedList = filter.transform(boostedList);
		}

		// adding device filter
		DeviceFilter deviceFilter = new DeviceFilter(rankOfferKey.os,
				rankOfferKey.device);
		boostedList = deviceFilter.transform(boostedList);

		// filtering out score lower than TAIL_THRESH
		Iterator<Row> iter = boostedList.iterator();
		while (iter.hasNext()) {
			Row row = iter.next();			
			Double boostedScore = RankBoostListTransformer.getRankBoost(row);
			
			if (boostedScore < Configuration.Ranking.TAIL_THRESH) {
				logger.debug("based on rank adjusted score::"
						+ boostedScore + ", filtering out " + row);
				iter.remove();
			}

			row.setColumn(RANK_ADJUSTED_SCORE, boostedScore);
		}

		return boostedList;
	}
}
