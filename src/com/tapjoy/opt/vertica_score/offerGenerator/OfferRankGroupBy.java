package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.GroupBy;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.Sum;
import com.tapjoy.opt.common.GroupBy.CallBackPair;

public class OfferRankGroupBy implements ColumnDef {
	private static Logger logger = Logger.getLogger(OfferRankGroupBy.class);
	public static String[] groupByColumns = { OS, OFFER_ID, OFFERWALL_RANK };

	/**
	 * group by os, offer_id, offerwall_rank
	 * 
	 * @param rowMap
	 * @return
	 */
	public Map<Row, Row> groupBy(Map<Row, Row> rowMap) {

		CallBackPair[] callbackpairs = new CallBackPair[4];
		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = IMPRESSIONS_SUM;
		callbackpairs[0].callBack = new Sum(IMPRESSIONS);

		callbackpairs[1] = new CallBackPair();
		callbackpairs[1].columnName = ACTIONS_SUM;
		callbackpairs[1].callBack = new Sum(ACTIONS);

		callbackpairs[2] = new CallBackPair();
		callbackpairs[2].columnName = EXPECTED_ACTIONS_SUM;
		
		callbackpairs[3] = new CallBackPair();
		callbackpairs[3].columnName = EXPECTED_ACTIONS_VARIANCE_SUM;

		Iterator<Entry<Row, Row>> iter = rowMap.entrySet().iterator();
		GroupBy groupBy = new GroupBy(OfferRowUtil.getKeyColumns(rowMap), callbackpairs);

		while (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			Row row = entry.getValue();

			groupBy.proceess(row);
		}

		Map<Row, Row> results = groupBy.getGroupByResult();

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(results, "after group by offer/rank");
		}

		return results;
	}
	
}
