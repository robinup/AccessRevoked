package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.vertica_score.objectCache.AuditionCache;

public class DiscountScoreTransformer implements ColumnDef {
	private static Logger logger = Logger.getLogger(DiscountScoreTransformer.class);
	private Map<String, Row> auditionOfferMap;

	public DiscountScoreTransformer(String os, int platform) {
		auditionOfferMap = AuditionCache.getInstance().getAuditionMap(os, platform);
	}
	
	public void transform(List<Row> offerlist){
		for (Row row : offerlist){
			String offerId = row.getColumn(ID);
			Row auditionRow = auditionOfferMap.get(offerId);
			
			if( auditionRow == null){
				logger.warn("Opt Row missing from Audition Map" + offerId);
				continue;
			}
			
			double origScore = Double.parseDouble(row.getColumn(CONVERT_SCORE));
			double bidDiscount = Double.parseDouble(auditionRow.getColumn(BID_DISCOUNT));
			double bidOrig = Double.parseDouble(row.getColumn(BID));
			double newScore = 0;
			if(bidOrig != 0){
				newScore = origScore * bidDiscount / bidOrig;
			}
			
			row.setColumn(CONVERT_SCORE, "" + newScore);
			
		}
		
	}

}
