package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.DoubleSum;
import com.tapjoy.opt.common.GroupBy;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.Sum;
import com.tapjoy.opt.common.GroupBy.CallBackPair;

/**
 * Getting the total action sum, total expected sum across all the rank position
 * 
 * @author lli
 *
 */
public class OfferGroupBy implements ColumnDef {
	private static Logger logger = Logger.getLogger(OfferRankGroupBy.class);

	public OfferGroupBy() {
	}

	/**
	 * group by os, offer_id, offerwall_rank
	 * 
	 * @param rowMap
	 * @return
	 */
	public Map<Row, Row> groupBy(Map<Row, Row> rowMap) {

		CallBackPair[] callbackpairs = new CallBackPair[5];

		callbackpairs[0] = new CallBackPair();
		callbackpairs[0].columnName = TOTAL_ACTIONS_SUM;
		callbackpairs[0].callBack = new Sum(ACTIONS_SUM);

		callbackpairs[1] = new CallBackPair();
		callbackpairs[1].columnName = TOTAL_EXPECTED_ACTIONS_SUM;
		callbackpairs[1].callBack = new DoubleSum(EXPECTED_ACTIONS_SUM);

		callbackpairs[2] = new CallBackPair();
		callbackpairs[2].columnName = TOTAL_EXPECTED_ACTIONS_VARIANCE_SUM;
		callbackpairs[2].callBack = new DoubleSum(EXPECTED_ACTIONS_VARIANCE_SUM);

		callbackpairs[3] = new CallBackPair();
		callbackpairs[3].columnName = CONVERT_SCORE;

		callbackpairs[4] = new CallBackPair();
		callbackpairs[4].columnName = CONVERT_SCORE_WITH_VARIANCE;

		// getting the group by without the Column of OFFERWALL_RANK
		String[] keyList = OfferRowUtil.getKeyColumns(rowMap);
		List<String> newKeyList = new ArrayList<String>();
		for (String key : keyList) {
			if (key.equals(OFFERWALL_RANK) == false) {
				newKeyList.add(key);
			}
		}
		
		String[] groupKeyList = OfferRowUtil.toStringArray(newKeyList);
		GroupBy groupBy = new GroupBy(groupKeyList,
				callbackpairs);

		for (Row keyRow : rowMap.keySet()) {
			Row row = rowMap.get(keyRow);
			groupBy.proceess(row);
		}

		Map<Row, Row> results = groupBy.getGroupByResult();

		if (logger.isDebugEnabled()) {
			OfferRowUtil.debugGroupByMap(results, "after group by offer");
		}

		return results;
	}
}
