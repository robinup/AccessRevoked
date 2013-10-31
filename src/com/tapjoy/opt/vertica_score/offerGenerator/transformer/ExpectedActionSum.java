package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmailLogger;
import com.tapjoy.opt.common.OfferRowUtil;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.common.RowFactory;

/**
 * Appending an additon column called EXPECTED_ACTIONS_SUM and SCORE
 * 
 * @author lli
 * 
 */
public class ExpectedActionSum implements Transformer, ColumnDef {
	private static Logger logger = Logger.getLogger(ExpectedActionSum.class);

	private Map<Row, Row> globalCVRMap = null;

	public ExpectedActionSum(Map<Row, Row> globalCVRMap) {
		this.globalCVRMap = globalCVRMap;
	}

	private Double getExpectedConversion(Row keyRow, Map<Row, Row> globalCVRMap) {
		Double expected = null;

		RowFactory cvrMapKeyFactory = new RowFactory(
				OfferRowUtil.getKeyColumns(globalCVRMap));

		if (keyRow != null) {
			Row os_rank = cvrMapKeyFactory.newRow();
			os_rank.copy(keyRow);

			Row result = globalCVRMap.get(os_rank);

			if ((result != null) && (result.getColumn(CVR) != null)) {
				expected = new Double(result.getColumn(CVR).toString());
			}
		}

		return expected;
	}

	/*
	 * Adding new expected conversion sum
	 */
	@Override
	public Map<Row, Row> transform(Map<Row, Row> rowMap) {
		Iterator<Entry<Row, Row>> iter = rowMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Row, Row> entry = iter.next();
			Row keyRow = entry.getKey();
			Row row = entry.getValue();

			if ((row.getColumn(ACTIONS_SUM) == null)
					|| (row.getColumn(IMPRESSIONS_SUM) == null)) {
				logger.warn("row::" + row
						+ " does not have actions sum or impressions sum");
				iter.remove();
				continue;
			}

			double actions = Double.parseDouble(row.getColumn(ACTIONS_SUM)
					.toString());
			double impressions = Double.parseDouble(row.getColumn(
					IMPRESSIONS_SUM).toString());
			if (actions > impressions) {
				EmailLogger.getInstance().error(
						logger,
						"row::" + row
								+ "actions number is greater than impressions");
				iter.remove();
				continue;
			}

			Double expectedConversionRate = getExpectedConversion(keyRow,
					globalCVRMap);
			// if expected action sum is not positive, remove it from results
			if ((expectedConversionRate == null)
					|| (expectedConversionRate <= 0)) {
				logger.trace("row::" + row
						+ "expectedConversion rate is null or less than zero");
				iter.remove();
				continue;
			}

			double expectedActionSum = impressions * expectedConversionRate;
			row.setColumn(EXPECTED_ACTIONS_SUM, expectedActionSum);

			Double expectedVarianceSum = impressions * expectedConversionRate
					* (1.0 - expectedConversionRate);
			row.setColumn(EXPECTED_ACTIONS_VARIANCE_SUM, expectedVarianceSum);

			OfferRowUtil.debugOffer(row, "impressions::" + impressions
					+ " actions::" + actions + " expectedConversionRate::"
					+ expectedConversionRate + " expectedActionSum::"
					+ expectedActionSum + " expectedVarianceSum::"
					+ expectedVarianceSum);

			// if expected action sum is not positive, remove it from results
			if (expectedActionSum <= 0) {
				logger.warn("row::" + row
						+ "expectedActionSum is less than zero");
				iter.remove();
				continue;
			}
		}

		OfferRowUtil.debugGroupByMap(rowMap,
				"After expected action transformation");
		return rowMap;
	}
}
