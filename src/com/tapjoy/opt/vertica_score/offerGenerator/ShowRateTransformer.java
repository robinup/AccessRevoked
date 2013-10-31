package com.tapjoy.opt.vertica_score.offerGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.showrate.OfferBudget;

public class ShowRateTransformer implements ColumnDef {
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ShowRateTransformer.class);

	public ShowRateTransformer() {
	}

	/**
	 * RankBoosted the offer list 1. Rank boosted 2. filtering out row with
	 * lower than TAIL_THRESH boost adjusted score
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public List<Row> transform(List<Row> offerList, Connection conn) throws SQLException, ParseException {
		OfferBudget offerBudget = new OfferBudget();
		Map<String, Double> showRateMap = offerBudget.retrieveShowRate(conn);
		
		for (Row row : offerList) {
			String offerId = row.getColumn(ID);
			
			if(offerId != null) {
				Double showRate = showRateMap.get(offerId);
				if(showRate != null) {
					row.setColumn(SHOW_RATE_NEW, showRate.toString());
				}
			}
		}

		return offerList;
	}
}
